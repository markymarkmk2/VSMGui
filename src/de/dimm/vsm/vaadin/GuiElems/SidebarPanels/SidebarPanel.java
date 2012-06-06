/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import com.vaadin.ui.VerticalLayout;
import de.dimm.vsm.vaadin.VSMCMain;

/**
 *
 * @author Administrator
 */
public abstract class SidebarPanel extends VerticalLayout
{
    protected VSMCMain main;
    public void activate()
    {

    }

    public void deactivate()
    {
    }

    public SidebarPanel( VSMCMain main )
    {
        this.main = main;
    }

    public VSMCMain getMain()
    {
        return main;
    }

    
}
