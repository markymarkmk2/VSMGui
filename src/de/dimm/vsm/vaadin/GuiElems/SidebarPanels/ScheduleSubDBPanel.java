/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import de.dimm.vsm.records.Schedule;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.ScheduleTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author mw
 */
public class ScheduleSubDBPanel extends PoolSubDBPanel<Schedule> {

    ScheduleSubDBPanel(PoolEditorWin win, VSMCMain main)
    {
        super(win, main, "Sicherungspläne", "images/schedule.png");
        
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
        long idx = pool.getIdx();
        List<Schedule> list = null;
        try
        {
            list = VSMCMain.get_util_em(pool).createQuery("select s from Schedule s where T1.pool_idx=" + idx, Schedule.class);
        }
        catch (SQLException sQLException)
        {
            VSMCMain.notify(win, "Fehler beim Erzeugen der Liste", sQLException.getMessage());
        }
        //List<Schedule> list = tq.getResultList();

        table = ScheduleTable.createTable(main, pool, list, l);

        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = table.createHeader(VSMCMain.Txt("Liste der Sicherungspläne:"));

        tableWin.addComponent(head);
        tableWin.addComponent(table);
        tableWin.setExpandRatio(table, 1.0f);
        return tableWin;
    }
    
}
