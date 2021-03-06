/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.Exceptions.PathResolveException;
import de.dimm.vsm.Exceptions.PoolReadOnlyException;
import de.dimm.vsm.auth.User;
import de.dimm.vsm.auth.UserManager;
import de.dimm.vsm.hash.StringUtils;
import de.dimm.vsm.net.SearchWrapper;
import de.dimm.vsm.net.StoragePoolWrapper;
import de.dimm.vsm.net.interfaces.IWrapper;
import de.dimm.vsm.records.MountEntry;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.ComboBoxDlg;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.NewMountDlg;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.NewPoolQryDlg;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTreePanel;
import de.dimm.vsm.vaadin.VSMCMain;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class FileSystemViewer extends SidebarPanel
{
    boolean volMounted = false;        
    StoragePoolWrapper mountWrapper;
    
    Button btViewVol;
    Button btWebDavVol;
    
    MountEntryViewTable mountpanel;
    final Refresher refresher = new Refresher();
    public static final int RF_INTERVALL = 1000;
    FSTreePanel treePanel;

    public FileSystemViewer( VSMCMain _main )
    {
        super(_main);

        this.setStyleName("statusWin");
        this.setSizeFull();
        this.setSpacing(true);
        VerticalLayout vl = new VerticalLayout();

        this.addComponent(vl);
        this.setExpandRatio(vl, 1.0f);
        vl.setSpacing(true);
        vl.setSizeFull();
        HorizontalLayout hlMount = new HorizontalLayout();
        hlMount.setSpacing(true);
        hlMount.setWidth("100%");
        hlMount.setMargin(false, false, true, false);
        VerticalLayout vlMountCombo = new VerticalLayout();
        vlMountCombo.setSpacing(true);

        Button btMount = new Button("Mount", new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                doMount();
            }
        });
        Button btUnMount = new Button("UnMount", new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                doUnMount();
            }
        });
        Button btNewMount = new Button("Neuer Mount", new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                doNewMount();
            }
        });

        VerticalLayout vlMountTable = new VerticalLayout();
        vlMountTable.setWidth("100%");
        vlMountTable.setSpacing(true);
        vlMountTable.setHeight("150px");

        mountpanel = new MountEntryViewTable(main);
        mountpanel.setSizeFull();
       

        vlMountTable.addComponent(new Label(VSMCMain.Txt("Gemountete Volumes")));
        vlMountTable.addComponent(mountpanel);
        vlMountTable.setExpandRatio(mountpanel, 1);
        

        vlMountCombo.addComponent(new Label(VSMCMain.Txt("Volumes mounten")));

        btMount.setWidth("100%");
        vlMountCombo.addComponent(btMount);

        btUnMount.setWidth("100%");
        vlMountCombo.addComponent(btUnMount);

        btNewMount.setWidth("100%");
        vlMountCombo.addComponent(btNewMount);

        hlMount.addComponent(vlMountCombo);
        vlMountCombo.setWidth("150px");
        hlMount.addComponent(vlMountTable);
        hlMount.setExpandRatio(vlMountTable, 1);

        vl.addComponent(hlMount);

        btViewVol = new Button(VSMCMain.Txt("Dateisystem anzeigen"));
        btWebDavVol = new Button(VSMCMain.Txt("WebDav öffnen"));
        btWebDavVol.setImmediate(true);
        
        Button.ClickListener listener = new Button.ClickListener()
        {
            @Override
            public void buttonClick( com.vaadin.ui.Button.ClickEvent event )
            {
                doOpenFsTree();
            }
        };
        Button.ClickListener webDavlistener = new Button.ClickListener() {
            @Override
            public void buttonClick( com.vaadin.ui.Button.ClickEvent event ) {
                doOpenWebDav(main, treePanel.getViewWrapper(), getWindow(), null);
            }
        };

        btViewVol.addListener(listener);
        btWebDavVol.addListener(webDavlistener);

        HorizontalLayout hl3 = new HorizontalLayout();
        hl3.setSpacing(true);

        hl3.addComponent(btViewVol);
        hl3.addComponent(btWebDavVol);
        btWebDavVol.setVisible(false);
         hl3.setMargin(false, false, true, false);

        
        vl.addComponent(hl3);

        treePanel = new FSTreePanel(_main);
        // reserve excess space for the "treecolumn"
        treePanel.setSizeFull();

        vl.addComponent(treePanel);
        vl.setExpandRatio(treePanel, 1.0f);

        refresher.addListener(new Refresher.RefreshListener()
        {

            @Override
            public void refresh( Refresher source )
            {
                source.setRefreshInterval(0);
                long s = System.currentTimeMillis();
                mountpanel.refresh();

                long e = System.currentTimeMillis();

                // COMM TIME SHOULD NOT EXCEED 20% OF CYCLE TIME
                int rfi = (int) ((e - s) * 5);
                if (rfi < RF_INTERVALL)
                {
                    rfi = RF_INTERVALL;
                }

                source.setRefreshInterval(rfi);
            }
        });
        this.addComponent(refresher);
    }

    void closePoolView()
    {
        if (treePanel.isMounted())
        {
            treePanel.unMount();
        }
        
        if (volMounted)
        {
            if (mountWrapper != null)
            {
                main.getGuiServerApi().unmountVolume(mountWrapper);
                mountWrapper = null;
            }
        }

        btViewVol.setVisible(true);
        btViewVol.setCaption(VSMCMain.Txt("Dateisystem anzeigen"));
        btWebDavVol.setVisible(false);
    }

    @Override
    public void deactivate()
    {
        super.deactivate();

        mountpanel.deactivate();
        closePoolView();
    }

    @Override
    public void activate()
    {
        mountpanel.activate();
    }

    public static void filterUserEntries( User usr, List<MountEntry> mountedEntries)
    {        
        if (!usr.isAdmin())
        {
            for (int i = 0; i < mountedEntries.size(); i++)
            {
                MountEntry mountEntry = mountedEntries.get(i);
                // No User or other user
                if (StringUtils.isEmpty(mountEntry.getUsername() ) || 
                        (!mountEntry.getUsername().equals(usr.getLoginName())
                           && !mountEntry.getUsername().equals(usr.getRole().getName()) ))
                {
                    mountedEntries.remove(i);
                    i--;
                }
            }
        }
    }

    User getUser( MountEntry mountEntry )
    {
        if (main.getGuiWrapper().getUser().isAdmin())
        {
            return main.getGuiWrapper().getUser();
        }
        
        if (mountEntry.getUsername() == null 
                || mountEntry.getUsername().isEmpty() 
                || mountEntry.getUsername().equals( main.getGuiWrapper().getUser().getRole().getName()) )
        {
            return main.getGuiWrapper().getUser();
        }

        UserManager um = (UserManager) VSMCMain.callLogicControl("getUsermanager");
        User user = um.getUser(mountEntry.getUsername());
        return user;
    }

    void doMount()
    {
        List<MountEntry> allEntries = main.getDummyGuiServerApi().getAllMountEntries();
        List<MountEntry> mountedEntries = main.getDummyGuiServerApi().getMountedMountEntries();
        allEntries.removeAll(mountedEntries);
        filterUserEntries( main.getUser(), allEntries);

        String userChoiceButtonText = main.isSuperUser() ?  VSMCMain.Txt("Aktuellen User verwenden") : null;
        final ComboBoxDlg<MountEntry> dlg = new ComboBoxDlg("Mount", VSMCMain.Txt("Auswahl"),userChoiceButtonText, allEntries);
        
        
        dlg.setOkActionListener(new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                MountEntry val = dlg.getValue();
                if (val == null)
                {
                    return;
                }
                User usr = main.getUser();
                if (!dlg.isButton())
                {
                    usr = getUser(val);
                    if (usr == null && !val.getUsername().isEmpty())
                    {
                        // Keine Rechte
                        main.Msg().errmOk(VSMCMain.Txt("Sie haben keine Rechte für diesen Mount"));
                    }
                    
                }

                try
                {
                    main.getDummyGuiServerApi().mountEntry(usr, val);
                }
                catch (Exception e)
                {
                    main.Msg().errmOk(VSMCMain.Txt("Der Mount schlug fehl") + ": " + e.getMessage());
                }
            }
        });
        main.getRootWin().addWindow(dlg);
    }

    void doUnMount()
    {
        List<MountEntry> mountedEntries = main.getDummyGuiServerApi().getMountedMountEntries();        
        filterUserEntries( main.getUser(), mountedEntries);
        final ComboBoxDlg<MountEntry> dlg = new ComboBoxDlg("UnMount", VSMCMain.Txt("Auswahl"), mountedEntries);
        dlg.setOkActionListener(new Button.ClickListener()
        {

            @Override
            public void buttonClick( ClickEvent event )
            {
                MountEntry val = dlg.getValue();
                if (val == null)
                {
                    return;
                }
                main.getDummyGuiServerApi().unMountEntry(val);
            }
        });
        main.getRootWin().addWindow(dlg);
    }

    void doNewMount()
    {
        final NewMountDlg dlg = new NewMountDlg(main);
        ClickListener ok = new Button.ClickListener()
        {

            @Override
            public void buttonClick( ClickEvent event )
            {
                MountEntry val = dlg.getMountEntry();
                User usr = main.getUser();

                if (!StringUtils.isEmpty(val.getUsername()))
                {
                    usr = getUser(val);
                    if (usr == null)
                    {
                        VSMCMain.notify(btViewVol, VSMCMain.Txt("Unbekannter User"), val.getUsername() + " " + VSMCMain.Txt("konnte nicht gefunden werden"));
                        return;
                    }
                }
                try
                {
                    main.getDummyGuiServerApi().mountEntry(usr, val);
                }
                catch (Exception e)
                {
                    main.Msg().errmOk(VSMCMain.Txt("Der Mount schlug fehl") + ": " + e.getMessage());
                }
            }
        };
        dlg.setOkClick(ok);
        dlg.openDlg();
    }

    void doOpenFsTree()
    {
        if (treePanel.isMounted())
        {
            treePanel.unMount();
            btViewVol.setCaption(VSMCMain.Txt("Dateisystem anzeigen"));
            btWebDavVol.setVisible(false);
            return;
        }

        final NewPoolQryDlg dlg = new NewPoolQryDlg(main);
        ClickListener ok = new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                MountEntry me = dlg.getMountEntry();
                treePanel.mount(me);
                btViewVol.setCaption(VSMCMain.Txt("Dateisystem ausblenden"));
                btWebDavVol.setVisible(true);
            }
        };
        dlg.setOkClick(ok);
        dlg.openDlg();
    }
    
    public static void doOpenWebDav(VSMCMain main, IWrapper viewWrapper, Window window, String vsmPath) {        
        try {
            int port = -1;
            if (viewWrapper instanceof StoragePoolWrapper) {
                port = main.getGuiServerApi().createWebDavServer((StoragePoolWrapper)viewWrapper);
            }
            else if (viewWrapper instanceof SearchWrapper) {
                port = main.getGuiServerApi().createWebDavSearchServer((SearchWrapper)viewWrapper);
            }
            if (port < 0) {
                main.Msg().errmOk(VSMCMain.Txt("WebDav wurde nicht erstellt"));
            }           
            else {
                URL url = main.getRoot().getURL();     
                String path = url.getProtocol() + "://" + url.getHost() + ":" + port + "/" + viewWrapper.getWebDavToken();
                if (!StringUtils.isEmpty(vsmPath)) {
                    path += "/" + vsmPath;
                }
                
                window.executeJavaScript("window.open('" + path + "', '_blank')");
                //main.getRoot().open(new ExternalResource(path),"_blank");
            }
        }
        catch (IOException | PoolReadOnlyException | PathResolveException ex) {
            main.Msg().errmOk(VSMCMain.Txt("WebDav wurde abgebrochen: " + ex.getMessage()));
        }
    }
    
}
