/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import de.dimm.vsm.net.LogQuery;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.LogTable;
import com.github.wolfie.refresher.Refresher;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.LoqQueryProvider;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.Date;

/**
 *
 * @author Administrator
 */
public class LogWin extends SidebarPanel implements LoqQueryProvider
{
    LogTable logPanel;

    DateField df_olderThan;
    CheckBox cb_debug;
    CheckBox cb_error;
    CheckBox cb_warn;
    CheckBox cb_info;

    TextField txt_qry;
    TextField txt_cnt;
    public static final int DEFAULTLEN = 25;
    


    final Refresher refresher = new Refresher();

    public LogWin( VSMCMain _main )
    {
        super(_main);

        ItemClickListener l = new ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                setActiveMessageLog();
            }
        };
        logPanel = LogTable.createTable(main, l, this);
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);

        txt_cnt = new TextField(VSMCMain.Txt("Anzahl"));
        txt_cnt.setValue(DEFAULTLEN);
        txt_cnt.addValidator( new IntegerValidator(VSMCMain.Txt("Bitte geben Sie die Anzahl Zeilen ein")));
        
//        TextChangeListener resetLog = new TextChangeListener() {
//
//            @Override
//            public void textChange( TextChangeEvent event )
//            {
//                logPanel.resetLog();
//            }
//        };
        ValueChangeListener vcListener = new ValueChangeListener() {

            @Override
            public void valueChange( ValueChangeEvent event )
            {
                logPanel.resetLog();
            }
        };
        txt_cnt.addListener(vcListener);
        txt_cnt.setImmediate(true);


        hl.addComponent(txt_cnt);

        txt_qry = new TextField(VSMCMain.Txt("Filter"));
        txt_qry.addListener(vcListener);
        txt_qry.setImmediate(true);
        hl.addComponent(txt_qry);

        df_olderThan = new DateField(VSMCMain.Txt("älter als"));
        df_olderThan.addListener( vcListener);
        hl.addComponent(df_olderThan);


        VerticalLayout vl = new VerticalLayout();

        cb_info = new CheckBox("Info", true);
        cb_warn = new CheckBox("Warning", true);
        cb_error = new CheckBox("Error", true);
        cb_debug = new CheckBox("Debug", true);

        vl.addComponent(cb_info);
        vl.addComponent(cb_warn);
        vl.addComponent(cb_error);
        vl.addComponent(cb_debug);

        cb_info.addListener(vcListener);
        cb_warn.addListener(vcListener);
        cb_error.addListener(vcListener);
        cb_debug.addListener(vcListener);
        cb_info.setImmediate(true);
        cb_warn.setImmediate(true);
        cb_error.setImmediate(true);
        cb_debug.setImmediate(true);

        hl.addComponent( vl );
        
        this.addComponent( hl );
                
        logPanel.setSizeFull();
        logPanel.setImmediate(true);

        this.addComponent( logPanel );
        this.setExpandRatio(logPanel, 1.0f);
        
        setImmediate(true);
        
        this.addComponent(refresher);

        this.setSizeFull();

    }
    @Override
    public void activate()
    {
        logPanel.activate();        
    }

    @Override
    public void deactivate()
    {
        logPanel.deactivate();        
    }

    @Override
    public LogQuery getLogQuery()
    {
        Date d = (Date) df_olderThan.getValue();
        String qry = txt_qry.getValue().toString();
        int flags = 0;
        if (cb_debug.booleanValue())
            flags |= LogQuery.LV_DEBUG;
        if (cb_warn.booleanValue())
            flags |= LogQuery.LV_WARN;
        if (cb_error.booleanValue())
            flags |= LogQuery.LV_ERROR;
        if (cb_info.booleanValue())
            flags |= LogQuery.LV_INFO;

        // DEFAULT IS ALL (MAKES QUERY FASTER)
        if (flags == LogQuery.LV_MASK)
            flags = 0;

        LogQuery lq = new LogQuery(flags, qry, d);
        
        return lq;
    }

    @Override
    public int getCnt()
    {
        try
        {
            return Integer.parseInt(txt_cnt.getValue().toString());
        }
        catch (NumberFormatException numberFormatException)
        {
        }
        return DEFAULTLEN;
    }

    @Override
    public int getOffset()
    {
        return 0;
    }

    private void setActiveMessageLog()
    {
        AbstractLayout panel = logPanel.createLocalPreviewPanel();
        panel.setSizeFull();
        HorizontalLayout bt_hl = new HorizontalLayout();
        bt_hl.setSpacing(true);
        bt_hl.setSizeFull();

        final NativeButton bt_ok = new NativeButton(VSMCMain.Txt("Schließen"));
        bt_hl.addComponent(bt_ok);
        bt_hl.setComponentAlignment(bt_ok, Alignment.MIDDLE_RIGHT);
        panel.addComponent(bt_hl);

        final Window win = new Window( "LogDetail");

        bt_ok.addListener( new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                VSMCMain.Me(win).getRootWin().removeWindow(win);
            }
        });


        win.addComponent(panel);
        win.setModal(true);
        win.setStyleName("vsm");
        win.setWidth("500px");

        // OPEN ADJUSTED
        Window parentWin = this.getWindow();
        if (parentWin != null && parentWin != this.getApplication().getMainWindow())
        {
            win.setPositionX( parentWin.getPositionX() + 25 );
            win.setPositionY( parentWin.getPositionY() + 25 );
        }

        VSMCMain.Me(this).getRootWin().addWindow(win);
    }

}
