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
