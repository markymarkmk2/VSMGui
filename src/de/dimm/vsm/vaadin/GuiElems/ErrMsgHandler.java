/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems;


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

/**
 *
 * @author Administrator
 */
public class ErrMsgHandler extends Window
{
    TextArea ta;
    Button ok;
    Button abort;
    CssLayout icon;

    ClickListener closeListener;
    ClickListener ok_listener;
    ClickListener abort_listener;

    GenericMain main;

    public ErrMsgHandler(GenericMain _main)
    {
        this.main = _main;
        this.setStyleName("vsm");
        
        ta = new TextArea();
        ta.setStyleName("ErrMsgText");
        ta.setSizeFull();
     

        ok = new Button();
        abort = new Button();
        icon = new CssLayout();
        icon.setStyleName("errorIcon");
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

        
        final Window w = this;
        closeListener = new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                main.getRootWin().removeWindow(w);

                if (event.getButton() == ok && ok_listener != null)
                    ok_listener.buttonClick(event);
                if (event.getButton() == abort && abort_listener != null)
                    abort_listener.buttonClick(event);

                // RELEASE EXTERNAL DEPENDENCIES
                ok_listener = null;
                abort_listener = null;
            }
        };
        ok.addListener(closeListener);
        abort.addListener(closeListener);

        // BUTTONS
        HorizontalLayout right_aligned_hl = new HorizontalLayout();
        right_aligned_hl.setWidth("100%");

        HorizontalLayout bt_panel = new HorizontalLayout();
        bt_panel.setWidth("100%");
        bt_panel.setHeight("30px");
        bt_panel.setSpacing(true);
        bt_panel.addComponent(abort);
        bt_panel.addComponent(ok);
        bt_panel.setComponentAlignment(abort, Alignment.MIDDLE_RIGHT);
        bt_panel.setComponentAlignment(ok, Alignment.MIDDLE_RIGHT);

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
    }
    
    public void notify(String largeText, String smallText) {
        main.getRootWin().showNotification(smallText, largeText, Window.Notification.TYPE_HUMANIZED_MESSAGE);
    }
    public void warn(String largeText, String smallText) {
        main.getRootWin().showNotification(smallText, largeText, Window.Notification.TYPE_WARNING_MESSAGE);
    }
    public void fatal(String largeText, String smallText) {
        main.getRootWin().showNotification(smallText, largeText, Window.Notification.TYPE_ERROR_MESSAGE);
    }
    

    public void errmOk(String txt, ClickListener ok_listener)
    {
        this.ok_listener = ok_listener;
        this.abort_listener = null;

        this.setCaption(VSMCMain.Txt("Error"));
        abort.setVisible(false);
        ok.setVisible(true);
        ta.setValue(txt);
        icon.setStyleName("errorIcon");
        //icon.setSizeFull();

        ok.setCaption(VSMCMain.Txt("Okay"));

        setModal(true);

        main.getRootWin().addWindow(this);
    }
    public void errmOk(String txt)
    {
        errmOk(txt, null);
    }

    public void errmOkCancel( String txt, ClickListener ok_listener, ClickListener abort_listener)
    {
        errmOkCancel(txt, VSMCMain.Txt("Error"), ok_listener, abort_listener);
    }
    public void errmOkCancel( String txt, String caption, ClickListener ok_listener, ClickListener abort_listener)
    {
        this.ok_listener = ok_listener;
        this.abort_listener = abort_listener;

        this.setCaption(caption);
        
        abort.setVisible(true);
        ok.setVisible(true);

        ta.setValue(txt);

        ok.setCaption(VSMCMain.Txt("Okay"));
        abort.setCaption(VSMCMain.Txt("Abbruch"));

        icon.setStyleName("errorIcon");
        //icon.setSizeFull();

        setModal(true);

        main.getRootWin().addWindow(this);
    }
    public void infoOkCancel( String txt, ClickListener ok_listener, ClickListener abort_listener)
    {
        infoOkCancel(txt, VSMCMain.Txt("Info"), ok_listener, abort_listener);
    }

    public void infoOkCancel( String txt, String caption, ClickListener ok_listener, ClickListener abort_listener)
    {
        this.ok_listener = ok_listener;
        this.abort_listener = abort_listener;

        this.setCaption(caption);

        abort.setVisible(true);
        ok.setVisible(true);

        ta.setValue(txt);

        icon.setStyleName("infoIcon");
        //icon.setSizeFull();

        ok.setCaption(VSMCMain.Txt("Okay"));
        abort.setCaption(VSMCMain.Txt("Abbruch"));

        setModal(true);

        main.getRootWin().addWindow(this);
    }

    public void info(String txt, ClickListener ok_listener)
    {
        this.ok_listener = ok_listener;
        this.abort_listener = null;

        this.setCaption(VSMCMain.Txt("Info"));
        abort.setVisible(false);
        ok.setVisible(true);
        ta.setValue(txt);

        icon.setStyleName("infoIcon");
        //icon.setSizeFull();

        ok.setCaption(VSMCMain.Txt("Okay"));

        setModal(true);

        main.getRootWin().addWindow(this);
    }
    void info( String txt)
    {
        info( txt, null);
    }
}
