/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import de.dimm.vsm.records.Retention;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.RetentionTable;
import de.dimm.vsm.vaadin.VSMCMain;

/**
 *
 * @author mw
 */
public class RetentionSubDBPanel extends PoolSubDBPanel<Retention> {

    RetentionSubDBPanel(PoolEditorWin win, VSMCMain main)
    {
        super(win, main, "Gültigkeitspläne", "images/retention.png");
        
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


        //List<Schedule> list = tq.getResultList();

        try
        {
            table = RetentionTable.createTable(main, pool, l);
        }
        catch (Exception sQLException)
        {
            VSMCMain.notify(win, "Fehler beim Erzeugen der Retention-Tabelle", sQLException.getMessage());
            return new VerticalLayout();
        }
        
        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = table.createHeader(VSMCMain.Txt("Liste der Retentions:"));

        tableWin.addComponent(head);
        tableWin.addComponent(table);
        tableWin.setExpandRatio(table, 1.0f);
        return tableWin;
    }
    
}
