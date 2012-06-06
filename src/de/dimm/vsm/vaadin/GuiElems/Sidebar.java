/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems;

import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class Sidebar extends VerticalLayout
{
    List<SidebarButton> buttons;

    public Sidebar()
    {
        buttons = new ArrayList<SidebarButton>();
    }

    public void add( SidebarButton btstatus )
    {
        buttons.add(btstatus);
        addComponent(btstatus);
    }
    public void deselectAll()
    {
        for (int i = 0; i < buttons.size(); i++)
        {
            SidebarButton bt = buttons.get(i);
            bt.setStyleName(null);
        }
    }



}
