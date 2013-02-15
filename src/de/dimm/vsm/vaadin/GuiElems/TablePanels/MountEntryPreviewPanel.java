/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import de.dimm.vsm.records.MountEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADBLinkField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.PreviewPanel;

/**
 *
 * @author Administrator
 */
public class MountEntryPreviewPanel extends PreviewPanel<MountEntry>
{
    HorizontalLayout hlMM;

    public MountEntryPreviewPanel( MountEntryTable j, boolean readOnly )
    {
        super(j, readOnly);
        setWidth("100%");
    }

    

    int getActualPort()
    {
        JPATextField portfield = (JPATextField)table.getField("port");
        try
        {
            return Integer.parseInt(portfield.getGuiValue(this));
        }
        catch (NumberFormatException numberFormatException)
        {
        }
        return 0;
    }

    @Override
    public void recreateContent( final MountEntry node )
    {
        removeAllComponents();

         // CREATE GUI ITEMS
        for (int i = 0; i < table.getFieldList().size(); i++)
        {
            JPAField jPAField = table.getFieldList().get(i);

            if (jPAField.getFieldName().equals("mmVerify") || jPAField.getFieldName().equals("mmMediaType") ||
                    jPAField.getFieldName().equals("mmIP") || jPAField.getFieldName().equals("mmMountPath")  )
                continue;

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


    
       

      
    }

   

}
