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
import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteFSElemEditor;

/**
 *
 * @author Administrator
 */
public class JPARemoteFSField extends JPAField
{
    String ip;
    int port;
    String ipField;
    String portField;
    boolean onlyDirs = true;

    public JPARemoteFSField(String caption, String fieldName, String ip, int port)
    {
        super( caption, fieldName );
        this.ip = ip;
        this.port = port;
    }
    public JPARemoteFSField(String caption, String fieldName, String ipField, String portField)
    {
        super( caption, fieldName );
        this.ipField = ipField;
        this.portField = portField;
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

        RemoteFSElemEditor tf = null;
        int options = 0;
        if (onlyDirs)
            options |= RemoteFSElemEditor.ONLY_DIRS;

        if (ipField != null)
            tf = new RemoteFSElemEditor(caption, p, node, ipField, portField, options);
        else
            tf = new RemoteFSElemEditor(caption, p, ip, port, options);

        if (toolTip != null)
            tf.setDescription(toolTip);

        


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
        super.setReadOnly(gui, b);
        if (gui instanceof RemoteFSElemEditor)
        {
            RemoteFSElemEditor tf = (RemoteFSElemEditor)gui;
            tf.setReadOnly(b);
        }

    }

}
