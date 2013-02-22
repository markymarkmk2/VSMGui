/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import de.dimm.vsm.records.Snapshot;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.SnapshotTable;
import de.dimm.vsm.vaadin.VSMCMain;

/**
 *
 * @author mw
 */
public class SnapshotSubDBPanel extends PoolSubDBPanel<Snapshot> {

    SnapshotSubDBPanel(PoolEditorWin win, VSMCMain main)
    {
        super(win, main, "Snapshots", "images/snapshot.png");
        
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

        try
        {
            table = SnapshotTable.createTable(main, pool, l);
        }
        catch (Exception sQLException)
        {
            VSMCMain.notify(win, "Fehler beim Erzeugen der Snapshot-Tabelle", sQLException.getMessage());
            return new VerticalLayout();
        }
        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = table.createHeader(VSMCMain.Txt("Liste der Snapshots:"));

        tableWin.addComponent(head);
        tableWin.addComponent(table);
        tableWin.setExpandRatio(table, 1.0f);
        return tableWin;
    }

}
