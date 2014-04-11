/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;


import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import de.dimm.vsm.fsengine.JDBCEntityManager;
import de.dimm.vsm.net.RemoteCallFactory;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.net.StoragePoolWrapper;
import de.dimm.vsm.net.interfaces.AgentApi;
import de.dimm.vsm.records.TextBase;
import de.dimm.vsm.vaadin.GuiElems.Charts;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.TextBaseEntryTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;



/**
 *
 * @author Administrator
 */
public class DiagnoseWin extends SidebarPanel
{


    boolean mounted = false;
    StoragePoolWrapper wrapper = null;

    Charts charts;

    boolean abortBusy;
    CheckBox trcb;

    public DiagnoseWin( VSMCMain _main )
    {        
        super(_main);

        this.setStyleName("statusWin");
        this.setSizeFull();

        charts = new Charts();

        AbsoluteLayout al = new AbsoluteLayout();
        al.setSizeFull();
        this.addComponent(al);


        final TextField txt_agent_ip = new TextField("IP");
        final TextField txt_size = new TextField("MB");
        final TextField txt_bs = new TextField("BS (kB)");
        final TextField txt_agent_port = new TextField("Port");
        final Button bt_speed = new Button("Speedtest");
        txt_agent_ip.setValue("192.168.1.145");
        txt_agent_port.setValue("8082");
        txt_size.setValue("250");
        txt_bs.setValue("1024");

        al.addComponent( bt_speed, "top:30px;left:10px" );
        al.addComponent( txt_size, "top:30px;left:120px" );
        al.addComponent( txt_agent_ip, "top:30px;left:320px" );
        al.addComponent( txt_agent_port, "top:30px;left:440px" );
        al.addComponent( txt_bs, "top:30px;left:560px" );

        bt_speed.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                int mb = Integer.parseInt( txt_size.getValue().toString());
                int sb = Integer.parseInt( txt_bs.getValue().toString());
                int port = Integer.parseInt( txt_agent_port.getValue().toString() );

                speed_test( mb, sb,  txt_agent_ip.getValue().toString(), port );
            }

        });
        
        // al.addComponent( bt_busy_abort, "top:50px;left:110px" );
        
        trcb = new CheckBox("Tracelogging");        
        trcb.setImmediate(true);
        al.addComponent( trcb, "top:60px;left:10px" );
        trcb.addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event ) {
                VSMCMain.setTrace(trcb.booleanValue());
            }
            
        });

       final Button bt_busy = new Button("Buytest");
       bt_busy.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                Runnable r = new Runnable() {

                    @Override
                    public void run()
                    {
                        abortBusy = false;
                        try
                        {
                            int i = 30;
                            while (i-- > 0 && !abortBusy)
                            {
                                Thread.sleep(100);
                            }
                        }
                        catch (InterruptedException interruptedException)
                        {
                        }
                    }
                };
                main.runInBusy("Geht?", r);
            }
        });
        final Button bt_busy_abort = new Button("Busyaborttest");
        // al.addComponent( bt_busy_abort, "top:50px;left:110px" );
        final Button.ClickListener aboret = new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                abortBusy = true;
            }
        };
        bt_busy_abort.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                Runnable r = new Runnable() {

                    @Override
                    public void run()
                    {
                        abortBusy = false;
                        try
                        {
                            int i = 30;
                            while (i-- > 0 && !abortBusy)
                            {
                                Thread.sleep(100);
                            }
                        }
                        catch (InterruptedException interruptedException)
                        {
                        }
                    }
                };
                main.runInBusyCancel("Geht?", r, aboret);
            }
        });


        ArrayList<RemoteFSElem> root_list = new ArrayList<RemoteFSElem>();
/*        File[] roots = File.listRoots();
        for (int i = 0; i < roots.length; i++)
        {
            File file1 = roots[i];
            RemoteFSElem root = new RemoteFSElem(file1.getPath(), file1.isDirectory(), file1.lastModified(), 0, 0, file1.length(), file1.length());
            root_list.add(root);
        }*/
//        File file1 = new File("z:\\storage");
//        String typ = file1.isDirectory() ? FileSystemElemNode.FT_DIR : FileSystemElemNode.FT_FILE;
//        RemoteFSElem root = new RemoteFSElem(file1.getPath(), typ, file1.lastModified(), 0, 0, file1.length(), file1.length());
//        root_list.add(root);
//
//
//        ArrayList<FSTreeColumn> fields = new ArrayList<FSTreeColumn>();
//        fields.add( new FSTreeColumn("name", VSMCMain.Txt("Name"), -1, 1.0f, Table.ALIGN_LEFT, String.class));
//        fields.add( new FSTreeColumn("date", VSMCMain.Txt("Datum"), 100, -1, Table.ALIGN_LEFT, String.class));
//        fields.add( new FSTreeColumn("size", VSMCMain.Txt("Größe"), 80, -1, Table.ALIGN_RIGHT, String.class));
//        fields.add( new FSTreeColumn("atttribute", VSMCMain.Txt("Attribute"), 80, -1, Table.ALIGN_LEFT, String.class));
//
//
//        RemoteProvider provider = new RemoteProvider()
//        {
//
//            @Override
//            public RemoteFSElemTreeElem createNode( RemoteProvider provider, RemoteFSElem elem, RemoteFSElemTreeElem parent)
//            {
//                return new BootstrapFSElemTreeElem(provider, elem, parent);
//            }
//
//            @Override
//            public List<RemoteFSElemTreeElem> getChildren(RemoteFSElemTreeElem elem)
//            {
//                List<RemoteFSElemTreeElem> childList = new ArrayList<RemoteFSElemTreeElem>();
//
//                File f = new File(elem.getElem().getPath());
//                File[] l = f.listFiles();
//                if (l == null)
//                    return childList;
//
//                for (int i = 0; i < l.length; i++)
//                {
//                    File file = l[i];
//                    String typ = file.isDirectory() ? FileSystemElemNode.FT_DIR : FileSystemElemNode.FT_FILE;
//                    RemoteFSElem rfse = new RemoteFSElem(file.getPath(), typ, file.lastModified(), 0, 0, file.length(), file.length());
//
//                    RemoteFSElemTreeElem e = new BootstrapFSElemTreeElem(this, rfse, elem);
//                    childList.add(e );
//                }
//                return childList;
//            }
//
//            @Override
//            public boolean createDir( RemoteFSElemTreeElem elem )
//            {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//            @Override
//            public ItemDescriptionGenerator getItemDescriptionGenerator()
//            {
//                return new LocalItemDescriptionGenerator();
//            }
//
//
//        };
//
//        FSTreeContainer cs = new BootstrapFSTreeContainer( provider, fields);
//        cs.initRootlist(root_list);
//
//        TreeTable tr = new FSTree(fields, /*sort*/ false);
//        tr.setContainerDataSource(cs);
//        tr.setItemDescriptionGenerator(provider.getItemDescriptionGenerator());
//
//        // reserve excess space for the "treecolumn"
//        tr.setWidth("100%");
        
         hotFolderSplitter = new VerticalSplitPanel();
        hotFolderSplitter.setSizeFull();
        hotFolderSplitter.setSplitPosition(35, Sizeable.UNITS_PERCENTAGE);

        
                
        activate();
        
        al.addComponent(hotFolderSplitter, "top:100px;left:10px");

    }
    VerticalSplitPanel hotFolderSplitter;
    
    TextBaseEntryTable table;
    final Component createTextBasePanel()
    {        
        ItemClickEvent.ItemClickListener l = new ItemClickEvent.ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                setActiveTextBase();
            }
        };
        List<TextBase> list = null;
        try
        {            
            list = VSMCMain.get_txt_em().createQuery("select p from TextBase p", TextBase.class);
        }
        catch (SQLException sQLException)
        {
            VSMCMain.notify(this, "Fehler beim Erzeugen der Hotfolder-Tabelle", sQLException.getMessage());
            return new VerticalLayout();
        }

        table = TextBaseEntryTable.createTable(main, list, l);

        final VerticalLayout tableWin  = new VerticalLayout();
        tableWin.setSizeFull();
        tableWin.setSpacing(true);

        Component head = table.createHeader(VSMCMain.Txt("Liste der Texteinträge:"));

        tableWin.addComponent(head);
        tableWin.addComponent(table);
        tableWin.setExpandRatio(table, 1.0f);
        return tableWin;
    }    
    

    @Override
    public void activate()
    {
        Component tableWin = createTextBasePanel();
        hotFolderSplitter.setFirstComponent(tableWin);
        hotFolderSplitter.setSecondComponent(new Label(""));

        if (hotFolderSplitter.getSecondComponent() != null)
            hotFolderSplitter.removeComponent(hotFolderSplitter.getSecondComponent());
        
        trcb.setValue(VSMCMain.getTrace().booleanValue());
       
    }


    private void setActiveTextBase()
    {
        AbstractLayout panel = table.createLocalPreviewPanel();
        panel.setSizeUndefined();
        panel.setWidth("100%");

        hotFolderSplitter.setSecondComponent(panel);

        // RELOAD
        VSMCMain.get_base_util_em().em_refresh(table.getActiveElem());// find(HotFolder.class, hotFolderTable.getActiveElem().getIdx());        
    }    
    

    private void speed_test( int mb, int sb, String ip, int port )
    {
        try
        {
            InetAddress addr = InetAddress.getByName(ip);
            RemoteCallFactory factory = new RemoteCallFactory(addr, port, "net", false,/*tcp*/ true, 2000, 5000);

            final AgentApi api = (AgentApi) factory.create(AgentApi.class);

            try
            {
                api.get_properties();
            }
            catch (Exception exception)
            {                
                main.Msg().errmOk("Connect failed!");
                return;
            }

            Properties p = api.get_properties();

            long start = System.currentTimeMillis();
            long sum = mb*1024l*1024l;
            long act = 0;
            while ( act < sum )
            {
                int blocklen = 1024*sb;
                api.fetch_null_data(blocklen);
                act += blocklen;
            }
            long diff = System.currentTimeMillis() - start;

            if (diff == 0)
                diff = 1;
            double ratio = sum/(1024.0 * diff);

            factory.close();

            main.Msg().errmOk("Speedtest gave a Speed of " + ratio + " MB/s");
        }
        catch (Exception exception)
        {
            main.Msg().errmOk("Speedtest schlug fehl:" + exception.getMessage() );
        }
    }

   
}
