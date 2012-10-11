/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractBeanContainer;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Table;
import de.dimm.vsm.net.ScheduleStatusEntry;
import de.dimm.vsm.net.ScheduleStatusEntry.ValueEntry;
import de.dimm.vsm.net.StoragePoolWrapper;
import de.dimm.vsm.records.Schedule;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.SelectObjectCallback;
import de.dimm.vsm.vaadin.VSMCMain;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.Timer;



/**
 *
 * @author Administrator
 */
public class StartBackupWin extends SidebarPanel
{
    
    boolean mounted = false;
    StoragePoolWrapper wrapper = null;

    Table table;
//    StatusEntryContainer container;
    Timer timer;

    public StartBackupWin( VSMCMain _main )
    {
        super(_main);
        
        this.main = _main;
        this.setStyleName("statusWin");
        this.setSizeFull();
        AbsoluteLayout al = new AbsoluteLayout();
        al.setSizeFull();
        this.addComponent(al);


        HorizontalLayout bt_hl = new HorizontalLayout();
        bt_hl.setSpacing(true);
        bt_hl.setWidth("100%");



        timer = new Timer(1000, new ActionListener()
        {

            @Override
            public void actionPerformed( ActionEvent e )
            {
                timer.stop();
                requestStatus();
                timer.start();
            }
        });
        


        final Button startBackup = new NativeButton("Start Backup");
        startBackup.addListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( ClickEvent event )
            {
                SelectObjectCallback cb = new SelectObjectCallback<Schedule>() {

                    @Override
                    public void SelectedAction( Schedule o )
                    {
                        try
                        {
                            main.getGuiServerApi().startBackup(o, main.getUser());
                            VSMCMain.notify(startBackup, VSMCMain.Txt("Backup") + " " + o.getName() + " " + VSMCMain.Txt("wurde_gestartet"), "");
                        }
                        catch (Exception exception)
                        {
                            main.Msg().errmOk(VSMCMain.Txt("Das_Backup_konnte_nicht_gestartet_werden") + " :" + exception.getMessage() );
                        }
                    }
                };

                final List<Schedule> allSchedList = buildAllSchedList();
                main.SelectObject(Schedule.class, VSMCMain.Txt("Zeitplan"), VSMCMain.Txt("Weiter"), allSchedList, cb );
            }
        });
        al.addComponent( startBackup, "top:30px;left:10px" );

        Button stopBackup = new NativeButton("Stop Backup");
        stopBackup.addListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( ClickEvent event )
            {
                SelectObjectCallback cb = new SelectObjectCallback<Schedule>() {

                    @Override
                    public void SelectedAction( Schedule o )
                    {
                        try
                        {
                            main.getGuiServerApi().abortBackup(o);
                        }
                        catch (Exception exception)
                        {
                            main.Msg().errmOk(VSMCMain.Txt("Das_Backup_konnte_nicht_gestoppt_werden") + " :" + exception.getMessage() );
                        }
                    }
                };

                final List<Schedule> allSchedList = buildAllSchedList();
                main.SelectObject( Schedule.class, VSMCMain.Txt("Zeitplan"), VSMCMain.Txt("Weiter"), allSchedList, cb );
            }
        });
        al.addComponent( stopBackup, "top:30px;left:140px" );

        table = new Table("BackupStatus");
        table.setSizeFull();
        al.addComponent( table, "top:80px;left:10px" );
    }

    
    List<Schedule> buildAllSchedList()
    {
        List<StoragePool> list = main.getStoragePoolList();
        final List<Schedule> allSchedList = new ArrayList<Schedule>();
        for (int i = 0; i < list.size(); i++)
        {
            try
            {
                StoragePool storagePool = list.get(i);
                List<Schedule> sch = VSMCMain.get_util_em(storagePool).createQuery("Select s from Schedule s", Schedule.class);
                allSchedList.addAll(sch);
            }
            catch (SQLException ex)
            {
                VSMCMain.notify(this, "Fehler beim Erstellen der Poolliste", ex.getMessage());
            }
        }
        return allSchedList;
    }

    @Override
    public void activate()
    {
        timer.start();
    }

    @Override
    public void deactivate()
    {
        timer.stop();
    }

    class StatusProperty implements Property
    {
        String prop;
        ScheduleStatusEntry sse;

        public StatusProperty( String prop, ScheduleStatusEntry sse )
        {
            this.prop = prop;
            this.sse = sse;
        }


        @Override
        public Object getValue()
        {
            if (prop.equals("sched"))
                return sse.getSchedule().getName();
            if (prop.equals("start"))
            {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                return sdf.format( new Date( sse.getTimestamp()));
            }
            for (int i = 0; i < sse.getValues().size(); i++)
            {
                 ValueEntry ve = sse.getValues().get(i);
                 if (ve.getNiceName().equals(prop))
                     return ve.getValue().toString();
            }
            return "?";
        }

        @Override
        public void setValue( Object newValue ) throws ReadOnlyException, ConversionException
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Class<?> getType()
        {
            return String.class;
        }

        @Override
        public boolean isReadOnly()
        {
            return true;
        }

        @Override
        public void setReadOnly( boolean newStatus )
        {

        }
    }

    class StatusBeanContainer extends AbstractBeanContainer<String, ScheduleStatusEntry>
    {
        List<ScheduleStatusEntry> statusList;

        public StatusBeanContainer(List<ScheduleStatusEntry> statusList)
        {
            super(ScheduleStatusEntry.class);
            this.addAll(statusList);
            this.statusList = statusList;
        }

        @Override
        public Property getContainerProperty( Object itemId, Object propertyId )
        {
            String sched = itemId.toString();
            String prop =propertyId.toString();
            for (int i = 0; i < statusList.size(); i++)
            {
                ScheduleStatusEntry scheduleStatusEntry = statusList.get(i);
                if (scheduleStatusEntry.getSchedule().getName().equals(sched))
                {
                    return new StatusProperty(prop, scheduleStatusEntry);
                }
            }
            return null;
        }
        @Override
        protected String resolveBeanId(ScheduleStatusEntry bean)
        {
            return bean.getSchedule().getName();
        }
    
    }


    void requestStatus()
    {
        List<ScheduleStatusEntry> statusList = main.getDummyGuiServerApi().listSchedulerStats();

        if (!statusList.isEmpty() && !statusList.get(0).getValues().isEmpty() && table.getVisibleColumns().length == 0)
        {
            ScheduleStatusEntry entry = statusList.get(0);

            List<ScheduleStatusEntry.ValueEntry> values = entry.getValues();

            table.addContainerProperty("sched", String.class,  "");
            table.addContainerProperty("start", String.class,  "");
            for (int i = 0; i < values.size(); i++)
            {
                ScheduleStatusEntry.ValueEntry valueEntry = values.get(i);
                table.addContainerProperty(valueEntry.getNiceName(), String.class,  "");
            }
        }

        table.removeAllItems();
        StatusBeanContainer ct = new StatusBeanContainer(statusList);

        
        table.setContainerDataSource(ct);
        
        table.requestRepaintAll();
    }
}
