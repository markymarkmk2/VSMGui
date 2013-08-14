/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.FileSystem;

import com.vaadin.Application;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;


import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import de.dimm.vsm.auth.User;
import de.dimm.vsm.auth.UserManager;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.net.StoragePoolWrapper;
import de.dimm.vsm.net.interfaces.AgentApi;
import de.dimm.vsm.net.interfaces.GuiServerApi;
import de.dimm.vsm.net.interfaces.IWrapper;
import de.dimm.vsm.records.HotFolder;
import de.dimm.vsm.records.MountEntry;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.FileinfoWindow;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.RestoreLocationDlg;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.FileSystemViewer;
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
public class FSTreePanel extends HorizontalLayout
{

    VSMCMain main;
    FSTreeContainer container;
    TreeTable tree;
    StoragePoolWrapper viewWrapper = null;
    boolean mounted = false;

    public FSTreePanel( VSMCMain main )
    {
        this.main = main;
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
                    RemoteFSElem dir = RemoteFSElem.createDir(elem.getAbsolutePath());

                    List<RemoteFSElem> elem_list = main.getGuiServerApi().listDir(wrapper, dir);
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
                return new RemoteItemDescriptionGenerator(wrapper, main);
            }
        };

        container = new FSTreeContainer(provider, fields);
        container.setSkipEmptyDirs(true);


        List<RemoteFSElem> poolRootList = new ArrayList<RemoteFSElem>();
        RemoteFSElem slash = RemoteFSElem.createDir("/");
        poolRootList.add(slash);
/*        try
        {
            poolRootList = main.getGuiServerApi().listDir(wrapper, slash);

            // LIST DIR GIVES RELPATH, WE NEED ABSOLUTE PATHS HERE
            for (int i = 0; i < poolRootList.size(); i++)
            {
                RemoteFSElem remoteFSElem = poolRootList.get(i);
                remoteFSElem.makeAbsolut(slash);
            }
        }
        catch (SQLException sQLException)
        {
            VSMCMain.notify(this, "Rootverzeichnis kann nicht gelesen werden", "");
            poolRootList = new ArrayList<RemoteFSElem>();
            poolRootList.add(slash);
        }
 *
 */
        container.initRootlist(poolRootList);

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
                    RemoteFSElemTreeElem clickedItem = (RemoteFSElemTreeElem) event.getItemId();
                    Object sel = tree.getValue();

                    if (sel instanceof Set<?> && ((Set<?>) sel).size() > 1)
                    {
                        Set<RemoteFSElemTreeElem> set = (Set<RemoteFSElemTreeElem>) sel;
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
                    DownloadResource downloadResource = createDownloadResource(main, getApplication(), viewWrapper, rfstreeelem);
                    getWindow().open(downloadResource);
                }
            }
        });


        return tree;
    }
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
            if (remoteFSElemTreeElem.getElem().isFile())
            {
                hasFile = true;
            }
            if (remoteFSElemTreeElem.getElem().isDirectory())
            {
                _hasDir = true;
            }
        }
        if (rfstreeelems.size() == 1)
        {
            oneSelected = true;
        }
        final boolean hasDir = _hasDir;


        // Generate main level items
        if (oneSelected)
        {
            _info = menu.addItem(VSMCMain.Txt("Information"));
        }
        final ContextMenuItem ver = menu.addItem(VSMCMain.Txt("Versions"));
        if (oneSelected && main.isSuperUser() && !wrapper.isReadOnly())
        {
            _remove = menu.addItem(VSMCMain.Txt("Endgültig aus dem Dateisystem entfernen"));
            _del = menu.addItem(rfstreeelems.get(0).getElem().isDeleted() ? VSMCMain.Txt("Undelete") : VSMCMain.Txt("Delete"));
        }

        final ContextMenuItem restore = menu.addItem(VSMCMain.Txt("Restore"));
        if (oneSelected && hasFile)
        {
            dl = menu.addItem(VSMCMain.Txt("Download"));
        }


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
                if (clickedItem == del && main.isSuperUser())
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
                        {
                            main.Msg().infoOkCancel(VSMCMain.Txt("Wollen_Sie_dieses_Verzeichnis_wiederherstellen?"), ok, null);
                        }
                        else
                        {
                            main.Msg().infoOkCancel(VSMCMain.Txt("Wollen_Sie_dieses_Verzeichnis_als_gelöscht_markieren?"), ok, null);
                        }
                    }
                    else
                    {
                        if (rfstreeelems.get(0).getElem().isDeleted())
                        {
                            main.Msg().infoOkCancel(VSMCMain.Txt("Wollen_Sie_diese_Datei_wiederherstellen?"), ok, null);
                        }
                        else
                        {
                            main.Msg().infoOkCancel(VSMCMain.Txt("Wollen_Sie_diese_Datei_als_gelöscht_markieren?"), ok, null);
                        }
                    }
                }
                if (clickedItem == remove && main.isSuperUser())
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
            this.removeComponent(lastMenu);
        }

        IContextMenuCallback callback = new IContextMenuCallback()
        {

            @Override
            public void handleRestoreTargetDialog( List<RemoteFSElemTreeElem> rfstreeelems )
            {
                RestoreLocationDlg dlg = createRestoreTargetDialog(main, viewWrapper, rfstreeelems);
                getApplication().getMainWindow().addWindow(dlg);
            }

            @Override
            public void handleDownload( RemoteFSElemTreeElem singleRfstreeelem )
            {
                DownloadResource downloadResource = createDownloadResource(main, getApplication(), viewWrapper, singleRfstreeelem);
                getWindow().open(downloadResource);
            }
        };

        lastMenu = create_fs_popup(main, viewWrapper, tree, container, event, rfstreeelems, callback);
    }

    public static DownloadResource createDownloadResource( final VSMCMain main, Application app, final IWrapper wrapper, final RemoteFSElemTreeElem rfstreeelem )
    {
        RemoteFSElem fs = rfstreeelem.getElem();

        InputStream is = main.getGuiServerApi().openStream(wrapper, fs);

        DownloadResource downloadResource = new DownloadResource(is, fs.getName(), app);

        return downloadResource;
    }

    public static RestoreLocationDlg createRestoreTargetDialog( final VSMCMain main, final IWrapper wrapper, final List<RemoteFSElemTreeElem> rfstreeelems )
    {
        boolean allowOrig = true;

        for (int i = 0; i < rfstreeelems.size(); i++)
        {
            RemoteFSElemTreeElem remoteFSElemTreeElem = rfstreeelems.get(i);
            if (!isHotfolderPath(remoteFSElemTreeElem))
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
                handleRestoreOkayDialog(main, wrapper, dlg, rfstreeelems);
            }
        };
        dlg.setOkListener(okListener);

        return dlg;
    }

    public static void handleRestoreOkayDialog( final VSMCMain main, final IWrapper wrapper, final RestoreLocationDlg dlg, final List<RemoteFSElemTreeElem> rfstreeelems )
    {
        Button.ClickListener ok = new Button.ClickListener()
        {

            @Override
            public void buttonClick( ClickEvent event )
            {
                try
                {
                    boolean rret = true;
                    List<RemoteFSElem> restoreList = new ArrayList<RemoteFSElem>();
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
                            if (isHotfolderPath(rfstreeelem))
                            {
                                main.Msg().errmOk(VSMCMain.Txt("Hotfolderobjekte_können_nicht_an_Original_restauriert_werden"));
                                return;
                            }
                            ip = getIpFromPath(rfstreeelem);
                            port = getPortFromPath(rfstreeelem);


                            Properties p = main.getGuiServerApi().getAgentProperties(ip, port, false);
                            boolean isWindows = (p != null && p.getProperty(AgentApi.OP_OS).startsWith("Win"));

                            path = getTargetpathFromPath(rfstreeelem, isWindows);
                        }

                        int rflags = GuiServerApi.RF_RECURSIVE;
                        if (isHotfolderPath(rfstreeelem))
                        {
                            rflags |= GuiServerApi.RF_SKIPHOTFOLDER_TIMSTAMPDIR;
                        }
                        if (dlg.isCompressed())
                        {
                            rflags |= GuiServerApi.RF_COMPRESSION;
                        }
                        if (dlg.isEncrypted())
                        {
                            rflags |= GuiServerApi.RF_ENCRYPTION;
                        }

                        // CHENGED TARGET ? RESTORE EVERYTHING GATHERED UNTIL NOW
                        if (!lastIp.equals(ip) || lastPort != port || !lastPath.equals(path))
                        {
                            if (!restoreList.isEmpty())
                            {
                                if (!main.getGuiServerApi().restoreFSElems(wrapper, restoreList, lastIp, lastPort, lastPath, lastRflags, main.getUser()))
                                {
                                    rret = false;
                                }

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
                        {
                            rret = false;
                        }

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

    static String getClientAddressPath( RemoteFSElemTreeElem elem )
    {
        StringBuilder sb = new StringBuilder();

        sb.insert(0, elem.getName());
        while (elem.getParent() != null)
        {

            sb.insert(0, '/');
            if (elem.getParent().getName().equals("/") && elem.getParent().getParent() == null)
            {
                break;
            }

            sb.insert(0, elem.getParent().getName());
            elem = elem.getParent();
        }
        return sb.toString();
    }

    static boolean isHotfolderPath( String fp )
    {
        return fp.startsWith("/" + HotFolder.HOTFOLDERBASE);
    }

    static boolean isHotfolderPath( RemoteFSElemTreeElem elem )
    {
        String fp = getClientAddressPath(elem);
        return fp.startsWith("/" + HotFolder.HOTFOLDERBASE);
    }

    private static String getIpFromPath( RemoteFSElemTreeElem elem )
    {
        String fp = getClientAddressPath(elem);
        String[] pathArr = fp.split("/");

        if (pathArr.length < 2)
        {
            return null;
        }

        if (isHotfolderPath(fp))
        {
            return pathArr[2];
        }

        // 0 IS ROOT
        return pathArr[1];
    }

    private static int getPortFromPath( RemoteFSElemTreeElem elem )
    {
        String fp = getClientAddressPath(elem);
        String[] pathArr = fp.split("/");


        if (pathArr.length < 3)
        {
            return 0;
        }

        if (isHotfolderPath(fp))
        {
            return Integer.parseInt(pathArr[3]);
        }

        return Integer.parseInt(pathArr[2]);
    }

    private static String getTargetpathFromPath( RemoteFSElemTreeElem elem, boolean isWindows )
    {
        String fp = getClientAddressPath(elem);

        if (isHotfolderPath(fp))
        {
            return null;
        }

        String[] pathArr = fp.split("/");


        if (pathArr.length < 4)
        {
            return null;
        }

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
                    sb.append(pathArr[i]);
                    sb.append(":");
                    continue;
                }
            }
            sb.append("/");
            sb.append(pathArr[i]);
        }
        return sb.toString();
    }

    public void unMount()
    {
        if (viewWrapper != null)
        {
            main.getGuiServerApi().closePoolView(viewWrapper);
            viewWrapper = null;
            removeAllComponents();
        }
        mounted = false;
    }
    User getUser( MountEntry mountEntry )
    {
        if (mountEntry.getUsername() == null || mountEntry.getUsername().isEmpty())
        {
            return main.getGuiWrapper().getUser();
        }

        UserManager um = (UserManager) VSMCMain.callLogicControl("getUsermanager");
        User user = um.getUser(mountEntry.getUsername());
        return user;
    }
    public void mount(MountEntry me)
    {
       if (viewWrapper != null)
        {
            main.getGuiServerApi().closePoolView(viewWrapper);
            viewWrapper = null;
        }

        User usr = getUser(me);

        if (me.getTyp().equals(MountEntry.TYP_RDONLY))
        {
            viewWrapper = main.getGuiServerApi().openPoolView(me.getPool(), me.isReadOnly(), me.isShowDeleted(), "", usr);
        }
        else if (me.getTyp().equals(MountEntry.TYP_RDWR))
        {
            viewWrapper = main.getGuiServerApi().openPoolView(me.getPool(), me.isReadOnly(), me.isShowDeleted(), "", usr);
        }
        else if (me.getTyp().equals(MountEntry.TYP_SNAPSHOT))
        {
            Date timestamp = me.getSnapShot().getCreation();
            viewWrapper = main.getGuiServerApi().openPoolView(me.getPool(), timestamp, "", usr);
        }
        else if (me.getTyp().equals(MountEntry.TYP_TIMESTAMP))
        {
            Date timestamp = me.getTs();
            viewWrapper = main.getGuiServerApi().openPoolView(me.getPool(), timestamp, "", usr);
        }


        Component c = initFsTree(viewWrapper);
        c.setSizeFull();
        addComponent(tree);

        mounted = true;
    }

    public boolean isMounted()
    {
        return mounted;
    }

}
