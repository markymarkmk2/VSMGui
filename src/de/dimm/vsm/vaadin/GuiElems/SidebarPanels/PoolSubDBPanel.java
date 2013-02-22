/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalSplitPanel;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;

/**
 *
 * @author mw
 */
public abstract class PoolSubDBPanel<T> {
    
    protected VerticalSplitPanel splitter;
    protected BaseDataEditTable<T> table;
    String iconPath;
    protected VSMCMain main;
    PoolEditorWin win;
    String name;

    public PoolSubDBPanel(PoolEditorWin win, VSMCMain main, String name, String iconPath) {
        this.iconPath = iconPath;
        this.name = name;
        this.main= main;
        this.win = win;
    }
    
    void build_gui()
    {
        splitter = new VerticalSplitPanel();
        splitter.setSplitPosition(30, Sizeable.UNITS_PERCENTAGE);
    }

    public VerticalSplitPanel getSplitter() {
        return splitter;
    }
    
    
    ThemeResource getIcon()
    {
        return new ThemeResource( iconPath );
    }

    public String getName() {
        return name;
    }
    
    void setActive()
    {
        AbstractLayout panel = table.createLocalPreviewPanel();
        panel.setSizeUndefined();
        panel.setWidth("100%");
        splitter.setSecondComponent(panel);
    }
    void setActiveStoragePool()
    {

        Component tableWin;
        tableWin = createTablePanel( win.getPoolTable().getActiveElem() );
        splitter.setFirstComponent(tableWin);
        splitter.setSecondComponent(new Label(""));
    }
    abstract Component createTablePanel( StoragePool pool );
    
    
}
