/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

//
//import com.vaadin.data.Property.ValueChangeEvent;
//import com.vaadin.data.Property.ValueChangeListener;
//import com.vaadin.event.ItemClickEvent;
//import com.vaadin.event.ItemClickEvent.ItemClickListener;
//import com.vaadin.event.ShortcutAction.KeyCode;
//import com.vaadin.event.ShortcutListener;
//import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
//import com.vaadin.ui.Alignment;
//import com.vaadin.ui.Button;
//import com.vaadin.ui.Button.ClickEvent;
//import com.vaadin.ui.CheckBox;
//import com.vaadin.ui.ComboBox;
//import com.vaadin.ui.Component;
//import com.vaadin.ui.DateField;
//import com.vaadin.ui.HorizontalLayout;
//import com.vaadin.ui.NativeButton;
//import com.vaadin.ui.NativeSelect;
//import com.vaadin.ui.Table;
//import com.vaadin.ui.TextField;
//import com.vaadin.ui.TreeTable;
//import com.vaadin.ui.VerticalLayout;
//import de.dimm.vsm.net.RemoteFSElem;
//import de.dimm.vsm.net.SearchEntry;
//import de.dimm.vsm.net.SearchWrapper;
//import de.dimm.vsm.net.StoragePoolWrapper;
//import de.dimm.vsm.net.interfaces.AgentApi;
//import de.dimm.vsm.net.interfaces.GuiServerApi;
//import de.dimm.vsm.records.FileSystemElemNode;
//import de.dimm.vsm.records.HotFolder;
//import de.dimm.vsm.records.StoragePool;
//import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
//import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTree;
//import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTreeColumn;
//import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTreeContainer;
//import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteFSElemTreeElem;
//import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteProvider;
//import de.dimm.vsm.vaadin.GuiElems.Dialogs.FileinfoWindow;
//import de.dimm.vsm.vaadin.GuiElems.Dialogs.MountLocationDlg;
//import de.dimm.vsm.vaadin.GuiElems.Dialogs.RestoreLocationDlg;
//import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteItemDescriptionGenerator;
//import de.dimm.vsm.vaadin.SelectObjectCallback;
//import de.dimm.vsm.vaadin.VSMCMain;
//import de.dimm.vsm.vaadin.net.DownloadResource;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Properties;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.vaadin.peter.contextmenu.ContextMenu;
//import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

///**
// *
// * @author Administrator
// */
//public class SearchWin extends SidebarPanel
//{

//
//
//    boolean mountedView = false;
//    boolean mountedVol = false;
//    SearchWrapper searchWrapper = null;
//    StoragePoolWrapper mountWrapper = null;
//    HorizontalLayout treePanel;
//    FSTreeContainer container;
//    TreeTable tree;
//    final TextField txt_search_name = new TextField(VSMCMain.Txt("Name"));
//    final ComboBox cb_type;
//    final CheckBox cb_is_dir = new CheckBox(VSMCMain.Txt("Verzeichnis"));
//
//    final DateField dt_from = new DateField(VSMCMain.Txt("Von"));
//    final DateField dt_till = new DateField(VSMCMain.Txt("Bis"));
//    final TextField txt_status = new TextField(VSMCMain.Txt("Status"));
//    final Button btViewVol = new NativeButton(VSMCMain.Txt("View file system"));
//    final String mntText = VSMCMain.Txt(VSMCMain.Txt("Mount_Volume"));
//    final String umntText = VSMCMain.Txt(VSMCMain.Txt("Unmount_Volume"));
//
//    final Button btVol = new NativeButton(mntText);
//
//    NativeSelect poolSelector;
//    int lastPoolCnt = -1;
//
//    String[] typ = {SearchEntry.OP_EQUAL, SearchEntry.OP_BEGINS, SearchEntry.OP_CONTAINS, SearchEntry.OP_ENDS };
//    String[] niceTyp = {VSMCMain.Txt("enthält Begriff"),VSMCMain.Txt("beginnt_mit"), VSMCMain.Txt("enthält"),  VSMCMain.Txt("endet_mit") };
//
//    public SearchWin( VSMCMain _main )
//    {
//        super(_main);
//
//        this.setStyleName("statusWin");
//        this.setSizeFull();
//
//        VerticalLayout mainLayout = new VerticalLayout();
//
//        mainLayout.setSizeFull();
//
//
//        this.addComponent(mainLayout);
//
//        poolSelector = new NativeSelect(VSMCMain.Txt("Pool"));
//        poolSelector.setNewItemsAllowed(false);
//        poolSelector.setInvalidAllowed(false);
//        poolSelector.setNullSelectionAllowed(false);
//        poolSelector.setImmediate(true);
//        HorizontalLayout pshl = new HorizontalLayout();
//        pshl.setMargin(false, false, true, false);
//        pshl.addComponent(poolSelector);
//        pshl.setComponentAlignment(poolSelector, Alignment.MIDDLE_LEFT);
//
//        mainLayout.addComponent(pshl);
//
//        txt_status.setReadOnly(true);
//
//        List<ComboEntry> entries = new ArrayList<ComboEntry>();
//        for (int i = 0; i < niceTyp.length; i++)
//        {
//            entries.add( new ComboEntry( typ[i],  niceTyp[i]) );
//        }
//
//        cb_type = new ComboBox("Suche", entries);
//        cb_type.setNullSelectionAllowed(false);
//        cb_type.select(entries.get(0)); // EQUALS
//
//        txt_search_name.setImmediate(true);
//        txt_search_name.addShortcutListener( new ShortcutListener("enter", KeyCode.ENTER, null)
//        {
//            @Override
//            public void handleAction( Object sender, Object target )
//            {
//                startSearch();
//            }
//        });
//
//
//        final Button btUpdateReadIndex = new NativeButton(VSMCMain.Txt("Index aktualisieren"));
//        btUpdateReadIndex.addListener(new Button.ClickListener()
//        {
//            @Override
//            public void buttonClick( ClickEvent event )
//            {
//                updateReadIndex();
//            }
//
//
//        });
//
//        final Button btStartSearch = new NativeButton(VSMCMain.Txt("Suche starten"));
//        btStartSearch.addListener(new Button.ClickListener()
//        {
//            @Override
//            public void buttonClick( ClickEvent event )
//            {
//                startSearch();
//            }
//        });
//
//        VerticalLayout vl = new VerticalLayout();
//        mainLayout.addComponent(vl);
//
//        vl.setSpacing(true);
//
//        HorizontalLayout hl = new HorizontalLayout();
//        hl.setSpacing(true);
//        hl.addComponent(txt_search_name);
//        hl.addComponent(cb_type);
//        hl.addComponent(cb_is_dir);
//        hl.setComponentAlignment(cb_is_dir, Alignment.BOTTOM_RIGHT);
//        vl.addComponent(hl);
//
//
//        hl = new HorizontalLayout();
//        hl.setSpacing(true);
//        hl.addComponent(dt_from);
//        hl.addComponent(dt_till);
//        vl.addComponent(hl);
//
//        hl = new HorizontalLayout();
//        hl.setSpacing(true);
//        hl.addComponent(btUpdateReadIndex);
//        hl.addComponent(btStartSearch);
//        hl.setComponentAlignment(btStartSearch, Alignment.BOTTOM_RIGHT);
//
//
//        btViewVol.addListener( new Button.ClickListener()
//        {
//            @Override
//            public void buttonClick( com.vaadin.ui.Button.ClickEvent event )
//            {
//                if (mountedView)
//                    hideFS();
//                else
//                    showFs();
//            }
//        });
//
//        btVol.addListener( new Button.ClickListener()
//        {
//            @Override
//            public void buttonClick( ClickEvent event )
//            {
//                startMount();
//            }
//        });
//
//        hl = new HorizontalLayout();
//        hl.setSpacing(true);
//        hl.addComponent(btViewVol);
//        hl.addComponent(btVol);
//
//
//
//        mainLayout.addComponent(hl);
//
//
//        treePanel = new HorizontalLayout();
//        // reserve excess space for the "treecolumn"
//        treePanel.setSizeFull();
//
//        mainLayout.addComponent(treePanel);
//
//    }
//
//
//    void closeArchivResults()
//    {
//        unmountVol();
//
//        if (searchWrapper != null)
//        {
//            main.getGuiServerApi().closeSearch(searchWrapper);
//            searchWrapper = null;
//        }
//
//        hideFS();
//    }
//
//    @Override
//    public void activate()
//    {
//
//        List<StoragePool> list = main.getStoragePoolList();
//        if (lastPoolCnt == list.size() || list.isEmpty())
//            return;
//
//
//        //backupJobResultContainer.removeAllComponents();
//        //jobTableContainer = new VerticalLayout();
//
//        //backupJobResultContainer.addComponent(poolSelector);
//        //backupJobResultContainer.addComponent(jobTableContainer);
//
//        poolSelector.removeAllItems();
//
//        for (int i = 0; i < list.size(); i++)
//        {
//            Object object = list.get(i);
//            poolSelector.addItem(object);
//        }
//        if (!list.isEmpty())
//        {
//            poolSelector.setValue(list.get(0));
//
//        }
//        poolSelector.addListener( new ValueChangeListener()
//        {
//
//            @Override
//            public void valueChange( ValueChangeEvent event )
//            {
//                closeArchivResults();
//            }
//        });
//
//
//        lastPoolCnt = list.size();
//
//
//        txt_search_name.focus();
//    }
//
//
//    @Override
//    public void deactivate()
//    {
//        unmountVol();
//
//        closeArchivResults();
//    }
//
//
//
//
//    void updateReadIndex()
//    {
//        SelectObjectCallback cb = new SelectObjectCallback<StoragePool>()
//        {
//            @Override
//            public void SelectedAction( StoragePool pool )
//            {
//                main.getGuiServerApi().updateReadIndex(pool);
//                VSMCMain.notify(txt_search_name,  VSMCMain.Txt("Der Index ist jetzt auf dem neuesten Stand"), "");
//            }
//        };
//
//        List<StoragePool> list = main.getStoragePoolList();
//        main.SelectObject(StoragePool.class, VSMCMain.Txt("Pool"), VSMCMain.Txt("Weiter"), list, cb);
//    }
//
//    void startSearch()
//    {
//        SelectObjectCallback cb = new SelectObjectCallback<StoragePool>()
//        {
//
//            @Override
//            public void SelectedAction( StoragePool pool )
//            {
//                ArrayList<SearchEntry> slist = new ArrayList<SearchEntry>();
//
//                boolean ci = true; // LUCENE ALWAYS CI
//
//                ComboEntry cb = (ComboEntry) cb_type.getValue();
//                if (!txt_search_name.getValue().toString().isEmpty())
//                    slist.add( new SearchEntry(txt_search_name.getValue().toString(), SearchEntry.ARG_NAME,  cb.getDbEntry().toString(),  false, false, ci, null) );
//
//                if (dt_from.getValue() != null)
//                {
//                    Date d = (Date)dt_from.getValue();
//
//                    slist.add( new SearchEntry(Long.toString(d.getTime()), SearchEntry.ARG_MDATE,  SearchEntry.OP_GE,  false, false, ci, null) );
//                }
//                if (dt_till.getValue() != null)
//                {
//                    Date d = (Date)dt_till.getValue();
//
//                    slist.add( new SearchEntry(Long.toString(d.getTime()), SearchEntry.ARG_MDATE,  SearchEntry.OP_LE,  false, false, ci, null) );
//                }
//                if (cb_is_dir.booleanValue())
//                {
//                    slist.add( new SearchEntry(FileSystemElemNode.FT_DIR, SearchEntry.ARG_TYP,  SearchEntry.OP_EQUAL,  false, false, ci, null) );
//                }
//                else
//                {
//                    slist.add( new SearchEntry(FileSystemElemNode.FT_FILE, SearchEntry.ARG_TYP,  SearchEntry.OP_EQUAL,  false, false, ci, null) );
//                }
//
//
//                if (mountedView)
//                {
//                    hideFS();
//                }
//
//                if (searchWrapper != null)
//                {
//                    main.getGuiServerApi().closeSearch(searchWrapper);
//                }
//
//                searchWrapper = main.getGuiServerApi().search( pool, slist );
//
//                showFs();
//            }
//        };
//
//        StoragePool p =(StoragePool) poolSelector.getValue();
//        cb.SelectedAction(p);
////        main.SelectObject(StoragePool.class, VSMCMain.Txt("Pool"), VSMCMain.Txt("Weiter"), list, cb);
//    }
//
//    void startMount()
//    {
//        if (mountedVol)
//        {
//            unmountVol();
//            return;
//        }
//        final MountLocationDlg dlg = VSMCMain.mountDlg;
//        dlg.setIP( main.getHost() );
//
//        dlg.setOkListener( new Button.ClickListener()
//        {
//
//            @Override
//            public void buttonClick( ClickEvent event )
//            {
//                if (searchWrapper == null)
//                {
//                     main.Msg().errmOk(VSMCMain.Txt("Bitte starten Sie zuerst eine Suche"));
//                     return;
//                }
//                mountVol( dlg);
//            }
//        });
//        treePanel.getApplication().getMainWindow().addWindow( dlg );
//    }
//
//    void mountVol( MountLocationDlg loc )
//    {
//        String ip = loc.getIP();
//        int port = loc.getPort();
//        String drive = loc.getPath();
//        Date timestamp = loc.getDate();
//
//        mountWrapper = main.getGuiServerApi().getMounted(ip, port, searchWrapper);
//
//        if (!mountedVol)
//        {
//            if (mountWrapper != null)
//            {
//                 main.getGuiServerApi().unmountVolume(mountWrapper);
//            }
//            mountWrapper = main.getGuiServerApi().mountVolume(ip, port, searchWrapper,  null, drive);
//
//            btVol.setCaption(umntText);
//            mountedVol = true;
//        }
//    }
//
//    void unmountVol()
//    {
//        if (mountWrapper != null)
//        {
//            main.getGuiServerApi().unmountVolume(mountWrapper);
//            mountWrapper = null;
//        }
//        btVol.setCaption(mntText);
//        mountedVol = false;
//    }
//
//    void hideFS()
//    {
//        btViewVol.setCaption(VSMCMain.Txt("View file system"));
//
//        mountedView = false;
//
//        treePanel.removeAllComponents();
//    }
//
//    void showFs()
//    {
//        List<RemoteFSElem> ret = main.getGuiServerApi().getSearchResult(searchWrapper, 0, 100);
//
//        btViewVol.setCaption(VSMCMain.Txt("Unmount view"));
//        mountedView = true;
//
//        Component _tree = initFsTree(ret);
//        _tree.setSizeFull();
//        treePanel.addComponent(_tree);
//    }
//
//
//    Component initFsTree( List<RemoteFSElem> root_list )
//    {
//        ArrayList<FSTreeColumn> fields = new ArrayList<FSTreeColumn>();
//        fields.add(new FSTreeColumn("name", VSMCMain.Txt("Name"), -1, 1.0f, Table.ALIGN_LEFT, String.class));
//        fields.add(new FSTreeColumn("date", VSMCMain.Txt("Datum"), 100, -1, Table.ALIGN_LEFT, String.class));
//        fields.add(new FSTreeColumn("size", VSMCMain.Txt("Größe"), 80, -1, Table.ALIGN_RIGHT, String.class));
//        fields.add(new FSTreeColumn("atttribute", VSMCMain.Txt("Attribute"), 80, -1, Table.ALIGN_LEFT, String.class));
//
//
//        RemoteProvider provider = new RemoteProvider()
//        {
//
//            @Override
//            public RemoteFSElemTreeElem createNode( RemoteProvider provider, RemoteFSElem elem, RemoteFSElemTreeElem parent )
//            {
//                return new RemoteFSElemTreeElem(provider, elem, parent);
//            }
//
//            @Override
//            public List<RemoteFSElemTreeElem> getChildren( RemoteFSElemTreeElem elem )
//            {
//                List<RemoteFSElemTreeElem> childList = new ArrayList<RemoteFSElemTreeElem>();
//                try
//                {
//
//                    List<RemoteFSElem> elem_list = main.getGuiServerApi().listSearchDir(searchWrapper, elem.getElem());
//                    for (int i = 0; i < elem_list.size(); i++)
//                    {
//                        RemoteFSElem rfse = elem_list.get(i);
//                        RemoteFSElemTreeElem e = new RemoteFSElemTreeElem(this, rfse, elem);
//                        childList.add(e);
//                    }
//
//                }
//                catch (Exception ex)
//                {
//                    Logger.getLogger(SearchWin.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                return childList;
//            }
//
//            @Override
//            public boolean createDir( RemoteFSElemTreeElem elem )
//            {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//
//            @Override
//            public ItemDescriptionGenerator getItemDescriptionGenerator()
//            {
//                return new RemoteItemDescriptionGenerator(searchWrapper, main );
//            }
//
//        };
//
//        container = new FSTreeContainer(provider, fields, main.getUser());
//        if (root_list != null)
//            container.initRootlist(root_list);
//
//        tree = new FSTree(fields, /*sort*/ true);
//        tree.setContainerDataSource(container);
//
//        tree.setItemDescriptionGenerator(provider.getItemDescriptionGenerator());
//
//
//        tree.addListener(new ItemClickListener()
//        {
//
//            // HANDLE
//            @Override
//            public void itemClick( ItemClickEvent event )
//            {
//                if (event.getItemId() instanceof RemoteFSElemTreeElem
//                        && (event.getButton() & com.vaadin.event.MouseEvents.ClickEvent.BUTTON_RIGHT) == com.vaadin.event.MouseEvents.ClickEvent.BUTTON_RIGHT)
//                {
//                    RemoteFSElemTreeElem rfstreeelem = (RemoteFSElemTreeElem) event.getItemId();
//                    create_fs_popup(event, rfstreeelem);
//                }
//            }
//        });
//
//
//        return tree;
//    }
//    ContextMenu lastMenu = null;
//
//    void create_fs_popup( ItemClickEvent event, final RemoteFSElemTreeElem rfstreeelem )
//    {
//        ContextMenu menu = new ContextMenu();
//
//        // Generate main level items
//        final ContextMenuItem info = menu.addItem(VSMCMain.Txt("Information"));
//
//        // SEPARATOR BENEATH THIS ONE
//        info.setSeparatorVisible(true);
//
//        //final ContextMenuItem ver = menu.addItem(VSMCMain.Txt("Versions"));
//        final ContextMenuItem restore = menu.addItem(VSMCMain.Txt("Restore"));
//        final ContextMenuItem download = menu.addItem(VSMCMain.Txt("Download"));
//
//        // Generate sub item to photos menu
//        //ContextMenuItem topRated = ver.addItem("Letzte Woche (Todo...)");
//
//        //photos.setIcon(new FileResource(new File("images/dir.png"), event.getComponent().getApplication()));
//
//        // Enable separator line under this item
//        //ver.setSeparatorVisible(true);
//
//        // Show notification when menu items are clicked
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
//
//    private void handleDownload( final RemoteFSElemTreeElem rfstreeelem)
//    {
//        RemoteFSElem fs = rfstreeelem.getElem();
//
//        InputStream is = main.getGuiServerApi().openStream(searchWrapper, fs);
//
//        DownloadResource downloadResource = new DownloadResource(is, fs.getName(), btVol.getApplication());
//
//        getWindow().open(downloadResource);
//    }
//
//
//    private void handleRestoreTargetDialog( final RemoteFSElemTreeElem rfstreeelem)
//    {
//        final RestoreLocationDlg dlg = new RestoreLocationDlg(main, main.getIp(), 8082, "",  /*allowOriginal*/false );
//        Button.ClickListener okListener = new Button.ClickListener()
//        {
//            @Override
//            public void buttonClick( ClickEvent event )
//            {
//                handleRestoreOkayDialog( dlg, rfstreeelem);
//            }
//        };
//        dlg.setOkListener( okListener );
//        treePanel.getApplication().getMainWindow().addWindow( dlg );
//    }
//
//    private void handleRestoreOkayDialog( final RestoreLocationDlg dlg, final RemoteFSElemTreeElem rfstreeelem)
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
//
//                                        if (dlg.isCompressed())
//                        rflags |= GuiServerApi.RF_COMPRESSION;
//                    if (dlg.isEncrypted())
//                        rflags |= GuiServerApi.RF_ENCRYPTION;
//
//                    boolean rret = main.getGuiServerApi().restoreFSElem(searchWrapper, rfstreeelem.getElem(), ip, port, path, rflags, main.getUser());
//                    if (!rret)
//                    {
//                        main.Msg().errmOk(VSMCMain.Txt("Der_Restore_schlug_fehl"));
//                    }
//                    else
//                    {
//                        main.Msg().info(VSMCMain.Txt("Der_Restore_wurde_gestartet"), null);
//                    }
//                }
//                catch (Exception ex)
//                {
//                    main.Msg().errmOk(VSMCMain.Txt("Der_Restore_wurde_abgebrochen"));
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
//    String getClientAddressPath(  RemoteFSElemTreeElem elem )
//    {
//        StringBuilder sb = new StringBuilder();
//
//        sb.insert(0, elem.getName());
//        while (elem.getParent() != null)
//        {
//
//            sb.insert(0, '/');
//            if (elem.getParent().getName().equals("/") && elem.getParent().getParent() == null)
//                break;
//
//            sb.insert( 0, elem.getParent().getName() );
//            elem = elem.getParent();
//        }
//        return sb.toString();
//    }
//    boolean isHotfolderPath(String fp)
//    {
//        return fp.startsWith("/"  + HotFolder.HOTFOLDERBASE);
//    }
//    boolean isHotfolderPath(RemoteFSElemTreeElem elem)
//    {
//        String fp = getClientAddressPath(elem);
//        return fp.startsWith("/"  + HotFolder.HOTFOLDERBASE);
//    }
//
//    private String getIpFromPath( RemoteFSElemTreeElem elem )
//    {
//        String fp = getClientAddressPath(elem);
//        String[] pathArr = fp.split("/");
//
//
//        if (pathArr.length < 2)
//            return null;
//
//
//        if (isHotfolderPath(fp))
//            return pathArr[2];
//
//        // 0 IS ROOT
//        return pathArr[1];
//    }
//
//    private int getPortFromPath( RemoteFSElemTreeElem elem )
//    {
//        String fp = getClientAddressPath(elem);
//        String[] pathArr = fp.split("/");
//
//
//        if (pathArr.length < 3)
//            return 0;
//
//        if (isHotfolderPath(fp))
//            return Integer.parseInt(pathArr[3]);
//
//        return Integer.parseInt(pathArr[2]);
//    }
//
//    private String getTargetpathFromPath( RemoteFSElemTreeElem elem, boolean isWindows )
//    {
//        String fp = getClientAddressPath(elem);
//
//        if (isHotfolderPath(fp))
//            return null;
//
//        String[] pathArr = fp.split("/");
//
//
//        if (pathArr.length < 4)
//            return null;
//
//        StringBuilder sb = new StringBuilder();
//
//        // THE FIRST ENTRTIES ARE ROOT/IP/PORT, THE LAST ENTRY IS OURSELVES
//        for (int i = 3; i < pathArr.length - 1; i++)
//        {
//            // RECREATE WINDOWS DRIVE
//            if (i == 3 && isWindows)
//            {
//                if (pathArr[i].length() == 1
//                        && pathArr[i].toLowerCase().charAt(0) >= 'a'
//                        && pathArr[i].toLowerCase().charAt(0) <= 'z')
//                {
//                    sb.append( pathArr[i] );
//                    sb.append( ":" );
//                    continue;
//                }
//            }
//            sb.append("/");
//            sb.append( pathArr[i] );
//        }
//        return sb.toString();
//    }
//}
//
//
