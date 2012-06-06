/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.FileSystem;

import com.vaadin.data.util.MethodProperty;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import de.dimm.vsm.net.RemoteCallFactory;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.net.StoragePoolWrapper;
import de.dimm.vsm.net.interfaces.AgentApi;
import de.dimm.vsm.records.FileSystemElemNode;
import de.dimm.vsm.vaadin.VSMCMain;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 *
 * @author Administrator
 */

public class RemoteFSElemEditor extends LocalFSElemEditor
{
   

    String ip;
    int port;
    String ipField;
    String portField;

    AbstractField tfIp;
    TextField tfPort;

    public RemoteFSElemEditor(String caption, String val, AbstractField ip, TextField port, int options)
    {
        super(caption, val, options);
        this.tfIp = ip;
        this.tfPort = port;
        this.port = -1;

      
    }
    public RemoteFSElemEditor( String caption, ArrayList<String>pathList, AbstractField ip, TextField port, int options)
    {
        super( caption, "", options);
        this.tfIp = ip;
        this.tfPort = port;
        this.options = options;
        this.port = -1;

        if (tf != null)
            removeComponent(tf);

        tf = new ComboBox(caption, pathList);
        tf.setInvalidAllowed(true);
        ((ComboBox)tf).setNullSelectionAllowed(false);
        ((ComboBox)tf).setNewItemsAllowed(true);

        // MUST INIT AGAIN, TF HAS CHANGED
        initButton();
    }
    public RemoteFSElemEditor( String caption, MethodProperty p, String ip, int port, int options)
    {
        super(caption, p, options);
        this.ip = ip;
        this.port = port;

        
    }
    public RemoteFSElemEditor( String caption, MethodProperty p, Object node, String ipField, String portField, int options)
    {
        super( caption, p, node, options);
        this.node = node;
        this.ipField = ipField;
        this.portField = portField;
        this.port = -1;      
    }
   

    String getActIP()
    {
        if (ip!= null)
            return ip;

        if (ipField != null)
        {
            final MethodProperty p = new MethodProperty(node,ipField);
            if (p.getValue() != null)
                return p.getValue().toString();
        }
        if (tfIp != null)
        {
            return tfIp.getValue().toString();
        }
        return "";
    }
    int getActPort()
    {
        if (port != -1)
            return port;

        if (portField != null)
        {
            final MethodProperty p = new MethodProperty(node,portField);
            if (p.getValue() != null)
                return Integer.parseInt(p.getValue().toString());
        }
        if (tfPort != null)
        {
            try
            {
                return Integer.parseInt(tfPort.getValue().toString());
            }
            catch (NumberFormatException numberFormatException)
            {
            }
        }
        return 0;
    }
    
    @Override
    protected FSTree createClientPathTree()
    {
        return createClientPathTree(  null, null);
    }

    public static FSTree createClientPathTree( final StoragePoolWrapper wrapper, final VSMCMain main, final AgentApi api, final AbstractField tf, RemoteFSElem startPath, final int options, boolean ssl, String keystore, String keypwd )
    {
            Properties p = api.get_properties();
            System.out.println("Agent ver: " + p.getProperty(AgentApi.OP_AG_VER));

            ArrayList<RemoteFSElem> root_list;

            if (startPath == null)
                root_list = api.list_roots();
            else
                root_list = api.list_dir(startPath,/*withAcl*/ true);

            RemoteProvider provider = new RemoteProvider()
            {
                @Override
                public RemoteFSElemTreeElem createNode( RemoteProvider provider, RemoteFSElem elem, RemoteFSElemTreeElem parent)
                {
                    return new RemoteFSElemTreeElem(provider, elem, parent);
                }

                @Override
                public List<RemoteFSElemTreeElem> getChildren(RemoteFSElemTreeElem elem)
                {
                    List<RemoteFSElemTreeElem> childList = new ArrayList<RemoteFSElemTreeElem>();

                    ArrayList<RemoteFSElem> elem_list = api.list_dir(elem.getElem(),/*withAcl*/ true);

                    for (int i = 0; i < elem_list.size(); i++)
                    {
                        RemoteFSElem rfse = elem_list.get(i);

                        if ((options & ONLY_DIRS) != 0)
                        {
                            if (!rfse.isDirectory())
                                continue;
                        }

                        RemoteFSElemTreeElem e = new RemoteFSElemTreeElem(this, rfse, elem);
                        childList.add(e );
                    }
                    return childList;
                }
                @Override
                public boolean createDir( RemoteFSElemTreeElem elem )
                {
                    try
                    {
                        boolean b = api.create_dir(elem.getElem());
                        if (!b)
                        {
                            VSMCMain.notify(tf, VSMCMain.Txt("das Verzeichnis konnte nicht angelegt werden"), "");
                        }
                        return b;
                    }
                    catch (Exception iOException)
                    {
                        VSMCMain.notify(tf, VSMCMain.Txt("das Verzeichnis konnte nicht angelegt werden"), iOException.getMessage());
                    }
                    return false;
                }

                @Override
                public ItemDescriptionGenerator getItemDescriptionGenerator()
                {
                    if (wrapper != null && main != null)
                        return new RemoteItemDescriptionGenerator(wrapper, main );
                    return null;
                }



            };

            FSTree tree = createClientPathTree(provider, root_list, tf, startPath, options, ssl, keystore, keypwd);
            return tree;
    }
    protected FSTree createClientPathTree(StoragePoolWrapper wrapper, VSMCMain main)
    {
        RemoteCallFactory _factory = null;
        try
        {
            InetAddress addr = InetAddress.getByName(getActIP());
            _factory = new RemoteCallFactory(addr, getActPort(), "net", /*ssl*/ false,/*tcp*/ true);

            api = (AgentApi) _factory.create(AgentApi.class);
            api.get_properties();
        }
        catch (Exception unknownHostException)
        {
            String cause = unknownHostException.getMessage();
            if (cause == null && unknownHostException.getCause() != null)
                cause = unknownHostException.getCause().getLocalizedMessage();
            if (cause == null)
                cause = "?";

            tf.getWindow().showNotification(VSMCMain.Txt("Der_Agent_kann_nicht_kontaktiert_werden"), cause, Notification.TYPE_WARNING_MESSAGE);
            return null;
        }
        final RemoteCallFactory factory = _factory;


        final FSTree treePanel = createClientPathTree( wrapper, main, api, tf, /*startPath*/ null, options, false, null, null);

        return treePanel;
    }



    AgentApi api;

    @Override
    protected void createChildDir( Window win, RemoteFSElemTreeElem rfs, String name )
    {
        String path = rfs.getElem().getPath() + rfs.getElem().getSeparatorChar() + name;
        RemoteFSElem newDir = new RemoteFSElem(path, FileSystemElemNode.FT_DIR, 0,0, 0, 0, 0);
        try
        {
            boolean b = api.create_dir(newDir);
            if (!b)
            {
                VSMCMain.notify(tf, VSMCMain.Txt("das Verzeichnis konnte nicht angelegt werden"), "");
            }

        }
        catch (IOException iOException)
        {
            VSMCMain.notify(tf, VSMCMain.Txt("das Verzeichnis konnte nicht angelegt werden"), iOException.getMessage());
        }
    }
}
