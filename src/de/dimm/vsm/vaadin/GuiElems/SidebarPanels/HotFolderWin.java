/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.data.util.BeanItem;
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
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.HotFolder;
import de.dimm.vsm.records.HotFolderError;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.HotFolderErrorTable;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.HotFolderTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.sql.SQLException;

import java.util.List;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;




/**
 *
 * @author Administrator
 */
public class HotFolderWin extends SidebarPanel  implements Refresher.RefreshListener
{

    BaseDataEditTable<HotFolder> hotFolderTable;
    HorizontalSplitPanel mainPanel;
    VerticalSplitPanel hfErrorSplitter;
    VerticalSplitPanel hotFolderSplitter;

    HotFolderErrorTable hotFolderErrorTable;


    public static final int RF_INTERVALL = 1000;
    final Refresher refresher = new Refresher();

    public HotFolderWin(VSMCMain main)
    {
        super(main);
        
        setStyleName("editHsplitter");
        
        

        hotFolderSplitter = new VerticalSplitPanel();
        hotFolderSplitter.setSizeFull();
        hotFolderSplitter.setSplitPosition(35, Sizeable.UNITS_PERCENTAGE);
        

        hfErrorSplitter = new VerticalSplitPanel();
        hfErrorSplitter.setSizeFull();
        hfErrorSplitter.setSplitPosition(35, Sizeable.UNITS_PERCENTAGE);


        mainPanel = new HorizontalSplitPanel();
        mainPanel.setSizeFull();
        mainPanel.setSplitPosition(70, Sizeable.UNITS_PERCENTAGE);
        mainPanel.setFirstComponent(hotFolderSplitter);
        mainPanel.setSecondComponent(hfErrorSplitter);

        this.addComponent(mainPanel);
        setExpandRatio(mainPanel, 1.0f);
        this.setSizeFull();

        refresher.addListener(this);
        this.addComponent(refresher);
    }

    @Override
    public void activate()
    {
        Component tableWin = createHotfolderPanel();
        hotFolderSplitter.setFirstComponent(tableWin);
        hotFolderSplitter.setSecondComponent(new Label(""));

        if (hotFolderSplitter.getSecondComponent() != null)
            hotFolderSplitter.removeComponent(hotFolderSplitter.getSecondComponent());

        if (hfErrorSplitter.getFirstComponent() != null)
            hfErrorSplitter.removeComponent(hfErrorSplitter.getFirstComponent());

        if (hfErrorSplitter.getSecondComponent() != null)
            hfErrorSplitter.removeComponent(hfErrorSplitter.getSecondComponent());

    }


    private void setActiveHotFolder()
    {
        AbstractLayout panel = hotFolderTable.createLocalPreviewPanel();
        panel.setSizeUndefined();
        panel.setWidth("100%");

        hotFolderSplitter.setSecondComponent(panel);

        // RELOAD
        VSMCMain.get_base_util_em().em_refresh(hotFolderTable.getActiveElem());// find(HotFolder.class, hotFolderTable.getActiveElem().getIdx());

        Component tableWin  = createHotFolderErrorPanel( hotFolderTable.getActiveElem() );
        hfErrorSplitter.setFirstComponent(tableWin);
        hfErrorSplitter.setSecondComponent(new Label(""));
    }    

    final Component createHotfolderPanel()
    {        
        ItemClickListener l = new ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                setActiveHotFolder();
            }
        };
        List<HotFolder> list = null;
        try
        {
            list = VSMCMain.get_base_util_em().createQuery("select p from HotFolder p", HotFolder.class);
        }
        catch (SQLException sQLException)
        {
            VSMCMain.notify(this, "Fehler beim Erzeugen der Hotfolder-Tabelle", sQLException.getMessage());
            return new VerticalLayout();
        }

        hotFolderTable = HotFolderTable.createTable(main, list, l);

        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = hotFolderTable.createHeader(VSMCMain.Txt("Liste der Hotfolder:"));

        tableWin.addComponent(head);
        tableWin.addComponent(hotFolderTable);
        tableWin.setExpandRatio(hotFolderTable, 1.0f);
        return tableWin;
    }

    final Component createHotFolderErrorPanel( HotFolder hotFolder)
    {
        GenericEntityManager em =  VSMCMain.get_base_util_em();
        ItemClickListener l = new ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {

                if (event.getButton() == ItemClickEvent.BUTTON_LEFT && !event.isDoubleClick())
                {
                    setActiveHFError();
                }
                if (event.getButton() == ItemClickEvent.BUTTON_RIGHT && !event.isDoubleClick())
                {
                    HotFolderError job = (HotFolderError)((BeanItem)event.getItem()).getBean();
                    create_hferror_popup(event, job);
                }

            }
        };

        hotFolderErrorTable = HotFolderErrorTable.createTable(main, hotFolder, hotFolder.getErrlist(em), l);

        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = hotFolderErrorTable.createHeader(VSMCMain.Txt("Liste der HotFolder-Fehler:"));

        tableWin.addComponent(head);
        tableWin.addComponent(hotFolderErrorTable);
        tableWin.setExpandRatio(hotFolderErrorTable, 1.0f);
        return tableWin;
    }

    private void setActiveHFError()
    {
        AbstractLayout panel = hotFolderErrorTable.createLocalPreviewPanel();
        panel.setSizeUndefined();
        panel.setWidth("100%");
        hfErrorSplitter.setSecondComponent(panel);
    }

    @Override
    public void refresh( Refresher source )
    {
        source.setRefreshInterval(0);
        long s = System.currentTimeMillis();
        refreshErrorTable();
        long e = System.currentTimeMillis();

        // COMM TIME SHOULD NOT EXCEED 20% OF CYCLE TIME
        int rfi = (int)((e-s) * 5);
        if (rfi < RF_INTERVALL)
            rfi = RF_INTERVALL;

        source.setRefreshInterval(rfi);
    }

    private void refreshErrorTable()
    {

        if (hotFolderTable == null || hotFolderTable.getActiveElem() == null)
            return;

        GenericEntityManager em = VSMCMain.get_base_util_em();

        int l1 = hotFolderTable.getActiveElem().getErrlist(em).size();
        em.em_refresh(hotFolderTable.getActiveElem());
        int l2 = hotFolderTable.getActiveElem().getErrlist(em).size();
        if (l1 != l2)
        {
            Component tableWin  = createHotFolderErrorPanel( hotFolderTable.getActiveElem() );
            hfErrorSplitter.setFirstComponent(tableWin);
            hfErrorSplitter.setSecondComponent(new Label(""));
        }
    }

    ContextMenu lastArMenu = null;

    void create_hferror_popup( ItemClickEvent event, final HotFolderError hferr )
    {
        ContextMenu menu = new ContextMenu();

        // Generate main level items
        final ContextMenuItem remove = menu.addItem(VSMCMain.Txt("Löschen"));

        menu.addListener(new ContextMenu.ClickListener()
        {

            @Override
            public void contextItemClick( ContextMenu.ClickEvent event )
            {
                // Get reference to clicked item
                ContextMenuItem clickedItem = event.getClickedItem();

                if (clickedItem == remove)
                {
                    hotFolderErrorTable.deleteHotFolderError(hferr);
                }
            }
        }); // Open Context Menu to mouse coordinates when user right clicks layout


        if (lastArMenu != null)
        {
            hfErrorSplitter.removeComponent(lastArMenu);
        }

        // HAS TO BE IN VAADIN VIEW
        hfErrorSplitter.getApplication().getMainWindow().addComponent(menu);
        lastArMenu = menu;

        menu.show(event.getClientX(), event.getClientY());

    }



}
