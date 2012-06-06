/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems;

import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.NativeButton;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.SidebarPanel;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.Iterator;

/**
 *
 * @author Administrator
 */
public class SidebarButton extends NativeButton
{

    SidebarButtonCallback callback;
    SidebarPanel panel;
    VSMCMain main;

    public SidebarButton(VSMCMain _main, String text, SidebarPanel _panel, SidebarButtonCallback _callback)
    {
        super( text );
        main = _main;
        this.panel = _panel;
        this.callback = _callback;

        setWidth("100%");
        addListener(new ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {

                if (!main.checkLogin(this))
                    return;


                setSelected();
                
                if (callback != null)
                    callback.action();
                
                if (panel != null)
                {
                    panel.getMain().setMainComponent(panel);
                }
            }
        });
    }
    public SidebarButton(VSMCMain _main, String text, SidebarPanel _panel)
    {
        this( _main, text, _panel, null );
    }
    public void doCallback()
    {
        if (!main.checkLogin())
            return;
        
        if (callback != null)
            callback.action();
    }

    public void setSelected()
    {
        AbstractComponentContainer sidebar = (AbstractComponentContainer)getParent();

        for (Iterator<Component> it = sidebar.getComponentIterator(); it.hasNext();)
        {
            Component object = it.next();
            object.setStyleName(null);
        }

        setStyleName("selected");
    }

    public SidebarButtonCallback getCallback()
    {
        return callback;
    }

    public SidebarPanel getPanel()
    {
        return panel;
    }
    

}
