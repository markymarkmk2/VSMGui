/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Fields;

import com.vaadin.data.Container;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.ComboBox;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import java.util.Iterator;
import java.util.List;


class JPAComboMethodProperty extends MethodProperty
{
    List<ComboEntry> entries;

    public JPAComboMethodProperty( Object node,String fieldName, List<ComboEntry> entries)
    {
        super(node,fieldName);
        this.entries = entries;
    }

    @Override
    public Object getValue()
    {
        Object dbVal = super.getValue();
        if (dbVal == null)
            return null;

        for (int i = 0; i < entries.size(); i++)
        {
            ComboEntry comboEntry = entries.get(i);
            
            if (comboEntry.getDbEntry() == dbVal)
                return comboEntry.getGuiEntryKey();

            if (comboEntry.getDbEntry().toString().equals( dbVal.toString()))
            {
                //System.out.println("JPAComboMethodProperty.getValue returns " + comboEntry.getGuiEntryKey());
                return comboEntry.getGuiEntryKey();
            }
        }
        //System.out.println("JPAComboMethodProperty.getValue returns empty");
        return "";
    }



    @Override
    public void setValue( Object newValue ) throws ReadOnlyException, ConversionException
    {
        if (newValue != null)
        {

            for (int i = 0; i < entries.size(); i++)
            {
                ComboEntry comboEntry = entries.get(i);
                if (comboEntry.isGuiEntry(newValue.toString()))
                    newValue = comboEntry.getDbEntry();
            }
        }

        //System.out.println("JPAComboMethodProperty.setValue " + newValue);
        super.setValue(newValue);
    }

    @Override
    protected void invokeSetMethod( Object value )
    {
        super.invokeSetMethod(value);
    }

    @Override
    public String toString()
    {
        return super.toString();
    }

}
/**
 *
 * @author Administrator
 */
public class JPAComboField extends JPAAbstractComboField
{
    List<ComboEntry> entries;
    public JPAComboField(String caption, String fieldName, List<ComboEntry> entries, String toolTip)
    {
        super( caption, fieldName, toolTip );
        this.entries = entries;
    }
    public JPAComboField(String caption, String fieldName, List<ComboEntry> entries)
    {
        this( caption, fieldName, entries, null);
    }


    @Override
    public List<ComboEntry> getEntries()
    {
        return entries;
    }

    public void setEntries( List<ComboEntry> list, AbstractOrderedLayout panel )
    {
        this.entries.clear();
        this.entries.addAll(list);
        
        ComboBox cb = (ComboBox)getGuiforField(panel);
        if (cb != null)
        {
            final Container c = cb.getContainerDataSource();
            c.removeAllItems();
            if (entries != null)
            {
                for (final Iterator<?> i = entries.iterator(); i.hasNext();)
                {
                    c.addItem(i.next());
                }
            }
        }
    }


}
