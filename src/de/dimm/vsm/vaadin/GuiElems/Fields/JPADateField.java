/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Fields;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;


class DateColumnGenerator implements Table.ColumnGenerator
{
    Label label;
    int resolution;
    SimpleDateFormat sdf;

    public DateColumnGenerator( int resolution )
    {
        this.resolution = resolution;
        sdf = new SimpleDateFormat(JPADateField.getFormatStr(resolution));
    }

    @Override
    public Component generateCell( Table source, Object itemId, Object columnId )
    {
        BeanItem it = (BeanItem) source.getItem(itemId);
        Object o = it.getBean();
        try
        {
            Field f = o.getClass().getDeclaredField(columnId.toString());
            f.setAccessible(true);
            Object d = f.get(o);
            if (d instanceof Date)
            {
                Date date = (Date) d;                
                label = new Label(sdf.format(date));
                return label;
            }
        }
        catch (Exception exception)
        {
        }
        label = new Label("?");
        return label;
    }
}

/**
 *
 * @author Administrator
 */
public class JPADateField extends JPAField implements ColumnGeneratorField
{
    String format;
    int resolution;


    public JPADateField(String caption, String fieldName, int resolution)
    {
        super( caption, fieldName );
        this.resolution = resolution;
    }
    public JPADateField(String caption, String fieldName, String format)
    {
        super( caption, fieldName );
        this.format = format;
        resolution = -1;
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


        if (resolution >= 0)
        {
            format = getFormatStr( resolution );
        }
        DateField tf = new DateField(caption, p);
        tf.setDateFormat(format);
        if (toolTip != null)
            tf.setDescription(toolTip);
        
        tf.setData(this);
        
        tf.addListener(vcl);
        tf.setPropertyDataSource(p);
        tf.setVisible(isFieldVisible());


        return tf;
    }
    static String getFormatStr( int r )
    {
        String format = "dd.MM.yyyy";
        if (r >= 0)
        {
            if (r == DateField.RESOLUTION_DAY)
                format = "dd.MM.yyyy";
            else if (r == DateField.RESOLUTION_SEC)
                format = "dd.MM.yyyy HH:mm:ss";
            else if (r == DateField.RESOLUTION_MIN)
                format = "dd.MM.yyyy HH:mm";
            else if (r == DateField.RESOLUTION_HOUR)
                format = "dd.MM.yyyy HH:00";
        }
        return format;
    }

    @Override
    public int getWidth()
    {
        return 100;
    }

    @Override
    public ColumnGenerator getColumnGenerator()
    {
        return new DateColumnGenerator(resolution);
    }

    
}
