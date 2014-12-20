/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.search;

import de.dimm.vsm.vaadin.GuiElems.FileSystem.IContextMenuCallback;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.*;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.net.SearchEntry;
import de.dimm.vsm.net.SearchWrapper;
import de.dimm.vsm.net.StoragePoolWrapper;
import de.dimm.vsm.records.FileSystemElemNode;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTree;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTreeColumn;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTreeContainer;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteFSElemTreeElem;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteProvider;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.MountLocationDlg;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.RestoreLocationDlg;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTreePanel;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteItemDescriptionGenerator;
import de.dimm.vsm.vaadin.SelectObjectCallback;
import de.dimm.vsm.vaadin.VSMCMain;
import de.dimm.vsm.vaadin.net.DownloadResource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.peter.contextmenu.ContextMenu;



/**
 *
 * @author Administrator
 */
public class SearchClientWin extends SidebarPanel
{



    boolean mountedView = false;
    boolean mountedVol = false;
    SearchWrapper searchWrapper = null;
    HorizontalLayout treePanel;
    FSTreeContainer container;
    TreeTable tree;
    final TextField txt_search_name = new TextField(VSMCMain.Txt("Suchbegriff"));
    final ComboBox cb_file_dir;
    final ComboBox cb_type;

    final TextField txt_status = new TextField(VSMCMain.Txt("Status"));
    //final Button btViewVol = new NativeButton(VSMCMain.Txt("View file system"));
    final String mntText = VSMCMain.Txt("Suchergebnisse_als_Dateisystem_laden");
    final String umntText = VSMCMain.Txt("Dateisystem_entfernen");

    final Button btMountVol;
    final Button btStartSearch;

    final ComboBox cb_maxResults;

    NativeSelect poolSelector;
    int lastPoolCnt = -1;


    String[] typ = {SearchEntry.OP_EQUAL, SearchEntry.OP_BEGINS, SearchEntry.OP_CONTAINS, SearchEntry.OP_ENDS };
    String[] niceTyp = {VSMCMain.Txt("enthält Begriff"),VSMCMain.Txt("beginnt_mit"), VSMCMain.Txt("enthält"),  VSMCMain.Txt("endet_mit") };

    String[] df = {"file", "dir"};
    String[] niceDf = {VSMCMain.Txt("Datei"), VSMCMain.Txt("Verzeichnis")};

    TimeIntervalPanel[] t_panels =
    {
        new TimeIntervalPanel(VSMCMain.Txt("Letzte Änderung"), SearchEntry.ARG_MDATE, false),
        new TimeIntervalPanel(VSMCMain.Txt("Erstellt"), SearchEntry.ARG_CDATE, false),
        new TimeIntervalPanel(VSMCMain.Txt("Sicherung"), SearchEntry.ARG_TS, false)
    };

    public SearchClientWin( VSMCMain _main )
    {
        super(_main);
        
        this.setStyleName("statusWin");
        this.setSizeFull();
        this.setSpacing(true);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        this.addComponent(mainLayout);

        poolSelector = new NativeSelect(VSMCMain.Txt("Pool"));
        poolSelector.setNewItemsAllowed(false);
        poolSelector.setInvalidAllowed(false);
        poolSelector.setNullSelectionAllowed(false);
        poolSelector.setImmediate(true);
        HorizontalLayout pshl = new HorizontalLayout();
        pshl.setMargin(false, false, true, false);
        pshl.addComponent(poolSelector);
        pshl.setComponentAlignment(poolSelector, Alignment.MIDDLE_LEFT);

        mainLayout.addComponent(pshl);
        txt_status.setReadOnly(true);

        List<ComboEntry> entries = new ArrayList<ComboEntry>();
        for (int i = 0; i < niceTyp.length; i++)
        {
            entries.add( new ComboEntry( typ[i],  niceTyp[i]) );
        }
        
        cb_type = new ComboBox(VSMCMain.Txt("Suche"), entries);
        cb_type.setNullSelectionAllowed(false);
        cb_type.select(entries.get(2));

        entries = new ArrayList<ComboEntry>();
        for (int i = 0; i < niceDf.length; i++)
        {
            entries.add( new ComboEntry( df[i],  niceDf[i]) );
        }
        cb_file_dir = new ComboBox(VSMCMain.Txt("Typ"), entries);
        cb_file_dir.setNullSelectionAllowed(false);
        cb_file_dir.select(entries.get(0));

        txt_search_name.setImmediate(true);
        txt_search_name.setWidth("200px");
        txt_search_name.addShortcutListener( new ShortcutListener("enter", KeyCode.ENTER, null)
        {
            @Override
            public void handleAction( Object sender, Object target )
            {
                startSearch();
            }
        });

        entries = new ArrayList<ComboEntry>();

        entries.add( new ComboEntry( new Integer(20),  "20") );
        entries.add( new ComboEntry( new Integer(200),  "200") );
        entries.add( new ComboEntry( new Integer(5000),  "5000") );
        entries.add( new ComboEntry( new Integer(0),  VSMCMain.Txt("Unbegrenzt")) );

        cb_maxResults = new ComboBox(VSMCMain.Txt("Ergebnisse"), entries);
        cb_maxResults.setNullSelectionAllowed(false);
        cb_maxResults.select(entries.get(0));

        

        btMountVol = new Button(mntText);
        btMountVol.addListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                startMount();
            }
        });
        btMountVol.setVisible(false);

        final Button btUpdateReadIndex = new Button(VSMCMain.Txt("Index aktualisieren"));
        btUpdateReadIndex.addListener(new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                updateReadIndex();
            }


        });

        btStartSearch = new Button(VSMCMain.Txt("Suche starten"));
        btStartSearch.addListener(new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                startSearch();
            }
        });

        

        VerticalLayout vl = new VerticalLayout();
        mainLayout.addComponent(vl);
        vl.setSpacing(true);

        HorizontalLayout hl_main = new HorizontalLayout();
        hl_main.setSpacing(true);
        hl_main.setSizeFull();

        Panel pn_search = new Panel();
        pn_search.setCaption(VSMCMain.Txt("Suchbegriff"));
        pn_search.setSizeFull();
        
        VerticalLayout pn_search_layout = (VerticalLayout) pn_search.getContent();
        pn_search_layout.setSpacing(true);
        pn_search_layout.setSizeFull();


        pn_search.addComponent(txt_search_name);
        pn_search.addComponent(cb_file_dir);
        pn_search.addComponent(cb_type);
        



        Panel pn_options = new Panel();
        pn_options.setCaption(VSMCMain.Txt("Optionen"));
        pn_options.setSizeFull();
        VerticalLayout pn_options_layout = (VerticalLayout) pn_search.getContent();
        pn_options_layout.setSpacing(true);
        pn_options_layout.setSizeFull();
        


        
        for (int i = 0; i < t_panels.length; i++)
        {
            TimeIntervalPanel t_panel = t_panels[i];
            pn_options.addComponent(t_panel);
        }
        pn_options.addComponent(cb_maxResults);

        hl_main.addComponent(pn_search);
        hl_main.setExpandRatio(pn_search, 1.0f);
        hl_main.addComponent(pn_options);
        hl_main.setExpandRatio(pn_options, 1.0f);

        vl.addComponent(hl_main);
        

        // BUTTONS
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.setSizeFull();
        hl.addComponent(btUpdateReadIndex);
        hl.addComponent(btMountVol);
        hl.addComponent( btStartSearch);
        hl.setComponentAlignment(btStartSearch, Alignment.BOTTOM_RIGHT);


        pn_search.addComponent(hl);
        pn_search_layout.setComponentAlignment(hl, Alignment.BOTTOM_LEFT);
        

        treePanel = new HorizontalLayout();
        // reserve excess space for the "treecolumn"
        treePanel.setSizeFull();

        mainLayout.addComponent(treePanel);
        mainLayout.setExpandRatio(treePanel, 1.0f);
    }

    void closeSearchResults()
    {
        unmountVol();

        if (searchWrapper != null)
        {
            main.getGuiServerApi().closeSearch(searchWrapper);
            searchWrapper = null;
        }

        hideFS();
    }
    
    @Override
    public void activate()
    {

        List<StoragePool> list = main.getStoragePoolList();
        if (lastPoolCnt == list.size() || list.isEmpty())
            return;


        //backupJobResultContainer.removeAllComponents();
        //jobTableContainer = new VerticalLayout();

        //backupJobResultContainer.addComponent(poolSelector);
        //backupJobResultContainer.addComponent(jobTableContainer);

        poolSelector.removeAllItems();

        for (int i = 0; i < list.size(); i++)
        {
            Object object = list.get(i);
            poolSelector.addItem(object);
        }
        if (!list.isEmpty())
        {
            poolSelector.setValue(list.get(0));

        }
        poolSelector.addListener( new ValueChangeListener()
        {

            @Override
            public void valueChange( ValueChangeEvent event )
            {
                closeSearchResults();
            }
        });


        lastPoolCnt = list.size();


        txt_search_name.focus();
    }

    @Override
    public void deactivate()
    {
        closeSearchResults();
    }


    void updateReadIndex()
    {
        SelectObjectCallback cb = new SelectObjectCallback<StoragePool>()
        {
            @Override
            public void SelectedAction( StoragePool pool )
            {
                main.getGuiServerApi().updateReadIndex(pool);
                VSMCMain.notify(txt_search_name,  VSMCMain.Txt("Der Index ist jetzt auf dem neuesten Stand"), "");
            }
        };

        StoragePool p =(StoragePool) poolSelector.getValue();
        cb.SelectedAction(p);
    }

    void startSearch()
    {
        final String searchStr = txt_search_name.getValue().toString().replace('_', ' ').trim();


        if (searchStr.isEmpty())
        {
            VSMCMain.notify(this, VSMCMain.Txt("Bitte_geben Sie einen Suchbegriff ein"), "");
            return;
        }
        SelectObjectCallback cb = new SelectObjectCallback<StoragePool>()
        {

            @Override
            public void SelectedAction( StoragePool pool )
            {
                ArrayList<SearchEntry> slist = new ArrayList<SearchEntry>();

                boolean ci = true;

                ComboEntry cb = (ComboEntry) cb_type.getValue();

                slist.add( new SearchEntry(searchStr, SearchEntry.ARG_NAME,  cb.getDbEntry().toString(),  false, false, ci, null) );


                for (int i = 0; i < t_panels.length; i++)
                {
                    TimeIntervalPanel tp = t_panels[i];
                    if (!tp.isTimeValid())
                        continue;

                    if (tp.getFrom() != null)
                        slist.add( new SearchEntry(Long.toString(tp.getFrom().getTime()), tp.getField(),  SearchEntry.OP_GE,  false, false, ci, null) );

                    if (tp.getTill() != null)
                        slist.add( new SearchEntry(Long.toString(tp.getTill().getTime()), tp.getField(),  SearchEntry.OP_LE,  false, false, ci, null) );
                }

                cb = (ComboEntry)cb_file_dir.getValue();

                if (cb.isDbEntry("dir"))
                {
                    slist.add( new SearchEntry(FileSystemElemNode.FT_DIR, SearchEntry.ARG_TYP,  SearchEntry.OP_EQUAL,  false, false, ci, null) );
                }
                else
                {
                    slist.add( new SearchEntry(FileSystemElemNode.FT_FILE, SearchEntry.ARG_TYP,  SearchEntry.OP_EQUAL,  false, false, ci, null) );
                }


                cb = (ComboEntry)cb_maxResults.getValue();
                int max = ((Integer)cb.getDbEntry()).intValue();


                if (mountedView)
                {
                    hideFS();
                }

                if (searchWrapper != null)
                {
                    main.getGuiServerApi().closeSearch(searchWrapper);
                }

                searchWrapper = main.getGuiServerApi().search( pool, slist, max );

                showFs();
            }
        };
        StoragePool p =(StoragePool) poolSelector.getValue();
        cb.SelectedAction(p);

//        List<StoragePool> list = main.getStoragePoolList();
//        main.SelectObject(StoragePool.class, VSMCMain.Txt("Pool"), VSMCMain.Txt("Weiter"), list, cb);
    }

    void startMount()
    {
        if (mountedVol)
        {
            unmountVol();
            return;
        }
        final MountLocationDlg dlg = main.mountDlg;
        dlg.setIP( main.getHost() );
        
        dlg.setOkListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( ClickEvent event )
            {
                if (searchWrapper == null)
                {
                     main.Msg().errmOk(VSMCMain.Txt("Bitte starten Sie zuerst eine Suche"));
                     return;
                }
                mountVol( dlg);
            }
        });
        treePanel.getApplication().getMainWindow().addWindow( dlg );
    }

    String mountedIP;
    int mountedPort;
    String mountedDrive;
    void mountVol( MountLocationDlg loc )
    {
        mountedIP = loc.getIP();
        mountedPort = loc.getPort();
        mountedDrive = loc.getPath();
        

        StoragePoolWrapper wrapper = main.getGuiServerApi().getMounted(mountedIP, mountedPort, searchWrapper);

        if (!mountedVol)
        {
            if (wrapper != null)
            {
                 main.getGuiServerApi().unmountVolume(wrapper);
            }
            wrapper = main.getGuiServerApi().mountVolume(mountedIP, mountedPort, searchWrapper,  null, mountedDrive);
            if (wrapper != null)
            {

                btMountVol.setCaption(umntText);
                mountedVol = true;
                VSMCMain.notify(txt_search_name,  VSMCMain.Txt("Die Ergebnisse wurden bereitgestellt auf"), mountedIP + "->" + mountedDrive);
            }
            else
            {
               main.Msg().errmOk(VSMCMain.Txt("Das Mounten des Volumes schlug fehl"));
            }
        }
    }

    void unmountVol()
    {
        if (!mountedVol || mountedIP == null)
            return;

        StoragePoolWrapper wrapper = main.getGuiServerApi().getMounted(mountedIP, mountedPort, searchWrapper);

        if (wrapper != null)
        {
            main.getGuiServerApi().unmountVolume(wrapper);
        }
        btMountVol.setCaption(mntText);
        mountedVol = false;
        mountedDrive = null;
        mountedIP = null;
    }

    void hideFS()
    {

        btMountVol.setVisible(false);

        mountedView = false;

        treePanel.removeAllComponents();
    }

    void showFs()
    {
        btMountVol.setVisible(true);
        List<RemoteFSElem> ret = main.getGuiServerApi().getSearchResult(searchWrapper, 0, 100);

        mountedView = true;

        Component _tree = initFsTree(ret);
        _tree.setSizeFull();
        treePanel.addComponent(_tree);
    }
    
    boolean checkWrapperValid( )
    {
        if (!main.getGuiServerApi().isWrapperValid(searchWrapper))
        {
            main.Msg().errmOk(VSMCMain.Txt("Die Ergebnisse stehen nicht mehr zur Verfügung"));
            return false;                    
        }   
        return true;        
    }


    Component initFsTree( List<RemoteFSElem> root_list )
    {
        ArrayList<FSTreeColumn> fields = new ArrayList<FSTreeColumn>();
        fields.add(new FSTreeColumn("name", VSMCMain.Txt("Name"), -1, 1.0f, Table.ALIGN_LEFT, String.class));
        fields.add(new FSTreeColumn("date", VSMCMain.Txt("Datum"), 100, -1, Table.ALIGN_LEFT, String.class));
        fields.add(new FSTreeColumn("size", VSMCMain.Txt("Größe"), 80, -1, Table.ALIGN_RIGHT, String.class));
        fields.add(new FSTreeColumn("atttribute", VSMCMain.Txt("Attribute"), 80, -1, Table.ALIGN_LEFT, String.class));

        RemoteProvider provider = new RemoteProvider()
        {

            @Override
            public RemoteFSElemTreeElem createNode( RemoteProvider provider, RemoteFSElem elem, RemoteFSElemTreeElem parent )
            {
                return new RemoteFSElemTreeElem(provider, elem, parent);
            }

            @Override
            public List<RemoteFSElemTreeElem> getChildren( RemoteFSElemTreeElem elem )
            {
                List<RemoteFSElemTreeElem> childList = new ArrayList<RemoteFSElemTreeElem>();
                try
                {
                    if (!checkWrapperValid())
                        return childList;                    
                    
                    List<RemoteFSElem> elem_list = main.getGuiServerApi().listSearchDir(searchWrapper, elem.getElem());
                    for (int i = 0; i < elem_list.size(); i++)
                    {
                        RemoteFSElem rfse = elem_list.get(i);
                        RemoteFSElemTreeElem e = new RemoteFSElemTreeElem(this, rfse, elem);
                        childList.add(e);
                    }

                }
                catch (Exception ex)
                {
                    Logger.getLogger(SearchClientWin.class.getName()).log(Level.SEVERE, null, ex);
                }
                return childList;
            }

            @Override
            public boolean createDir( RemoteFSElemTreeElem elem )
            {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public ItemDescriptionGenerator getItemDescriptionGenerator()
            {
                return new RemoteItemDescriptionGenerator(searchWrapper, main );
            }


        };

        container = new FSTreeContainer(provider, fields);
        if (root_list != null)
            container.initRootlist(root_list);

        tree = new FSTree(fields, /*sort */ true);
        tree.setContainerDataSource(container);

        tree.setItemDescriptionGenerator(provider.getItemDescriptionGenerator());


        tree.addListener(new ItemClickListener()
        {

            // HANDLE
            @Override
            public void itemClick( ItemClickEvent event )
            {
                if (event.getItemId() instanceof RemoteFSElemTreeElem
                        && (event.getButton() & com.vaadin.event.MouseEvents.ClickEvent.BUTTON_RIGHT) == com.vaadin.event.MouseEvents.ClickEvent.BUTTON_RIGHT)
                {
                    if (!checkWrapperValid())
                        return;                    
                    
                    RemoteFSElemTreeElem clickedItem = (RemoteFSElemTreeElem)event.getItemId();
                    Object sel = tree.getValue();

                    if (sel instanceof Set<?> && ((Set<?>)sel).size() > 1)
                    {
                        Set<RemoteFSElemTreeElem> set = (Set<RemoteFSElemTreeElem>)sel;
                        List<RemoteFSElemTreeElem> list = new ArrayList<RemoteFSElemTreeElem>(set);
                        create_fs_popup(event, list);
                    }
                    else
                    {
                        List<RemoteFSElemTreeElem> list = new ArrayList<RemoteFSElemTreeElem>();
                        list.add(clickedItem);
                        create_fs_popup(event, list);
                    }
                }
                if (event.getItemId() instanceof RemoteFSElemTreeElem && event.isDoubleClick())
                {
                    if (!checkWrapperValid())
                        return;                    
                    
                    RemoteFSElemTreeElem rfstreeelem = (RemoteFSElemTreeElem) event.getItemId();
                    DownloadResource downloadResource = FSTreePanel.createDownloadResource( main, getApplication(), searchWrapper, rfstreeelem);
                    getWindow().open(downloadResource);
                }
            }
        });


        return tree;
    }
    ContextMenu lastMenu = null;

    void create_fs_popup( ItemClickEvent event, final List<RemoteFSElemTreeElem> rfstreeelems )
    {
        if (!checkWrapperValid())
            return;                    
                
        if (lastMenu != null)
        {
            treePanel.removeComponent(lastMenu);
        }

        IContextMenuCallback callback = new IContextMenuCallback() {

            @Override
            public void handleRestoreTargetDialog( List<RemoteFSElemTreeElem> rfstreeelems )
            {
                 RestoreLocationDlg dlg = FSTreePanel.createRestoreTargetDialog(main, searchWrapper, rfstreeelems );
                 treePanel.getApplication().getMainWindow().addWindow( dlg );
            }

            @Override
            public void handleDownload( RemoteFSElemTreeElem singleRfstreeelem )
            {
                if (!checkWrapperValid())
                    return;                    
                
                DownloadResource downloadResource = FSTreePanel.createDownloadResource( main, getApplication(), searchWrapper, singleRfstreeelem);
                if (downloadResource == null)
                {
                    VSMCMain.notify(txt_search_name,  VSMCMain.Txt("Auf die Ergebnisse kann nicht mehr zugegriffen werden"), mountedIP + "->" + mountedDrive);
                }
                getWindow().open(downloadResource);
            }
        };

        lastMenu = FSTreePanel.create_fs_popup(main, searchWrapper, tree, container, event, rfstreeelems, callback, null);
    }

}


