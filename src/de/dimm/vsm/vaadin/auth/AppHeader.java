/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.auth;

import de.dimm.vsm.auth.GuiUser;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import de.dimm.vsm.records.RoleOption;
import de.dimm.vsm.vaadin.GenericMain;
import de.dimm.vsm.vaadin.VSMCMain;
import java.text.SimpleDateFormat;

/**
 *
 * @author Administrator
 */
public class AppHeader
{
    GenericMain main;
    GuiUser actUser;
    Label lbUser;
    Label lbLastLogin;
    Label lbUserMode;
    VerticalLayout gui;
    Button btLogin;

    public AppHeader( GenericMain main )
    {
        this.main = main;

        gui = new VerticalLayout();
        gui.setWidth("100%");
        gui.setHeight("100px");
        //gui.setMargin(false, true, false, true);
        gui.setSpacing(false);

        btLogin = main.getLoginElem().createButton();
        gui.addComponent(btLogin);
        gui.setComponentAlignment(btLogin, Alignment.TOP_RIGHT);

        lbUser = new Label("");
        lbUser.setContentMode(Label.CONTENT_XHTML);
        lbUser.setWidth(null);
        lbLastLogin = new Label("");
        lbLastLogin.setContentMode(Label.CONTENT_XHTML);
        lbLastLogin.setWidth(null);
        lbUserMode = new Label("");
        lbUserMode.setContentMode(Label.CONTENT_XHTML);
        lbUserMode.setWidth(null);

        gui.addComponent(lbUser);
        gui.addComponent(lbLastLogin);
        gui.addComponent(lbUserMode);
        gui.setComponentAlignment(lbUser, Alignment.TOP_RIGHT);
        gui.setComponentAlignment(lbLastLogin, Alignment.TOP_RIGHT);
        gui.setComponentAlignment(lbUserMode, Alignment.TOP_RIGHT);

        main.setAppHeader(gui);
    }
    public void setUser( GuiUser u )
    {
        actUser = u;
        lbUser.setValue(VSMCMain.Txt("Willkommen") + " <b>" + actUser.toString() + "</b> ");
        String dstr = "-";
        if (u.getLastLogin() != null)
        {
            String um = VSMCMain.Txt("um");
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy '" + um + "' HH:mm");
            dstr = sdf.format(u.getLastLogin());
        }
        lbLastLogin.setValue("<small>" + VSMCMain.Txt("Letzter Login") + ": " + dstr + "</small> " );

        StringBuilder mode = new StringBuilder();
        if (u.getUser().getRole().hasRoleOption(RoleOption.RL_ADMIN))
        {
            if (mode.length() > 0)
                mode.append(", " );
            mode.append(VSMCMain.Txt("Admin"));
        }
        if (u.getUser().getRole().hasRoleOption(RoleOption.RL_ALLOW_VIEW_PARAM))
        {
            if (mode.length() > 0)
                mode.append(", " );
            mode.append(VSMCMain.Txt("Parameter lesen"));
        }
        if (u.getUser().getRole().hasRoleOption(RoleOption.RL_ALLOW_EDIT_PARAM))
        {
            if (mode.length() > 0)
                mode.append(", " );
            mode.append(VSMCMain.Txt("Parameter ändern"));
        }
        if (mode.length() == 0)
            mode.append("-");
        
        lbUserMode.setValue("<small>" + VSMCMain.Txt("Rechte") + ": " + mode.toString() + "</small> ");

        gui.requestRepaint();
    }
    public void resetUser()
    {
        actUser = null;
        lbUser.setValue("");
        lbLastLogin.setValue("");
        lbUserMode.setValue("");

        gui.requestRepaint();
    }
    public Component getGui()
    {
        return gui;
    }

    public GuiUser getUser()
    {
        return actUser;
    }
    

}
