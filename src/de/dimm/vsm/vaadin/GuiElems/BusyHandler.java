/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems;


import com.github.wolfie.refresher.Refresher;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.vaadin.GenericMain;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Administrator
 */
public class BusyHandler extends Window  implements Refresher.RefreshListener
{
    TextArea ta;
    
    Button abort;
    CssLayout icon;

    ClickListener abort_listener;

    GenericMain main;

    final Refresher refresher = new Refresher();
    public static final int RF_INTERVALL = 500;

    int openCnt = 0;

    public BusyHandler(GenericMain _main)
    {
        this.main = _main;
        this.setStyleName("vsm");
        
        ta = new TextArea();
        ta.setStyleName("ErrMsgText");
        ta.setSizeFull();
     

        abort = new Button();
        icon = new CssLayout();
        icon.setStyleName("busyIcon");
        icon.setSizeFull();

        HorizontalLayout ihl = new HorizontalLayout();
        ihl.addComponent(icon);
        
        

        HorizontalLayout msg_panel = new HorizontalLayout();
        msg_panel.setSizeFull();

        msg_panel.addComponent(ihl);
        msg_panel.setComponentAlignment(ihl, Alignment.TOP_LEFT);
        msg_panel.addComponent(ta);
        msg_panel.setComponentAlignment(ta, Alignment.MIDDLE_RIGHT);
        msg_panel.setExpandRatio(ta, 1.0f);

        
        
        ClickListener closeListener = new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                hideBusy();
                if (event.getButton() == abort && abort_listener != null)
                    abort_listener.buttonClick(event);
            }
        };
        
        abort.addListener(closeListener);

        // BUTTONS
        HorizontalLayout right_aligned_hl = new HorizontalLayout();
        right_aligned_hl.setWidth("100%");

        HorizontalLayout bt_panel = new HorizontalLayout();
        bt_panel.setWidth("100%");
        bt_panel.setHeight("30px");
        bt_panel.setSpacing(true);
        bt_panel.addComponent(abort);
       
        bt_panel.setComponentAlignment(abort, Alignment.MIDDLE_CENTER);

        right_aligned_hl.addComponent(bt_panel);
        right_aligned_hl.setComponentAlignment(bt_panel, Alignment.MIDDLE_RIGHT);

        // ADD PANLES VERTICALLY
        
        final VerticalLayout layout = new VerticalLayout();
        layout.addComponent(msg_panel);
        layout.setExpandRatio(msg_panel, 1.0f);
        layout.addComponent(right_aligned_hl);
        layout.setSpacing(false);
        layout.setSizeFull();

        setWidth("400px");
               
        setImmediate(true);
        this.addComponent(layout);

        refresher.setRefreshInterval(RF_INTERVALL);
        this.addComponent(refresher);
        //this.setSizeFull();

        refresher.addListener(this);
    }

    public void showBusy(String txt)
    {
        this.abort_listener = null;

        this.setCaption(VSMCMain.Txt("Busy"));
        abort.setVisible(false);
        ta.setValue(txt + "...");
        icon.setStyleName("busyIcon");

        setModal(true);
        doHide = false;
        openCnt++;

        if (!isVisible())
        {
            main.getRootWin().addWindow(this);
        }
    }


    public void showBusyCancel( String txt,  ClickListener abort_listener)
    {
        this.abort_listener = abort_listener;

        this.setCaption(VSMCMain.Txt("Busy"));
        
        abort.setVisible(true);
        

        ta.setValue(txt + "...");

       
        abort.setCaption(VSMCMain.Txt("Abbruch"));

        icon.setStyleName("busyIcon");

        setModal(true);
        doHide = false;
        openCnt++;

       
        if (!isVisible())
        {
            main.getRootWin().addWindow(this);
        }
    }
    boolean doHide = false;

    @Override
    public boolean isVisible()
    {
        Set<Window> set = main.getRootWin().getChildWindows();
        for (Iterator<Window> it = set.iterator(); it.hasNext();)
        {
            Window w = it.next();
            if (w == this)
                return true;

        }
        return false;
    }



    public void hideBusy()
    {
        doHide = true;
    }

    @Override
    public void refresh( Refresher source )
    {
        if (doHide)
        {
            openCnt--;
            if (openCnt <= 0)
            {
                main.getRootWin().removeWindow(this);
            }
            doHide = false;
        }
    }
   

}
