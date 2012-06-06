/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Dialogs;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.Utilities.SizeStr;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.net.AttributeContainer;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.net.SearchWrapper;
import de.dimm.vsm.net.StoragePoolWrapper;
import de.dimm.vsm.records.FileSystemElemAttributes;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.Date;

/**
 *
 * @author Administrator
 */
public class FileinfoWindow extends Window
{

     RemoteFSElem elem;
     VSMCMain main;
     SearchWrapper sw = null;
     StoragePoolWrapper sp = null;
     VerticalLayout vl = new VerticalLayout();
     long poolIdx;

    public FileinfoWindow( VSMCMain main, SearchWrapper sw, RemoteFSElem elem )
    {
        this.elem = elem;
        this.main = main;
        this.sw = sw;
        poolIdx = sw.getPoolIdx();

        build_gui(elem);
    }
    public FileinfoWindow( VSMCMain main, StoragePoolWrapper sp, RemoteFSElem elem )
    {
        this.elem = elem;
        this.main = main;
        this.sp = sp;
        poolIdx = sp.getPoolIdx();

        build_gui(elem);
    }

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
        try
        {
            StoragePool pool = main.getStoragePool(poolIdx);
            GenericEntityManager em = main.get_util_em(pool);
            attr = em.em_find(FileSystemElemAttributes.class, elem.getAttrIdx());

            if (sp != null)
            {
                path = main.getGuiServerApi().resolvePath(sp, elem);

            }
            else if (sw != null)
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

        name.setReadOnly(true);
        DateField at = new DateField(VSMCMain.Txt("LastAccess"), elem.getAtime() );
        vl.addComponent(at);
        at.setReadOnly(true);
        DateField mt = new DateField(VSMCMain.Txt("LastModification"), elem.getMtime() );
        vl.addComponent(mt);
        mt.setReadOnly(true);
        DateField ct = new DateField(VSMCMain.Txt("Created"), elem.getCtime() );
        vl.addComponent(ct);
        ct.setReadOnly(true);

        TextField size = new TextField(VSMCMain.Txt("DatatSize"), SizeStr.format(elem.getDataSize()) );
        vl.addComponent(size);
        size.setReadOnly(true);
        TextField asize = new TextField(VSMCMain.Txt("AttributeSize"), SizeStr.format(elem.getStreamSize()) );
        vl.addComponent(asize);
        asize.setReadOnly(true);

        TextField user = new TextField(VSMCMain.Txt("User"), Integer.toString(elem.getUid()) );
        vl.addComponent(user);
        user.setReadOnly(true);
        TextField group = new TextField(VSMCMain.Txt("Group"), Integer.toString(elem.getGid()) );
        vl.addComponent(group);
        group.setReadOnly(true);
        CheckBox deleted = new CheckBox(VSMCMain.Txt("Gelöscht"), elem.isDeleted());
        vl.addComponent(deleted);
        deleted.setReadOnly(true);

        if (attr != null)
        {
            DateField ts = new DateField(VSMCMain.Txt("TimeStamp"), new Date(attr.getTs()) );
            vl.addComponent(ts);
            ts.setReadOnly(true);
        }

        String aclInfoData = elem.getAclinfoData();
        if (aclInfoData != null)
        {
            AttributeContainer ac = AttributeContainer.unserialize(aclInfoData);

            TextArea acl = new TextArea(VSMCMain.Txt("ACL"),ac.toString() );

            vl.addComponent(acl);
            acl.setWidth("100%");
            vl.setExpandRatio(acl, 1.0f);
            acl.setWordwrap(false);
            acl.setReadOnly(true);
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

        Button close = new NativeButton(VSMCMain.Txt("Ok"));

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
}
