/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import de.dimm.vsm.records.MountEntry;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.MountEntryTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author mw
 */
public class MountEntrySubDBPanel extends PoolSubDBPanel<MountEntry> {

    MountEntrySubDBPanel(PoolEditorWin win, VSMCMain main)
    {
        super(win, main, VSMCMain.Txt("Mounteinträge"), "images/retention.png");
        
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

        List<MountEntry> list = null;
        try
        {
            list = VSMCMain.get_util_em(pool).createQuery("select s from MountEntry s", MountEntry.class);
        }
        catch (SQLException sQLException)
        {
            VSMCMain.notify(win, VSMCMain.Txt("Fehler beim Erzeugen der Liste"), sQLException.getMessage());
        }


        try
        {
            table = MountEntryTable.createTable(main, pool, list, l);
        }
        catch (Exception sQLException)
        {
            VSMCMain.notify(win, VSMCMain.Txt("Fehler beim Erzeugen der Mountentry-Tabelle"), sQLException.getMessage());
            return new VerticalLayout();
        }
        
        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = table.createHeader(VSMCMain.Txt("Liste der Mount-Einträge:"));

        tableWin.addComponent(head);
        tableWin.addComponent(table);
        tableWin.setExpandRatio(table, 1.0f);
        return tableWin;
    }
    
}
