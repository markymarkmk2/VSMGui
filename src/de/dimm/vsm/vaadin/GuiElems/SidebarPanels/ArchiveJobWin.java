/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.NativeButton;
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
import de.dimm.vsm.net.interfaces.GuiServerApi;
import de.dimm.vsm.records.ArchiveJob;
import de.dimm.vsm.records.FileSystemElemNode;
import de.dimm.vsm.records.HotFolder;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTree;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTreeColumn;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTreeContainer;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteFSElemTreeElem;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteProvider;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.ArchiveJobInfoWindow;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.MountLocationDlg;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.RestoreLocationDlg;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.IContextMenuCallback;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteItemDescriptionGenerator;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.ArchivJobTable;
import de.dimm.vsm.vaadin.SelectObjectCallback;
import de.dimm.vsm.vaadin.VSMCMain;
import de.dimm.vsm.vaadin.net.DownloadResource;
import de.dimm.vsm.vaadin.search.TimeIntervalPanel;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;



/**
 *
 * @author Administrator
 */
public class ArchiveJobWin extends SidebarPanel
{



    boolean mountedView = false;
    boolean mountedVol = false;
    SearchWrapper searchWrapper = null;
    StoragePoolWrapper poolWrapper = null;

    HorizontalLayout treePanel;
    FSTreeContainer container;
    TreeTable tree;
    final TextField txt_search_name = new TextField(VSMCMain.Txt("Suchbegriff"));
    final ComboBox cb_file_dir;
    final ComboBox cb_type;
    

    final TextField txt_status = new TextField(VSMCMain.Txt("Status"));
    //final Button btViewVol = new NativeButton(VSMCMain.Txt("View file system"));
    final String mntText = VSMCMain.Txt(VSMCMain.Txt("Suchergebnisse_als_Dateisystem_laden"));
    final String umntText = VSMCMain.Txt(VSMCMain.Txt("Dateisystem_entfernen"));

    final Button btMountVol;
    final Button btStartSearch;

    final ComboBox cb_maxResults;

    String[] typ = {SearchEntry.OP_EQUAL, SearchEntry.OP_BEGINS, SearchEntry.OP_CONTAINS, SearchEntry.OP_ENDS };
    String[] niceTyp = {VSMCMain.Txt("enthält Begriff"),VSMCMain.Txt("beginnt_mit"), VSMCMain.Txt("enthält"),  VSMCMain.Txt("endet_mit") };

    String[] df = {"job","file", "dir"};
    String[] niceDf = {VSMCMain.Txt("Auftrag"), VSMCMain.Txt("Datei"), VSMCMain.Txt("Verzeichnis")};


    NativeSelect poolSelector;
    int lastPoolCnt = -1;

    ArchivJobTable jobTable;


    TimeIntervalPanel[] t_panels =
    {
        new TimeIntervalPanel(VSMCMain.Txt("Job erzeugt"), SearchEntry.ARG_JOBCREATION, false),
        new TimeIntervalPanel(VSMCMain.Txt("Letzte Änderung"), SearchEntry.ARG_MDATE, false),
        new TimeIntervalPanel(VSMCMain.Txt("Erstellt"), SearchEntry.ARG_CDATE, false),
        new TimeIntervalPanel(VSMCMain.Txt("Sicherung"), SearchEntry.ARG_TS, false)
    };

    public ArchiveJobWin( VSMCMain _main )
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

        entries.add( new ComboEntry( new Integer(1),  "1") );
        entries.add( new ComboEntry( new Integer(20),  "20") );
        entries.add( new ComboEntry( new Integer(200),  "200") );
        entries.add( new ComboEntry( new Integer(5000),  "5000") );
        //entries.add( new ComboEntry( new Integer(0),  VSMCMain.Txt("Unbegrenzt")) );

        cb_maxResults = new ComboBox(VSMCMain.Txt("Ergebnisse"), entries);
        cb_maxResults.setNullSelectionAllowed(false);
        cb_maxResults.select(entries.get(1));

        

        btMountVol = new NativeButton(mntText);
        btMountVol.addListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                startMount();
            }
        });
        btMountVol.setVisible(false);

        final Button btUpdateReadIndex = new NativeButton(VSMCMain.Txt("Index aktualisieren"));
        btUpdateReadIndex.addListener(new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                updateReadIndex();
            }
        });

        btStartSearch = new NativeButton(VSMCMain.Txt("Suche starten"));
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
        pn_search.setCaption("Suchbegriff");
        pn_search.setSizeFull();
        
        VerticalLayout pn_search_layout = (VerticalLayout) pn_search.getContent();
        pn_search_layout.setSpacing(true);
        pn_search_layout.setSizeFull();


        pn_search.addComponent(txt_search_name);
        pn_search.addComponent(cb_file_dir);
        pn_search.addComponent(cb_type);
        



        Panel pn_options = new Panel();
        pn_options.setCaption("Optionen");
        pn_options.setSizeFull();
        


        
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
                closeArchivResults();
            }
        });


        lastPoolCnt = list.size();


        txt_search_name.focus();
    }

    void closeArchivResults()
    {
        unmountVol();
        
        if (searchWrapper != null)
        {
            main.getGuiServerApi().closeSearch(searchWrapper);
            searchWrapper = null;
        }
        if (poolWrapper != null)
        {
            main.getGuiServerApi().closePoolView(poolWrapper);
            poolWrapper = null;
        }

        hideFS();

    }

    @Override
    public void deactivate()
    {
        closeArchivResults();

        treePanel.removeAllComponents();
    }




    void startSearch()
    {
        final String searchStr = txt_search_name.getValue().toString().replace('_', ' ').trim();


        
        SelectObjectCallback cb = new SelectObjectCallback<StoragePool>()
        {

            @Override
            public void SelectedAction( StoragePool pool )
            {
                ArrayList<SearchEntry> slist = new ArrayList<SearchEntry>();

                boolean ci = true;

                ComboEntry cb = (ComboEntry) cb_type.getValue();
                ComboEntry cb_fileDirJob = (ComboEntry)cb_file_dir.getValue();

                if (!searchStr.isEmpty())
                {
                    if (cb_fileDirJob.isDbEntry("job"))
                        slist.add( new SearchEntry(txt_search_name.getValue().toString(), SearchEntry.ARG_JOBNAME,  cb.getDbEntry().toString(),  false, false, ci, null) );
                    else
                        slist.add( new SearchEntry(txt_search_name.getValue().toString(), SearchEntry.ARG_NAME,  cb.getDbEntry().toString(),  false, false, ci, null) );
                }

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

                

                if (cb_fileDirJob.isDbEntry("dir"))
                {
                    slist.add( new SearchEntry(FileSystemElemNode.FT_DIR, SearchEntry.ARG_TYP,  SearchEntry.OP_EQUAL,  false, false, ci, null) );
                }
                else if (cb_fileDirJob.isDbEntry("file"))
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

                searchWrapper = main.getGuiServerApi().searchJob( pool, slist, max );

                showJobList();
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

        if (searchWrapper == null)
        {
             main.Msg().errmOk(VSMCMain.Txt("Bitte starten Sie zuerst eine Suche"));
             return;
        }

        if (poolWrapper == null)
        {
             main.Msg().errmOk(VSMCMain.Txt("Bitte wählen Sie zuerst einen Archivjob aus"));
             return;
        }


        final MountLocationDlg dlg = VSMCMain.mountDlg;
        dlg.setIP( main.getHost() );
        
        dlg.setOkListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( ClickEvent event )
            {
                mountVol( dlg);
            }
        });
        treePanel.getApplication().getMainWindow().addWindow( dlg );
    }

    String mountedIP;
    int mountedPort;
    String mountedDrive;
    StoragePool lastMountedPool = null;
    void mountVol( MountLocationDlg loc )
    {
        mountedIP = loc.getIP();
        mountedPort = loc.getPort();
        mountedDrive = loc.getPath();

        unmountVol();
       

        if (!mountedVol && poolWrapper != null)
        {
            main.getGuiServerApi().mountVolume(mountedIP, mountedPort, poolWrapper, mountedDrive);

            btMountVol.setCaption(umntText);
            mountedVol = true;
            lastMountedPool = main.getStoragePool(poolWrapper.getPoolIdx());
        }
    }

    void unmountVol()
    {
        if (lastMountedPool == null)
            return;

        StoragePoolWrapper wrapper = main.getGuiServerApi().getMounted(mountedIP, mountedPort, lastMountedPool);

        if (wrapper != null)
        {
            main.getGuiServerApi().unmountVolume(wrapper);
        }
        btMountVol.setCaption(mntText);
        mountedVol = false;
        mountedDrive = null;
        mountedIP = null;
        lastMountedPool = null;
    }

    void hideFS()
    {

        btMountVol.setVisible(false);

        mountedView = false;

        treePanel.removeAllComponents();
    }

    void showJobList()
    {
        btMountVol.setVisible(true);
        ComboEntry cb = (ComboEntry)cb_maxResults.getValue();
        int max = ((Integer)cb.getDbEntry()).intValue();
        List<ArchiveJob> ret = main.getGuiServerApi().getJobSearchResult(searchWrapper, 0, max);
        
        final HorizontalSplitPanel spl = new HorizontalSplitPanel();

        ItemClickListener l = new ItemClickListener()
        {

            @Override
            public void itemClick( ItemClickEvent event )
            {
                if (event.getButton() == ItemClickEvent.BUTTON_LEFT && event.isDoubleClick())
                {
                    ArchiveJob job = (ArchiveJob)((BeanItem)event.getItem()).getBean();
                    ArchiveJobInfoWindow win = new ArchiveJobInfoWindow(main, job);

                    // Do something with the reference
                    getApplication().getMainWindow().addWindow(win);
                }

                if (event.getButton() == ItemClickEvent.BUTTON_LEFT && !event.isDoubleClick())
                {
                    ArchiveJob job = (ArchiveJob)((BeanItem)event.getItem()).getBean();
                    Component c = createFSTree( job );
                    spl.setSecondComponent(c);
                }
                if (event.getButton() == ItemClickEvent.BUTTON_RIGHT && !event.isDoubleClick())
                {
                    ArchiveJob job = (ArchiveJob)((BeanItem)event.getItem()).getBean();
                    create_archive_popup(event, job);
                }
            }
        };

        jobTable = ArchivJobTable.createTable(main, this, ret, l);
        jobTable.setSizeFull();

        treePanel.removeAllComponents();
        
        treePanel.addComponent(spl);
        spl.setSplitPosition(50, Sizeable.UNITS_PERCENTAGE);

        spl.setFirstComponent(jobTable);
        



    }
    Component createFSTree(ArchiveJob job)
    {
        mountedView = true;

        Component _tree = initFsTree(job);
        
        return _tree;
    }


    Component initFsTree( ArchiveJob job )
    {
        if (job.getDirectory() == null)
        {
            VSMCMain.notify(this, VSMCMain.Txt("Keine Verzeichnisdaten"), VSMCMain.Txt("Dieser Job wurde nicht korrekt gestartet"));
            return null;
        }

        
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
                    Logger.getLogger(ArchiveJobWin.class.getName()).log(Level.SEVERE, null, ex);
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

        if (poolWrapper != null)
            main.getGuiServerApi().closePoolView(poolWrapper);

        StoragePool pool = main.resolveStoragePool(searchWrapper.getPoolIdx());


        poolWrapper = main.getGuiServerApi().openPoolView(pool, true, job.getDirectory(), main.getGuiWrapper().getUser());
        // DO NOT MAP ARCHIVE PATHS 
        poolWrapper.getQry().setUseMappingFilter(false);

        RemoteFSElem root = new RemoteFSElem(job.getDirectory(), job.getDirectory().getAttributes());
        root.setIdx(job.getDirectory().getIdx());
        List<RemoteFSElem> root_list = null;

        try
        {
            root_list = main.getGuiServerApi().listDir(poolWrapper, root);
        }
        catch (Exception sQLException)
        {
            VSMCMain.notify(this, VSMCMain.Txt("Keine Verzeichnisdaten"), sQLException.getMessage());
            return null;
        }

        container.initRootlist(root_list);

        tree = new FSTree(fields, /*sort */ false);
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
                    RemoteFSElemTreeElem rfstreeelem = (RemoteFSElemTreeElem) event.getItemId();
                    handleDownload(rfstreeelem);
                }
            }
        });


        tree.setSizeFull();
        return tree;
    }
    ContextMenu lastMenu = null;
    void create_fs_popup( ItemClickEvent event, final List<RemoteFSElemTreeElem> rfstreeelems )
    {
        if (lastMenu != null)
        {
            treePanel.removeComponent(lastMenu);
        }

        IContextMenuCallback callback = new IContextMenuCallback() {

            @Override
            public void handleRestoreTargetDialog( List<RemoteFSElemTreeElem> rfstreeelems )
            {
                 RestoreLocationDlg dlg = FileSystemViewer.createRestoreTargetDialog(main, searchWrapper, rfstreeelems );
                 treePanel.getApplication().getMainWindow().addWindow( dlg );
            }

            @Override
            public void handleDownload( RemoteFSElemTreeElem singleRfstreeelem )
            {
                DownloadResource downloadResource = FileSystemViewer.createDownloadResource( main, getApplication(), searchWrapper, singleRfstreeelem);
                getWindow().open(downloadResource);
            }
        };

        lastMenu = FileSystemViewer.create_fs_popup(main, searchWrapper, tree, container, event, rfstreeelems, callback);
    }

//    void create_fs_popup( ItemClickEvent event, final RemoteFSElemTreeElem rfstreeelem )
//    {
//        ContextMenu menu = new ContextMenu();
//        ContextMenuItem dl = null;
//
//        // Generate main level items
//        final ContextMenuItem info = menu.addItem(VSMCMain.Txt("Information"));
//
//        // SEPARATOR BENEATH THIS ONE
//        info.setSeparatorVisible(true);
//
//        //final ContextMenuItem ver = menu.addItem(VSMCMain.Txt("Versions"));
//        final ContextMenuItem restore = menu.addItem(VSMCMain.Txt("Restore"));
//        if (!rfstreeelem.getElem().isDirectory())
//            dl = menu.addItem(VSMCMain.Txt("Download"));
//
//        final ContextMenuItem download = dl;
//
//
//        menu.addListener(new ContextMenu.ClickListener()
//        {
//
//            @Override
//            public void contextItemClick( ContextMenu.ClickEvent event )
//            {
//                // Get reference to clicked item
//                ContextMenuItem clickedItem = event.getClickedItem();
//                if (clickedItem == info)
//                {
//                    FileinfoWindow win = new FileinfoWindow(main, searchWrapper, rfstreeelem.getElem());
//
//                    // Do something with the reference
//                    getApplication().getMainWindow().addWindow(win);
//                }
//
//                if (clickedItem == restore)
//                {
//                    handleRestoreTargetDialog(rfstreeelem);
//                }
//                if (clickedItem == download)
//                {
//                    handleDownload(rfstreeelem);
//                }
//            }
//        }); // Open Context Menu to mouse coordinates when user right clicks layout
//
//
//        if (lastMenu != null)
//        {
//            treePanel.removeComponent(lastMenu);
//        }
//
//        // HAS TO BE IN VAADIN VIEW
//        treePanel.getApplication().getMainWindow().addComponent(menu);
//        lastMenu = menu;
//
//        menu.show(event.getClientX(), event.getClientY());
//
//    }

    ContextMenu lastArMenu = null;
    void create_archive_popup( ItemClickEvent event, final ArchiveJob job )
    {
        ContextMenu menu = new ContextMenu();

        ContextMenuItem _remove = null;
        // Generate main level items
        final ContextMenuItem info = menu.addItem(VSMCMain.Txt("Information"));
        if (main.getGuiUser().isSuperUser())
        {
            info.setSeparatorVisible(true);
            _remove = menu.addItem(VSMCMain.Txt("Löschen"));
        }
        info.setSeparatorVisible(true);
        final ContextMenuItem restore = menu.addItem(VSMCMain.Txt("Restore"));

        final ContextMenuItem remove = _remove;
        menu.addListener(new ContextMenu.ClickListener()
        {

            @Override
            public void contextItemClick( ContextMenu.ClickEvent event )
            {
                // Get reference to clicked item
                ContextMenuItem clickedItem = event.getClickedItem();
                if (clickedItem == info)
                {
                    ArchiveJobInfoWindow win = new ArchiveJobInfoWindow(main, job);

                    // Do something with the reference
                    getApplication().getMainWindow().addWindow(win);
                }

                if (clickedItem == restore)
                {
                    handleRestoreTargetDialog(job);
                }
                if (clickedItem == remove && main.getGuiUser().isSuperUser())
                {
                    handleRemoveDialog(job);
                }
//                if (clickedItem == download)
//                {
//                    handleDownload(job);
//                }
            }
        }); // Open Context Menu to mouse coordinates when user right clicks layout


        if (lastArMenu != null)
        {
            treePanel.removeComponent(lastArMenu);
        }

        // HAS TO BE IN VAADIN VIEW
        treePanel.getApplication().getMainWindow().addComponent(menu);
        lastArMenu = menu;

        menu.show(event.getClientX(), event.getClientY());

    }


    private void handleDownload( final RemoteFSElemTreeElem rfstreeelem)
    {
        RemoteFSElem fs = rfstreeelem.getElem();

        InputStream is = main.getGuiServerApi().openStream(searchWrapper, fs);

        DownloadResource downloadResource = new DownloadResource(is, "\"" + fs.getName() + "\"", btMountVol.getApplication());

        getWindow().open(downloadResource);
    }


//    private void handleDownload( final ArchiveJob job)
//    {
//        RemoteFSElem fs = rfstreeelem.getElem();
//
//        InputStream is = main.getGuiServerApi().openStream(searchWrapper, fs);
//
//        DownloadResource downloadResource = new DownloadResource(is, "\"" + fs.getName() + "\"", btMountVol.getApplication());
//
//        getWindow().open(downloadResource);
//    }


    private void handleRestoreTargetDialog( final ArchiveJob job)
    {
        String ip = main.getIp();

        if (job.getSourceType().equals(ArchiveJob.AJ_SOURCE_HF))
        {
            HotFolder hf = VSMCMain.get_base_util_em().em_find( HotFolder.class, job.getSourceIdx());
            if (hf != null)
                ip = hf.getIp();
        }
        
        final RestoreLocationDlg dlg = new RestoreLocationDlg( main, ip, 8082, "",  /*allowOriginal*/false );
        Button.ClickListener okListener = new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                handleRestoreOkayDialog( dlg, job);
            }
        };
        dlg.setOkListener( okListener );
        treePanel.getApplication().getMainWindow().addWindow( dlg );
    }

    private void handleRemoveDialog( final ArchiveJob job)
    {
        Button.ClickListener okListener = new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                handleRemoveJob( job);
            }

       };

        main.Msg().errmOkCancel(VSMCMain.Txt("Wollen Sie diesen Auftrag vollständig löschen?"), okListener, null);

    }

    public void handleRemoveJob( ArchiveJob job )
    {
        try
        {
            main.getGuiServerApi().removeJob(searchWrapper, job);

            Container ct = jobTable.getContainerDataSource();
            if (ct instanceof BeanContainer)
            {
                BeanContainer<Long, ArchiveJob> bct = (BeanContainer<Long, ArchiveJob>) ct;
                bct.removeItem(job.getIdx());
            }

        }
        catch (Exception exc)
        {
            exc.printStackTrace();
            VSMCMain.notify(treePanel, VSMCMain.Txt("Das Löschen des Jobs schlug fehl"), exc.getMessage());
        }
    }
//
//    private void handleRestoreTargetDialog(  final List<RemoteFSElemTreeElem> rfstreeelems)
//    {
//        final RestoreLocationDlg dlg = new RestoreLocationDlg(main, main.getIp(), 8082, "",  /*allowOriginal*/false );
//        Button.ClickListener okListener = new Button.ClickListener()
//        {
//            @Override
//            public void buttonClick( ClickEvent event )
//            {
//                handleRestoreOkayDialog( dlg, rfstreeelems);
//            }
//        };
//        dlg.setOkListener( okListener );
//        treePanel.getApplication().getMainWindow().addWindow( dlg );
//    }
//
//    private void handleRestoreOkayDialog( final RestoreLocationDlg dlg, final List<RemoteFSElemTreeElem> rfstreeelems)
//    {
//        Button.ClickListener ok = new Button.ClickListener()
//        {
//            @Override
//            public void buttonClick( ClickEvent event )
//            {
//                try
//                {
//                    String ip = dlg.getIP();
//                    int port = dlg.getPort();
//                    String path = dlg.getPath();
//                    if (dlg.isOriginal())
//                    {
//                        if (isHotfolderPath( rfstreeelem ))
//                        {
//                            main.Msg().errmOk(VSMCMain.Txt("Hotfolderobjekte_können_nicht_an_Original_restauriert_werden"));
//                            return;
//                        }
//                        ip = getIpFromPath( rfstreeelem );
//                        port = getPortFromPath( rfstreeelem );
//
//
//                        Properties p = main.getGuiServerApi().getAgentProperties( ip, port, false );
//                        boolean isWindows =  ( p != null && p.getProperty(AgentApi.OP_OS).startsWith("Win"));
//
//                        path = getTargetpathFromPath( rfstreeelem, isWindows );
//                    }
//
//                    int rflags = GuiServerApi.RF_RECURSIVE;
//                    if (isHotfolderPath(rfstreeelem))
//                        rflags |= GuiServerApi.RF_SKIPHOTFOLDER_TIMSTAMPDIR;
//                    if (dlg.isCompressed())
//                        rflags |= GuiServerApi.RF_COMPRESSION;
//                    if (dlg.isEncrypted())
//                        rflags |= GuiServerApi.RF_ENCRYPTION;
//
//
//                    boolean rret = main.getGuiServerApi().restoreFSElem(searchWrapper, rfstreeelem.getElem(), ip, port, path, rflags, main.getUser());
//                    if (!rret)
//                    {
//                        main.Msg().errmOk(VSMCMain.Txt("Der_Restore_schlug_fehl"));
//                    }
//                    else
//                    {
//                        main.Msg().errmOk(VSMCMain.Txt("Der_Restore_wurde_gestartet"));
//                    }
//                }
//                catch (Exception ex)
//                {
//                    main.Msg().errmOk(VSMCMain.Txt("Der_Restore_wurde_abgebrochen") + ": "+ ex.getMessage() );
//                }
//            }
//
//        };
//        if (rfstreeelem.getElem().isDirectory())
//        {
//            main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_dieses_Verzeichnis_und_alle_darin_enthaltenen_Dateien_restaurieren?"), ok, null);
//        }
//        else
//        {
//            main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_diese_Datei_restaurieren?"), ok, null);
//        }
//
//    }
    private void handleRestoreOkayDialog( final RestoreLocationDlg dlg, final ArchiveJob job)
    {
        Button.ClickListener ok = new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                try
                {
                    String ip = dlg.getIP();
                    int port = dlg.getPort();
                    String path = dlg.getPath();
                    if (dlg.isOriginal())
                    {

                        main.Msg().errmOk(VSMCMain.Txt("ArchivJobs_können_nicht_an_Original_restauriert_werden"));
                            return;
                    }

                    int rflags = GuiServerApi.RF_RECURSIVE | GuiServerApi.RF_RECURSIVE | GuiServerApi.RF_SKIPHOTFOLDER_TIMSTAMPDIR;
                    if (dlg.isCompressed())
                        rflags |= GuiServerApi.RF_COMPRESSION;
                    if (dlg.isEncrypted())
                        rflags |= GuiServerApi.RF_ENCRYPTION;


                    boolean rret = main.getGuiServerApi().restoreJob(searchWrapper, job, ip, port, path, rflags, main.getUser());
                    if (!rret)
                    {
                        main.Msg().errmOk(VSMCMain.Txt("Der_Restore_schlug_fehl"));
                    }
                    else
                    {
                        main.Msg().info(VSMCMain.Txt("Der_Restore_wurde_gestartet"), null);
                    }
                }
                catch (Exception ex)
                {
                    main.Msg().errmOk(VSMCMain.Txt("Der_Restore_wurde_abgebrochen"));
                }
            }

        };

        main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_diesen_Auftrag_restaurieren?"), ok, null);


    }
    String getClientAddressPath(  RemoteFSElemTreeElem elem )
    {
        StringBuilder sb = new StringBuilder();

        sb.insert(0, elem.getName());
        while (elem.getParent() != null)
        {

            sb.insert(0, '/');
            if (elem.getParent().getName().equals("/") && elem.getParent().getParent() == null)
                break;

            sb.insert( 0, elem.getParent().getName() );
            elem = elem.getParent();
        }
        return sb.toString();
    }
    boolean isHotfolderPath(String fp)
    {
        return fp.startsWith("/"  + HotFolder.HOTFOLDERBASE);
    }
    boolean isHotfolderPath(RemoteFSElemTreeElem elem)
    {
        String fp = getClientAddressPath(elem);
        return fp.startsWith("/"  + HotFolder.HOTFOLDERBASE);
    }

    private String getIpFromPath( RemoteFSElemTreeElem elem )
    {
        String fp = getClientAddressPath(elem);
        String[] pathArr = fp.split("/");


        if (pathArr.length < 2)
            return null;


        if (isHotfolderPath(fp))
            return pathArr[2];

        // 0 IS ROOT
        return pathArr[1];
    }

    private int getPortFromPath( RemoteFSElemTreeElem elem )
    {
        String fp = getClientAddressPath(elem);
        String[] pathArr = fp.split("/");


        if (pathArr.length < 3)
            return 0;

        if (isHotfolderPath(fp))
            return Integer.parseInt(pathArr[3]);

        return Integer.parseInt(pathArr[2]);
    }

    private String getTargetpathFromPath( RemoteFSElemTreeElem elem, boolean isWindows )
    {
        String fp = getClientAddressPath(elem);

        if (isHotfolderPath(fp))
            return null;

        String[] pathArr = fp.split("/");


        if (pathArr.length < 4)
            return null;

        StringBuilder sb = new StringBuilder();

        // THE FIRST ENTRTIES ARE ROOT/IP/PORT, THE LAST ENTRY IS OURSELVES
        for (int i = 3; i < pathArr.length - 1; i++)
        {
            // RECREATE WINDOWS DRIVE
            if (i == 3 && isWindows)
            {
                if (pathArr[i].length() == 1
                        && pathArr[i].toLowerCase().charAt(0) >= 'a'
                        && pathArr[i].toLowerCase().charAt(0) <= 'z')
                {
                    sb.append( pathArr[i] );
                    sb.append( ":" );
                    continue;
                }
            }
            sb.append("/");
            sb.append( pathArr[i] );
        }
        return sb.toString();
    }



}


