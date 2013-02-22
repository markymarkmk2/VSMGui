/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import de.dimm.vsm.records.AbstractStorageNode;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.AbstractStorageNodeTable;
import de.dimm.vsm.vaadin.VSMCMain;

/**
 *
 * @author mw
 */
public class StorageNodeSubDBPanel extends PoolSubDBPanel<AbstractStorageNode> {

    StorageNodeSubDBPanel(PoolEditorWin win, VSMCMain main)
    {
        super(win, main, "Speicherorte", "images/storagenode.png");
        
    }
    
    @Override
    Component createTablePanel(StoragePool pool) {
    ItemClickEvent.ItemClickListener l = new ItemClickEvent.ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                setActive();
            }
        };
        table = AbstractStorageNodeTable.createTable(main, pool, l);

        final VerticalLayout tablePanel  = new VerticalLayout();
        tablePanel.setSizeFull();
        tablePanel.setSpacing(true);

        Component head = table.createHeader(VSMCMain.Txt("StorageNodes des aktuellen StoragePools:"));

        tablePanel.addComponent( head );
        tablePanel.addComponent(table);
        tablePanel.setExpandRatio(table, 1.0f);

        return tablePanel;
    }
    
}
