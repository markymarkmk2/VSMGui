/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems;

import com.vaadin.ui.ComboBox;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Administrator
 */
public class VaadinHelpers
{
    public static int getSelectedIndex( ComboBox box )
    {
        Collection<?> col = box.getItemIds();
        Object o = box.getValue();
        int idx = 0;
        for (Iterator<? extends Object> it = col.iterator(); it.hasNext();)
        {
            Object object = it.next();
            if (object.equals(o))
                return idx;
            idx++;
        }
        return -1;
    }
    public static boolean setSelectedIndex( ComboBox box, int i )
    {
        Collection<?> col = box.getItemIds();
        Object o = box.getValue();


        int idx = 0;
        for (Iterator<? extends Object> it = col.iterator(); it.hasNext();)
        {
            Object object = it.next();
            if (i == idx)
            {
                box.setValue(object);
                return true;
            }
            idx++;
        }
        return false;
    }

}
