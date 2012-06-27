/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import de.dimm.vsm.records.RoleOption;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAComboField;
import de.dimm.vsm.vaadin.GuiElems.Table.PreviewPanel;
import de.dimm.vsm.vaadin.VSMCMain;

/**
 *
 * @author Administrator
 */
public class RoleOptionPreviewPanel extends PreviewPanel<RoleOption>
{

    public RoleOptionPreviewPanel( RoleOptionTable j, boolean readOnly )
    {
        super(j, readOnly);
    }



    @Override
    public void recreateContent( final RoleOption node )
    {
        super.recreateContent( node );

        Component c = table.getField("token").getGuiforField(this);
        ComboBox cb = (ComboBox) c;
        cb.addListener( new ValueChangeListener() {

            @Override
            public void valueChange( ValueChangeEvent event )
            {
                updateVisibility(node);
            }
        });

        updateVisibility( node );

    }
    void updateVisibility(final RoleOption node )
    {
        Component c = table.getField("flags").getGuiforField(this);

        // NEW ENTRY ?
        if (node.getToken() == null)
        {
            JPAComboField cb = (JPAComboField) table.getField("token");
            node.setToken(cb.getEntries().get(0).getDbEntry().toString());
        }
        c.setVisible(node.hasFlagsField());

        c = table.getField("optionStr").getGuiforField(this);
        c.setVisible(node.hasOptionField());
        if (node.getToken().endsWith(RoleOption.RL_USERPATH))
        {
            ((TextField)c).setDescription(VSMCMain.Txt("Format für erlaubte Restorepfade IP:Port/Pfad" ));
        }
        else
        {
            ((TextField)c).setDescription("");
        }
       
    }


}
