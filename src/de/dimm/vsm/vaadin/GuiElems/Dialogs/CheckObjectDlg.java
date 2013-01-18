/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Dialogs;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.fsengine.checks.ICheck;
import de.dimm.vsm.vaadin.GuiElems.OkAbortPanel;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.List;



/**
 *
 * @author Administrator
 */
public class CheckObjectDlg extends Window
{
     Object object;
     VSMCMain main;
     String name;
     
     VerticalLayout vl = new VerticalLayout();


    public CheckObjectDlg( VSMCMain main, Object object, String name )
    {
        this.main = main;
        this.object = object;
        this.name = name;
        build_gui();
    }
    
    void addChecks( VerticalLayout vl, List<String> niceTexts) {
        for (int i = 0; i < niceTexts.size(); i++) {
            String niceText = niceTexts.get(i);
            Button check = new NativeButton(niceText);
            vl.addComponent(check);
            check.addListener( new Button.ClickListener() {

                @Override
                public void buttonClick( ClickEvent event )
                {
                    String checkName = event.getButton().getCaption();
                    main.getGuiServerApi().initCheck(main.getUser(), checkName, object, null);
                    main.Msg().notify(checkName + " " + VSMCMain.Txt("wurde gestartet"), VSMCMain.Txt("Details siehe unter Jobs"));
                }
            });
        }
    }

    final void build_gui( )
    {
        addComponent(vl);


        setModal(true);
        setStyleName("vsm");

        //this.setSizeFull();
        this.setWidth("400px");

        vl.setSpacing(true);
        vl.setSizeFull();
        vl.setImmediate(true);
        vl.setStyleName("editWin");

        this.setCaption(VSMCMain.Txt("Prüffunktionen für") + name + " " + object.toString());
        
        
      
        addChecks( vl, main.getGuiServerApi().getCheckNames(object.getClass()));
             
        Button close = new NativeButton(VSMCMain.Txt("Zurück"));

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

    Window createUserSelect(  final ICheck check, String caption, final List<String> userSelect )
    {
        final Window win = new Window("Auswahl treffen");
        win.setModal(true);
        win.setStyleName("vsm");
        win.setWidth("400px");
        win.setHeight("350px");
        
        VerticalLayout mainVl = new VerticalLayout();
        win.addComponent(mainVl);

        mainVl.setSpacing(true);
        mainVl.setSizeFull();
        mainVl.setImmediate(true);
        mainVl.setStyleName("editWin");
        Label lb = new Label(caption);
        mainVl.addComponent(lb);

        final ComboBox cbSelect  = new ComboBox("Was tun", userSelect);
        cbSelect.setNullSelectionAllowed(false);
        cbSelect.setNewItemsAllowed(false);
        cbSelect.setValue(userSelect.get(0));

        mainVl.addComponent(cbSelect);

        OkAbortPanel panel = new OkAbortPanel();
        mainVl.addComponent(panel);

        panel.setOkText("Start");
        panel.getBtOk().addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                String val = cbSelect.getValue().toString();
                for (int i = 0; i < userSelect.size(); i++)
                {
                    String string = userSelect.get(i);
                    if (string.equals(val))
                    {
                        handleUserChoice( check, string, i );
                        event.getButton().getApplication().getMainWindow().removeWindow(win);
                    }
                }

            }
        });
        panel.getBtAbort().addListener( new Button.ClickListener() {
            @Override
            public void buttonClick( ClickEvent event )
            {
                event.getButton().getApplication().getMainWindow().removeWindow(win);
            }
        });
        return win;

    }

    void handleUserChoice( final ICheck check, final String caption, final int i )
    {
        Runnable r = new Runnable() {

            @Override
            public void run()
            {
                StringBuffer sb = new StringBuffer();
                if (!check.handleUserChoice(i, sb))
                {
                    VSMCMain.Me(vl).Msg().errmOk(sb.toString());
                }
            }
        };
        Button.ClickListener abortClick = new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                check.abort();
            }
        };

        VSMCMain.Me(vl).runInBusyCancel(check.getName() + " " + caption, r, abortClick );
    }

}
