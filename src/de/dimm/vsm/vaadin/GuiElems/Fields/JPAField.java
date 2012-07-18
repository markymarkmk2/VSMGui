/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Fields;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomComponent;
import de.dimm.vsm.fsengine.GenericEntityManager;
import java.util.Iterator;

/**
 *
 * @author Administrator
 */
public abstract class JPAField
{
    protected Object node;
    protected String caption;
    protected String fieldName;
    ValueChangeListener changeListener;
    //protected Component gui;
    protected String toolTip;
    boolean tableColumnVisible;
    boolean fieldVisible;
    private int defaultWidth = -1;
    private int defaultFieldWidth = -1;
    private float defaultExpandRatio = -1;

    Validator validator;

    public JPAField(  String caption, String fieldName )
    {
        this.caption = caption;
        this.fieldName = fieldName;
        tableColumnVisible = true;
        fieldVisible = true;
    }
    public JPAField(  String caption, String fieldName, String toolTip )
    {
        this.caption = caption;
        this.fieldName = fieldName;
        this.toolTip = toolTip;
        tableColumnVisible = true;
        fieldVisible = true;
    }


    public abstract Component createGui(Object node);
    public void setValueChangeListener( ValueChangeListener l)
    {
        changeListener = l;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public String getCaption()
    {
        return caption;
    }

    public Object getNode()
    {
        return node;
    }

    

    public void setReadOnly( Component gui, boolean b )
    {

        if (gui != null)
        {
            gui.setReadOnly(b);
            if (gui instanceof AbstractField)
            {
                AbstractField agui = (AbstractField)gui;
                if (agui.getPropertyDataSource() != null)
                    agui.getPropertyDataSource().setReadOnly(b);
            }
            if (gui instanceof ComponentContainer)
            {
                ComponentContainer cgui = (ComponentContainer)gui;

                for (Iterator<Component> it = cgui.getComponentIterator(); it.hasNext();)
                {
                    Component c = it.next();
                    if (c instanceof AbstractField)
                    {
                        AbstractField agui = (AbstractField)c;
                        if (agui.getPropertyDataSource() != null)
                            agui.getPropertyDataSource().setReadOnly(b);
                    }
                }
            }
        }
    }

    public int getWidth()
    {
        return defaultWidth;
    }

    public int getFieldWidth()
    {
        return defaultFieldWidth;
    }

    public float getExpandRatio()
    {
        return defaultExpandRatio;
    }

    public void update( BeanItem oldItem )
    {
        String property = getFieldName();
        if (oldItem.getItemProperty(property) == null)
        {
            System.out.println("No property: " + property);
            return;
        }
        Object v = oldItem.getItemProperty(property).getValue();
        oldItem.getItemProperty(property).setValue(v);
    }


    public void update( BeanItem oldItem, BeanItem newItem )
    {
        String property = getFieldName();
        Object v = newItem.getItemProperty(property).getValue();
        oldItem.getItemProperty(property).setValue(v);
    }

    public void addValidator(  AbstractOrderedLayout panel, Validator stringLengthValidator )
    {
        Component gui = getGuiforField( panel );
        if (gui != null && gui instanceof AbstractField)
            ((AbstractField)gui).addValidator(stringLengthValidator);
        else
            throw new RuntimeException("Calling addValidator on uninitialized or inapropriate gui");
    }

    public Component getGuiforField( AbstractOrderedLayout panel )
    {
        return getGuiforField(panel, fieldName);
    }
    public static Component getGuiforField( AbstractOrderedLayout panel, String field )
    {
        for (int i = 0; i < panel.getComponentCount(); i++)
        {
            Component component = panel.getComponent(i);
            if (component instanceof AbstractOrderedLayout)
            {
                Component c = getGuiforField( (AbstractOrderedLayout) component,field);
                if (c != null)
                    return c;
            }
            else if(component instanceof AbstractField)
            {
                AbstractField fld = (AbstractField) component;
                if (fld.getData() != null && fld.getData() instanceof JPAField)
                {
                    JPAField jpaFld = (JPAField) fld.getData();
                    if (jpaFld.getFieldName().equals(field))
                        return component;
                }
            }
            else if(component instanceof CustomComponent)
            {
                CustomComponent fld = (CustomComponent) component;
                if (fld.getData() != null && fld.getData() instanceof JPAField)
                {
                    JPAField jpaFld = (JPAField) fld.getData();
                    if (jpaFld.getFieldName().equals(field))
                        return component;
                }
            }
        }
        return null;
    }

    public void setTableColumnVisible( boolean tableColumnVisible )
    {
        this.tableColumnVisible = tableColumnVisible;
    }

    public boolean isTableColumnVisible()
    {
        return tableColumnVisible;
    }

    public void setFieldVisible( boolean fieldVisible )
    {
        this.fieldVisible = fieldVisible;
    }

    public boolean isFieldVisible()
    {
        return fieldVisible;
    }
    

    public void setWidth( int visible )
    {
        defaultWidth = visible;
    }
    public void setFieldWidth( int visible )
    {
        defaultFieldWidth = visible;
    }
    public void setExpandRatio( float visible )
    {
        defaultExpandRatio = visible;
    }

    public void setTooltipText( String txt )
    {
        toolTip = txt;
    }

    public void setValidator( Validator v )
    {
        validator = v;
    }

    public boolean isValid(  AbstractOrderedLayout panel )
    {
        return true;
    }

    

}
