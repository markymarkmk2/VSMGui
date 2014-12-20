/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import com.vaadin.ui.Button.ClickEvent;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.JobTable;
import com.github.wolfie.refresher.Refresher;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import de.dimm.vsm.jobs.JobEntry;
import de.dimm.vsm.jobs.JobInterface.JOBSTATE;
import de.dimm.vsm.vaadin.VSMCMain;

/**
 *
 * @author Administrator
 */
public class JobWin extends SidebarPanel implements Refresher.RefreshListener
{
    JobTable jobPanel;
    TasksPanel tasksPanel;
    final Refresher refresher = new Refresher();
    public static final int RF_INTERVALL = 1000;
    // MIN ABSTAND ZWISCHEN REFRESHES BEI BEI STAU 
    public static final int MIN_REFRESH_MS = 100;
    long lastRefresh = 0;
    

    public JobWin( VSMCMain _main )
    {
        super(_main);

        jobPanel = new JobTable(main);


        tasksPanel = new TasksPanel(main);

        Button bt = new Button(VSMCMain.Txt("Beendete Aufgaben entfernen"), new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                removeFinishedJobs();
            }
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        hl.setSpacing(true);
        hl.addComponent(bt);
        hl.setComponentAlignment(bt, Alignment.MIDDLE_RIGHT);
        hl.setMargin(false, false, true, false);


        this.addComponent( jobPanel );
        //this.setExpandRatio(jobPanel, 0.6f);
        this.addComponent( hl );
        this.addComponent( tasksPanel );
        //this.setExpandRatio(tasksPanel, 0.4f);
        this.addComponent(refresher);
        //this.setSizeFull();

        refresher.addListener(this);
    }

    void removeFinishedJobs()
    {
        BeanContainer<Long, JobEntry> bc = jobPanel.getBc();

        for (Long itemId : bc.getItemIds())
        {
            JobEntry je = bc.getItem(itemId).getBean();
            if (je.getJobStatus() == JOBSTATE.FINISHED_ERROR || je.getJobStatus() == JOBSTATE.FINISHED_OK)
            {
                je.getJob().abortJob();
            }
        }
    }

    @Override
    public void activate()
    {
        jobPanel.activate();
        if (main.getUser().isAdmin())
        {
            tasksPanel.activate();
        }
        tasksPanel.setVisible(main.getUser().isAdmin());


    }

    @Override
    public void deactivate()
    {
        jobPanel.deactivate();
        tasksPanel.deactivate();        
    }

    @Override
    public void refresh( Refresher source )
    {        
        long s = System.currentTimeMillis();
        if (s - lastRefresh < MIN_REFRESH_MS)
        {
            lastRefresh = s;
            return;
        }
        
        source.setRefreshInterval(0);
        
        jobPanel.refresh();
        tasksPanel.refresh();
        long e = System.currentTimeMillis();
        lastRefresh = e;

        // COMM TIME SHOULD NOT EXCEED 20% OF CYCLE TIME
        int rfi = (int)((e-s) * 5);
        if (rfi < RF_INTERVALL)
            rfi = RF_INTERVALL;

        source.setRefreshInterval(rfi);

    }

}
