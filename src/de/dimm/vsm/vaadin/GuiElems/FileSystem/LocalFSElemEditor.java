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
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.TextFieldDlg;
import de.dimm.vsm.vaadin.GuiElems.OkAbortPanel;
import de.dimm.vsm.vaadin.VSMCMain;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;



/**
 *
 * @author Administrator
 */



public class LocalFSElemEditor extends HorizontalLayout
{
    public static final int ONLY_DIRS = 0x0001;
    public static final int MOUNT_POINT_MODE = 0x0002;
    public static final int STAY_LOCAL_MODE = 0x0004;
    public static final String DEFAULTWIDTH = "250px";


    private static ArrayList<RemoteFSElem> list_roots()
    {
        ArrayList<RemoteFSElem> ret = new ArrayList<RemoteFSElem>();
        File[] roots = File.listRoots();
        for (int i = 0; i < roots.length; i++)
        {
            File file = roots[i];
            ret.add( new RemoteFSElem(file) );
        }
        return ret;
    }

    private static ArrayList<RemoteFSElem> list_dir( RemoteFSElem startPath )
    {
        ArrayList<RemoteFSElem> ret = new ArrayList<RemoteFSElem>();
        File[] roots = new File(startPath.getPath()).listFiles();
        for (int i = 0; i < roots.length; i++)
        {
            File file = roots[i];
            ret.add( new RemoteFSElem(file) );
        }
        return ret;
    }

    AbstractField tf;
    Button bt;
    Object node;
    int options;
    

    public LocalFSElemEditor(  String caption, String val,  int options)
    {
        this.options = options;
 

        tf = new TextField(caption);
        if (val != null)
        {
            tf.setValue(val);
        }
        initButton();
    }
   
    public LocalFSElemEditor( String caption, MethodProperty p, int options)
    {
        this.options = options;
    

        tf = new TextField(caption, p);
        tf.setWidth(DEFAULTWIDTH);

        initButton();
    }
    public LocalFSElemEditor( String caption, MethodProperty p, Object node, int options)
    {
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


    final void initButton()
    {
        bt = new Button("...");
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
    public static FSTree createClientPathTree( final AbstractField tf, String  path, final int options, boolean ssl, String keystore, String keypwd )
    {

        RemoteFSElem startPath = null;
        if (path != null)
            startPath = new RemoteFSElem(new File(path));

            ArrayList<RemoteFSElem> root_list;

            if (startPath == null)
                root_list = list_roots();
            else
                root_list = list_dir(startPath);

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

                    ArrayList<RemoteFSElem> elem_list = list_dir(elem.getElem());

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
            };

            FSTree tree = createClientPathTree(provider, root_list, tf, startPath, options, ssl, keystore, keypwd);
            return tree;
    }

    public static FSTree createClientPathTree( RemoteProvider provider,  ArrayList<RemoteFSElem> root_list, final AbstractField tf, RemoteFSElem startPath, final int options, boolean ssl, String keystore, String keypwd )
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

            cs.initRootlist(root_list);

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
                                tf.setValue(rfstreeelem.elem);
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
    protected FSTree createClientPathTree()
    {
        return createClientPathTree(  tf, /*startPath*/ null, options, false, null, null);
    }
    private void editPath( Object value )
    {
        String s = "";
        if (value != null)
            s = value.toString();

        
        final FSTree treePanel = createClientPathTree();

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

        Button createPath = buttonPanel.getBtRetry();
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
                    tf.setValue(rfs.getElem());
                }

               
                event.getButton().getApplication().getMainWindow().removeWindow(win);
            }
        });
        buttonPanel.getBtAbort().addListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( ClickEvent event )
            {
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
                    event.getButton().getApplication().getMainWindow().showNotification(VSMCMain.Txt("Bitte wählen Sie ein Vaterverzeichnis aus"),"", Notification.TYPE_WARNING_MESSAGE);
                    return;
                }
                if (o instanceof Set)
                {
                    o = ((Set)o).iterator().next();
                }
                if (o instanceof RemoteFSElemTreeElem)
                {
                    final RemoteFSElemTreeElem rfs = (RemoteFSElemTreeElem)o;
                    

                    final TextFieldDlg dlg = new TextFieldDlg(VSMCMain.Txt("Neuer Ordner"), VSMCMain.Txt("Verzeichnisname"), VSMCMain.Txt("Neuer Ordner"),
                            new StringLengthValidator(VSMCMain.Txt("Bitte gebgen Sie einen gültigen Pfad an"), 1, 256, false));

                    dlg.setOkActionListener( new Button.ClickListener()
                    {

                        @Override
                        public void buttonClick( ClickEvent event )
                        {
                            createChildDir( win, rfs, dlg.getText() );

                            treePanel.expandAndReread(rfs);
                        }
                    });

                    event.getButton().getApplication().getMainWindow().addWindow(dlg);
                     
                }
            }
        });

    }

    protected void createChildDir( Window win, RemoteFSElemTreeElem rfs, String path )
    {
        File f = new File(rfs.getElem().getPath(), path);
        if (!f.mkdir())
        {
            VSMCMain.Me(win).Msg().errmOk(VSMCMain.Txt("Das_Verzeichnis_konnte_nicht_erzeugt_werden"));
        }
    }
    @Override
    public void setReadOnly( boolean readOnly )
    {
        tf.setReadOnly(readOnly);
        bt.setReadOnly(readOnly);
    }

}
