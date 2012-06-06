/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import de.dimm.vsm.records.AbstractStorageNode;
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
            jPAField.setReadOnly(gui, rdOnly);
        }

        setData(node);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        
        NativeButton deleteNode = new NativeButton(VSMCMain.Txt("Nodeinhalt_leeren"), new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                ((AbstractStorageNodeTable)table).emptyNode( node );
            }
        });
        NativeButton moveNode = new NativeButton(VSMCMain.Txt("Nodeinhalt_umbewegen"), new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                ((AbstractStorageNodeTable)table).moveNode( node );
            }
        });

        hl.addComponent(deleteNode);
        hl.addComponent(moveNode);

        addComponent(hl);


    }


}
