/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.AbstractStorageNode;
import de.dimm.vsm.records.DedupHashBlock;
import de.dimm.vsm.records.PoolNodeFileLink;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAAbstractComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACloneNodeComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPALocalFSField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.SelectObjectCallback;
import de.dimm.vsm.vaadin.VSMCMain;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class AbstractStorageNodeTable extends BaseDataEditTable<AbstractStorageNode>
{
     StoragePool pool;
    private AbstractStorageNodeTable( VSMCMain main, StoragePool pool, List<AbstractStorageNode> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, AbstractStorageNode.class, _fieldList, listener);
        this.pool = pool;
    }

    public static AbstractStorageNodeTable createTable( VSMCMain main, StoragePool pool, ItemClickListener listener)
    {
        GenericEntityManager em = VSMCMain.get_util_em(pool);
        List<AbstractStorageNode> list = pool.getStorageNodes(em);
        

        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();

        fieldList.add(new JPATextField(VSMCMain.Txt("Name"), "name"));
        fieldList.add(new JPALocalFSField(VSMCMain.Txt("Pfad"), "mountPoint"));
        ArrayList<ComboEntry> entries = new ArrayList<ComboEntry>();
        entries.add( new ComboEntry(AbstractStorageNode.NM_VIRGIN, VSMCMain.Txt("Erzeugt")));
        entries.add( new ComboEntry(AbstractStorageNode.NM_ONLINE, VSMCMain.Txt("Online")));
        entries.add( new ComboEntry(AbstractStorageNode.NM_FULL, VSMCMain.Txt("Voll")));
        entries.add( new ComboEntry(AbstractStorageNode.NM_OFFLINE, VSMCMain.Txt("Offline")));
        entries.add( new ComboEntry(AbstractStorageNode.NM_EMPTYING, VSMCMain.Txt("Wird_geleert")));
        entries.add( new ComboEntry(AbstractStorageNode.NM_EMPTIED, VSMCMain.Txt("Wurde_geleert")));
        fieldList.add(new JPAComboField(VSMCMain.Txt("Status"), "nodeMode", entries));

        entries = new ArrayList<ComboEntry>();
        entries.add( new ComboEntry(AbstractStorageNode.NT_FILESYSTEM, VSMCMain.Txt("Dateisystem")));

        if (VSMCMain.isFTPStorageLicensed())
            entries.add( new ComboEntry(AbstractStorageNode.NT_FTP, VSMCMain.Txt("FTP")));

        if (VSMCMain.isS3StorageLicensed())
            entries.add( new ComboEntry(AbstractStorageNode.NT_S3, VSMCMain.Txt("S3")));

        fieldList.add(new JPAComboField(VSMCMain.Txt("Typ"), "nodeType", entries));
        fieldList.add(new JPACloneNodeComboField( main, pool, "cloneNode" ) );


        return new AbstractStorageNodeTable( main, pool, list, fieldList, listener);
    }

    @Override
    public void checkPlausibility(AbstractOrderedLayout editPanel, final AbstractStorageNode t, final Runnable ok, Runnable nok)
    {
        if (t.getMountPoint().isEmpty())
        {
            main.Msg().errmOk(VSMCMain.Txt("Bitte geben Sie einen Pfad an"));
            nok.run();
            return;
        }

        if (isNew())
        {
            // INIT STORAGENODE
            boolean ret = doInitNode(t);
            if (!ret)
            {
                main.Msg().errmOk(VSMCMain.Txt("Der_Node_konnte nicht im Dateisystem angelegt werden"));
                nok.run();
                return;
            }
        }
        if (!isNew())
        {
            // DETECT USERCHANGE ON MODE COMBO
            if (!getElemBeforEdit().getNodeMode().equals(t.getNodeMode()))
            {
                // IS IN PROGRESS ?
                if (isBusyNode(t))
                {
                    main.Msg().errmOk(VSMCMain.Txt("Der_Node_ist_in_Bearbeitung"));
                    nok.run();
                    return;
                }
                // IS INVALID_STATE ?
                if (t.getNodeMode().equals(AbstractStorageNode.NM_EMPTIED) || t.getNodeMode().equals(AbstractStorageNode.NM_VIRGIN))
                {
                    main.Msg().errmOk(VSMCMain.Txt("Der_Status_des_Node_kann_nicht_verändert_werden"));
                    nok.run();
                    return;
                }               
            }
        }

        // DETECT START OF CLONING
        JPAAbstractComboField nodeMode = (JPAAbstractComboField) getField("cloneNode");
        ComboEntry entry = nodeMode.getSelectedEntry((AbstractOrderedLayout) editPanel);
        if (entry != null)
        {
            if (getElemBeforEdit().getCloneNode() == null)
            {
                ClickListener okClick = new ClickListener()
                {
                    @Override
                    public void buttonClick( ClickEvent event )
                    {
                        try
                        {
                            doSyncNode( t );
                        }
                        catch (Exception exception)
                        {
                            main.Msg().errmOk(VSMCMain.Txt("Das Kopieren schlug fehl") + " " + exception.toString());
                        }
                    }
                };
                main.Msg().errmOkCancel(VSMCMain.Txt("Der CloneNode wird jetzt mit diesem Node abgeglichen, dies kann ja nach Füllstand dieses Nodes lange dauern"), okClick, null);
            }
        }
        
        ok.run();
    }

    @Override
    protected GenericEntityManager get_em()
    {
        return VSMCMain.get_util_em(pool);
    }


    @Override
    protected AbstractStorageNode createNewObject()
    {
        AbstractStorageNode n =  AbstractStorageNode.createFSNode();
        n.setName(VSMCMain.Txt("Neuer Storagenode"));
        n.setMountPoint("");
        n.setPool(pool);

       
        GenericEntityManager gem = get_em();
        
        try
        {
            gem.check_open_transaction();
            gem.em_persist(n);

            // UPDATE PARENT NODE
            pool.getStorageNodes().addIfRealized(n);
           

            gem.commit_transaction();

            this.requestRepaint();
            return n;
        }
        catch (Exception e)
        {
            gem.rollback_transaction();
            VSMCMain.notify(this, "Abbruch in createNewObject", e.getMessage());

        }
        return null;

    }

    
    NodePreviewPanel editPanel;

    @Override
    public AbstractOrderedLayout createEditComponentPanel(  boolean readOnly )
    {
        editPanel = new NodePreviewPanel(this, readOnly);
        editPanel.recreateContent(activeElem);

        return editPanel;
    }


    void emptyNode( final AbstractStorageNode node )
    {
        ClickListener okListener = new ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                doEmptyNode( node );
            }
        };
        main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_den_Inhalt_dieses_Nodes_auf_andere_freie_Nodes_bewegen?"),  okListener, null);
    }


    void moveNode( final AbstractStorageNode node )
    {
        ClickListener okListener = new ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                doMoveNode( node );
            }
        };
        main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_den_Inhalt_dieses_Nodes_auf_einen_anderen_freien_Node_bewegen?"),  okListener, null);
    }
    void syncNode( final AbstractStorageNode node )
    {
        ClickListener okListener = new ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                doSyncNode( node );
            }
        };
        main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_den_CloneNode_synchronisieren?"),  okListener, null);
    }

    boolean isBusyNode( AbstractStorageNode node )
    {
        return main.getGuiServerApi().isBusyNode(node);
    }

    void doEmptyNode( AbstractStorageNode node )
    {
        try
        {
            main.getGuiServerApi().emptyNode(node, main.getUser());
            main.Msg().info(VSMCMain.Txt("Der_Vorgang_wurde_als_Job_angemeldet_und_gestartet"), null);
        }
        catch (Exception exception)
        {
            main.Msg().errmOk(VSMCMain.Txt("Das_Entleeren_konnte_nicht_gestartet_werden") + " :" + exception.getMessage() );
        }
    }
    boolean doInitNode( AbstractStorageNode node )
    {
        try
        {
            boolean ret = main.getGuiServerApi().initNode(node, main.getUser());
            if (ret)
                main.Msg().info(VSMCMain.Txt("Der StorageNode wurde initialisiert"), null);
            else
                main.Msg().errmOk(VSMCMain.Txt("Der StorageNode konnte nicht initialisiert werden"), null);
            return ret;
        }
        catch (Exception exception)
        {
            main.Msg().errmOk(VSMCMain.Txt("Fehler beim Initalisieren des Nodes") + " :" + exception.getMessage() );
        }
        return false;

    }
    void doMoveNode( final AbstractStorageNode node )
    {
        SelectObjectCallback cb = new SelectObjectCallback<AbstractStorageNode>()
        {

            @Override
            public void SelectedAction( AbstractStorageNode toNode )
            {
                if (toNode == null)
                    return;

                try
                {
                    main.getGuiServerApi().moveNode(node, toNode, main.getUser());
                    main.Msg().info(VSMCMain.Txt("Der_Vorgang_wurde_als_Job_angemeldet_und_gestartet"), null);
                }
                catch (Exception exception)
                {
                    main.Msg().errmOk(VSMCMain.Txt("Das_Umbewegen_konnte_nicht_gestartet_werden") + " :" + exception.getMessage() );
                }
            }
        };

        main.SelectObject( pool, AbstractStorageNode.class, VSMCMain.Txt("SpeicherNode"), VSMCMain.Txt("Weiter"), "Select s from AbstractStorageNode s where "
                + "T1.pool_idx=" + node.getPool().getIdx() + " and T1.idx!=" + node.getIdx(), cb );

    }
    void doSyncNode( final AbstractStorageNode node )
    {
        SelectObjectCallback cb = new SelectObjectCallback<AbstractStorageNode>()
        {

            @Override
            public void SelectedAction( AbstractStorageNode toNode )
            {
                if (toNode == null)
                    return;

                try
                {
                    main.getGuiServerApi().syncNode(node, toNode, main.getUser());
                    main.Msg().info(VSMCMain.Txt("Der_Vorgang_wurde_als_Job_angemeldet_und_gestartet"), null);
                }
                catch (Exception exception)
                {
                    main.Msg().errmOk(VSMCMain.Txt("Das_Synchronisieren_konnte_nicht_gestartet_werden") + " :" + exception.getMessage() );
                }
            }
        };

        main.SelectObject( pool, AbstractStorageNode.class, VSMCMain.Txt("SpeicherNode"), VSMCMain.Txt("Weiter"), "Select s from AbstractStorageNode s where "
                + "T1.pool_idx=" + node.getPool().getIdx() + " and T1.idx!=" + node.getIdx(), cb );

    }
    @Override
    protected boolean checkDeletePlausible( AbstractStorageNode t ) throws SQLException
    {
        if (!super.checkDeletePlausible(t))
            return false;

        GenericEntityManager gem = get_em();

        String qry = "select T1 from PoolNodeFileLink T1 where T1.storagenode_idx=" + t.getIdx();
        PoolNodeFileLink n = gem.createSingleResultQuery(qry, PoolNodeFileLink.class);

        if (n != null)
        {
            main.Msg().errmOk(VSMCMain.Txt("Dieses_Objekt_ist_noch_in_Benutzung") + " -> " + VSMCMain.Txt("Dateizuordnung") );
            return false;
        }

        qry = "select T1 from DedupHashBlock T1 where T1.storagenode_idx=" + t.getIdx();
        DedupHashBlock dhb = gem.createSingleResultQuery(qry, DedupHashBlock.class);

        if (dhb != null)
        {
            main.Msg().errmOk(VSMCMain.Txt("Dieses_Objekt_ist_noch_in_Benutzung") + " -> " + VSMCMain.Txt("DedupBlöcke") );
            return false;
        }
        
        return true;
    }



}
