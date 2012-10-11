/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.FileSystem;

import com.vaadin.data.util.MethodProperty;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.auth.User;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.net.StoragePoolWrapper;
import de.dimm.vsm.net.interfaces.GuiServerApi;
import de.dimm.vsm.records.FileSystemElemNode;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.TextFieldDlg;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAPoolComboField;
import de.dimm.vsm.vaadin.GuiElems.OkAbortPanel;
import de.dimm.vsm.vaadin.GuiElems.Table.PreviewPanel;
import de.dimm.vsm.vaadin.VSMCMain;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;



/**
 *
 * @author Administrator
 */





public class DBFSElemEditor extends HorizontalLayout
{
    public static final int ONLY_DIRS = 0x0001;
    public static final String DEFAULTWIDTH = "250px";


    static class DBFSRemoteProvider implements RemoteProvider
    {
        GuiServerApi api;
        StoragePoolWrapper wrapper;
        int options;

        public DBFSRemoteProvider( GuiServerApi api, StoragePoolWrapper wrapper, int options )
        {
            this.api = api;
            this.wrapper = wrapper;
            this.options = options;
        }




        @Override
        public RemoteFSElemTreeElem createNode( RemoteProvider provider, RemoteFSElem elem, RemoteFSElemTreeElem parent)
        {
            return new RemoteFSElemTreeElem(provider, elem, parent);
        }

        @Override
        public List<RemoteFSElemTreeElem> getChildren(RemoteFSElemTreeElem elem)
        {
            List<RemoteFSElemTreeElem> childList = new ArrayList<RemoteFSElemTreeElem>();

            List<RemoteFSElem> elem_list = list_dir( api, wrapper, elem.getElem());

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
            File f = new File(elem.getElem().getPath());
            return f.mkdir();
        }

        @Override
        public ItemDescriptionGenerator getItemDescriptionGenerator()
        {
            return new LocalItemDescriptionGenerator();
        }
    }


    static RemoteFSElem rootElem = new RemoteFSElem("/", FileSystemElemNode.FT_DIR, System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), 0, 0);

    private static List<RemoteFSElem> list_roots(GuiServerApi api, StoragePoolWrapper wrapper)
    {
        
        RemoteFSElem elem = rootElem;

        List<RemoteFSElem> ret = new ArrayList<RemoteFSElem>();
        ret.add(elem);

//        try
//        {
//            ret = api.listDir(wrapper, elem);
//        }
//        catch (SQLException sQLException)
//        {
//        }


        return ret;
    }

    private static List<RemoteFSElem> list_dir( GuiServerApi api, StoragePoolWrapper wrapper, RemoteFSElem startPath )
    {
        List<RemoteFSElem> ret = new ArrayList<RemoteFSElem>();
        try
        {
            ret = api.listDir(wrapper, startPath);
        }
        catch (SQLException sQLException)
        {
        }


        return ret;
    }

    AbstractField tf;
    Button bt;
    Object node;
    int options;
    GuiServerApi api;
    JPAPoolComboField poolCombo;
    PreviewPanel panel;

    public void setPanel( PreviewPanel panel )
    {
        this.panel = panel;
    }
    

    public DBFSElemEditor( String caption, String val,  int options)
    {
        this.options = options;
 

        tf = new TextField(caption);
        tf.setValue(val);

        initButton();
    }
   
    public DBFSElemEditor( String caption, MethodProperty p, int options)
    {
        this.options = options;
    

        tf = new TextField(caption, p);
        tf.setWidth(DEFAULTWIDTH);

        initButton();
    }
    public DBFSElemEditor( GuiServerApi api, JPAPoolComboField poolCombo, PreviewPanel panel, String caption, MethodProperty p, Object node, int options)
    {
        this.api = api;
        this.poolCombo = poolCombo;
        this.panel = panel;
        this.node = node;
        
        this.options = options;

        tf = new TextField(caption, p);
        tf.setWidth("250px");

        initButton();
    }

    @Override
    public void setWidth( String width )
    {
        tf.setWidth(width);
    }

    @Override
    public void setReadOnly( boolean readOnly )
    {
        tf.setReadOnly(readOnly);
        bt.setReadOnly(readOnly);
    }



    final void initButton()
    {
        bt = new NativeButton("...");
        this.setSpacing(true);
        this.addComponent(tf);
        this.setComponentAlignment(tf, Alignment.MIDDLE_LEFT);
        this.addComponent(bt);
        this.setComponentAlignment(bt, Alignment.BOTTOM_LEFT);

        bt.addListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                editPath( tf.getValue() );
            }
        });
    }

    public AbstractField getTf()
    {
        return tf;
    }
    String getActPath()
    {

        return tf.getValue().toString();
    }
    public static FSTree createClientPathTree( GuiServerApi api, StoragePoolWrapper wrapper, final AbstractField tf, String  path, User user, final int options, boolean ssl, String keystore, String keypwd )
    {

        RemoteFSElem startPath = null;
        if (path != null)
            startPath = new RemoteFSElem(new File(path));

            List<RemoteFSElem> root_list;

            if (startPath == null)
                root_list = list_roots(api, wrapper);
            else
                root_list = list_dir(api, wrapper, startPath);

            RemoteProvider provider = new DBFSRemoteProvider( api, wrapper, options );


           

            FSTree tree = createClientPathTree(provider, root_list, tf, startPath, user, options, ssl, keystore, keypwd);
            return tree;
    }

    public static FSTree createClientPathTree( RemoteProvider provider,  List<RemoteFSElem> root_list, final AbstractField tf, RemoteFSElem startPath, User user, final int options, boolean ssl, String keystore, String keypwd )
    {
        if (keystore != null)
            System.setProperty("javax.net.ssl.trustStore", keystore);

        try
        {
                        
            ArrayList<FSTreeColumn> fields = new ArrayList<FSTreeColumn>();
            fields.add( new FSTreeColumn("name", VSMCMain.Txt("Name"), -1, 1.0f, Table.ALIGN_LEFT, String.class));
            fields.add( new FSTreeColumn("date", VSMCMain.Txt("Datum"), 100, -1, Table.ALIGN_LEFT, String.class));
            fields.add( new FSTreeColumn("size", VSMCMain.Txt("Größe"), 80, -1, Table.ALIGN_RIGHT, String.class));
            fields.add( new FSTreeColumn("atttribute", VSMCMain.Txt("Attribute"), 80, -1, Table.ALIGN_LEFT, String.class));


            FSTree tr = new FSTree(fields, /*sort*/ false);

            FSTreeContainer cs = new FSTreeContainer(provider, fields);

            if (!user.getFsMapper().isEmpty())
            {
                cs.initRootWithUserMapping(user.getFsMapper() );
            }
            else
            {
                cs.initRootlist(root_list);
            }
            
            tr.setContainerDataSource(cs);


            tr.addListener( new ItemClickListener()
            {

                @Override
                public void itemClick( ItemClickEvent event )
                {
                    if (event.getItemId() instanceof RemoteFSElemTreeElem)
                    {
                        RemoteFSElemTreeElem rfstreeelem = (RemoteFSElemTreeElem)event.getItemId();
                        //event.getComponent().getApplication().getMainWindow().showNotification(" Clicked " + rfstreeelem.getElem().getPath());

                        String path = rfstreeelem.elem.getPath();
                        if (tf instanceof ComboBox)
                        {
                            ComboBox ctb = (ComboBox)tf;
                            if (!ctb.containsId(path))
                                ctb.getContainerDataSource().addItem(path);
                            ctb.select(path);
                        }
                        else
                        {
                            if (tf.getPropertyDataSource() != null)
                            {
                                tf.setValue(buildPath(rfstreeelem));
                            }
                            else
                            {
                                tf.setValue(path);
                            }
                        }
                    }
                }
            });

            tr.setSizeFull();
            tr.setImmediate(true);

            tr.setItemDescriptionGenerator( provider.getItemDescriptionGenerator() );           

            /*HorizontalLayout hl = new HorizontalLayout();
            hl.addComponent(tr);
            hl.setSizeFull();*/
            return tr;

        }
        catch (Exception malformedURLException)
        {
            System.out.println("Err: " + malformedURLException.getMessage());
        }
        return null;
    }
    public FSTree createClientPathTree( GuiServerApi api, StoragePoolWrapper wrapper, User user )
    {
        return createClientPathTree(  api, wrapper,  tf, /*startPath*/ null, user, options, false, null, null);
    }
    private void editPath( Object value )
    {
        String s = "";
        if (value != null)
            s = value.toString();


        ComboEntry sel = poolCombo.getSelectedEntry(panel);
        if (sel == null)
            return;
        Long idx = (Long) sel.getDbEntry();
        VSMCMain main = VSMCMain.Me(tf);
        StoragePool pool = main.resolveStoragePool( idx );
        final StoragePoolWrapper wrapper = api.openPoolView(pool, false, "/", main.getGuiWrapper().getUser());
        final FSTree treePanel = createClientPathTree(api, wrapper, main.getUser());

        if (treePanel == null)
            return;

        final OkAbortPanel buttonPanel = new OkAbortPanel();


        final VerticalLayout vl = new VerticalLayout();
        vl.addComponent(treePanel);
        vl.setExpandRatio(treePanel, 1.0f);
        vl.addComponent(buttonPanel);
        vl.setComponentAlignment(buttonPanel, Alignment.BOTTOM_RIGHT);
        vl.setSizeFull();
        vl.setImmediate(true);
        vl.setMargin(true);


        final Window win = new Window();
        win.setContent(vl);
        win.setWidth("650px");
        win.setHeight("50%");
        win.setCaption(VSMCMain.Txt("Pfadauswahl"));
        win.setModal(true);
        win.setStyleName("vsm");

        NativeButton createPath = buttonPanel.getBtRetry();
        createPath.setCaption(VSMCMain.Txt("Neuer Ordner"));
        createPath.setVisible(true);

        this.getApplication().getMainWindow().addWindow(win);

        buttonPanel.getBtOk().addListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( ClickEvent event )
            {
                Object o = treePanel.getValue();
                if (o instanceof RemoteFSElemTreeElem)
                {
                    RemoteFSElemTreeElem rfs = (RemoteFSElemTreeElem)o;
                    tf.setValue(buildPath(rfs));
                }
                api.closePoolView(wrapper);

               
                event.getButton().getApplication().getMainWindow().removeWindow(win);
            }
        });
        buttonPanel.getBtAbort().addListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( ClickEvent event )
            {
                api.closePoolView(wrapper);
                event.getButton().getApplication().getMainWindow().removeWindow(win);
            }
        });
        buttonPanel.getBtRetry().addListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( ClickEvent event )
            {
                Object o = treePanel.getValue();
                if (o == null)
                {
                    // NO SELECTION MEANS ROOT
                    RemoteFSElemTreeElem root = new RemoteFSElemTreeElem(treePanel.getProvider(), rootElem, null);
                    o = root;
                }
                if (o instanceof RemoteFSElemTreeElem)
                {
                    final RemoteFSElemTreeElem rfs = (RemoteFSElemTreeElem)o;
                    

                    final TextFieldDlg dlg = new TextFieldDlg( VSMCMain.Txt("Neuer Ordner"), VSMCMain.Txt("Verzeichnisname"), VSMCMain.Txt("Neuer Ordner"),
                            new StringLengthValidator(VSMCMain.Txt("Bitte geben Sie einen gültigen Pfad an"), 1, 256, false));

                    dlg.setOkActionListener( new Button.ClickListener()
                    {

                        @Override
                        public void buttonClick( ClickEvent event )
                        {
                            createChildDir( wrapper, win, rfs, dlg.getText() );

                            treePanel.expandAndReread(rfs);
                        }
                    });

                    event.getButton().getApplication().getMainWindow().addWindow(dlg);
                     
                }
            }
        });

    }

    public static String buildPath( RemoteFSElemTreeElem rfs )
    {
        StringBuilder sb = new StringBuilder();

       
        while (rfs.getParent() != null)
        {
            sb.insert(0, rfs.getName());
            if (!rfs.getName().equals("/"))
                sb.insert(0, "/");

            rfs = rfs.getParent();
        }
        
        if (sb.length() == 0)
            return "/";

        return sb.toString();
    }

    protected void createChildDir( StoragePoolWrapper wrapper, Window win, RemoteFSElemTreeElem rfs, String path )
    {
        String newPath = buildPath(rfs);
        if (!newPath.endsWith("/"))
            newPath += "/";
        newPath += path;
        try
        {
            api.createFileSystemElemNode(wrapper, newPath, FileSystemElemNode.FT_DIR);
        }
        catch (Exception exception)
        {
            VSMCMain.Me(win).Msg().errmOk(VSMCMain.Txt("Das_Verzeichnis_konnte_nicht_erzeugt_werden") + ": " + exception.getMessage());
        }
    }
}
