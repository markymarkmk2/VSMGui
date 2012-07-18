/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Dialogs;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.net.interfaces.AgentApi;
import de.dimm.vsm.records.RoleOption;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteFSElemEditor;
import de.dimm.vsm.vaadin.GuiElems.OkAbortPanel;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Administrator
 */
public class RestoreLocationDlg extends Window implements Property.ValueChangeListener
{
     static ArrayList<String> ipList = new ArrayList<String>();
     static String lastPath = "";

     VerticalLayout vl = new VerticalLayout();
     OptionGroup select;
     Button.ClickListener okListener;
     TextField tfPort;
     ComboBox tfIP;
     RemoteFSElemEditor editor;
     ComboBox cbPath;
     HorizontalLayout targetVl;

     ArrayList<RoleOption> userPathList;

     VSMCMain main;
     CheckBox encrypted;
     CheckBox compressed;
     TextField agentInfo;

    public RestoreLocationDlg( VSMCMain main, String ip, int port, String path, boolean allowOrig )
    {
        this.main = main;
        userPathList = new ArrayList<RoleOption>();

        List<RoleOption> roList = main.getGuiUser().getUser().getRole().getRoleOptions();
        for (int i = 0; i < roList.size(); i++)
        {
            RoleOption roleOption = roList.get(i);
            if (roleOption.getToken().equals(RoleOption.RL_USERPATH))
            {
                if (roleOption.isValidUserPath())
                {
                    userPathList.add(roleOption);
                }
            }
        }        
        if (path == null || path.isEmpty())
            path = lastPath;
        
        build_gui(ip, port, path, allowOrig);
    }

    ArrayList<String> getUserPathServers(  )
    {
        ArrayList<String> servers = new ArrayList<String>();

        for (int i = 0; i < userPathList.size(); i++)
        {
            RoleOption roleOption = userPathList.get(i);
            String ups = roleOption.getUserPathServer();

            if (!ups.isEmpty() && !servers.contains(ups))
            {
                servers.add(ups);
            }
        }
        return servers;
    }

    ArrayList<String> getPortFromUserPath( String ip )
    {
        ArrayList<String> ports = new ArrayList<String>();

        for (int i = 0; i < userPathList.size(); i++)
        {
            RoleOption roleOption = userPathList.get(i);

            if (roleOption.getUserPathServer().equals(ip))
            {
                if (!ports.contains(Integer.toString(roleOption.getUserPathPort())))
                {
                    ports.add(Integer.toString(roleOption.getUserPathPort()));
                }
            }
        }
        return ports;
    }
    ArrayList<String> getPathFromUserPath( String ip )
    {
        ArrayList<String> paths = new ArrayList<String>();

        for (int i = 0; i < userPathList.size(); i++)
        {
            RoleOption roleOption = userPathList.get(i);
            if (roleOption.getUserPathServer().equals(ip))
            {
                String upp = roleOption.getUserPathPath();
                if (!upp.isEmpty() && !paths.contains(upp))
                {
                    paths.add(upp);
                }
            }
        }
        return paths;
    }



    public void setOkListener( ClickListener okListener )
    {
        this.okListener = okListener;
    }

    boolean restrictedPath;

    void updateRestrictedView()
    {
        if (restrictedPath)
        {
            tfPort.setValue(getPortFromUserPath(tfIP.getValue().toString()).get(0));
            Container c = new IndexedContainer();
            List<String> list = getPathFromUserPath(tfIP.getValue().toString());
            if (!list.isEmpty())
            {

                for (int i = 0; i < list.size(); i++)
                {
                    String string = list.get(i);
                    c.addItem(string);
                }

                cbPath.setContainerDataSource(c);
            }
        }
    }

    final void build_gui( String ip, int port, String path, boolean allowOrig)
    {
        addComponent(vl);
        setModal(true);
        setStyleName("vsm");

        //this.setSizeFull();
        this.setWidth("600px");

        vl.setSpacing(true);
        vl.setSizeFull();
        vl.setStyleName("editWin");


        HorizontalLayout buttonVl = new HorizontalLayout();
        buttonVl.setSpacing(true);

        targetVl = new HorizontalLayout();

        List<String> strlist = Arrays.asList(new String[] {VSMCMain.Txt("Original"), VSMCMain.Txt("Auswahl")} );
        select = new OptionGroup(VSMCMain.Txt("Zielauswahl"), strlist);
        select.addListener(this);
        if (allowOrig)
        {
            select.select(VSMCMain.Txt("Original"));
            targetVl.setVisible(false);
        }
        else
        {
             select.select(VSMCMain.Txt("Auswahl"));
             select.setItemEnabled(VSMCMain.Txt("Original"), false);
             targetVl.setVisible(true);
        }
        select.setImmediate(true);
        
        targetVl.setSpacing(true);
        restrictedPath = false;

        if (!getUserPathServers().isEmpty())
        {
            ipList.clear();
            ipList.addAll(getUserPathServers());
            restrictedPath = true;
        }
        else
        {
            restrictedPath = false;
            if (!ipList.contains(ip))
                ipList.add(ip);
        }

        tfPort = new TextField(VSMCMain.Txt("Port"), Integer.toString(port) );

        if (restrictedPath)
        {
            tfPort.setValue(getPortFromUserPath(tfIP.getValue().toString()).get(0));
            tfPort.setReadOnly(true);
        }
        else
        {
            tfPort.addValidator( new IntegerValidator(""));
            tfPort.setReadOnly(false);
        }

        tfIP = new ComboBox(VSMCMain.Txt("IP"), ipList );
        tfIP.setNullSelectionAllowed(false);
        
        if (restrictedPath)
        {
            tfIP.setInvalidAllowed(false);
            tfIP.setNewItemsAllowed(false);
            tfIP.setValue(getUserPathServers().get(0));

            tfIP.addListener(new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( ValueChangeEvent event )
                {
                    updateRestrictedView();
                }
            });
        }
        else
        {
            tfIP.setInvalidAllowed(true);
            tfIP.setNewItemsAllowed(true);
            tfIP.setValue(ip);
        }

        targetVl.addComponent(tfIP);
        targetVl.setComponentAlignment(tfIP, Alignment.BOTTOM_LEFT);
        
        targetVl.addComponent(tfPort);
        targetVl.setComponentAlignment(tfPort, Alignment.BOTTOM_LEFT);


        if (restrictedPath && !getPathFromUserPath(tfIP.getValue().toString()).isEmpty())
        {
            List<String> l = getPathFromUserPath(tfIP.getValue().toString());
            boolean useFilter = false;
            for (int i = 0; i < l.size(); i++)
            {
                String string = l.get(i);
                if (string.endsWith("*"))
                    useFilter = true;
            }
            
            if (useFilter)
            {   
                path = lastPath;
                editor = new RemoteFSElemEditor(  VSMCMain.Txt("Zielpfad"), path, tfIP, tfPort, RemoteFSElemEditor.ONLY_DIRS);
                editor.setFilter( l );
                targetVl.addComponent(editor);
            }
            else
            {
                cbPath =  new ComboBox(VSMCMain.Txt("Zielpfad"), l );
                cbPath.setNullSelectionAllowed(false);
                cbPath.setInvalidAllowed(false);
                cbPath.setNewItemsAllowed(false);
                cbPath.setValue(l.get(0));

                targetVl.addComponent(cbPath);
            }
        }
        else
        {
            editor = new RemoteFSElemEditor(  VSMCMain.Txt("Zielpfad"), path, tfIP, tfPort, RemoteFSElemEditor.ONLY_DIRS);
            targetVl.addComponent(editor);
        }
        buttonVl.addComponent(select);
        buttonVl.addComponent(targetVl);
        buttonVl.setComponentAlignment(targetVl, Alignment.BOTTOM_RIGHT);


        OkAbortPanel okPanel = new OkAbortPanel();
        okPanel.setOkText(VSMCMain.Txt("Weiter"));
        vl.addComponent(buttonVl);

        compressed = new CheckBox(VSMCMain.Txt("Komprimierung"));
        encrypted = new CheckBox(VSMCMain.Txt("Verschlüsselung"));
        agentInfo = new TextField("Agent");
        agentInfo.setReadOnly(true);
        
        vl.addComponent(compressed);
        vl.addComponent(encrypted);
        vl.addComponent(okPanel);

        addComponent(vl);

        tfIP.addListener( new Property.ValueChangeListener() {

            @Override
            public void valueChange( ValueChangeEvent event )
            {
                updateIPGui();
            }
        });

        updateIPGui();

        final Window w = this;
        okPanel.getBtAbort().addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                event.getButton().getApplication().getMainWindow().removeWindow(w);
            }
        });
        okPanel.getBtOk().addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                if (getPath() == null  || getPath().isEmpty())
                {
                    VSMCMain.notify(vl, VSMCMain.Txt("Bitte geben Sie einen gültigen Pfad an"), "");
                    return;
                }
                event.getButton().getApplication().getMainWindow().removeWindow(w);
                String ip = getIP();
                if (!ipList.contains(ip))
                    ipList.add(ip);
                if (okListener != null)
                {
                    okListener.buttonClick(event);
                }
            }
        });
    }

    private void updateIPGui()
    {
        compressed.setVisible( false );
        encrypted.setVisible( false );
        
        String ip = getIP();
        Properties p = main.getGuiServerApi().getAgentProperties( ip, getPort(), false );
        if (p != null)
        {
            compressed.setVisible(p.getProperty(AgentApi.OP_AG_COMP, "false").equals( Boolean.TRUE.toString() ));
            encrypted.setVisible(p.getProperty(AgentApi.OP_AG_ENC, "false").equals( Boolean.TRUE.toString() ));

            StringBuilder sb = new StringBuilder();
            sb.append("OS:");
            sb.append(p.getProperty(AgentApi.OP_OS_VER));
            sb.append(" Ver:");
            sb.append(p.getProperty(AgentApi.OP_AG_VER));
        }
    }

    public OptionGroup getSelect()
    {
        return select;
    }
    public String getIP()
    {
        return tfIP.getValue().toString();
    }
    public int getPort()
    {
        return Integer.parseInt(tfPort.getValue().toString());
    }
    public String getPath()
    {
        if (restrictedPath && cbPath != null)
        {
            lastPath = cbPath.getValue().toString();
        }
        else
        {
            lastPath = editor.getTf().getValue().toString();
        }
        return lastPath;
    }
    

    @Override
    public void valueChange( ValueChangeEvent event )
    {
        targetVl.setVisible(event.getProperty().getValue().toString().equals(VSMCMain.Txt("Auswahl")));
    }

    public boolean isOriginal()
    {
        return select.isSelected(VSMCMain.Txt("Original"));
    }
    public boolean isCompressed()
    {
        return compressed.booleanValue();
    }
    public boolean isEncrypted()
    {
        return encrypted.booleanValue();
    }
}
