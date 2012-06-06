/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Dialogs;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.tasks.TaskEntry;
import de.dimm.vsm.vaadin.VSMCMain;

/**
 *
 * @author Administrator
 */
public class TaskInfoWindow extends Window
{
     TaskEntry taskEntry;
     VSMCMain main;
     
     VerticalLayout vl = new VerticalLayout();

    public TaskInfoWindow( VSMCMain main, TaskEntry taskEntry )
    {
        this.taskEntry = taskEntry;
        this.main = main;
        

        build_gui(taskEntry);
    }
    

    final void build_gui( TaskEntry taskEntry )
    {
        addComponent(vl);
        setModal(true);
        setStyleName("vsm");

        //this.setSizeFull();
        this.setWidth("400px");

        vl.setSpacing(true);
        vl.setSizeFull();
        vl.setStyleName("editWin");

        this.setCaption(VSMCMain.Txt("Informationen_für") + " " + taskEntry.getName());

        TextField name = new TextField(VSMCMain.Txt("Name"), taskEntry.getName() );
        vl.addComponent(name);
        name.setWidth("100%");
        vl.setExpandRatio(name, 1.0f);

        name.setReadOnly(true);
        DateField at = new DateField(VSMCMain.Txt("Start"), taskEntry.getStarted());
        vl.addComponent(at);
        at.setReadOnly(true);

        TextField status = new TextField(VSMCMain.Txt("Status"), taskEntry.getStatusStr() );
        vl.addComponent(status);
        status.setReadOnly(true);

        TextField statistic = new TextField(VSMCMain.Txt("Statistik"), taskEntry.getStatistic() );
        vl.addComponent(statistic);
        statistic.setReadOnly(true);

        TextField procent = new TextField(VSMCMain.Txt("Fortschritt"), taskEntry.getProcessPercent() + taskEntry.getProcessPercentDimension() );
        vl.addComponent(procent);
        procent.setReadOnly(true);

        

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
}
