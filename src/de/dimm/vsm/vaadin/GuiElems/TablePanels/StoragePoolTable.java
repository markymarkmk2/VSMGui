/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.AbstractStorageNode;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADBLinkField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAReadOnlyDateField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class StoragePoolTable extends BaseDataEditTable<StoragePool>
{
    private StoragePoolTable( VSMCMain main, List<StoragePool> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main,  list, StoragePool.class, _fieldList, listener);
    }

    public static StoragePoolTable createTable( VSMCMain main, List<StoragePool> list, ItemClickListener listener)
    {

        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();
        fieldList.add(new JPATextField(VSMCMain.Txt("Name"), "name"));
        fieldList.add(new JPAReadOnlyDateField(VSMCMain.Txt("Erzeugt"), "creation", DateField.RESOLUTION_DAY));
        //fieldList.add(new JPACheckBox(VSMCMain.Txt("Dedup nur Änderungen"), "landingZone"));
        fieldList.add(new JPADBLinkField(null, VSMCMain.Txt("Storageknoten"), "storageNodes", AbstractStorageNode.class));

        setFieldVisible(fieldList, "landingZone", false);

        return new StoragePoolTable( main, list, fieldList, listener);
    }

    @Override
    public <AbstractStorageNode> BaseDataEditTable createChildTable( VSMCMain main, StoragePool parent, List<AbstractStorageNode> list, Class child, ItemClickListener listener )
    {
        return AbstractStorageNodeTable.createTable(main, parent, listener);
    }

    StoragePoolPreviewPanel editPanel;

    @Override
    public AbstractOrderedLayout createEditComponentPanel(  boolean readOnly )
    {
        editPanel = new StoragePoolPreviewPanel(this, readOnly);
        editPanel.recreateContent(activeElem);

        return editPanel;
    }


    @Override
    protected StoragePool createNewObject()
    {
        final Component _this = this;
        StoragePool ret = null;
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    StoragePool pool =  (StoragePool) VSMCMain.callLogicControl("createPool", /*catch Exc*/ false);
                    if (pool != null)
                    {
                        registerNewObject(pool);
                    }                    
                    _this.requestRepaint();
                }
                catch (Exception e)
                {
                    main.getBusy().hideBusy();
                    VSMCMain.notify(_this, VSMCMain.Txt("Es konnte kein neuer Pool angelegt werden"), e.getMessage());
                }
            }
        };
        main.runInBusy("Lege neuen StoragePool an", r);
        

        // IS HANDLED IN RUNNABLE
        return null;
    }

    @Override
    protected void deleteObject( final StoragePool node )
    {
        final ClickListener okDel = new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                 main.deletePool( node );
            }
        };
        final ClickListener okDelPhys = new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                 main.deletePoolPhysically( node );
            }
        };

        ClickListener ok = new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_auch_die_Datenbank_dieses_Knotens_entfernen") + " -> " + VSMCMain.Txt("Storageknoten") + " " + node.getName(), okDelPhys, okDel);
            }
        };

        main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_diesen_Knoten_wirklich_entfernen") + " -> " + VSMCMain.Txt("Storageknoten") + " " + node.getName(), ok, null );

       
//        // REMOVE ROOT DIR FIRST
//        GenericEntityManager gem = main.get_util_em();
//        gem.check_open_transaction();
//        try
//        {
//            FileSystemElemNode rootDir = node.getRootDir();
//
//            if (rootDir != null)
//            {
//                FileSystemElemAttributes a = rootDir.getAttributes();
//                rootDir.setAttributes(null);
//                node.setRootDir(null);
//                rootDir.setPool(null);
//                gem.em_merge(rootDir);
//                gem.em_merge(node);
//                gem.commit_transaction();
//
//                List<FileSystemElemNode> l = gem.createQuery("select T1 from FileSystemElemNode T1 where T1.attributes_idx="+ a.getIdx(), FileSystemElemNode.class);
//                List<FileSystemElemNode> l2 = gem.createQuery("select T1 from FileSystemElemNode T1 where T1.pool_idx="+ node.getIdx(), FileSystemElemNode.class);
//
//                gem.em_remove(rootDir);
//                gem.em_remove(a);
//
//            }
//
//            gem.em_remove(node);
//
//            gem.commit_transaction();
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            gem.rollback_transaction();
//        }

    }




    @Override
    protected boolean checkDeletePlausible( StoragePool t ) throws SQLException
    {
        if (!super.checkDeletePlausible(t))
            return false;

        GenericEntityManager gem = VSMCMain.get_util_em(t);

        String qry = "select T1 from AbstractStoragePool T1 where T1.pool_idx=" + t.getIdx();
        AbstractStorageNode n = gem.createSingleResultQuery(qry, AbstractStorageNode.class);

        if (n != null)
        {
            main.Msg().errmOk(VSMCMain.Txt("Dieses_Objekt_ist_noch_in_Benutzung") + " -> " + VSMCMain.Txt("Storageknoten") + " " + n.getName() );
            return false;
        }
        return true;
    }
    @Override
    protected GenericEntityManager get_em()
    {
        GenericEntityManager gem = VSMCMain.get_util_em(activeElem);
        return gem;
    }

}
