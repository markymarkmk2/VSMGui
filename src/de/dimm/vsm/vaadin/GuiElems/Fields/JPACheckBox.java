/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Fields;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;

/**
 *
 * @author Administrator
 */
public class JPACheckBox extends JPAField
{
    public JPACheckBox(String caption, String fieldName)
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




        CheckBox checkBox = new CheckBox(caption);
        checkBox.setPropertyDataSource(p);
        checkBox.setReadOnly(false);
        checkBox.setInvalidAllowed(false);
        checkBox.setImmediate(true);
        if (toolTip != null)
            checkBox.setDescription(toolTip);

        if (validator != null)
        {
            checkBox.addValidator(validator);
            checkBox.setValidationVisible(true);
        }

        checkBox.setData(this);
        checkBox.setVisible(isFieldVisible());
        
        
        checkBox.addListener( new ClickListener()
        {

            @Override
            public void buttonClick( ClickEvent event )
            {
                if (changeListener != null)
                    changeListener.valueChange( null );
            }
        });

        return checkBox;
    }

    public boolean getBooleanValue(AbstractOrderedLayout panel)
    {
        Component gui = getGuiforField(panel, fieldName);
        
        if (gui == null)
            throw new RuntimeException("Gui not initialized");

        CheckBox checkBox = (CheckBox)gui;

        Boolean b = (Boolean) checkBox.getValue();
        return b.booleanValue();
    }
    public CheckBox getCheckBox(AbstractOrderedLayout panel)
    {
        Component gui = getGuiforField(panel, fieldName);
        CheckBox checkBox = (CheckBox)gui;

        return checkBox;
    }
    @Override
    public boolean isValid( AbstractOrderedLayout panel )
    {
        Component gui = getGuiforField(panel, fieldName);
        if (gui != null && gui.isVisible())
        {
            CheckBox tf = (CheckBox)gui;
            return tf.isValid();
        }
        // MAYBE NOT VISIUAL, WE USSUME UNCHANGED OR IRRELEVANT
        return true;
    }
}
