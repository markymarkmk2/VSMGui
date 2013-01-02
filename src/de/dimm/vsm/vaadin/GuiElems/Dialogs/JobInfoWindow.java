/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Dialogs;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.MMapi.JobError;
import de.dimm.vsm.jobs.JobEntry;
import de.dimm.vsm.jobs.JobInterface;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class JobInfoWindow extends Window
{
     JobInterface ji;
     VSMCMain main;
     
     VerticalLayout vl = new VerticalLayout();

    public JobInfoWindow( VSMCMain main, JobInterface taskEntry )
    {
        this.ji = taskEntry;
        this.main = main;
        
        build_gui();
    }
    

    final void build_gui( )
    {
        addComponent(vl);
        setModal(true);
        setStyleName("vsm");

        //this.setSizeFull();
        this.setWidth("600px");

        vl.setSpacing(true);
        vl.setSizeFull();
        vl.setStyleName("editWin");

        this.setCaption(VSMCMain.Txt("Job-Informationen"));

        DateField at = new DateField(VSMCMain.Txt("Start"), ji.getStartTime());
        vl.addComponent(at);
        at.setReadOnly(true);

        TextField status = new TextField(VSMCMain.Txt("Status"), ji.getStatusStr() );
        vl.addComponent(status);
        status.setReadOnly(true);
        status.setWidth("100%");

        TextField statistic = new TextField(VSMCMain.Txt("Statistik"), ji.getStatisticStr() );
        vl.addComponent(statistic);
        statistic.setReadOnly(true);
        statistic.setWidth("100%");

        TextField procent = new TextField(VSMCMain.Txt("Fortschritt"), ji.getProcessPercent() + " " + ji.getProcessPercentDimension() );
        vl.addComponent(procent);
        procent.setReadOnly(true);
        procent.setWidth("100%");

        try
        {
            Object o = ji.getResultData();
            if (o != null)
            {
                if (o instanceof List)
                {
                    List l = (List)o;
                    if (!l.isEmpty())
                    {
                        Component c = createTableView( (List)o );
                        vl.addComponent(c);
                    }
                }
                else
                {
                    TextArea ta = new TextArea("Meldung");
                    ta.setValue(o.toString());
                    ta.setSizeFull();
                    vl.addComponent(ta);
                }
            }
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
        }

        

        vl.addComponent(new Label(" "));

        Button close = new NativeButton(VSMCMain.Txt("Ok"));

        vl.addComponent(close);
        vl.setComponentAlignment(close, Alignment.BOTTOM_RIGHT);

        final Window w = this;
        close.addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                event.getButton().getApplication().getMainWindow().removeWindow(w);
            }
        });
    }
    private String Txt( String key )
    {
        return VSMCMain.Txt(key);
    }

    private Component createTableView( List list )
    {

        Table table = new Table();

        Object o = list.get(0);

        if (o instanceof JobError)
        {
            table.setCaption(Txt("Fehlerhafte Jobs"));
            table.setContainerDataSource( new BeanItemContainer(JobError.class, list) );
            table.setVisibleColumns(new String[] { "stateText", "name", "createdTxt", "size", "status" });
            table.setColumnHeaders(new String[] { Txt("Status"), Txt("Name"), Txt("Erzeugt"), Txt("Größe"), Txt("Info") });
        }

        // set a style name, so we can style rows and cells
        table.setStyleName("vsm");

        // size
        table.setWidth("100%");
        table.setHeight("300px");

        // selectable
        table.setSelectable(true);
        table.setMultiSelect(true);
        table.setImmediate(true); // react at once when something is selected

        // turn on column reordering and collapsing
        table.setColumnCollapsingAllowed(true);

        return table;
    }


}
