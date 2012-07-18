/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.BackupJobResult;
import de.dimm.vsm.records.BackupVolumeResult;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.BackupResultTable;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.BackupVolumeResultTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.sql.SQLException;

import java.util.List;






/**
 *
 * @author Administrator
 */
public class BackupResult extends SidebarPanel
{
    VerticalSplitPanel jobSplitter;
    VerticalSplitPanel volumeSplitter;
    HorizontalSplitPanel mainPanel;
    BaseDataEditTable<BackupJobResult> table;
    BaseDataEditTable<BackupVolumeResult> volumeTable;
    //VerticalLayout backupJobResultContainer;
    //VerticalLayout jobTableContainer;
    NativeSelect poolSelector;
    int lastPoolCnt = -1;

    @Override
    public void activate()
    {
        List<StoragePool> list = main.getStoragePoolList();
        if (lastPoolCnt == list.size() || list.isEmpty())
            return;

                
        //backupJobResultContainer.removeAllComponents();
        //jobTableContainer = new VerticalLayout();

        //backupJobResultContainer.addComponent(poolSelector);
        //backupJobResultContainer.addComponent(jobTableContainer);

        poolSelector.removeAllItems();

        for (int i = 0; i < list.size(); i++)
        {
            Object object = list.get(i);
            poolSelector.addItem(object);
        }
        if (!list.isEmpty())
        {
            poolSelector.setValue(list.get(0));
            setBackupResultPanel(list.get(0));
        }
        poolSelector.addListener( new ValueChangeListener() {

            @Override
            public void valueChange( ValueChangeEvent event )
            {
                StoragePool p =(StoragePool) poolSelector.getValue();
                setBackupResultPanel(p);
            }
        });




       
        lastPoolCnt = list.size();
    }



    public BackupResult(VSMCMain main)
    {
        super(main);
        
        setStyleName("editHsplitter");
/*        backupJobResultContainer = new VerticalLayout();
        backupJobResultContainer.setSizeFull();
*/
        jobSplitter = new VerticalSplitPanel();
        jobSplitter.setSizeFull();
        jobSplitter.setSplitPosition(35, Sizeable.UNITS_PERCENTAGE);
  //      jobSplitter.setFirstComponent(backupJobResultContainer);

        volumeSplitter = new VerticalSplitPanel();
        volumeSplitter.setSizeFull();
        volumeSplitter.setSplitPosition(35, Sizeable.UNITS_PERCENTAGE);
        

        mainPanel = new HorizontalSplitPanel();
        mainPanel.setSizeFull();
        mainPanel.setSplitPosition(50, Sizeable.UNITS_PERCENTAGE);
        mainPanel.setFirstComponent(jobSplitter);
        mainPanel.setSecondComponent(volumeSplitter);


        poolSelector = new NativeSelect(VSMCMain.Txt("Pool"));
        poolSelector.setNewItemsAllowed(false);
        poolSelector.setInvalidAllowed(false);
        poolSelector.setNullSelectionAllowed(false);
        poolSelector.setImmediate(true);

        this.addComponent(poolSelector);
        this.addComponent(mainPanel);
        this.setExpandRatio(mainPanel, 1.0f);
        this.setSizeFull();
    }
    

    void setBackupResultPanel(StoragePool pool)
    {
        //jobTableContainer.removeAllComponents();

        ItemClickListener l = new ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                setActiveBackupJob();
            }
        };
        List<BackupJobResult> list = null;
        try
        {
            list = VSMCMain.get_util_em(pool).createQuery("select p from BackupJobResult p order by T1.idx desc", BackupJobResult.class);
        }
        catch (SQLException sQLException)
        {
            VSMCMain.notify(this, "Fehler beim Erzeugen der Ergebnis-Tabelle", sQLException.getMessage());
            return;
        }
        

        table = BackupResultTable.createTable(main, pool, list, l);

        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = table.createHeader(VSMCMain.Txt("Backupjob-Ergebisse:"));

        tableWin.addComponent(head);
        tableWin.addComponent(table);
        tableWin.setExpandRatio(table, 1.0f);

        jobSplitter.setFirstComponent(tableWin);

        if (jobSplitter.getSecondComponent() != null)
            jobSplitter.removeComponent(jobSplitter.getSecondComponent());
        
        if (volumeSplitter.getFirstComponent() != null)
            volumeSplitter.removeComponent(volumeSplitter.getFirstComponent());

        if (volumeSplitter.getSecondComponent() != null)
            volumeSplitter.removeComponent(volumeSplitter.getSecondComponent());
        //jobTableContainer.addComponent( tableWin );
    }

    private void setActiveBackupJob()
    {
        AbstractLayout panel = table.createLocalPreviewPanel();
        panel.setSizeUndefined();
        panel.setWidth("100%");

        jobSplitter.setSecondComponent(panel);

        Component tableWin  = createBackupVolumePanel( table.getActiveElem() );
        volumeSplitter.setFirstComponent(tableWin);
        volumeSplitter.setSecondComponent(new Label(""));
    }

    final Component createBackupVolumePanel(BackupJobResult job)
    {
        ItemClickListener l = new ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                setActiveBackupVolumeResult();
            }
        };
        GenericEntityManager em = VSMCMain.get_util_em(job.getSchedule().getPool());

        volumeTable = BackupVolumeResultTable.createTable(main, job, job.getBackupVolumeResults().getList(em), l);

        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = volumeTable.createHeader(VSMCMain.Txt("Volume-Ergebnisse:"));

        tableWin.addComponent(head);
        tableWin.addComponent(volumeTable);
        tableWin.setExpandRatio(volumeTable, 1.0f);
        return tableWin;
    }

    private void setActiveBackupVolumeResult()
    {
        AbstractLayout panel = volumeTable.createLocalPreviewPanel();
        panel.setSizeUndefined();
        panel.setWidth("100%");
        volumeSplitter.setSecondComponent(panel);
    }

}
