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
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.Utilities.SizeStr;
import de.dimm.vsm.net.SearchWrapper;
import de.dimm.vsm.net.StoragePoolWrapper;
import de.dimm.vsm.records.ArchiveJob;
import de.dimm.vsm.records.HotFolder;
import de.dimm.vsm.vaadin.VSMCMain;

/**
 *
 * @author Administrator
 */
public class ArchiveJobInfoWindow extends Window
{

     ArchiveJob elem;
     VSMCMain main;
     //SearchWrapper sw = null;
     //StoragePoolWrapper sp = null;
     VerticalLayout vl = new VerticalLayout();

    public ArchiveJobInfoWindow( VSMCMain main,  ArchiveJob elem )
    {
        this.elem = elem;
        this.main = main;
       // this.sw = sw;

        build_gui(elem);
    }

    final void build_gui( ArchiveJob job )
    {
        addComponent(vl);
        setModal(true);
        setStyleName("vsm");

        //this.setSizeFull();
        this.setWidth("400px");

        vl.setSpacing(true);
        vl.setSizeFull();
        vl.setStyleName("editWin");

        this.setCaption(VSMCMain.Txt("Informationen_für") + " " + job.getName());

        TextField name = new TextField(VSMCMain.Txt("Name"), job.getName() );
        vl.addComponent(name);
        name.setWidth("100%");
        vl.setExpandRatio(name, 1.0f);

        name.setReadOnly(true);
        DateField at = new DateField(VSMCMain.Txt("LastAccess"), job.getLastAccess() );
        vl.addComponent(at);
        at.setReadOnly(true);
        DateField mt = new DateField(VSMCMain.Txt("Start"), job.getStartTime() );
        vl.addComponent(mt);
        mt.setReadOnly(true);
        DateField ct = new DateField(VSMCMain.Txt("End"), job.getEndTime() );
        vl.addComponent(ct);
        ct.setReadOnly(true);

        TextField size = new TextField(VSMCMain.Txt("Size"), SizeStr.format(job.getTotalSize()) );
        vl.addComponent(size);
        size.setReadOnly(true);

        CheckBox ok = new CheckBox(VSMCMain.Txt("ok"), job.isOk());
        vl.addComponent(ok);
        ok.setReadOnly(true);

        String src = "?";
        if (job.getSourceType().equals(ArchiveJob.AJ_SOURCE_MM))
        {
            src = "MediaManager ID:" + job.getSourceIdx();
        }

        if (job.getSourceType().equals(ArchiveJob.AJ_SOURCE_HF))
        {
            HotFolder hf = VSMCMain.get_base_util_em().em_find( HotFolder.class, job.getSourceIdx());
            src = "Hotfolder " + hf.getName();
        }

        TextField source = new TextField(VSMCMain.Txt("Quelle"), src );
        vl.addComponent(source);
        vl.setExpandRatio(source, 1.0f);
        source.setWidth("100%");
        source.setReadOnly(true);


        vl.addComponent(new Label(" "));

        Button close = new NativeButton(VSMCMain.Txt("Ok"));

        vl.addComponent(close);
        vl.setComponentAlignment(close, Alignment.BOTTOM_RIGHT);

        final Window w = this;
        close.addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                event.getButton().getApplication()./*getMainWindow().*/removeWindow(w);
            }
        });
    }
}
