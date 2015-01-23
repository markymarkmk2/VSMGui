/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import de.dimm.vsm.records.RetentionWindow;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Table.PreviewPanel;
import de.dimm.vsm.vaadin.GuiElems.VaadinHelpers;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class RetentionWindowPreviewPanel extends PreviewPanel<RetentionWindow>
{
    
    JPACheckBox disabled;
    JPACheckBox negated;
    
    ComboBox cycleCycle;
    BeanItemContainer day_number_list;
    BeanItemContainer week_number_list;
    
    
    HorizontalLayout dayNumberPanel;
    HorizontalLayout weekNumberPanel;

    ComboBox start_day_number;
    ComboBox end_day_number;
    ComboBox start_week_number;
    ComboBox end_week_number;
    
    ComboBox startComboHour;
    ComboBox startComboMin;
    ComboBox endComboHour;
    ComboBox endComboMin;
    
    
    public RetentionWindowPreviewPanel( RetentionWindowTable j, boolean readOnly )
    {
        super(j, readOnly);  
        
        List<String> hdim = new ArrayList<>();
        List<String> mdim = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hdim.add(String.format("%02d", i));
        }
        for (int i = 0; i < 60; i+=5) {
            mdim.add(String.format("%02d", i));
        }        
        
        cycleCycle = new ComboBox(VSMCMain.Txt("Zyklus"), RetentionWindowTable.getCycleComboList() );
        cycleCycle.addListener( new Property.ValueChangeListener() {

            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                updateVisibility();
            }
        });
        cycleCycle.setImmediate(true);
        
        day_number_list = new BeanItemContainer(String.class);        
        for (int i = 1; i <= 7; i++) {
            String day = getCalWeekDayStr( i );
            day_number_list.addBean( day);            
        }
        week_number_list = new BeanItemContainer(String.class);        
        for (int i = 1; i <= 53; i++) {            
            week_number_list.addBean("KW " + i);            
        }
        
        startComboHour = new HourComboBox(VSMCMain.Txt("Start"), hdim);
        startComboHour.setWidth("60px");
        startComboMin = new MinuteComboBox("", mdim);
        startComboMin.setWidth("60px");        
        endComboHour = new HourComboBox(VSMCMain.Txt("Ende"), hdim);
        endComboHour.setWidth("60px");
        endComboMin = new MinuteComboBox("", mdim);
        endComboMin.setWidth("60px");        
    }
    
    @Override
    public void attach()
    {
        RetentionWindow node = (RetentionWindow)getData();
        super.attach();
        setValues(node );
        check_visibility(node);
    }

    RetentionWindow actNode;
    @Override
    public void recreateContent( final RetentionWindow node )
    {
        removeAllComponents();
        
        actNode = node;
        disabled = new JPACheckBox(VSMCMain.Txt("Gesperrt"), "disabled");                

        start_day_number = new ComboBox(VSMCMain.Txt("StartTag"));
        start_day_number.setContainerDataSource(day_number_list);
        start_day_number.setNullSelectionAllowed(false);
        end_day_number = new ComboBox(VSMCMain.Txt("EndTag"));
        end_day_number.setContainerDataSource(day_number_list);
        end_day_number.setNullSelectionAllowed(false);
        
        start_week_number = new ComboBox(VSMCMain.Txt("StartWoche"));
        start_week_number.setContainerDataSource(week_number_list);
        start_week_number.setNullSelectionAllowed(false);
        end_week_number = new ComboBox(VSMCMain.Txt("EndWoche"));
        end_week_number.setContainerDataSource(week_number_list);
        end_week_number.setNullSelectionAllowed(false);

        // ADD TO LAYOUT
        addComponent( disabled.createGui(node));
        addComponent( cycleCycle );
        HorizontalLayout startLayout = new HorizontalLayout();
        startLayout.addComponent( startComboHour );
        Label lbs = new Label(":");
        startLayout.addComponent(lbs);
        startLayout.addComponent( startComboMin );
        addComponent( startLayout );
        startLayout.setComponentAlignment(lbs, Alignment.BOTTOM_CENTER);
        
        HorizontalLayout endLayout = new HorizontalLayout();
        endLayout.addComponent( endComboHour );
        Label lbe = new Label(":");
        endLayout.addComponent( lbe);
        endLayout.addComponent( endComboMin );
        addComponent( endLayout );
        endLayout.setComponentAlignment(lbe, Alignment.BOTTOM_CENTER);

        dayNumberPanel = new HorizontalLayout();
        dayNumberPanel.setSpacing(true);
        dayNumberPanel.addComponent(start_day_number);
        dayNumberPanel.addComponent(end_day_number);
        addComponent( dayNumberPanel );

        weekNumberPanel = new HorizontalLayout();
        weekNumberPanel.setSpacing(true);
        weekNumberPanel.addComponent(start_week_number);
        weekNumberPanel.addComponent(end_week_number);
        addComponent( weekNumberPanel );
        
        negated = new JPACheckBox(VSMCMain.Txt("Invertiert"), "negated");
        addComponent( negated.createGui(node) );
               
        setData(node);
        
        setValues(node);
        
        check_visibility( node );
    }
    void updateVisibility() {
        check_visibility( actNode);
    }
    
    private String getCalWeekDayStr( int get )
    {
        // 1 == SUNDAY ... 7 == SATURDAY
        ArrayList<String> days = new ArrayList<>();
        days.add(Txt("Sonntag"));
        days.add(Txt("Montag"));
        days.add(Txt("Dienstag"));
        days.add(Txt("Mittwoch"));
        days.add(Txt("Donnerstag"));
        days.add(Txt("Freitag"));
        days.add(Txt("Samstag"));

        return days.get(get - 1);
    }

    private static String Txt(String key )
    {
        return VSMCMain.Txt(key);
    }
    void check_visibility(RetentionWindow node)
    {       
        if (getTyp(node).equals(RetentionWindow.DAILY)) {
            weekNumberPanel.setVisible(false);
            dayNumberPanel.setVisible(false);
        }
        if (getTyp(node).equals(RetentionWindow.WEEKLY)) {
            weekNumberPanel.setVisible(false);
            dayNumberPanel.setVisible(true);
        }
        if (getTyp(node).equals(RetentionWindow.YEARLY)) {
            weekNumberPanel.setVisible(true);
            dayNumberPanel.setVisible(true);
        }
    }
    
    String getTyp(RetentionWindow node) {
        List<ComboEntry> list = RetentionWindowTable.getCycleComboList();
        int selIdx = VaadinHelpers.getSelectedIndex(cycleCycle);
        if (selIdx < 0) {
            VaadinHelpers.setSelectedIndex(cycleCycle, 0);
            selIdx = 0;
        }
            
        String typ = list.get(selIdx).getDbEntry().toString();
        return typ;
    }
    
    private void setValues(RetentionWindow node)
    {        
        VaadinHelpers.setSelectedIndex(start_day_number, node.getStartDayNumber());
        VaadinHelpers.setSelectedIndex(end_day_number, node.getEndDayNumber());
        VaadinHelpers.setSelectedIndex(start_week_number, node.getStartWeekNumber());
        VaadinHelpers.setSelectedIndex(end_week_number, node.getEndWeekNumber());
        
        int h = (int)(node.getStartOffsetStartMs() / 3600000);
        int m = (int)(node.getStartOffsetStartMs() / 60000) % 60;
        VaadinHelpers.setSelectedIndex(startComboHour, h);
        VaadinHelpers.setSelectedIndex(startComboMin, m / 5);
                
        h = (int)(node.getEndOffsetStartMs() / 3600000);
        m = (int)(node.getEndOffsetStartMs() / 60000) % 60;
        VaadinHelpers.setSelectedIndex(endComboHour, h);
        VaadinHelpers.setSelectedIndex(endComboMin, m / 5);               
    }
    

    void updateObject( RetentionWindow activeElem )
    {
        int h = VaadinHelpers.getSelectedIndex(startComboHour);
        int m = VaadinHelpers.getSelectedIndex(startComboMin);
        long offsetMS = (h*3600 + m*5*60) * 1000l;
        activeElem.setStartOffsetStartMs(offsetMS);
        
        h = VaadinHelpers.getSelectedIndex(endComboHour);
        m = VaadinHelpers.getSelectedIndex(endComboMin);
        offsetMS = (h*3600 + m*5*60) * 1000l;
        activeElem.setEndOffsetStartMs(offsetMS);

        int n = VaadinHelpers.getSelectedIndex(start_day_number);
        activeElem.setStartDayNumber( n );        
        n = VaadinHelpers.getSelectedIndex(end_day_number);
        activeElem.setEndDayNumber( n );        
        n = VaadinHelpers.getSelectedIndex(start_week_number);
        activeElem.setStartWeekNumber( n );        
        n = VaadinHelpers.getSelectedIndex(end_week_number);
        activeElem.setEndWeekNumber( n );  
        
        activeElem.setCycleString(getTyp(activeElem));
    }    
    
    boolean checkPlausibility(VSMCMain main, RetentionWindow t) {
        boolean fail = false;
        String cycleString = getTyp(t);
        
        int sdn = VaadinHelpers.getSelectedIndex(start_day_number);        
        int edn = VaadinHelpers.getSelectedIndex(end_day_number);        
        int swn = VaadinHelpers.getSelectedIndex(start_week_number);        
        int ewn = VaadinHelpers.getSelectedIndex(end_week_number);
                
        if (cycleString.equals(RetentionWindow.DAILY))
        {
            // Ist zulässig als "outside"
//            if (endOffsetMS <= startOffsetMS) {
//                fail = true;
//                main.Msg().errmOk("Endzeitpunkt liegt vor Startzeitpunkt");
//            }            
        }
        if (cycleString.equals(RetentionWindow.WEEKLY))
        {            
            if (edn == sdn) {
                fail = true;
                main.Msg().errmOk("Tagnummer Ende gleich Tagnummer Start, wählen Sie bitte Zyklusdauer täglich");
            }
        }
        if (cycleString.equals(RetentionWindow.YEARLY))
        {
            // Ist zulässig als "outside"
//            if (ewn  < swn) {
//                fail = true;
//                main.Msg().errmOk("Start-KW liegt vor Ende-KW");
//            }
            if (ewn  == swn) {
                fail = true;
                main.Msg().errmOk("Start-KW gleich Ende-KW, wählen Sie bitte Zyklusdauer wöchentlich");
            }
        }
        return fail;
             
    }
}
