/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import de.dimm.vsm.records.Job;
import de.dimm.vsm.records.Schedule;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Table.PreviewPanel;
import de.dimm.vsm.vaadin.GuiElems.VaadinHelpers;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


class HourComboBox extends ComboBox
{
    HourComboBox( String txt, List l )
    {
        super(txt, l );
        pageLength = 24;
        setNewItemsAllowed(false);
        setNullSelectionAllowed(false);
    }
}
class MinuteComboBox extends ComboBox
{

    MinuteComboBox( String txt, List l )
    {
        super(txt, l );
        pageLength = 12; // 5 Minute entries
        setNewItemsAllowed(false);
        setNullSelectionAllowed(false);
    }
}
/**
 *
 * @author Administrator
 */
public class ScheduleJobPreviewPanel extends PreviewPanel<Job>
{
    JPACheckBox disabled;
    
    ComboBox day_number;
    HorizontalLayout starttime_panel;
    BeanItemContainer day_number_list;

    ComboBox comboHour;
    ComboBox comboMin;

    public ScheduleJobPreviewPanel( ScheduleJobTable j, boolean readOnly )
    {
        super(j, readOnly);

        List<String> hdim = new ArrayList<String>();
        List<String> mdim = new ArrayList<String>();
        for (int i = 0; i < 24; i++)
        {
            hdim.add(Integer.toString(i));
        }
        for (int i = 0; i < 60; i+=5)
        {
            mdim.add(Integer.toString(i));
        }
        comboHour = new HourComboBox(VSMCMain.Txt("Startzeit"), hdim);
        comboHour.setWidth("60px");
        comboMin = new MinuteComboBox(":", mdim);
        comboMin.setWidth("60px");


    }

    int getDayNumber()
    {
        return  VaadinHelpers.getSelectedIndex(day_number);
    }

    

    @Override
    public void recreateContent( final Job node )
    {
        removeAllComponents();

        disabled = new JPACheckBox(Txt("Gesperrt"), "disabled");
        
        day_number_list = new BeanItemContainer(String.class);
        day_number = new ComboBox(Txt("Tag"));
        day_number.setContainerDataSource(day_number_list);
        day_number.setNullSelectionAllowed(false);


        // ADD TO LAYOUT
        addComponent(disabled.createGui(node));
        

        addComponent( day_number );

        starttime_panel = new HorizontalLayout();
        starttime_panel.setSpacing(true);
        starttime_panel.addComponent(comboHour);
        starttime_panel.addComponent(comboMin);
        addComponent( starttime_panel );

        setData(node);

    }

    @Override
    public void attach()
    {
        Job node = (Job)getData();
        super.attach();
        setValues(node );
        check_visibility(node);
    }


    void check_visibility(Job node)
    {
        ScheduleJobTable tb = (ScheduleJobTable)this.table;

        boolean c = node.getSched().getIsCycle();
        long cycle_len_s = node.getSched().getCycleLengthMs() / 1000;
        int max_n = 1;
        Schedule sched = tb.sched;
        Date startDate = sched.getScheduleStart();
        if (startDate == null)
            startDate = new Date();

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(startDate);

        if (cycle_len_s > 86400)
        {
            max_n = (int)(cycle_len_s / 86400);

            day_number_list.removeAllItems();
            for (int i = 1; i <= max_n; i++)
            {
                
                String day = getCalWeekDayStr( cal.get(GregorianCalendar.DAY_OF_WEEK) );
                day_number_list.addBean("" + i + " (" + day + ")");
                cal.roll(GregorianCalendar.DAY_OF_MONTH, 1);
            }

            day_number.setVisible(true);
        }
        else
        {
            day_number.setVisible(false);
        }
                

        
    }

    private static String Txt(String key )
    {
        return VSMCMain.Txt(key);
    }

    private void setValues(Job node)
    {
        VaadinHelpers.setSelectedIndex(day_number, node.getDayNumber());
        int h = (int)(node.getOffsetStartMs() / 3600000);
        int m = (int)(node.getOffsetStartMs() / 60000) % 60;

         VaadinHelpers.setSelectedIndex(comboHour, h);
         VaadinHelpers.setSelectedIndex(comboMin, m / 5);

    }

    private String getCalWeekDayStr( int get )
    {
        // 1 == SUNDAY ... 7 == SATURDAY
        ArrayList<String> days = new ArrayList<String>();
        days.add(Txt("Sonntag"));
        days.add(Txt("Montag"));
        days.add(Txt("Dienstag"));
        days.add(Txt("Mittwoch"));
        days.add(Txt("Donnerstag"));
        days.add(Txt("Freitag"));
        days.add(Txt("Samstag"));

        return days.get(get - 1);
    }

    void updateObject( Job activeElem )
    {
        int h = VaadinHelpers.getSelectedIndex(comboHour);
        int m = VaadinHelpers.getSelectedIndex(comboMin);
        long offsetMS = (h*3600 + m*5*60) * 1000l;
        activeElem.setOffsetStartMs(offsetMS);


        int n = VaadinHelpers.getSelectedIndex(day_number);
        activeElem.setDayNumber( n );        
    }
}
