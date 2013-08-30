/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import de.dimm.vsm.auth.User;
import de.dimm.vsm.records.AbstractStorageNode;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.CheckObjectDlg;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADBLinkField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Table.PreviewPanel;
import de.dimm.vsm.vaadin.VSMCMain;

/**
 *
 * @author Administrator
 */
public class NodePreviewPanel extends PreviewPanel<AbstractStorageNode>
{

    public NodePreviewPanel( AbstractStorageNodeTable j, boolean readOnly )
    {
        super(j, readOnly);
    }



    @Override
    public void recreateContent( final AbstractStorageNode node )
    {
        removeAllComponents();

         // CREATE GUI ITEMS
        for (int i = 0; i < table.getFieldList().size(); i++)
        {
            JPAField jPAField = table.getFieldList().get(i);

            final Component gui =  jPAField.createGui(node);
            addComponent( gui );

            if (jPAField instanceof JPADBLinkField)
            {
                JPADBLinkField linkField = (JPADBLinkField)jPAField;
                addDBLinkClickListener( gui, linkField );
            }
            if (jPAField.getFieldName().equals("mountPoint"))
            {
                jPAField.setReadOnly(gui, rdOnly || !table.isNew() );
            }
            else
            {
                jPAField.setReadOnly(gui, rdOnly);
            }
        }

        setData(node);


        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        HorizontalLayout hCheck = new HorizontalLayout();
        hCheck.setSpacing(true); 
        
        Button deleteNode = new Button(VSMCMain.Txt("Nodeinhalt_leeren"), new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                ((AbstractStorageNodeTable)table).emptyNode( node );
            }
        });
        Button moveNode = new Button(VSMCMain.Txt("Nodeinhalt_umbewegen"), new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                ((AbstractStorageNodeTable)table).moveNode( node );
            }
        });
        Button syncNode = new Button(VSMCMain.Txt("CloneNode synchronisieren"), new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                ((AbstractStorageNodeTable)table).syncNode( node );
            }
        });


        Button checkNode = new Button(VSMCMain.Txt("Node prüfen"), new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                showCheckNodeDlg( node );
            }
        });

        Button scanNode = new Button(VSMCMain.Txt("Node einscannen"), new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                scanNode( node );
            }           
        });


        hl.addComponent(deleteNode);
        hl.addComponent(moveNode);

        if (node.getCloneNode() != null)
        {
             hl.addComponent(syncNode);
        }
        
        hCheck.addComponent(checkNode);
        hl.addComponent(scanNode);

        addComponent(hl);
        addComponent(hCheck);


    }

    private void showCheckNodeDlg( AbstractStorageNode activeElem )
    {
        CheckObjectDlg dlg = new CheckObjectDlg(table.getMain(), activeElem, "StorageNode");
        table.getApplication().getMainWindow().addWindow(dlg);
    }
    
     private void scanNode( AbstractStorageNode activeElem )
    {
        if (!table.getMain().getGuiUser().isSuperUser()) {
            VSMCMain.notify(this, VSMCMain.Txt("Nicht gestattet"), VSMCMain.Txt("Diese Funktion darf nur von Administratoren ausgeführt werden"));
            return;
        }
        
        table.getMain().getGuiServerApi().scanDatabase(User.createSystemInternal(), activeElem);
        VSMCMain.notify(this, VSMCMain.Txt("Job gestartet"), VSMCMain.Txt("Sie können den Fortstritt des Auftrags in der JobAnzeige verfolgen"));        
    }    



}
