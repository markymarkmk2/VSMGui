/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin;

import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import de.dimm.vsm.records.Schedule;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.ScheduleTable;
import java.sql.SQLException;

import java.util.List;




/**
 *
 * @author Administrator
 */
public class ScheduleEditor extends HorizontalSplitPanel
{
    VSMCMain main;

    VerticalSplitPanel poolSplitter;

    VerticalSplitPanel nodeSplitter;

    
    BaseDataEditTable<Schedule> table;



    public ScheduleEditor(VSMCMain main, StoragePool pool)
    {
        this.main = main;
        setStyleName("editHsplitter");
        
        Component tableWin = createScheduleTablePanel(pool);
                

        poolSplitter = new VerticalSplitPanel();
        poolSplitter.setSplitPosition(30, Sizeable.UNITS_PERCENTAGE);
        nodeSplitter = new VerticalSplitPanel();
        nodeSplitter.setSplitPosition(30, Sizeable.UNITS_PERCENTAGE);

        poolSplitter.setFirstComponent(tableWin);

        this.setSplitPosition(50, Sizeable.UNITS_PERCENTAGE);
        this.setFirstComponent(poolSplitter);
        this.setSecondComponent(nodeSplitter);
    }
    

    final Component createScheduleTablePanel(StoragePool pool)
    {        
        ItemClickListener l = new ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                showActiveElem();
            }
        };
        List<Schedule> list = null;
        try
        {
            list = VSMCMain.get_util_em(pool).createQuery("select s from Schedule s", Schedule.class);
        }
        catch (SQLException sQLException)
        {
            VSMCMain.notify(this, "Fehler beim Abruf der Pools", sQLException.getMessage());
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

    private void showActiveElem()
    {
        AbstractLayout panel = table.createEditComponentPanel(/*readonly*/true);
        panel.setSizeUndefined();
        panel.setWidth("100%");

        poolSplitter.setSecondComponent(panel);

        

        nodeSplitter.setFirstComponent(new Label(""));
        nodeSplitter.setSecondComponent(new Label(""));
    }


}
