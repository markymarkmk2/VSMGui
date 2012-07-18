/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin;

import de.dimm.vsm.auth.User;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.PoolEditorWin;
import com.vaadin.Application;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.mail.NotificationServer;
import de.dimm.vsm.net.GuiWrapper;
import de.dimm.vsm.net.interfaces.GuiLoginApi;
import de.dimm.vsm.net.interfaces.GuiServerApi;
import de.dimm.vsm.records.RoleOption;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.BusyHandler;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.MountLocationDlg;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.RichTextAreaDlg;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.DiagnoseWin;
import de.dimm.vsm.vaadin.GuiElems.ErrMsgHandler;
import de.dimm.vsm.vaadin.auth.AppHeader;
import de.dimm.vsm.vaadin.GuiElems.ExitElem;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.FileSystemViewer;
import de.dimm.vsm.vaadin.GuiElems.LoginElem;
import de.dimm.vsm.vaadin.GuiElems.Sidebar;
import de.dimm.vsm.vaadin.GuiElems.SidebarButton;
import de.dimm.vsm.vaadin.GuiElems.SidebarButtonCallback;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.ArchiveJobWin;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.AuthentificationWin;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.BackupResult;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.HotFolderWin;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.JobWin;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.LogWin;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.LogoutPanel;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.NotificationWin;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.StartBackupWin;
import de.dimm.vsm.vaadin.auth.GuiUser;
import de.dimm.vsm.vaadin.net.GuiServerProxy;
import de.dimm.vsm.vaadin.search.SearchClientWin;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.vaadin.jouni.animator.AnimatorProxy;

/**
 *
 * @author Administrator
 */
public class VSMCMain extends GenericMain
{
    protected String ip;
    protected String host;
    protected String args;

    private final static String version = "0.7.6 trunk";

    public static String getVersion()
    {
        return version;
    }



    public static boolean isFTPStorageLicensed()
    {
        Object o = callLogicControl("isFTPStorageLicensed");
        if (o != null && o instanceof Boolean)
        {
            return ((Boolean) o).booleanValue();
        }

        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static boolean isS3StorageLicensed()
    {
        Object o = callLogicControl("isS3StorageLicensed");
        if (o != null && o instanceof Boolean)
        {
            return ((Boolean) o).booleanValue();
        }

        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void notifyUnimplemented(Component c)
    {
        notify( c, "Not yet implemented", "Try again later");
    }

    private EntityManagerFactory get_emf()
    {
        Object o = callLogicControl("get_emf");
        if (o != null && o instanceof EntityManagerFactory)
        {
            return (EntityManagerFactory) o;
        }

        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static GenericEntityManager get_base_util_em()
    {
        Object o = callLogicControl("get_base_util_em");
        if (o != null && o instanceof GenericEntityManager)
        {
            return (GenericEntityManager) o;
        }

        throw new UnsupportedOperationException("Not yet implemented");
    }
    public static GenericEntityManager get_util_em(StoragePool pool)
    {
        Object o = callLogicControl("get_util_em", pool);
        if (o != null && o instanceof GenericEntityManager)
        {
            return (GenericEntityManager) o;
        }

        throw new UnsupportedOperationException("Not yet implemented");
    }
    public StoragePool resolveStoragePool( long idx )
    {
        List<StoragePool> list = getStoragePoolList();
        for (int i = 0; i < list.size(); i++)
        {
            StoragePool storagePool = list.get(i);
            if (storagePool.getIdx() == idx)
                return storagePool;

        }
        return null;
    }

    public static NotificationServer getNotificationServer()
    {
        Object o = callLogicControl("getNotificationServer");
        if (o != null && o instanceof NotificationServer)
        {
            return (NotificationServer) o;
        }

        throw new UnsupportedOperationException("Not yet implemented");
    }


    public static String Txt( String k )
    {
        try
        {
            Class cl = Class.forName("de.dimm.vsm.Main");
            Class[] cl_args =
            {
                String.class
            };
            Object[] ob_args =
            {
                k
            };

            Method mtxt = cl.getMethod("Txt", cl_args);
            Object txt = mtxt.invoke(null, ob_args);
            return txt.toString();
        }
        catch (Exception classNotFoundException)
        {
            System.out.println("Error in reflection call Txt:" + classNotFoundException.getMessage());
        }
        return k;

    }

    public static Object callLogicControl( String func )
    {
        try
        {
            return callLogicControl(func, true);
        }
        catch (Exception ex)
        {
            // CANNOT HAPPEN, IS CAUGHT INSIDE
        }
        return null;
    }


    public static Object callLogicControl( String func, boolean catchException ) throws Exception
    {
        try
        {
            Class cl = Class.forName("de.dimm.vsm.Main");
            Method get_control = cl.getMethod("get_control", (Class[]) null);
            Object logicControl = get_control.invoke(null, (Object[]) null);

            Class logic_class = Class.forName("de.dimm.vsm.LogicControl");
            Method m_func = logic_class.getMethod(func, (Class[]) null);
            Object result = m_func.invoke(logicControl, (Object[]) null);
            return result;
        }
        catch (Exception exc)
        {
            if (!catchException)
                throw exc;


            if (exc instanceof InvocationTargetException)
            {
                InvocationTargetException ite = (InvocationTargetException)exc;
                notify(VSMCMain.me.root, ite.getTargetException().getMessage(), "");
                return null;
            }
            System.out.println("Error in reflection call " + func + ":" + exc.getMessage());
            exc.printStackTrace();
        }
        return null;
    }

    public static Object callLogicControl( String func, Object arg)
    {
        try
        {
            return callLogicControl(func, arg, true);
        }
        catch (Exception ex)
        {
            // CANNOT HAPPEN, IS CAUGHT INSIDE
        }
        return null;
    }

    public static Object callLogicControl( String func, Object arg, boolean catchException ) throws Exception
    {
        try
        {
            Class cl = Class.forName("de.dimm.vsm.Main");
            Class[] types = new Class[1];
            types[0] = arg.getClass();
            Object[] args = new Object[1];
            args[0] = arg;
            Method get_control = cl.getMethod("get_control", (Class[]) null);
            Object logicControl = get_control.invoke(null, (Object[]) null);

            Class logic_class = Class.forName("de.dimm.vsm.LogicControl");
            Method m_func = logic_class.getMethod(func, types);
            Object result = m_func.invoke(logicControl, args);
            return result;
        }
        catch (Exception exc)
        {
            if (!catchException)
                throw exc;

            if (exc instanceof InvocationTargetException)
            {
                InvocationTargetException ite = (InvocationTargetException)exc;
                notify(VSMCMain.me.root, ite.getTargetException().getMessage(), "");
                return null;
            }
            System.out.println("Error in reflection call " + func + ":" + exc.getMessage());
        }
        return null;
    }

    public void runInBusy( String txt, final Runnable r )
    {
        getBusy().showBusy(txt);
        Runnable tht_r = new Runnable()
        {

            @Override
            public void run()
            {
                r.run();
                getBusy().hideBusy();

            }
        };

        Thread thr = new Thread(tht_r, "BusyRunner");
        thr.start();
    }
    public void runInBusyCancel( String txt, final Runnable r, ClickListener abortListener )
    {
        getBusy().showBusyCancel(txt, abortListener );
        
        Runnable tht_r = new Runnable()
        {

            @Override
            public void run()
            {
                r.run();
                getBusy().hideBusy();
            }
        };

        Thread thr = new Thread(tht_r, "BusyRunner");
        thr.start();
    }



    public void deletePool( StoragePool node )
    {
        callLogicControl( "deletePool" );
    }
    public void deletePoolPhysically( final StoragePool node )
    {
        Runnable r = new Runnable() {

            @Override
            public void run()
            {
                callLogicControl( "deletePoolPhysically", node );
            }
        };
        
        runInBusy(Txt("Lösche StoragePool"), r );
    }


    
    MenuItem menuItem;
    Application app;
    AppHeader header;
    LoginElem loginElem;
    boolean loggedIn;
    Component poolEditor;
    Component backupEditor;

    SidebarButton btstatus;
    ErrMsgHandler errMsgHandler;
    BusyHandler busyHandler;
    GuiServerProxy guiServerProxy;
    LogoutPanel logoutPanel;
    LogWin logWin;
    static VSMCMain me;
    Sidebar sidebar;
    GuiWrapper guiWrapper;

    protected String lastUser = null;
    protected String lastPwd= null;


    public GuiWrapper getGuiWrapper()
    {
        return guiWrapper;
    }

    

    public static MountLocationDlg mountDlg = new MountLocationDlg("127.0.0.1", 8082, "/mnt");

    public VSMCMain( Application _app )
    {
        super("VSMCClient");

        app = _app;

        //String mainClass = app.getProperty("servermain");

        me = this;

        root.setSizeFull();
        root.setStyleName("vsm");



        createGuiServerProxy();

        errMsgHandler = new ErrMsgHandler(this);
        busyHandler = new BusyHandler(this);



        app.setUser(this);

    }

    public BusyHandler getBusy()
    {
        return busyHandler;
    }

    public void setupGui()
    {
        createSidebar();

//        createHeader();

        setMainComponent(logoutPanel);
    }

    final void createGuiServerProxy()
    {
        try
        {
            String s = "de.dimm.vsm.vaadin.net.GuiServerProxy";
            Class cl = Class.forName(s);
            if (cl != null)
                System.out.println(cl.getSimpleName());
        }
        catch (ClassNotFoundException classNotFoundException)
        {
        }

        guiServerProxy = new GuiServerProxy(this);
    }

    public GuiServerApi getDummyGuiServerApi()
    {
        return guiServerProxy.getDummyGuiServerApi();
    }
    @Override
    public GuiServerApi getGuiServerApi()
    {
        if (guiWrapper == null)
        {
            Msg().errmOk("Not logged in");
            return null;
        }
        
        return guiWrapper.getApi();
    }
    @Override
    public GuiLoginApi getGuiLoginApi()
    {
        return guiServerProxy.getGuiLoginApi();
    }

    public static VSMCMain Me( Component c )
    {
        return (VSMCMain) c.getApplication().getUser();
    }

    @Override
    public boolean isLoggedIn()
    {
        return loggedIn;
    }

    @Override
    public LoginElem getLoginElem()
    {
        return loginElem;
    }

    public void createMenu()
    {
        if (menuItem != null)
            getMenuBar().removeItem(menuItem);


        menuItem = getMenuBar().addItem(Txt("Datei"), null);

        loginElem = new LoginElem(this);


        loginElem.attachTo(menuItem);


        menuItem.addSeparator();
        ExitElem exitElem = new ExitElem(this);
        exitElem.attachTo(menuItem);
    }
    void initMainScreen()
    {
        logoutPanel = new LogoutPanel(this);
        setMainComponent(logoutPanel);
        getMainSplitter().setSplitPosition(0);
    }

    public void createSidebar()
    {
        sidebar = new Sidebar();
        sidebar.setSpacing(true);
        sidebar.setStyleName("sidebar");

        btstatus = new SidebarButton( this, Txt("Action"), new StartBackupWin(this));
        SidebarButton btPools = new SidebarButton( this, Txt("StoragePools"), new PoolEditorWin(this));
        SidebarButton btHotFolder = new SidebarButton( this, Txt("HotFolder"), new HotFolderWin(this));
        SidebarButton btAuth = new SidebarButton( this, Txt("Authentifizierung"), new AuthentificationWin(this));

        //SidebarButton btTest = new SidebarButton( this, Txt("Test"),null );
        SidebarButton btFileSystem = new SidebarButton( this, Txt("Filesystem"), new FileSystemViewer(this) );
        SidebarButton btSearch = new SidebarButton( this, Txt("Suche"), new SearchClientWin(this) );
        SidebarButton btlogs = new SidebarButton( this, Txt("Logging"), new LogWin(this) );
        SidebarButton bt_diag = new SidebarButton( this, Txt("Diagnose"), new DiagnoseWin(this) );
        SidebarButton bt_baResult = new SidebarButton( this, Txt("Backuphistorie"), new BackupResult(this) );
        SidebarButton btarchive = new SidebarButton( this, Txt("Archiv"), new ArchiveJobWin(this) );
        SidebarButton btnotification = new SidebarButton( this, Txt("Benachrichtigungen"), new NotificationWin(this) );
        SidebarButton btjobs = new SidebarButton( this, Txt("Jobs"), new JobWin(this) );


        SidebarButton bthilfe = new SidebarButton( this, Txt("Hilfe"), null,  new SidebarButtonCallback()
        {
            @Override
            public void action()
            {
                Msg().errmOkCancel("Viel Hilfe ist an dieser Stelle noch nicht zu erwarten, dafür aber ein ziemlich langer sinnloser Text zum Testen der Warnmeldung", null, null);
            }
        });

        sidebar.add(btstatus);
        sidebar.add(btPools);
        sidebar.add(btHotFolder);
        sidebar.add(btAuth);
        //sidebar.add(btTest);
        sidebar.add(btFileSystem);
        sidebar.add(btSearch);
        sidebar.add(btlogs);
        sidebar.add(bt_baResult);
        sidebar.add(bt_diag);
        sidebar.add(btarchive);
        sidebar.add(btnotification);
        sidebar.add(btjobs);

//        btstatus.setSelected();
//        btstatus.doCallback();

        setSideComponent(sidebar, 150);

    }

    @Override
    public void exitApp()
    {
        header.resetUser();
        app.close();
    }

    @Override
    public void handleLogout()
    {
        if (lastPanel != null)
        {
            lastPanel.deactivate();
        }

        if (guiWrapper != null)
        {
            getGuiLoginApi().logout(guiWrapper);
        }

        guiWrapper = null;
        loggedIn = false;
        lastPwd = null;
        header.resetUser();
        getLoginElem().updateGui();

        // RESET GUI
        //createSidebar();
        setMainComponent(logoutPanel);
        if (sidebar != null)
        {
            sidebar.deselectAll();
            getMainSplitter().removeComponent(sidebar);
        }
        getMainSplitter().setSplitPosition(0);
    }

    protected boolean checkAccesRight(User user)
    {
         return (user.getRole().hasRoleOption(RoleOption.RL_ALLOW_VIEW_PARAM) ||
                 user.getRole().hasRoleOption(RoleOption.RL_ALLOW_EDIT_PARAM) );

    }
    @Override
    public void handleLogin(GuiWrapper wr, String name, String pwd )
    {
        guiWrapper = wr;
        loggedIn = (wr != null);

        if (!checkAccesRight( wr.getUser()))
        {
            wr = null;
            guiWrapper = null;
            loggedIn = false;
            notify(root, Txt("Sie haben keine Berechtigung für diesen Programmteil"),  Txt("zur Suche: http://.../search"));
        }


        getLoginElem().updateGui();

        if (loggedIn)
        {
            lastUser = name;
            lastPwd = pwd;
                        
            header.setUser(new GuiUser(wr.getUser(), wr.getLastLogin()));

            setupGui();
        }
        else
        {
            handleLogout();
        }
    }
    public GuiUser getGuiUser()
    {
        if (header != null)
            return header.getUser();

        return null;
    }

    protected void createHeader()
    {
        NativeButton icon = setAppIcon("appIconr", "Base V" + callLogicControl("getVersion").toString() + " Gui V" + getVersion());
        icon.addListener( new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                if (event.isShiftKey())
                {
                    String chl = callLogicControl("getChangelog", Boolean.TRUE).toString();
                    
                    RichTextAreaDlg dlg = new RichTextAreaDlg("ChangeLog", chl, true);
                    dlg.setWidth("500px");
                    dlg.setHeight("600px");
                    app.getMainWindow().addWindow(dlg);
                }
                if (event.isCtrlKey())
                {
                    String chl = callLogicControl("getGuiChangelog", Boolean.TRUE).toString();

                    RichTextAreaDlg dlg = new RichTextAreaDlg("ChangeLog", chl, true);
                    dlg.setWidth("500px");
                    dlg.setHeight("600px");
                    app.getMainWindow().addWindow(dlg);
                }
            }
        });

        header = new AppHeader(this);
        setAppHeader(header.getGui());
    }

    
    EntityManager em = null;

    public EntityManager get_em()
    {
        if (em == null)
        {
            em = get_emf().createEntityManager();
        }
        return em;
    }

    public ErrMsgHandler Msg()
    {
        return errMsgHandler;
    }

    @Override
    public Window getRootWin()
    {
        return app.getMainWindow();
    }

    @Override
    public AnimatorProxy getAnimatorProxy()
    {
        return animatorProxy;
    }

    public <T> void SelectObject( StoragePool pool, Class<T> t, String caption, String buttonCaption, String string, final SelectObjectCallback cb )
    {
        //EntityManager _em = get_em();

        List list = null;
        try
        {
            list = get_util_em(pool).createQuery(string, t);
        }
        catch (SQLException sQLException)
        {
        }
        //List list = qry.getResultList();

        if (list == null || list.isEmpty())
        {
            errMsgHandler.errmOk("Die Auswahl von " + caption + " ist leer");
            return;
        }
        SelectObject(t, caption, buttonCaption, list, cb);

    }

    public <T> void SelectObject(  Class<T> t, String caption, String buttonCaption, List<T> list, final SelectObjectCallback cb )
    {

        if (list.size() == 1)
        {
            cb.SelectedAction(list.get(0));
            return;
        }
        final NativeSelect sel = new NativeSelect(caption);
        sel.setNewItemsAllowed(false);
        sel.setInvalidAllowed(false);
        sel.setNullSelectionAllowed(false);


        for (int i = 0; i < list.size(); i++)
        {
            Object object = list.get(i);
            sel.addItem(object);
        }
        if (!list.isEmpty())
            sel.setValue(list.get(0));

        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(true);
        NativeButton ok = new NativeButton(buttonCaption);

        final Window win = new Window(Txt("Auswahl") + " " + caption);

        ok.addListener(new ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                getRootWin().removeWindow(win);

                cb.SelectedAction(sel.getValue());
            }
        });

        vl.addComponent(sel);
        vl.addComponent(ok);
        vl.setComponentAlignment(ok, Alignment.BOTTOM_RIGHT);

        vl.setSizeUndefined();
        win.addComponent(vl);
        getRootWin().addWindow(win);
    }

    @Override
    public boolean tryLogin(LoggedInEvent ev)
    {
        if (lastUser == null || lastPwd == null)
        {
            loginElem.manualLogin(ev);
            // THIS TAKES USER INTERACTION, WE RETURN IMMEDIATELY, THEREFORE WE ARE STILL LOGGED OUT
            return false;
        }

        GuiWrapper wr = getGuiLoginApi().login(lastUser, lastPwd);
        if (wr != null)
        {
            handleLogin(wr, lastUser, lastPwd);
            return true;
        }
        else
        {
            handleLogout();

        }
        return false;

        //loginElem.getClickListener().buttonClick(null);
    }

    public boolean checkLogin()
    {
        if (!isLoggedIn())
        {
            return tryLogin(null);
        }
        return true;
    }
    
    public boolean checkLogin(final ClickListener click   )
    {
        LoggedInEvent ev = new LoggedInEvent() {

            @Override
            public void loggedIn()
            {
                click.buttonClick(null);
            }
        };
        if (!isLoggedIn())
        {
            return tryLogin( ev  );
        }
        return true;
    }


    @Override
    public String getLastUser()
    {
        return lastUser;
    }
    public static void notify( Component c, String bigTxt, String smallTxt )
    {
        Window win = c.getWindow();
        if (win == null)
            win = me.getRoot();
        win.showNotification(bigTxt,smallTxt, Notification.TYPE_WARNING_MESSAGE);
    }
    public static void info( Component c, String bigTxt, String smallTxt )
    {
         Window win = c.getWindow();
        if (win == null)
            win = me.getRoot();
        win.showNotification(bigTxt,smallTxt, Notification.TYPE_HUMANIZED_MESSAGE);
    }

    public void initSchedules()
    {
        callLogicControl("initSchedules" );
    }

    protected void setClient( String ip, String host, String args )
    {
        this.ip = ip;
        this.host =host;
        this.args = args;
        
        if (args != null && args.length() > 0)
        {
            String[] arglist = args.split(";");
            for (int i = 0; i < arglist.length; i++)
            {
                String[] av = arglist[i].split("=");
                if (av.length == 2)
                {
                    if (av[0].toLowerCase().equals("pool"))
                    {
                        String pool = av[1];
                        setPool( pool );
                    }
                }
            }
        }
    }

    String slctPool = null;
    private void setPool( String pool )
    {
        slctPool = pool;
    }

    public String getSlctPool()
    {
        return slctPool;
    }


    List<StoragePool> _getStoragePoolList()
    {
        Object o = callLogicControl("getStoragePoolList");
        if (o != null && o instanceof List)
        {
            return (List<StoragePool>) o;
        }

        throw new UnsupportedOperationException("Not yet implemented");
    }

    public List<StoragePool> getStoragePoolList()
    {
        List<StoragePool> list = _getStoragePoolList();

        if (slctPool == null || slctPool.isEmpty())
            return list;

        List<StoragePool> ret = new ArrayList<StoragePool>();

        for (int i = 0; i < list.size(); i++)
        {
            StoragePool storagePool = list.get(i);
            if (storagePool.getName().equalsIgnoreCase(slctPool))
                ret.add(storagePool);
        }

        return ret;
    }

    public String getIp()
    {
        return ip;
    }

    public String getHost()
    {
        return host;
    }

    public StoragePool getStoragePool( long poolIdx )
    {
        List<StoragePool> list = _getStoragePoolList();
        for (int i = 0; i < list.size(); i++)
        {
            StoragePool storagePool = list.get(i);
            if (storagePool.getIdx() == poolIdx)
                return storagePool;

        }
        return null;
    }

    public User getUser()
    {
        if (getGuiUser() != null)
            return getGuiUser().getUser();

        return null;
    }

    public boolean allowEdit()
    {
        if (getGuiUser() != null)
        {
            if (getGuiUser().getUser().getRole().hasRoleOption(RoleOption.RL_ALLOW_EDIT_PARAM))
                return true;
        }
        

        return false;
    }

    public boolean allowEditGui()
    {
        if (!allowEdit())
        {
            notify(sidebar, Txt("Sie haben keine Berechtigung zum Ändern von Daten"), "");

            return false;
        }
        return true;
    }


}
