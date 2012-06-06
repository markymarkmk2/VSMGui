/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import com.github.wolfie.refresher.Refresher;
import de.dimm.vsm.vaadin.VSMCMain;

/**
 *
 * @author Administrator
 */
public class TasksWin extends SidebarPanel
{
    TasksPanel panel;
    
    final Refresher refresher = new Refresher();

    public TasksWin( VSMCMain _main )
    {
        super(_main);

        panel = new TasksPanel(main);
        this.addComponent( panel );
        this.addComponent(refresher);


    }
    @Override
    public void activate()
    {
        panel.activate();
    }

    @Override
    public void deactivate()
    {
        panel.deactivate();
    }

}
