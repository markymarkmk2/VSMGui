/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Fields;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import de.dimm.vsm.Utilities.SizeStr;
import java.lang.annotation.Retention;
import java.lang.reflect.Field;


class JPASizeStrProperty extends MethodProperty
{
   

    public JPASizeStrProperty( Object node,String fieldName)
    {
        super(node,fieldName);
    }

    @Override
    public Object getValue()
    {
        Object dbVal = super.getValue();
        if (dbVal == null)
            return null;

        return SizeStr.format( dbVal.toString());
    }



    @Override
    public void setValue( Object newValue ) throws ReadOnlyException, ConversionException
    {
        if (newValue != null)
        {
            long val = SizeStr.getSizeFromNormSize(newValue.toString());
            newValue = Long.toString(val);
        }

        //System.out.println("JPAComboMethodProperty.setValue " + newValue);
        super.setValue(newValue);
    }
}


class SizeStrColumnGenerator implements Table.ColumnGenerator
{
    JPASizeStr field; /* Format string for the Double values. */
    Label label;

    public SizeStrColumnGenerator(JPASizeStr fld)
    {
        this.field = fld;
    }

    /**
     * Generates the cell containing the value.
     * The column is irrelevant in this use case.
     */
    @Override
    public Component generateCell(Table source, Object itemId,Object columnId)
    {
        BeanItem bi = (BeanItem)source.getItem(itemId);
        Object _a = bi.getBean();

        try
        {
            Field f = _a.getClass().getDeclaredField(field.getFieldName());

            boolean ac = f.isAccessible();
            f.setAccessible(true);
            Long l = f.getLong(_a);
            f.setAccessible(ac);
            label = new Label(SizeStr.format(l));
            return label;
        }
        catch (Exception exception)
        {
            label = new Label("?");
            return label;
        }
    }
}

/**
 *
 * @author Administrator
 */
public class JPASizeStr extends JPATextField implements ColumnGeneratorField
{
    ColumnGenerator colgen;

    public JPASizeStr(String caption, String fieldName)
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

        final MethodProperty p = new JPASizeStrProperty(node,fieldName);
        p.addListener(vcl);
       


        AbstractTextField tf = (password)? new PasswordField(caption, p): new TextField(caption, p);
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
        tf.setVisible(isFieldVisible());

        return tf;
    }

    @Override
    public ColumnGenerator getColumnGenerator()
    {
        colgen =  new SizeStrColumnGenerator( this );
        return colgen;
    }
}
