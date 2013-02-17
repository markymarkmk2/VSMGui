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
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Component;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;



/**
 *
 * @author Administrator
 */
public class JPATextField<T> extends JPAField<T>
{
    protected boolean password = false;
    public JPATextField(String caption, String fieldName)
    {
        super( caption, fieldName );        
    }

    @Override
    public Component createGui(T _node)
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

        final MethodProperty p = new NoNullMethodProperty(node,fieldName);
        p.addListener(vcl);
       


        AbstractTextField tf = (password)? new PasswordField(caption, p): new TextField(caption, p);
        tf.setData(this);

        if (toolTip != null)
            tf.setDescription(toolTip);

        if (validator != null)
        {
            tf.addValidator(validator);
            tf.setValidationVisible(true);
            tf.setRequired(true);
        }
        
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
        else if (getExpandRatio() > 0)
        {
            tf.setWidth("100%");
        }
        tf.setVisible(isFieldVisible());


        return tf;
    }

    public void setPassword( boolean b )
    {
        password = b;
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

    @Override
    public boolean isValid( AbstractOrderedLayout panel )
    {
        Component gui = getGuiforField(panel, fieldName);
        if (gui != null && gui.isVisible())
        {
            AbstractTextField tf = (AbstractTextField)gui;
            return tf.isValid();
        }
        // MAYBE NOT VISIUAL, WE USSUME UNCHANGED OR IRRELEVANT
        return true;
    }
    public static class NoNullMethodProperty extends MethodProperty
    {

        public NoNullMethodProperty( Object o, String f)
        {
            super( o, f );
        }


        @Override
        public Object getValue()
        {
            Object o = super.getValue();
            if (o == null)
                o = "";
            return o;
        }

        @Override
        public void setValue( Object newValue ) throws ReadOnlyException, ConversionException
        {
            Object o = super.getValue();
            if (newValue != null && newValue.toString().isEmpty() && o == null)
            {
                return;
            }

            super.setValue(newValue);
        }

    }

}
