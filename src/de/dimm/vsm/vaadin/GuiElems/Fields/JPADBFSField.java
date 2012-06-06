/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Fields;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import de.dimm.vsm.net.interfaces.GuiServerApi;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.DBFSElemEditor;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.LocalFSElemEditor;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteFSElemEditor;
import de.dimm.vsm.vaadin.GuiElems.Table.PreviewPanel;
import de.dimm.vsm.vaadin.VSMCMain;

/**
 *
 * @author Administrator
 */
public class JPADBFSField extends JPAField
{
    boolean onlyDirs = true;
    VSMCMain main;
    JPAPoolComboField poolCombo;
    PreviewPanel panel;

    public JPADBFSField(String caption, String fieldName, VSMCMain main, JPAPoolComboField poolCombo)
    {
        super( caption, fieldName );
        this.main = main;
        this.poolCombo = poolCombo;
    }

    public void setOnlyDirs( boolean onlyDirs )
    {
        this.onlyDirs = onlyDirs;
    }


    @Override
    public Component createGui(Object _node)
    {
        node = _node;
        
        ValueChangeListener vcl = new ValueChangeListener()
        {
            @Override
            public void valueChange( ValueChangeEvent event )
            {
                if (changeListener != null)
                    changeListener.valueChange(event);
            }
        };

        final MethodProperty p = new MethodProperty(node,fieldName);
        p.addListener(vcl);

        DBFSElemEditor tf = null;
        int options = 0;
        if (onlyDirs)
            options |= RemoteFSElemEditor.ONLY_DIRS;

        GuiServerApi api = main.getGuiServerApi();
        

        tf = new DBFSElemEditor(api, poolCombo, panel, caption, p, node,  options);

        if (toolTip != null)
            tf.setDescription(toolTip);

        if (getFieldWidth() >= 0)
        {
            tf.setWidth(getFieldWidth() + "px");
        }
        tf.setVisible(isFieldVisible());

       

        tf.setData(this);
        tf.getTf().addListener(vcl);
        if (tf.getTf() instanceof TextField)
        {
            ((TextField)tf.getTf()).addListener( new TextChangeListener() {

                @Override
                public void textChange( TextChangeEvent event )
                {
                    if (changeListener != null)
                        changeListener.valueChange(null);
                }
            });
        }
        
        tf.getTf().setPropertyDataSource(p);

        return tf;
    }
     @Override
    public void setReadOnly( Component gui, boolean b )
    {
        if (gui instanceof DBFSElemEditor)
        {
            DBFSElemEditor tf = (DBFSElemEditor)gui;
            tf.setReadOnly(b);
        }
        else
        {
            super.setReadOnly( gui, b );
        }
    }

    public void setActualEditPanel( PreviewPanel panel )
    {
        this.panel = panel;
    }
}
