/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Dialogs;

import com.thoughtworks.xstream.XStream;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.Utilities.SizeStr;
import de.dimm.vsm.Utilities.ZipUtilities;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.net.AttributeContainer;
import de.dimm.vsm.net.AttributeList;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.net.SearchWrapper;
import de.dimm.vsm.net.interfaces.GuiServerApi;
import de.dimm.vsm.net.interfaces.IWrapper;
import de.dimm.vsm.records.ArchiveJob;
import de.dimm.vsm.records.ArchiveJobFileLink;
import de.dimm.vsm.records.FileSystemElemAttributes;
import de.dimm.vsm.records.HotFolder;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.ArchivJobTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;


class SecondDateField extends DateField
{
    public SecondDateField(String caption, Date d)
    {
        super(caption, d );
        setResolution(DateField.RESOLUTION_SEC);
    }
}
/**
 *
 * @author Administrator
 */
public class FileinfoWindow extends Window
{

     RemoteFSElem elem;
     VSMCMain main;
     IWrapper sw = null;
     //StoragePoolWrapper sp = null;
     VerticalLayout vl = new VerticalLayout();
     long poolIdx;

    HorizontalLayout jobPanel;
    List<ArchiveJob> jobList = null;

    public FileinfoWindow( VSMCMain main, IWrapper sw, RemoteFSElem elem )
    {
        this.elem = elem;
        this.main = main;
        this.sw = sw;
        poolIdx = sw.getPoolIdx();

        build_gui(elem);
    }
//    public FileinfoWindow( VSMCMain main, StoragePoolWrapper sp, RemoteFSElem elem )
//    {
//        this.elem = elem;
//        this.main = main;
//        this.sp = sp;
//        poolIdx = sp.getPoolIdx();
//
//        build_gui(elem);
//    }

    final void build_gui( RemoteFSElem elem )
    {
        addComponent(vl);


        setModal(true);
        setStyleName("vsm");

        //this.setSizeFull();
        this.setWidth("400px");

        vl.setSpacing(true);
        vl.setSizeFull();
        vl.setImmediate(true);
        vl.setStyleName("editWin");

        this.setCaption(VSMCMain.Txt("Datei_Informationen_für") + " " + elem.getName());

        TextField name = new TextField(VSMCMain.Txt("Name"), elem.getName() );
        vl.addComponent(name);
        name.setWidth("100%");
        vl.setExpandRatio(name, 1.0f);
        String path = "";
        FileSystemElemAttributes attr = null;
        List<ArchiveJobFileLink> linkList = null;
        jobList = null;
        
        try
        {
            StoragePool pool = main.getStoragePool(poolIdx);
            GenericEntityManager em = VSMCMain.get_util_em(pool);

            // READ ATTRIBUTES
            attr = em.em_find(FileSystemElemAttributes.class, elem.getAttrIdx());

            // READ JOBS FOR THIS FILE IF THIS FILE WAS FOUND DURING SEARCH
            if (sw != null)
            {
                linkList = em.createQuery("select T1 from ArchiveJobFileLink T1 where T1.fileNode_idx=" + elem.getIdx(),  ArchiveJobFileLink.class);
                if (!linkList.isEmpty())
                {
                    HashMap<Long, ArchiveJob> jobMap = new HashMap<Long, ArchiveJob>();

                    jobList = new ArrayList<ArchiveJob>();
                    for (int i = 0; i < linkList.size(); i++)
                    {
                        ArchiveJobFileLink l = linkList.get(i);
                        if (!jobMap.containsKey(l.getArchiveJob().getIdx()))
                        {
                            jobMap.put(l.getArchiveJob().getIdx(), l.getArchiveJob());
                            jobList.add(l.getArchiveJob());
                        }
                    }
                    jobMap.clear();
                }
            }

            // RESOLVE PATH
         /*   if (sp != null)
            {
                path = main.getGuiServerApi().resolvePath(sp, elem);

            }
            else*/ if (sw != null)
            {
                path = main.getGuiServerApi().resolvePath(sw, elem);
            }
        }
        catch (Exception exception)
        {
        }
        TextField txt_path = new TextField(VSMCMain.Txt("Pfad"), path );
        vl.addComponent(txt_path);
        txt_path.setWidth("100%");
        vl.setExpandRatio(txt_path, 1.0f);

        HorizontalLayout paramLayout = new HorizontalLayout();
        VerticalLayout row1 = new VerticalLayout();
        VerticalLayout row2 = new VerticalLayout();
        paramLayout.addComponent(row1);
        paramLayout.addComponent(row2);
        paramLayout.setSizeFull();
        row1.setSizeFull();
        row2.setSizeFull();
        row1.setSpacing(true);
        row2.setSpacing(true);

        

        name.setReadOnly(true);
        DateField at = new SecondDateField(VSMCMain.Txt("LastAccess"), elem.getAtime() );
        row1.addComponent(at);
        at.setReadOnly(true);
        DateField mt = new SecondDateField(VSMCMain.Txt("LastModification"), elem.getMtime() );
        row1.addComponent(mt);
        mt.setReadOnly(true);
        DateField ct = new SecondDateField(VSMCMain.Txt("Created"), elem.getCtime() );
        row1.addComponent(ct);
        ct.setReadOnly(true);
        if (attr != null)
        {
            DateField ts = new SecondDateField(VSMCMain.Txt("TimeStamp"), new Date(attr.getTs()) );
            row1.addComponent(ts);
            ts.setReadOnly(true);
        }
        CheckBox deleted = new CheckBox(VSMCMain.Txt("Gelöscht"), elem.isDeleted());
        row1.addComponent(deleted);
        deleted.setReadOnly(true);

        TextField size = new TextField(VSMCMain.Txt("DataSize"), SizeStr.format(elem.getDataSize()) );
        row2.addComponent(size);
        size.setReadOnly(true);
        TextField asize = new TextField(VSMCMain.Txt("StreamSize"), SizeStr.format(elem.getStreamSize()) );
        row2.addComponent(asize);
        asize.setReadOnly(true);
        TextField user = new TextField(VSMCMain.Txt("User"), Integer.toString(elem.getUid()) );
        row2.addComponent(user);
        user.setReadOnly(true);
        TextField group = new TextField(VSMCMain.Txt("Group"), Integer.toString(elem.getGid()) );
        row2.addComponent(group);
        group.setReadOnly(true);

        vl.addComponent(paramLayout);


        String aclInfoData = elem.getAclinfoData();
        if (aclInfoData != null)
        {
            String a = null;
            if (attr.getAclinfo() == RemoteFSElem.ACLINFO_OSX || attr.getAclinfo() == RemoteFSElem.ACLINFO_ES )
            {
                XStream xs = new XStream();
                String s = ZipUtilities.uncompress(elem.getAclinfoData());
                Object o = xs.fromXML(s);
                if (o instanceof AttributeList)
                {
                    a = o.toString();
                }
            }
            else
            {
                AttributeContainer ac = AttributeContainer.unserialize(aclInfoData);
                a = ac.toString();
            }

            TextArea acl = new TextArea(VSMCMain.Txt("ACL"),a );

            vl.addComponent(acl);
            acl.setWidth("100%");
            vl.setExpandRatio(acl, 1.0f);
            acl.setWordwrap(false);
            acl.setReadOnly(true);
        }
        
        jobPanel = new HorizontalLayout();
        vl.addComponent(jobPanel);
        if (jobList != null && !jobList.isEmpty())
        {
            showJobList();
        }


        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        
        TextField dbidx = new TextField(VSMCMain.Txt("FSEN-ID"), Long.toString(elem.getIdx()) );
        hl.addComponent(dbidx);
        dbidx.setReadOnly(true);
        TextField dbaidx = new TextField(VSMCMain.Txt("FSEA-ID"), Long.toString(elem.getAttrIdx()) );
        hl.addComponent(dbaidx);
        dbaidx.setReadOnly(true);

        vl.addComponent(hl);
        vl.addComponent(new Label(" "));

        Button close = new Button(VSMCMain.Txt("Ok"));

        vl.addComponent(close);
        vl.setComponentAlignment(close, Alignment.BOTTOM_RIGHT);

        final Window w = this;
        close.addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                event.getButton().getApplication().getMainWindow().removeWindow(w);
            }
        });
    }

    ArchivJobTable jobTable;

    void showJobList()
    {
        //btMountVol.setVisible(true);

        ItemClickListener l = new ItemClickListener()
        {

            @Override
            public void itemClick( ItemClickEvent event )
            {
                if (event.getButton() == ItemClickEvent.BUTTON_LEFT && event.isDoubleClick())
                {
                    ArchiveJob job = (ArchiveJob)((BeanItem)event.getItem()).getBean();
                    ArchiveJobInfoWindow win = new ArchiveJobInfoWindow(main, job);

                    // Do something with the reference
                    getApplication().getMainWindow().addWindow(win);
                }

                if (event.getButton() == ItemClickEvent.BUTTON_RIGHT && !event.isDoubleClick())
                {
                    ArchiveJob job = (ArchiveJob)((BeanItem)event.getItem()).getBean();
                    create_archive_popup(event, job);
                }
            }
        };

        jobTable = ArchivJobTable.createTable(main, /*ArchiveJobWin*/null, jobList, l, /*showEdit*/false, /*showDelete*/false);
        jobTable.setSizeFull();

        jobPanel.removeAllComponents();
        jobPanel.setSizeFull();



        jobPanel.addComponent(jobTable);
        jobPanel.setHeight("80px");
    }


    ContextMenu lastArMenu = null;
    void create_archive_popup( ItemClickEvent event, final ArchiveJob job )
    {
        ContextMenu menu = new ContextMenu();

        // Generate main level items
        final ContextMenuItem info = menu.addItem(VSMCMain.Txt("Information"));
        info.setSeparatorVisible(true);
        final ContextMenuItem restore = menu.addItem(VSMCMain.Txt("Restore"));

        menu.addListener(new ContextMenu.ClickListener()
        {

            @Override
            public void contextItemClick( ContextMenu.ClickEvent event )
            {
                // Get reference to clicked item
                ContextMenuItem clickedItem = event.getClickedItem();
                if (clickedItem == info)
                {
                    ArchiveJobInfoWindow win = new ArchiveJobInfoWindow(main, job);

                    // Do something with the reference
                    getApplication().getMainWindow().addWindow(win);
                }

                if (clickedItem == restore)
                {
                    handleRestoreTargetDialog(job);
                }


            }
        }); // Open Context Menu to mouse coordinates when user right clicks layout


        if (lastArMenu != null)
        {
            jobPanel.removeComponent(lastArMenu);
        }

        // HAS TO BE IN VAADIN VIEW
        jobPanel.getApplication().getMainWindow().addComponent(menu);
        lastArMenu = menu;

        menu.show(event.getClientX(), event.getClientY());

    }

    private void handleRestoreTargetDialog( final ArchiveJob job)
    {
        String ip = main.getIp();

        if (job.getSourceType().equals(ArchiveJob.AJ_SOURCE_HF))
        {
            HotFolder hf = VSMCMain.get_base_util_em().em_find( HotFolder.class, job.getSourceIdx());
            if (hf != null)
                ip = hf.getIp();
        }

        final RestoreLocationDlg dlg = new RestoreLocationDlg( main, ip, 8082, "",  /*allowOriginal*/false );
        Button.ClickListener okListener = new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                handleRestoreOkayDialog( dlg, job);
            }
        };
        dlg.setOkListener( okListener );
        jobPanel.getApplication().getMainWindow().addWindow( dlg );
    }




    private void handleRestoreOkayDialog( final RestoreLocationDlg dlg, final ArchiveJob job)
    {
        Button.ClickListener ok = new Button.ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                try
                {
                    String ip = dlg.getIP();
                    int port = dlg.getPort();
                    String path = dlg.getPath();
                    if (dlg.isOriginal())
                    {

                        main.Msg().errmOk(VSMCMain.Txt("ArchivJobs_können_nicht_an_Original_restauriert_werden"));
                            return;
                    }

                    int rflags = GuiServerApi.RF_RECURSIVE | GuiServerApi.RF_RECURSIVE | GuiServerApi.RF_SKIPHOTFOLDER_TIMSTAMPDIR;

                    if (dlg.isCompressed())
                        rflags |= GuiServerApi.RF_COMPRESSION;
                    if (dlg.isEncrypted())
                        rflags |= GuiServerApi.RF_ENCRYPTION;

                    if (sw instanceof SearchWrapper)
                    {
                        boolean rret = main.getGuiServerApi().restoreJob((SearchWrapper)sw, job, ip, port, path, rflags, main.getUser());
                        if (!rret)
                        {
                            main.Msg().errmOk(VSMCMain.Txt("Der_Restore_schlug_fehl"));
                        }
                        else
                        {
                            main.Msg().info(VSMCMain.Txt("Der_Restore_wurde_gestartet"), null);
                        }
                    }
                    else
                    {
                        main.Msg().info(VSMCMain.Txt("Restore von Jobs im dateisystem nicht implementiert -> Suche"), null);
                    }
                }
                catch (Exception ex)
                {
                    main.Msg().errmOk(VSMCMain.Txt("Der_Restore_wurde_abgebrochen"));
                }
            }

        };

        main.Msg().errmOkCancel(VSMCMain.Txt("Wollen_Sie_diesen_Auftrag_restaurieren?"), ok, null);


    }

}
