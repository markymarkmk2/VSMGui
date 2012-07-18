 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import de.dimm.vsm.records.AbstractStorageNode;
import de.dimm.vsm.records.Retention;
import de.dimm.vsm.records.Schedule;
import de.dimm.vsm.records.Snapshot;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.AbstractStorageNodeTable;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.RetentionTable;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.ScheduleTable;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.SnapshotTable;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.StoragePoolTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.sql.SQLException;

import java.util.List;




/**
 *
 * @author Administrator
 */
public class PoolEditorWin extends SidebarPanel
{
    VerticalSplitPanel poolSplitter;

    VerticalSplitPanel scheduleSplitter;
    VerticalSplitPanel snapshotSplitter;
    VerticalSplitPanel retentionSplitter;
    VerticalSplitPanel nodeSplitter;
    TabSheet poolSubElemsTabsheet;

    BaseDataEditTable<AbstractStorageNode> nodeTable;
    BaseDataEditTable<StoragePool> poolTable;
    BaseDataEditTable<Schedule> schedTable;
    BaseDataEditTable<Snapshot> snapshotTable;
    BaseDataEditTable<Retention> retentionTable;
    HorizontalSplitPanel mainPanel;



    public PoolEditorWin(VSMCMain main)
    {
        super(main);
    }

    @Override
    public void activate()
    {
        super.activate();
        build_gui();
    }

    void build_gui()
    {
        if (mainPanel != null)
            return;

        mainPanel = new HorizontalSplitPanel();

        this.addComponent(mainPanel);
        this.setSizeFull();
        setStyleName("editHsplitter");

        Component tableWin = createPoolTablePanel();


        poolSplitter = new VerticalSplitPanel();
        poolSplitter.setSplitPosition(30, Sizeable.UNITS_PERCENTAGE);
        scheduleSplitter = new VerticalSplitPanel();
        scheduleSplitter.setSplitPosition(30, Sizeable.UNITS_PERCENTAGE);
        snapshotSplitter = new VerticalSplitPanel();
        snapshotSplitter.setSplitPosition(30, Sizeable.UNITS_PERCENTAGE);
        retentionSplitter = new VerticalSplitPanel();
        retentionSplitter.setSplitPosition(30, Sizeable.UNITS_PERCENTAGE);
        nodeSplitter = new VerticalSplitPanel();
        nodeSplitter.setSplitPosition(30, Sizeable.UNITS_PERCENTAGE);

        poolSplitter.setFirstComponent(tableWin);

        mainPanel.setSizeFull();
        mainPanel.setSplitPosition(50, Sizeable.UNITS_PERCENTAGE);
        

        ThemeResource icon_node = new ThemeResource( "images/storagenode.png");

        TabSheet poolTabsheet = new TabSheet();
        poolTabsheet.setSizeFull();
/*        Label l = new Label("");
        l.setHeight("32px");
        vl.addComponent(l);
        vl.addComponent(poolSplitter);*/
        poolTabsheet.addTab(poolSplitter, VSMCMain.Txt("StoragePools"), icon_node);
        
        mainPanel.setFirstComponent(poolTabsheet);
        

        poolSubElemsTabsheet = new TabSheet();
        
        poolSubElemsTabsheet.setSizeFull();
        ThemeResource icon_schedule = new ThemeResource( "images/schedule.png");
        ThemeResource icon_snapshot = new ThemeResource( "images/snapshot.png");
        ThemeResource icon_retention = new ThemeResource( "images/retention.png");
        

        poolSubElemsTabsheet.addTab(nodeSplitter, VSMCMain.Txt("Speicherorte"), icon_node);
        poolSubElemsTabsheet.addTab(scheduleSplitter, VSMCMain.Txt("Sicherungspläne"), icon_schedule);
        poolSubElemsTabsheet.addTab(snapshotSplitter, VSMCMain.Txt("Snapshots"), icon_snapshot);
        poolSubElemsTabsheet.addTab(retentionSplitter, VSMCMain.Txt("Gültigkeitspläne"), icon_retention);

        mainPanel.setSecondComponent(poolSubElemsTabsheet);


    }
    

    final Component createPoolTablePanel()
    {        
        ItemClickListener l = new ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                setActiveStoragePool();
            }
        };

        List<StoragePool> list = main.getStoragePoolList();
//         List<StoragePool> list = main.get_util_em().createQuery("select p from StoragePool p", StoragePool.class);

        //List<StoragePool> list = tq.getResultList();

        poolTable = StoragePoolTable.createTable(main, list, l);

        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = poolTable.createHeader(VSMCMain.Txt("Liste der StoragePools:"));

        tableWin.addComponent(head);
        tableWin.addComponent(poolTable);
        tableWin.setExpandRatio(poolTable, 1.0f);
        return tableWin;
    }

    private void setActiveStoragePool()
    {
        AbstractLayout panel = poolTable.createLocalPreviewPanel();
        panel.setSizeUndefined();
        panel.setWidth("100%");

        poolSplitter.setSecondComponent(panel);

        Component tableWin  = createNodeTablePanel( poolTable.getActiveElem() );
        nodeSplitter.setFirstComponent(tableWin);
        nodeSplitter.setSecondComponent(new Label(""));

        tableWin  = createScheduleTablePanel( poolTable.getActiveElem() );
        scheduleSplitter.setFirstComponent(tableWin);
        scheduleSplitter.setSecondComponent(new Label(""));

        tableWin  = createSnapshotTablePanel( poolTable.getActiveElem() );
        snapshotSplitter.setFirstComponent(tableWin);
        snapshotSplitter.setSecondComponent(new Label(""));

        tableWin  = createRetentionTablePanel( poolTable.getActiveElem() );
        retentionSplitter.setFirstComponent(tableWin);
        retentionSplitter.setSecondComponent(new Label(""));
    }


    final Component createScheduleTablePanel(StoragePool pool)
    {
        ItemClickListener l = new ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                setActiveSched();
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
            VSMCMain.notify(this, "Fehelr beim Erzeugen der Liste", sQLException.getMessage());
        }
        //List<Schedule> list = tq.getResultList();

        schedTable = ScheduleTable.createTable(main, pool, list, l);

        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = schedTable.createHeader(VSMCMain.Txt("Liste der Sicherungspläne:"));

        tableWin.addComponent(head);
        tableWin.addComponent(schedTable);
        tableWin.setExpandRatio(schedTable, 1.0f);
        return tableWin;
    }
    final Component createSnapshotTablePanel(StoragePool pool)
    {
        ItemClickListener l = new ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                setActiveSnapshot();
            }
        };


        //List<Schedule> list = tq.getResultList();

        try
        {
            snapshotTable = SnapshotTable.createTable(main, pool, l);
        }
        catch (Exception sQLException)
        {
            VSMCMain.notify(this, "Fehler beim Erzeugen der Snapshot-Tabelle", sQLException.getMessage());
            return new VerticalLayout();
        }
        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = snapshotTable.createHeader(VSMCMain.Txt("Liste der Snapshots:"));

        tableWin.addComponent(head);
        tableWin.addComponent(snapshotTable);
        tableWin.setExpandRatio(snapshotTable, 1.0f);
        return tableWin;
    }

    final Component createRetentionTablePanel(StoragePool pool)
    {
        ItemClickListener l = new ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                setActiveRetention();
            }
        };


        //List<Schedule> list = tq.getResultList();

        try
        {
            retentionTable = RetentionTable.createTable(main, pool, l);
        }
        catch (Exception sQLException)
        {
            VSMCMain.notify(this, "Fehler beim Erzeugen der Retention-Tabelle", sQLException.getMessage());
            return new VerticalLayout();
        }
        
        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = retentionTable.createHeader(VSMCMain.Txt("Liste der Retentions:"));

        tableWin.addComponent(head);
        tableWin.addComponent(retentionTable);
        tableWin.setExpandRatio(retentionTable, 1.0f);
        return tableWin;
    }



    Component createNodeTablePanel( StoragePool pool )
    {
        ItemClickListener l = new ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                setActiveStorageNode();
            }
        };
        nodeTable = AbstractStorageNodeTable.createTable(main, pool, l);

        final VerticalLayout tablePanel  = new VerticalLayout();
        tablePanel.setSizeFull();
        tablePanel.setSpacing(true);

        Component head = nodeTable.createHeader(VSMCMain.Txt("StorageNodes des aktuellen StoragePools:"));

        tablePanel.addComponent( head );
        tablePanel.addComponent(nodeTable);
        tablePanel.setExpandRatio(nodeTable, 1.0f);

        return tablePanel;
        
    }


    private void setActiveSched()
    {
        AbstractLayout panel = schedTable.createLocalPreviewPanel();
        panel.setSizeUndefined();
        panel.setWidth("100%");
        scheduleSplitter.setSecondComponent(panel);
    }
    private void setActiveSnapshot()
    {
        AbstractLayout panel = snapshotTable.createLocalPreviewPanel();
        panel.setSizeUndefined();
        panel.setWidth("100%");
        snapshotSplitter.setSecondComponent(panel);
    }
    private void setActiveRetention()
    {
        AbstractLayout panel = retentionTable.createLocalPreviewPanel();
        panel.setSizeUndefined();
        panel.setWidth("100%");
        retentionSplitter.setSecondComponent(panel);
    }
    private void setActiveStorageNode()
    {
        AbstractLayout panel = nodeTable.createLocalPreviewPanel();
        panel.setSizeUndefined();
        panel.setWidth("100%");
        nodeSplitter.setSecondComponent(panel);
    }


}
