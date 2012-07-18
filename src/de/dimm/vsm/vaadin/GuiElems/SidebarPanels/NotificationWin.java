/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import com.vaadin.Application;
import com.vaadin.ui.Button.ClickEvent;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import de.dimm.vsm.records.MailGroup;
import de.dimm.vsm.records.MailNotifications;
import de.dimm.vsm.records.SmtpLoginData;
import de.dimm.vsm.vaadin.GuiElems.OkAbortPanel;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.MailGroupTable;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.MailNotificationTable;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.SmtpDataTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.sql.SQLException;

import java.util.List;
import org.vaadin.jouni.animator.client.ui.VAnimatorProxy.AnimType;





/**
 *
 * @author Administrator
 */
public class NotificationWin extends SidebarPanel
{
    VerticalSplitPanel notificationSplitter;
    VerticalSplitPanel groupSplitter;
    VerticalSplitPanel smtpSplitter;
    HorizontalSplitPanel mainPanel;
    HorizontalSplitPanel lowerPanel;
    BaseDataEditTable<MailNotifications> notificationTable;
    BaseDataEditTable<MailGroup> groupTable;
    SmtpDataTable smtpTable;
    //VerticalLayout backupJobResultContainer;
    //VerticalLayout jobTableContainer;


    @Override
    public void activate()
    {
      

    }



    public NotificationWin(VSMCMain main)
    {
        super(main);
        
        setStyleName("editHsplitter");

        notificationSplitter = new VerticalSplitPanel();
        notificationSplitter.setSizeFull();
        notificationSplitter.setSplitPosition(35, Sizeable.UNITS_PERCENTAGE);
  
        groupSplitter = new VerticalSplitPanel();
        groupSplitter.setSizeFull();
        groupSplitter.setSplitPosition(35, Sizeable.UNITS_PERCENTAGE);

        smtpSplitter = new VerticalSplitPanel();
        smtpSplitter.setSizeFull();
        smtpSplitter.setSplitPosition(35, Sizeable.UNITS_PERCENTAGE);


        mainPanel = new HorizontalSplitPanel();
        mainPanel.setSizeFull();
        mainPanel.setSplitPosition(50, Sizeable.UNITS_PERCENTAGE);
        //mainPanel.setFirstComponent(notificationSplitter);
        mainPanel.setFirstComponent(notificationSplitter);
        mainPanel.setSecondComponent(groupSplitter);

        lowerPanel = new HorizontalSplitPanel();
        lowerPanel.setSizeFull();
        lowerPanel.setSplitPosition(50, Sizeable.UNITS_PERCENTAGE);
        lowerPanel.setSecondComponent(smtpSplitter);

        this.addComponent(mainPanel);
        this.setExpandRatio(mainPanel, 1.0f);
        this.setSizeFull();
//        NativeButton bt = new NativeButton(VSMCMain.Txt("SMTP-Server"), new ClickListener() {
//
//            @Override
//            public void buttonClick( ClickEvent event )
//            {
//                openSmtpWin();
//            }
//        });
//        HorizontalLayout buttonPanel = new HorizontalLayout();
//        buttonPanel.setSpacing(true);
//        buttonPanel.addComponent(bt);
//        buttonPanel.setComponentAlignment(bt, Alignment.MIDDLE_RIGHT);
//        buttonPanel.setWidth("100%");
//        this.addComponent(buttonPanel);
        this.addComponent(lowerPanel);
        this.setExpandRatio(lowerPanel, 1.0f);


        // CRATE TABLEPANELS
        createMailNotificationPanel();
        createMailGroupPanel();
        createSmtpData();
    }
    

    final void createMailNotificationPanel()
    {
        //jobTableContainer.removeAllComponents();

        ItemClickListener l = new ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                setActiveMailNotification();
            }
        };

        List<MailNotifications> list = null;
        try
        {
            list = VSMCMain.get_base_util_em().createQuery("select p from MailNotifications p order by T1.idx desc", MailNotifications.class);
        }
        catch (SQLException sQLException)
        {
            VSMCMain.notify(this, "Fehler beim Erzeugen der Ergebnis-Tabelle", sQLException.getMessage());
            return;
        }
        

        notificationTable = MailNotificationTable.createTable(main,  list, l);

        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = notificationTable.createHeader(VSMCMain.Txt("Benachrichtigungen"));

        tableWin.addComponent(head);
        tableWin.addComponent(notificationTable);
        tableWin.setExpandRatio(notificationTable, 1.0f);

        notificationSplitter.setFirstComponent(tableWin);

        if (notificationSplitter.getSecondComponent() != null)
            notificationSplitter.removeComponent(notificationSplitter.getSecondComponent());
        
       
        //jobTableContainer.addComponent( tableWin );
    }

    private void setActiveMailNotification()
    {
        AbstractLayout panel = notificationTable.createLocalPreviewPanel();
        panel.setSizeUndefined();
        panel.setWidth("100%");

        notificationSplitter.setSecondComponent(panel);
               
    }

    final void createMailGroupPanel()
    {
        ItemClickListener l = new ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                setActiveMailGroup();
            }
        };

        List<MailGroup> list = null;
        try
        {
            list = VSMCMain.get_base_util_em().createQuery("select p from MailGroup p order by T1.idx desc", MailGroup.class);
        }
        catch (SQLException sQLException)
        {
            VSMCMain.notify(this, "Fehler beim Erzeugen der MailGruppen-Tabelle", sQLException.getMessage());
            return;
        }

        groupTable = MailGroupTable.createTable(main, list, l);

        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = groupTable.createHeader(VSMCMain.Txt("MailGruppen"));

        tableWin.addComponent(head);
        tableWin.addComponent(groupTable);
        tableWin.setExpandRatio(groupTable, 1.0f);

        groupSplitter.setFirstComponent(tableWin);
        groupSplitter.setSecondComponent(new Label(""));
        
    }
    final void createSmtpData()
    {
        ItemClickListener l = new ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                setActiveSmtpData();
            }
        };

         List<SmtpLoginData> list = null;
        try
        {
            list = VSMCMain.get_base_util_em().createQuery("select p from SmtpLoginData p", SmtpLoginData.class);
        }
        catch (SQLException sQLException)
        {
            VSMCMain.notify(this, "Fehler beim Erzeugen der SmtpLoginData-Tabelle", sQLException.getMessage());
            return;
        }
        // CREATE PANEL
        smtpTable = SmtpDataTable.createTable(main, list, null);


        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = smtpTable.createHeader(VSMCMain.Txt("SMTP-Server"));

        tableWin.addComponent(head);
        tableWin.addComponent(smtpTable);
        tableWin.setExpandRatio(smtpTable, 1.0f);

        smtpSplitter.setFirstComponent(tableWin);
        smtpSplitter.setSecondComponent(new Label(""));

    }
    private void setActiveMailGroup()
    {
        AbstractLayout panel = groupTable.createLocalPreviewPanel();
        panel.setSizeUndefined();
        panel.setWidth("100%");
        groupSplitter.setSecondComponent(panel);
    }
    private void setActiveSmtpData()
    {
        AbstractLayout panel = smtpTable.createLocalPreviewPanel();
        panel.setSizeUndefined();
        panel.setWidth("100%");
        smtpSplitter.setSecondComponent(panel);
    }

    

}
