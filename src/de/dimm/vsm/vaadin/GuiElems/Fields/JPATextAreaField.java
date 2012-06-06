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
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextArea;

/**
 *
 * @author Administrator
 */
public class JPATextAreaField extends JPAField
{
    int fieldHeight = -1;

    public JPATextAreaField(String caption, String fieldName)
    {
        super( caption, fieldName );        
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
       


        TextArea tf = new TextArea(caption, p);
        tf.setData(this);

        if (toolTip != null)
            tf.setDescription(toolTip);
        
        tf.addListener(vcl);
        tf.addListener( new TextChangeListener() {

            @Override
            public void textChange( TextChangeEvent event )
            {
                if (changeListener != null)
                    changeListener.valueChange(null);
            }
        });
        
        tf.setPropertyDataSource(p);
        if (getFieldWidth() >= 0)
        {
            tf.setWidth(getFieldWidth() + "px");
        }
        if (getFieldHeight() >= 0)
        {
            tf.setRows(fieldHeight);
        }
        tf.setVisible(isFieldVisible());

        return tf;
    }

    public void setFieldHeight( int fieldHeight )
    {
        this.fieldHeight = fieldHeight;
    }

    public int getFieldHeight()
    {
        return fieldHeight;
    }


    public String getGuiValue(AbstractOrderedLayout panel)
    {
        Component gui = getGuiforField(panel, fieldName);
        if (gui != null)
        {
            AbstractTextField tf = (AbstractTextField)gui;
            return tf.getValue().toString();
        }
        return null;
    }
}
