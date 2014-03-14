 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import de.dimm.vsm.records.AccountConnector;
import de.dimm.vsm.records.Role;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.AccountConnectorTable;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.RoleTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.sql.SQLException;

import java.util.List;




/**
 *
 * @author Administrator
 */
public class AuthentificationWin extends SidebarPanel
{
    VerticalSplitPanel roleSplitter;

    VerticalSplitPanel acctSplitter;

    BaseDataEditTable<AccountConnector> acctTable;
    RoleTable rolelTable;
    
    HorizontalSplitPanel mainPanel;



    public AuthentificationWin(VSMCMain main)
    {
        super(main);
    }

    @Override
    public void activate()
    {
        super.activate();
        build_gui();
        
        if (rolelTable != null)
        {
            rolelTable.setNewList();
        }
    }

    void build_gui()
    {
        if (mainPanel != null)
            return;

        mainPanel = new HorizontalSplitPanel();

        this.addComponent(mainPanel);
        this.setSizeFull();
        setStyleName("editHsplitter");

        Component tableWin = createRoleTablePanel();


        roleSplitter = new VerticalSplitPanel();
        roleSplitter.setSplitPosition(30, Sizeable.UNITS_PERCENTAGE);
        acctSplitter = new VerticalSplitPanel();
        acctSplitter.setSplitPosition(30, Sizeable.UNITS_PERCENTAGE);

        roleSplitter.setFirstComponent(tableWin);

        mainPanel.setSizeFull();
        mainPanel.setSplitPosition(50, Sizeable.UNITS_PERCENTAGE);
        mainPanel.setFirstComponent(acctSplitter);
        mainPanel.setSecondComponent(roleSplitter);

        createAcctTablePanel();
    }
    

    final Component createRoleTablePanel()
    {        
        List<Role> list = null;
        try
        {
            list = VSMCMain.get_base_util_em().createQuery(RoleTable.ROLE_QRY, Role.class);
        }
        catch (SQLException sQLException)
        {
            VSMCMain.notify(this, "Fehler beim Erzeugen der Role-Tabelle", sQLException.getMessage());
            return new VerticalLayout();
        }
        //List<StoragePool> list = tq.getResultList();
        ItemClickListener l = new ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                setActiveRole();
            }
        };

        rolelTable = RoleTable.createTable(main, list, l);

        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = rolelTable.createHeader(VSMCMain.Txt("Liste der Rollen in Reihenfolge der Abarbeitung:"));

        tableWin.addComponent(head);
        tableWin.addComponent(rolelTable);
        tableWin.setExpandRatio(rolelTable, 1.0f);
        return tableWin;
    }

    private void createAcctTablePanel()
    {
        

        Component tableWin  = createAccountConnectorTablePanel(  );


        acctSplitter.setFirstComponent(tableWin);
        acctSplitter.setSecondComponent(new Label(""));
    }


    final Component createAccountConnectorTablePanel()
    {
        ItemClickListener l = new ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                setActiveAcct();
            }
        };
        
        List<AccountConnector> list = null;
        try
        {
            list = VSMCMain.get_base_util_em().createQuery("select T1 from AccountConnector T1", AccountConnector.class);
        }
        catch (SQLException sQLException)
        {
            VSMCMain.notify(this, "Fehler beim Erzeugen der Authentifizierer-Tabelle", sQLException.getMessage());
            return new VerticalLayout();
        }
        //List<Schedule> list = tq.getResultList();

        acctTable = AccountConnectorTable.createTable(main,  list, l);

        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = acctTable.createHeader(VSMCMain.Txt("Liste der Authentifizierer:"));

        tableWin.addComponent(head);
        tableWin.addComponent(acctTable);
        tableWin.setExpandRatio(acctTable, 1.0f);
        return tableWin;
    }



   

    private void setActiveAcct()
    {
        AbstractLayout panel = acctTable.createLocalPreviewPanel();
        panel.setSizeUndefined();
        panel.setWidth("100%");
        acctSplitter.setSecondComponent(panel);
    }


    private void setActiveRole()
    {
        AbstractLayout panel = rolelTable.createLocalPreviewPanel();
        panel.setSizeUndefined();
        panel.setWidth("100%");
        roleSplitter.setSecondComponent(panel);
    }



}
