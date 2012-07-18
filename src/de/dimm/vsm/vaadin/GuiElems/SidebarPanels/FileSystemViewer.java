/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbsoluteLayout;
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
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.net.StoragePoolWrapper;
import de.dimm.vsm.net.interfaces.AgentApi;
import de.dimm.vsm.net.interfaces.GuiServerApi;
import de.dimm.vsm.records.FileSystemElemNode;
import de.dimm.vsm.records.HotFolder;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTree;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTreeColumn;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTreeContainer;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteFSElemTreeElem;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteProvider;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.FileinfoWindow;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.RestoreLocationDlg;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteItemDescriptionGenerator;
import de.dimm.vsm.vaadin.SelectObjectCallback;
import de.dimm.vsm.vaadin.VSMCMain;
import de.dimm.vsm.vaadin.net.DownloadResource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
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
        AbsoluteLayout al = new AbsoluteLayout();
        al.setSizeFull();
        this.addComponent(al);


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
        al.addComponent(btVol, "top:30px;left:10px");
        al.addComponent(dta_ts, "top:30px;left:150px");
        al.addComponent(txt_agent_ip, "top:30px;left:320px");
        al.addComponent(txt_agent_port, "top:30px;left:440px");
        al.addComponent(txt_mnt_drive, "top:30px;left:560px");



        final DateField dta_view_ts = new DateField("Timestamp", new Date());

        btViewVol = new NativeButton("View file system");
        HorizontalLayout hl = new HorizontalLayout();
        btViewVolLive = new NativeButton("Open live file system");
        cbDeleted = new CheckBox("Gelöschte Dateien sichtbar");
        hl.addComponent(btViewVolLive);
        hl.addComponent(cbDeleted);
        hl.setSpacing(true);
        

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
        al.addComponent(btViewVol, "top:70px;left:10px");
        al.addComponent(dta_view_ts, "top:70px;left:150px");

        al.addComponent(hl, "top:110px;left:10px");

        treePanel = new HorizontalLayout();
        // reserve excess space for the "treecolumn"
        treePanel.setSizeFull();

        al.addComponent(treePanel, "top:150px;left:10px");
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
        RemoteFSElem root = new RemoteFSElem("/", FileSystemElemNode.FT_DIR, 0, 0, 0, 0, 0);

        ArrayList<RemoteFSElem> root_list = new ArrayList<RemoteFSElem>();
        root_list.add(root);


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
        container.initRootlist(root_list);

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
                    RemoteFSElemTreeElem rfstreeelem = (RemoteFSElemTreeElem) event.getItemId();
                    create_fs_popup(event, rfstreeelem);
                }
                if (event.getItemId() instanceof RemoteFSElemTreeElem && event.isDoubleClick())
                {
                    RemoteFSElemTreeElem rfstreeelem = (RemoteFSElemTreeElem) event.getItemId();
                    handleDownload(rfstreeelem);
                }
            }
        });


        return tree;
    }
    ContextMenu lastMenu = null;

    void create_fs_popup( ItemClickEvent event, final RemoteFSElemTreeElem rfstreeelem )
    {
        ContextMenu menu = new ContextMenu();
        ContextMenuItem dl = null;
        ContextMenuItem _remove = null;
        ContextMenuItem _del = null;



        // Generate main level items
        final ContextMenuItem info = menu.addItem(VSMCMain.Txt("Information"));
        final ContextMenuItem ver = menu.addItem(VSMCMain.Txt("Versions"));
        if (main.getGuiUser().isSuperUser() && !viewWrapper.isReadOnly())
        {
            _remove = menu.addItem( VSMCMain.Txt("Endgültig aus dem Dateisystem entfernen"));
            _del = menu.addItem(rfstreeelem.getElem().isDeleted() ? VSMCMain.Txt("Undelete") : VSMCMain.Txt("Delete"));
        }

        final ContextMenuItem restore = menu.addItem(VSMCMain.Txt("Restore"));
        if (!rfstreeelem.getElem().isDirectory())
            dl = menu.addItem(VSMCMain.Txt("Download"));


        //final ContextMenuItem rename = menu.addItem(VSMCMain.Txt("Rename"));

        final ContextMenuItem download = dl;
        final ContextMenuItem del = _del;
        final ContextMenuItem remove = _remove;

        // Generate sub item to photos menu
        ContextMenuItem topRated = ver.addItem("Letzte Woche (Todo...)");

        //photos.setIcon(new FileResource(new File("images/dir.png"), event.getComponent().getApplication()));

        // Enable separator line under this item
        ver.setSeparatorVisible(true);

        // Show notification when menu items are clicked
        menu.addListener(new ContextMenu.ClickListener()
        {

            @Override
            public void contextItemClick( ContextMenu.ClickEvent event )
            {
                // Get reference to clicked item
                ContextMenuItem clickedItem = event.getClickedItem();
                if (clickedItem == info)
                {
                    FileinfoWindow win = new FileinfoWindow(main, viewWrapper, rfstreeelem.getElem());

                    // Do something with the reference
                    getApplication().getMainWindow().addWindow(win);
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
                                if (rfstreeelem.getElem().isDeleted())
                                {
                                    main.getGuiServerApi().undeleteFSElem(viewWrapper, rfstreeelem.getElem());
                                }
                                else
                                {
                                    main.getGuiServerApi().deleteFSElem(viewWrapper, rfstreeelem.getElem());
                                }

                                tree.setCollapsed(rfstreeelem.getParent(), true);
                                tree.setCollapsed(rfstreeelem.getParent(), false);
                                tree.requestRepaint();

                            }
                            catch (Exception ex)
                            {
                                main.Msg().errmOk(VSMCMain.Txt("Der_Löscheintrag_kann_nicht_verändert_werden"));
                            }
                        }
                    };
                    if (rfstreeelem.getElem().isDirectory())
                    {
                        if (rfstreeelem.getElem().isDeleted())
                            main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_dieses_Verzeichnis_wiederherstellen?"), ok, null);
                        else
                            main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_dieses_Verzeichnis_als_gelöscht_markieren?"), ok, null);
                    }
                    else
                    {
                        if (rfstreeelem.getElem().isDeleted())
                            main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_diese_Datei_wiederherstellen?"), ok, null);
                        else
                            main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_diese_Datei_als_gelöscht_markieren?"), ok, null);
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
                                main.getGuiServerApi().removeFSElem(viewWrapper, rfstreeelem.getElem());
                                container.removeItem(rfstreeelem);
                                
                                tree.setCollapsed(rfstreeelem.getParent(), true);
                                tree.setCollapsed(rfstreeelem.getParent(), false);
                                tree.requestRepaint();

                            }
                            catch (Exception ex)
                            {
                                main.Msg().errmOk(VSMCMain.Txt("Eintrag_konnte_nicht_entfernt_werden"));
                            }
                        }
                    };
                    if (rfstreeelem.getElem().isDirectory())
                    {
                        main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_dieses_Verzeichnis_und_alle_darin_enthaltenen_Dateien_endgültig_entfernen?"), ok, null);
                    }
                    else
                    {
                        main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_diese_Datei_endgültig_entfernen?"), ok, null);
                    }
                }
                if (clickedItem == restore)
                {
                    handleRestoreTargetDialog(rfstreeelem);
                }
                if (clickedItem == download)
                {
                    handleDownload(rfstreeelem);
                }

            }
        }); // Open Context Menu to mouse coordinates when user right clicks layout


        if (lastMenu != null)
        {
            treePanel.removeComponent(lastMenu);
        }

        // HAS TO BE IN VAADIN VIEW
        treePanel.getApplication().getMainWindow().addComponent(menu);
        lastMenu = menu;

        menu.show(event.getClientX(), event.getClientY());

    }

    private void handleDownload( final RemoteFSElemTreeElem rfstreeelem)
    {
        RemoteFSElem fs = rfstreeelem.getElem();

        InputStream is = main.getGuiServerApi().openStream(viewWrapper, fs);

        DownloadResource downloadResource = new DownloadResource(is, fs.getName(), treePanel.getApplication());

        getWindow().open(downloadResource);
    }


    private void handleRestoreTargetDialog( final RemoteFSElemTreeElem rfstreeelem)
    {
        boolean allowOrig = !isHotfolderPath( rfstreeelem );

        final RestoreLocationDlg dlg = new RestoreLocationDlg(main, "127.0.0.1", 8082, "", allowOrig);
        Button.ClickListener okListener = new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                handleRestoreOkayDialog( dlg, rfstreeelem );
            }
        };
        dlg.setOkListener( okListener );
        treePanel.getApplication().getMainWindow().addWindow( dlg );
    }

    private void handleRestoreOkayDialog( final RestoreLocationDlg dlg, final RemoteFSElemTreeElem rfstreeelem)
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


                    boolean rret = main.getGuiServerApi().restoreFSElem(viewWrapper, rfstreeelem.getElem(), ip, port, path, rflags, main.getUser());
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
        if (rfstreeelem.getElem().isDirectory())
        {
            main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_dieses_Verzeichnis_und_alle_darin_enthaltenen_Dateien_restaurieren?"), ok, null);
        }
        else
        {
            main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_diese_Datei_restaurieren?"), ok, null);
        }

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


