/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.Schedule;
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
public class SchedulePreviewPanel extends PreviewPanel<Schedule>
{

    ComboBox cycle_dim;
    TextField cycle_len;
    HorizontalLayout cycle_panel;
    
    JPAJobDBLinkField jobs;


    public SchedulePreviewPanel( ScheduleTable tb, boolean readOnly )
    {
        super(tb, readOnly);
    }

    private static String Txt(String key )
    {
        return VSMCMain.Txt(key);
    }


    @Override
    public void recreateContent( final Schedule node )
    {
        super.recreateContent( node );
      /*  removeAllComponents();

         // CREATE GUI ITEMS
        for (int i = 0; i < table.getFieldList().size(); i++)
        {
            JPAField jPAField = table.getFieldList().get(i);
            
            addComponent( jPAField.createGui(node) );

            if (jPAField instanceof JPADBLinkField)
            {
                final Component gui = jPAField.getGui();
                final JPADBLinkField linkField = (JPADBLinkField)jPAField;
                linkField.addGuiClickListener( new ClickListener()
                {

                    @Override
                    public void buttonClick( ClickEvent event )
                    {
                        ClickListener ok = new ClickListener()
                        {
                            @Override
                            public void buttonClick( ClickEvent event )
                            {
                                linkField.reload(gui);
                            }
                        };
                        table.createChildDB( linkField.getFieldName(), ok );
                    }
                } );

            }
            jPAField.setReadOnly(rdOnly);
        }*/

        int isCycleIdx = getComponentIndex( getGui("isCycle"));


        // ADD NECESSARY
        cycle_len = new TextField(Txt("Zyklusdauer"));
        cycle_len.addValidator( new IntegerValidator(Txt("Bitte geben Sie eine gültige Zahl ein")));

        List<String> dim = new ArrayList<String>();
        dim.add(Txt("Stunde(n)"));
        dim.add(Txt("Tag(e)"));
        dim.add(Txt("Woche(n)"));
        cycle_dim = new ComboBox(Txt("Einheit"), dim);
        cycle_dim.setNullSelectionAllowed(false);
        cycle_panel = new HorizontalLayout();
        cycle_panel.setSpacing(true);
        cycle_panel.addComponent(cycle_len);
        cycle_panel.addComponent(cycle_dim);
        
        // ADD CYCLE_MODE AND LEN AFTER DISABLED BUTTON
        addComponent( cycle_panel, isCycleIdx+1 );
        final SchedulePreviewPanel pnl = this;

        final JPACheckBox cycle_mode = (JPACheckBox) table.getField("isCycle");
        
        cycle_mode.getCheckBox(this).addListener( new ValueChangeListener() {

            @Override
            public void valueChange( ValueChangeEvent event )
            {
                getGui("jobs").setVisible(!cycle_mode.getBooleanValue(pnl));
            }
        });
        


        // SET LISTENERS
        ValueChangeListener vc = new ValueChangeListener() {

            @Override
            public void valueChange( ValueChangeEvent event )
            {
                
            }
        };
        cycle_dim.setImmediate(true);
        cycle_dim.addListener( vc);


        cycle_len.setImmediate(true);
        cycle_len.addListener( vc);
        cycle_len.addListener( new TextChangeListener() {

            @Override
            public void textChange( TextChangeEvent event )
            {
                
            }
        });
        GenericEntityManager em = VSMCMain.get_util_em(node.getPool());

        jobs = new JPAJobDBLinkField(em);
        // TO ENABLE CALLBACKS AND CLICKLISTENERES WE HAVE TO ADD FIELD TO FIELDLIST AND CREATE CLICKLISTENER
        Component gui = jobs.createGui(node);
        addComponent(gui);
        addDBLinkClickListener( gui, jobs );



        // JOBS ARE ONLY VISIBLE IF WE ARE !CYCLEMODE
        getGui("jobs").setVisible(!cycle_mode.getBooleanValue(this));


        setData(node);
    }
    


    @Override
    public void attach()
    {
        Schedule node = (Schedule)getData();
        super.attach();
        setValues(node );
        //check_visibility(node);
    }



    private void setValues(Schedule node)
    {
        long cs = node.getCycleLengthMs() / 1000;
        int f;
        if (cs < 86400)
        {
            VaadinHelpers.setSelectedIndex(cycle_dim, 0);
            f = 3600;
        }
        else if(cs < 7*86400)
        {
            VaadinHelpers.setSelectedIndex(cycle_dim, 1);
            f = 86400;
        }
        else
        {
            VaadinHelpers.setSelectedIndex(cycle_dim, 2);
            f = 7*86400;
        }
        long n = cs / f;

        cycle_len.setValue( n );

    }

    long calc_cycle_len_s()
    {
        int f = 0;
        int s = VaadinHelpers.getSelectedIndex(cycle_dim);
        if (s == -1)
            return f;

        if (s == 0) // Stunden
            f = 3600;
        else if(s == 1) // TAGE
            f = 86400;
        else if(s == 2) // Wochen
            f = 7*86400;

        int n = 0;
        try
        {
            n = Integer.parseInt(cycle_len.getValue().toString());
        }
        catch (NumberFormatException numberFormatException)
        {
            return 0;
        }
        return n*f;
    }

    void updateObject( Schedule activeElem )
    {
        long len = calc_cycle_len_s();
        activeElem.setCycleLengthMs( len * 1000);

    }


}
