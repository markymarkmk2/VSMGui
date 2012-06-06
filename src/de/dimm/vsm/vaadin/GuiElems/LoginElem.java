/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems;

import com.vaadin.Application;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import de.dimm.vsm.net.GuiWrapper;
import de.dimm.vsm.vaadin.GenericMain;
import de.dimm.vsm.vaadin.LoggedInEvent;
import de.dimm.vsm.vaadin.VSMCLoginForm;
import de.dimm.vsm.vaadin.VSMCLoginForm.LoginListener;
import de.dimm.vsm.vaadin.VSMCLoginForm.VSMCLoginEvent;
import de.dimm.vsm.vaadin.VSMCMain;
import org.vaadin.jouni.animator.client.ui.VAnimatorProxy.AnimType;

/**
 *
 * @author Administrator
 */
public class LoginElem extends GUIElem
{
    GenericMain main;
    LoggedInEvent ev;

    public LoginElem(GenericMain m)
    {
        super(m);
        main = m;
    }

    @Override
    public String getItemText()
    {
        if (main.isLoggedIn())
            return Txt("Logout");
        else
            return Txt("Login");
    }

    public void manualLogin(LoggedInEvent ev)
    {
        this.ev = ev;
        action();
    }

    @Override
    void action()
    {
        if (main.isLoggedIn())
        {
            main.handleLogout();
            updateGui();
            return;
        }

        VSMCLoginForm df = new VSMCLoginForm();
        df.setWidth("100%");
        final Window myw = new Window(Txt("Login"));
        final Application app = parentWin.getRoot().getApplication();
        myw.setWidth("350px");
        df.addListener( new LoginListener()
        {

            @Override
            public void onLogin(VSMCLoginEvent event)
            {
                // TODO Auto-generated method stub
                String name = event.getLoginParameter("username");
                String pwd =  event.getLoginParameter("password");

//                // HACK HACK HACK; LOGIN WITH EMPTY LOGIN CREDENTIALS
//                if (name.isEmpty() && pwd.isEmpty())
//                {
//                    System.out.println("************** TEST SYSTEMLOGIN *******************");
//                    name = "system";
//                    pwd = "admin";
//                }

                GuiWrapper wr = main.getGuiLoginApi().login(name, pwd);
                if (wr != null)
                {
                    app.getMainWindow().removeWindow(myw);
                    
                    main.handleLogin(wr, name, pwd);
                    updateGui();
                    if (ev != null)
                        ev.loggedIn();
                }
                else
                {
                    parentWin.getRoot().getWindow().showNotification(Txt("Die Anmeldung ist fehlgeschlagen"),Txt("Bitte erneut versuchen"), Notification.TYPE_WARNING_MESSAGE);
                    main.handleLogout();
                }
            }
        });

        myw.setModal(true);
        myw.addComponent(df);
        
        if (main.getLastUser() != null)
            df.setUsername(main.getLastUser());

        app.getMainWindow().addWindow(myw);

        main.getAnimatorProxy().animate(myw, AnimType.FADE_IN).setDuration(300);
    }

}
