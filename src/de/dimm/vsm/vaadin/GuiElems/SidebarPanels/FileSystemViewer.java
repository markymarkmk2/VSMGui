/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import com.vaadin.Application;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.IContextMenuCallback;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.net.StoragePoolWrapper;
import de.dimm.vsm.net.interfaces.AgentApi;
import de.dimm.vsm.net.interfaces.GuiServerApi;
import de.dimm.vsm.net.interfaces.IWrapper;
import de.dimm.vsm.records.FileSystemElemNode;
import de.dimm.vsm.records.HotFolder;
import de.dimm.vsm.records.MountEntry;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.FileinfoWindow;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.RestoreLocationDlg;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTree;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTreeColumn;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTreeContainer;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteFSElemTreeElem;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteProvider;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteItemDescriptionGenerator;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.MountEntryTable;
import de.dimm.vsm.vaadin.SelectObjectCallback;
import de.dimm.vsm.vaadin.VSMCMain;
import de.dimm.vsm.vaadin.net.DownloadResource;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

/**
 *
 * @author Administrator
 */
public class FileSystemViewer extends SidebarPanel
{

  
    boolean mounted = false;
    boolean volMounted = false;
    StoragePoolWrapper viewWrapper = null;
    HorizontalLayout treePanel;
    FSTreeContainer container;
    TreeTable tree;
    final TextField txt_agent_ip = new TextField("IP");
    final TextField txt_agent_port = new TextField("Port");
    StoragePoolWrapper mountWrapper;
    CheckBox cbDeleted;
    Button btViewVol;
    Button btViewVolLive;


    public FileSystemViewer( VSMCMain _main )
    {

        super(_main);

        this.setStyleName("statusWin");
        this.setSizeFull();
//        AbsoluteLayout al = new AbsoluteLayout();
//        al.setSizeFull();
//        this.addComponent(al);
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        this.addComponent(vl);
        vl.setSpacing(true);
        Component c = createHotfolderPanel();

        vl.addComponent(c);

        final TextField txt_mnt_drive = new TextField("MountDrive");
        final DateField dta_ts = new DateField("Timestamp", new Date());

        txt_agent_ip.setValue("localhost");
        txt_agent_port.setValue("8082");
        txt_mnt_drive.setValue("R:");
        final Button btVol = new NativeButton("Mount Volume");

        btVol.addListener(new Button.ClickListener()
        {

            @Override
            public void buttonClick( ClickEvent event )
            {
                SelectObjectCallback cb = new SelectObjectCallback<StoragePool>()
                {

                    @Override
                    public void SelectedAction( StoragePool pool )
                    {
                        String ip = txt_agent_ip.getValue().toString();
                        int port = Integer.parseInt(txt_agent_port.getValue().toString());
                        String drive = txt_mnt_drive.getValue().toString();
                        Date timestamp = (Date) dta_ts.getValue();


                        mountWrapper = main.getGuiServerApi().getMounted(ip, port, pool);

                        if (!volMounted)
                        {
                            Properties p = null;

                            try
                            {
                                p = main.getGuiServerApi().getAgentProperties(ip, port, false);
                            }
                            catch (Exception e)
                            {
                            }
                            if (p == null)
                            {
                                main.Msg().errmOk(VSMCMain.Txt("Der Agent ist nicht erreichbar"));
                                return;
                            }

                            if (mountWrapper != null && !mountWrapper.isPhysicallyMounted())
                            {
                                main.getGuiServerApi().remountVolume(mountWrapper);
                                volMounted = true;
                            }
                            else
                            {
                                mountWrapper = main.getGuiServerApi().mountVolume(ip, port, pool, timestamp, "", main.getGuiWrapper().getUser(), drive);
                                if (mountWrapper == null)
                                {
                                    main.Msg().errmOk(VSMCMain.Txt("Der Mount schlug fehl"));
                                    return;
                                }
                            }

                            btVol.setCaption("Unmount Volume");
                            volMounted = true;

                        }
                        else
                        {
                            if (mountWrapper != null)
                            {
                                main.getGuiServerApi().unmountVolume(mountWrapper);
                                mountWrapper = null;
                            }

                            btVol.setCaption("Mount Volume");
                            volMounted = false;
                        }
                    }
                };

                List<StoragePool> list = main.getStoragePoolList();
                main.SelectObject(StoragePool.class, VSMCMain.Txt("Pool"), VSMCMain.Txt("Weiter"), list, cb);
            }
        });
        
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.addComponent(btVol);
        hl.addComponent(dta_ts);
        hl.addComponent(txt_agent_ip);
        hl.addComponent(txt_agent_port);
        hl.addComponent(txt_mnt_drive);



        final DateField dta_view_ts = new DateField("Timestamp", new Date());

        btViewVol = new NativeButton("View file system");
        HorizontalLayout hl2 = new HorizontalLayout();
        btViewVolLive = new NativeButton("Open live file system");
        cbDeleted = new CheckBox("Gelöschte Dateien sichtbar");
        hl2.addComponent(btViewVolLive);
        hl2.addComponent(cbDeleted);
        hl2.setSpacing(true);
        

        Button.ClickListener listener = new Button.ClickListener()
        {

            @Override
            public void buttonClick( com.vaadin.ui.Button.ClickEvent event )
            {
                final Button clickedButton = event.getButton();

                if (mounted)
                {
                    if (viewWrapper != null)
                    {
                        main.getGuiServerApi().closePoolView(viewWrapper);
                        viewWrapper = null;
                    }
                    btViewVol.setCaption("View file system");
                    btViewVolLive.setVisible(true);
                    cbDeleted.setVisible(true);
                    dta_view_ts.setVisible(true);
                    mounted = false;

                    treePanel.removeAllComponents();
                }
                else
                {
                    SelectObjectCallback cb = new SelectObjectCallback<StoragePool>()
                    {

                        @Override
                        public void SelectedAction( StoragePool pool )
                        {

                            if (viewWrapper != null)
                            {
                                main.getGuiServerApi().closePoolView(viewWrapper);
                                viewWrapper = null;
                            }

                            if (clickedButton == btViewVol)
                            {

                                Date timestamp = (Date) dta_view_ts.getValue();
                                viewWrapper = main.getGuiServerApi().openPoolView(pool, timestamp, "", main.getGuiWrapper().getUser());
                            }
                            if (clickedButton == btViewVolLive)
                            {                                
                                viewWrapper = main.getGuiServerApi().openPoolView(pool, /*rdonly*/ false, cbDeleted.booleanValue(), "", main.getGuiWrapper().getUser());
                            }


                            btViewVol.setCaption("Unmount view");
                            btViewVolLive.setVisible(false);
                            cbDeleted.setVisible(false);
                            dta_view_ts.setVisible(false);

                            mounted = true;

                            Component tree = initFsTree(viewWrapper);
                            tree.setSizeFull();
                            treePanel.addComponent(tree);
                        }
                    };
                    List<StoragePool> list = main.getStoragePoolList();
                    main.SelectObject(StoragePool.class, VSMCMain.Txt("Pool"), VSMCMain.Txt("Weiter"), list, cb);
                }
            }
        };

        btViewVol.addListener(listener);
        btViewVolLive.addListener(listener);
        HorizontalLayout hl3 = new HorizontalLayout();
        hl3.setSpacing(true);

        hl3.addComponent(btViewVol);
        hl3.addComponent(dta_view_ts);

        vl.addComponent(hl);
        vl.addComponent(hl2);
        vl.addComponent(hl3);

        treePanel = new HorizontalLayout();
        // reserve excess space for the "treecolumn"
        treePanel.setSizeFull();

        vl.addComponent(treePanel);
    }
    
    void closePoolView()
    {
        if (mounted)
        {
            if (viewWrapper != null)
            {
                main.getGuiServerApi().closePoolView(viewWrapper);
                viewWrapper = null;
            }
            mounted = false;
            treePanel.removeAllComponents();
        }   
        if (volMounted)
        {
            if (mountWrapper != null)
            {
                main.getGuiServerApi().unmountVolume(mountWrapper);
                mountWrapper = null;
            }
        }
        btViewVolLive.setVisible(true);
        cbDeleted.setVisible(true);
        btViewVol.setVisible(true);
        btViewVol.setCaption("View file system");
    }

    @Override
    public void deactivate()
    {
        super.deactivate();

        closePoolView();
    }


    Component initFsTree( final StoragePoolWrapper wrapper )
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
                    
                    List<RemoteFSElem> elem_list = main.getGuiServerApi().listDir(wrapper, elem.getElem());
                    for (int i = 0; i < elem_list.size(); i++)
                    {
                        RemoteFSElem rfse = elem_list.get(i);
                        RemoteFSElemTreeElem e = new RemoteFSElemTreeElem(this, rfse, elem);
                        childList.add(e);
                    }

                }
                catch (Exception ex)
                {
                    Logger.getLogger(FileSystemViewer.class.getName()).log(Level.SEVERE, null, ex);
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
                return new RemoteItemDescriptionGenerator(wrapper, main );
            }
        };

        container = new FSTreeContainer(provider, fields);
        container.setSkipEmptyDirs(true);


//        if (!VSMCMain.Me(this).getGuiUser().getUser().getFsMapper().isEmpty())
//        {
//            container.initRootWithUserMapping( VSMCMain.Me(this).getGuiUser().getUser().getFsMapper() );
//        }
//        else
        {
            List<RemoteFSElem> poolRootList;
            RemoteFSElem slash =  new RemoteFSElem("/", FileSystemElemNode.FT_DIR, 0, 0, 0, 0, 0);
            try
            {
                poolRootList = main.getGuiServerApi().listDir(wrapper, slash);

                // LIST DIR GIVES RELPATH, WE NEED ABSOLUTE PATHS HERE
                for (int i = 0; i < poolRootList.size(); i++)
                {
                    RemoteFSElem remoteFSElem = poolRootList.get(i);
                    remoteFSElem.makeAbsolut( slash );
                }
            }
            catch (SQLException sQLException)
            {
                VSMCMain.notify(this, "Rootverzeichnis kann nicht gelesen werden", "");
                poolRootList = new ArrayList<RemoteFSElem>();
                poolRootList.add(slash);
            }
            container.initRootlist(poolRootList);
        }
        
        

        tree = new FSTree(fields, /*sort*/ false);
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
                    DownloadResource downloadResource = createDownloadResource( main, getApplication(), viewWrapper, rfstreeelem);
                    getWindow().open(downloadResource);
                }
            }
        });


        return tree;
    }

//    RemoteFSElemTreeElem buildValidUserRoot(RemoteProvider provider, StoragePoolWrapper wrapper, User user) throws SQLException
//    {
//        List<User.VsmFsEntry> vsmList = user.getFsMapper().getVsmList();
//
//        // FILTER OUT ALL EMPTY -> INVISIBLE ENTRIES
//        vsmList = getValidVsmEntryList( vsmList, wrapper );
//
//        MappingTreeElem rootTreeElem = new MappingTreeElem(vsmList, provider, new RemoteFSElem("/", FileSystemElemNode.FT_DIR, System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), 0, 0 ), null);
//
//        return rootTreeElem;
//    }
//
//    // DETECT ALL VSMFS ENTRIES WHICH GIVE VALID RESULTS FOR THIS USER
//    List<VsmFsEntry> getValidVsmEntryList( List<VsmFsEntry> vsmList, StoragePoolWrapper wrapper ) throws SQLException
//    {
//        RemoteFSElem root =  new RemoteFSElem("/", FileSystemElemNode.FT_DIR, System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), 0, 0 );
//        List<RemoteFSElem> rootChilds = main.getGuiServerApi().listDir(wrapper, root);
//
//        List<VsmFsEntry> validVsmList = new ArrayList<VsmFsEntry>();
//
//        for (int i = 0; i < rootChilds.size(); i++)
//        {
//            RemoteFSElem remoteFSElem = rootChilds.get(i);
//
//            for (int j = 0; j < vsmList.size(); j++)
//            {
//                VsmFsEntry vfse = vsmList.get(j);
//
//                String[] rootPath = vfse.getvPath().split("/");
//                if (rootPath == null || rootPath.length < 2)
//                    continue;
//                if (rootPath[1].equals(remoteFSElem.getName()))
//                {
//                    validVsmList.add(vfse);
//                }
//            }
//        }
//        return validVsmList;
//    }

    ContextMenu lastMenu = null;



    public static ContextMenu create_fs_popup( final VSMCMain main, final IWrapper wrapper, final TreeTable tree,
            final FSTreeContainer container, ItemClickEvent event, final List<RemoteFSElemTreeElem> rfstreeelems,
            final IContextMenuCallback callback )
    {
        ContextMenu menu = new ContextMenu();
        ContextMenuItem dl = null;
        ContextMenuItem _remove = null;
        ContextMenuItem _del = null;
        ContextMenuItem _info = null;


        
        boolean hasFile = false;
        boolean _hasDir = false;
        boolean oneSelected = false;
        for (int i = 0; i < rfstreeelems.size(); i++)
        {
            RemoteFSElemTreeElem remoteFSElemTreeElem = rfstreeelems.get(i);
            if ( remoteFSElemTreeElem.getElem().isFile())
                hasFile = true;
            if ( remoteFSElemTreeElem.getElem().isDirectory())
                _hasDir = true;
        }
        if (rfstreeelems.size() == 1)
        {
            oneSelected = true;
        }
        final boolean hasDir = _hasDir;


        // Generate main level items
        if (oneSelected)
        _info = menu.addItem(VSMCMain.Txt("Information"));
        final ContextMenuItem ver = menu.addItem(VSMCMain.Txt("Versions"));
        if (oneSelected && main.getGuiUser().isSuperUser() && !wrapper.isReadOnly())
        {
            _remove = menu.addItem( VSMCMain.Txt("Endgültig aus dem Dateisystem entfernen"));
            _del = menu.addItem(rfstreeelems.get(0).getElem().isDeleted() ? VSMCMain.Txt("Undelete") : VSMCMain.Txt("Delete"));
        }

        final ContextMenuItem restore = menu.addItem(VSMCMain.Txt("Restore"));
        if (oneSelected && hasFile)
            dl = menu.addItem(VSMCMain.Txt("Download"));


        final ContextMenuItem download = dl;
        final ContextMenuItem del = _del;
        final ContextMenuItem remove = _remove;
        final ContextMenuItem info = _info;

        // Enable separator line under this item
        ver.setSeparatorVisible(true);

        // Show notification when menu items are clicked
        menu.addListener(new ContextMenu.ClickListener()
        {

            @Override
            public void contextItemClick( ContextMenu.ClickEvent event )
            {
                // Get reference to clicked item

                // INFO, DEL AND REMOVE WORK ONLY WITH SINGLE SELECTION
                final RemoteFSElemTreeElem singleRfstreeelem = rfstreeelems.get(0);
                ContextMenuItem clickedItem = event.getClickedItem();
                if (clickedItem == info)
                {
                    FileinfoWindow win = new FileinfoWindow(main, wrapper, singleRfstreeelem.getElem());

                    // Do something with the reference
                    event.getComponent().getApplication().getMainWindow().addWindow(win);
                }
                if (clickedItem == del && main.getGuiUser().isSuperUser())
                {
                    Button.ClickListener ok = new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( ClickEvent event )
                        {
                            try
                            {
                                if (rfstreeelems.get(0).getElem().isDeleted())
                                {
                                    main.getGuiServerApi().undeleteFSElem(wrapper, singleRfstreeelem.getElem());
                                }
                                else
                                {
                                    main.getGuiServerApi().deleteFSElem(wrapper, singleRfstreeelem.getElem());
                                }

                                tree.setCollapsed(singleRfstreeelem.getParent(), true);
                                tree.setCollapsed(singleRfstreeelem.getParent(), false);
                                tree.requestRepaint();

                            }
                            catch (Exception ex)
                            {
                                main.Msg().errmOk(VSMCMain.Txt("Der_Löscheintrag_kann_nicht_verändert_werden"));
                            }
                        }
                    };
                    if (rfstreeelems.get(0).getElem().isDirectory())
                    {
                        if (rfstreeelems.get(0).getElem().isDeleted())
                            main.Msg().infoOkCancel(VSMCMain.Txt("Wollen_Sie_dieses_Verzeichnis_wiederherstellen?"), ok, null);
                        else
                            main.Msg().infoOkCancel(VSMCMain.Txt("Wollen_Sie_dieses_Verzeichnis_als_gelöscht_markieren?"), ok, null);
                    }
                    else
                    {
                        if (rfstreeelems.get(0).getElem().isDeleted())
                            main.Msg().infoOkCancel(VSMCMain.Txt("Wollen_Sie_diese_Datei_wiederherstellen?"), ok, null);
                        else
                            main.Msg().infoOkCancel(VSMCMain.Txt("Wollen_Sie_diese_Datei_als_gelöscht_markieren?"), ok, null);
                    }
                }
                if (clickedItem == remove && main.getGuiUser().isSuperUser())
                {
                    Button.ClickListener ok = new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( ClickEvent event )
                        {

                            try
                            {
                                main.getGuiServerApi().removeFSElem(wrapper, singleRfstreeelem.getElem());
                                container.removeItem(singleRfstreeelem);
                                
                                tree.setCollapsed(singleRfstreeelem.getParent(), true);
                                tree.setCollapsed(singleRfstreeelem.getParent(), false);
                                tree.requestRepaint();

                            }
                            catch (Exception ex)
                            {
                                main.Msg().errmOk(VSMCMain.Txt("Eintrag_konnte_nicht_entfernt_werden"));
                            }
                        }
                    };
                    String caption = VSMCMain.Txt("Achtung");
                    if (singleRfstreeelem.getElem().isDirectory())
                    {
                        main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_dieses_Verzeichnis_und_alle_darin_enthaltenen_Dateien_endgültig_entfernen?"), caption, ok, null);
                    }
                    else
                    {
                        main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_diese_Datei_endgültig_entfernen?"), caption, ok, null);
                    }
                }
                if (clickedItem == restore)
                {
                    callback.handleRestoreTargetDialog(rfstreeelems);
                }
                if (clickedItem == download)
                {
                    callback.handleDownload(singleRfstreeelem);
                }
            }
        }); // Open Context Menu to mouse coordinates when user right clicks layout


        // HAS TO BE IN VAADIN VIEW
        tree.getApplication().getMainWindow().addComponent(menu);
        

        menu.show(event.getClientX(), event.getClientY());

        return menu;

    }

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
                 RestoreLocationDlg dlg = createRestoreTargetDialog(main, viewWrapper, rfstreeelems );
                 treePanel.getApplication().getMainWindow().addWindow( dlg );
            }

            @Override
            public void handleDownload( RemoteFSElemTreeElem singleRfstreeelem )
            {
                DownloadResource downloadResource = createDownloadResource( main, getApplication(), viewWrapper, singleRfstreeelem);
                getWindow().open(downloadResource);
            }
        };
        
        lastMenu = create_fs_popup(main, viewWrapper, tree, container, event, rfstreeelems, callback);
    }

    public static DownloadResource createDownloadResource( final VSMCMain main, Application app, final IWrapper wrapper, final RemoteFSElemTreeElem rfstreeelem)
    {
        RemoteFSElem fs = rfstreeelem.getElem();

        InputStream is = main.getGuiServerApi().openStream(wrapper, fs);

        DownloadResource downloadResource = new DownloadResource(is, fs.getName(),app);

        return downloadResource;
        
    }


    public static RestoreLocationDlg createRestoreTargetDialog( final VSMCMain main, final IWrapper wrapper, final List<RemoteFSElemTreeElem> rfstreeelems)
    {
        boolean allowOrig = true;

        for (int i = 0; i < rfstreeelems.size(); i++)
        {
            RemoteFSElemTreeElem remoteFSElemTreeElem = rfstreeelems.get(i);
            if (!isHotfolderPath( remoteFSElemTreeElem ))
            {
                allowOrig = false;
                break;
            }
        }


        final RestoreLocationDlg dlg = new RestoreLocationDlg(main, "", 8082, null, allowOrig);
        Button.ClickListener okListener = new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                handleRestoreOkayDialog( main, wrapper, dlg, rfstreeelems );
            }
        };
        dlg.setOkListener( okListener );

        return dlg;
       
    }

    public static void handleRestoreOkayDialog( final VSMCMain main, final IWrapper wrapper, final RestoreLocationDlg dlg, final List<RemoteFSElemTreeElem> rfstreeelems)
    {
        Button.ClickListener ok = new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                try
                {
                    boolean rret = true;
                    List<RemoteFSElem>restoreList = new ArrayList<RemoteFSElem>();
                    String lastIp = "";
                    int lastPort = 0;
                    String lastPath = "";
                    int lastRflags = -1;

                    for (int i = 0; i < rfstreeelems.size(); i++)
                    {
                        RemoteFSElemTreeElem rfstreeelem = rfstreeelems.get(i);                        


                        String ip = dlg.getIP();
                        int port = dlg.getPort();
                        String path = dlg.getPath();
                        if (dlg.isOriginal())
                        {
                            if (isHotfolderPath( rfstreeelem ))
                            {
                                main.Msg().errmOk(VSMCMain.Txt("Hotfolderobjekte_können_nicht_an_Original_restauriert_werden"));
                                return;
                            }
                            ip = getIpFromPath( rfstreeelem );
                            port = getPortFromPath( rfstreeelem );


                            Properties p = main.getGuiServerApi().getAgentProperties( ip, port, false );
                            boolean isWindows =  ( p != null && p.getProperty(AgentApi.OP_OS).startsWith("Win"));

                            path = getTargetpathFromPath( rfstreeelem, isWindows );
                        }

                        int rflags = GuiServerApi.RF_RECURSIVE;
                        if (isHotfolderPath(rfstreeelem))
                            rflags |= GuiServerApi.RF_SKIPHOTFOLDER_TIMSTAMPDIR;
                        if (dlg.isCompressed())
                            rflags |= GuiServerApi.RF_COMPRESSION;
                        if (dlg.isEncrypted())
                            rflags |= GuiServerApi.RF_ENCRYPTION;

                        // CHENGED TARGET ? RESTORE EVERYTHING GATHERED UNTIL NOW
                        if (!lastIp.equals(ip) || lastPort != port || !lastPath.equals(path))
                        {
                            if (!restoreList.isEmpty())
                            {
                                if (!main.getGuiServerApi().restoreFSElems(wrapper, restoreList, lastIp, lastPort, lastPath, lastRflags, main.getUser()))
                                    rret = false;

                                restoreList.clear();
                            }
                        }
                        restoreList.add(rfstreeelem.getElem());
                        lastIp = ip;
                        lastPort = port;
                        lastPath = path;
                        lastRflags = rflags;
                    }

                    // RESTORE EVERYTHING GATHERED UNTIL NOW
                    if (!restoreList.isEmpty())
                    {
                        if (!main.getGuiServerApi().restoreFSElems(wrapper, restoreList, lastIp, lastPort, lastPath, lastRflags, main.getUser()))
                            rret = false;

                        restoreList.clear();
                    }

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

        if (rfstreeelems.size() == 1)
        {
            RemoteFSElemTreeElem rfstreeelem = rfstreeelems.get(0);
            if (rfstreeelem.getElem().isDirectory())
            {
                main.Msg().infoOkCancel(VSMCMain.Txt("Wollen_Sie_dieses_Verzeichnis_und_alle_darin_enthaltenen_Dateien_restaurieren?"), ok, null);
            }
            else
            {
                main.Msg().infoOkCancel(VSMCMain.Txt("Wollen_Sie_diese_Datei_restaurieren?"), ok, null);
            }
        }
        else
        {
            String caption = rfstreeelems.size() + " " + VSMCMain.Txt("Objekte");
            main.Msg().infoOkCancel(VSMCMain.Txt("Wollen_Sie_die_ausgewählten_Objekte_restaurieren?"), caption, ok, null);
        }
    }
    
    static String getClientAddressPath(  RemoteFSElemTreeElem elem )
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
    static boolean isHotfolderPath(String fp)
    {
        return fp.startsWith("/"  + HotFolder.HOTFOLDERBASE);
    }
    static boolean isHotfolderPath(RemoteFSElemTreeElem elem)
    {
        String fp = getClientAddressPath(elem);
        return fp.startsWith("/"  + HotFolder.HOTFOLDERBASE);
    }

    private static String getIpFromPath( RemoteFSElemTreeElem elem )
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

    private static int getPortFromPath( RemoteFSElemTreeElem elem )
    {
        String fp = getClientAddressPath(elem);
        String[] pathArr = fp.split("/");


        if (pathArr.length < 3)
            return 0;

        if (isHotfolderPath(fp))
            return Integer.parseInt(pathArr[3]);

        return Integer.parseInt(pathArr[2]);
    }

    private static String getTargetpathFromPath( RemoteFSElemTreeElem elem, boolean isWindows )
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
    
    
    final Component createHotfolderPanel()
    {        
        ItemClickListener l = new ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                //setActiveHotFolder();
            }
        };
        List<MountEntry> list = null;
        try
        {
            list = VSMCMain.get_base_util_em().createQuery("select p from MountEntry p", MountEntry.class);
        }
        catch (SQLException sQLException)
        {
            VSMCMain.notify(this, "Fehler beim Erzeugen der MountgEntry-Tabelle", sQLException.getMessage());
            return new VerticalLayout();
        }

        MountEntryTable mountEntryTable = MountEntryTable.createTable(main, list, l);

        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = mountEntryTable.createHeader(VSMCMain.Txt("Liste der Mount-Einträge:"));

        tableWin.addComponent(head);
        tableWin.addComponent(mountEntryTable);
        tableWin.setExpandRatio(mountEntryTable, 1.0f);
        return tableWin;
    }
}


