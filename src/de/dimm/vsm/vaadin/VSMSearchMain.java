/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin;

import com.vaadin.Application;
import de.dimm.vsm.auth.User;
import de.dimm.vsm.records.RoleOption;
import de.dimm.vsm.vaadin.GuiElems.ErrMsgHandler;
import de.dimm.vsm.vaadin.auth.AppHeader;
import de.dimm.vsm.vaadin.GuiElems.ExitElem;
import de.dimm.vsm.vaadin.GuiElems.LoginElem;
import de.dimm.vsm.vaadin.GuiElems.Sidebar;
import de.dimm.vsm.vaadin.GuiElems.SidebarButton;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.ArchiveJobWin;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.FileSystemViewer;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.JobWin;
import de.dimm.vsm.vaadin.search.SearchClientWin;

/**
 *
 * @author Administrator
 */
public class VSMSearchMain extends VSMCMain
{
    
    public VSMSearchMain( Application _app )
    {
        super(_app);

        root.setCaption("VSM SearchClient");

        createGuiServerProxy();

        errMsgHandler = new ErrMsgHandler(this);

    }

    @Override
    public void createMenu()
    {
        super.createMenu();
        if (menuItem != null)
            getMenuBar().removeItem(menuItem);


        menuItem = getMenuBar().addItem(Txt("Datei"), null);

        loginElem = new LoginElem(this);


        loginElem.attachTo(menuItem);


        menuItem.addSeparator();
        ExitElem exitElem = new ExitElem(this);
        exitElem.attachTo(menuItem);

    }

    @Override
    protected boolean checkAccesRight(User user)
    {
        // ANYBODY IS ALLOWED
         return true;
    }


    @Override
    public void createSidebar()
    {
        sidebar = new Sidebar();
        sidebar.setSpacing(true);
        sidebar.setStyleName("sidebar");

        
        SidebarButton btarchive = new SidebarButton( this, Txt("Archivsuche"), new ArchiveJobWin(this));
        SidebarButton btsearch = new SidebarButton( this, Txt("Dateisuche"), new SearchClientWin(this));
        SidebarButton btFileSystem = new SidebarButton( this, Txt("Filesystem"), new FileSystemViewer(this) );
        SidebarButton btjobs = new SidebarButton( this, Txt("Jobs"), new JobWin(this) );


        sidebar.add(btarchive);
        sidebar.add(btsearch);
        sidebar.add(btFileSystem);
        sidebar.add(btjobs);

        setSideComponent(sidebar, 150);
        
    }

   

    @Override
    protected void createHeader()
    {
        setAppIcon("appIconr", "Base V" + callLogicControl("getVersion").toString() + " SearchGui V" + getVersion());
        header = new AppHeader(this);
        setAppHeader(header.getGui());
    }


}
