/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

/**
 *
 * @author Administrator
 */
public class EmptyColumnGenerator implements Table.ColumnGenerator
{

    @Override
    public Component generateCell( Table source, Object itemId, Object columnId )
    {
        return null;
    }
}