/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Dialogs;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.records.MountEntry;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.PoolQryEditor;
import de.dimm.vsm.vaadin.GuiElems.OkAbortPanel;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.MountEntryTable;
import de.dimm.vsm.vaadin.SelectObjectCallback;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class NewPoolQryDlg {
     MountEntry mountEntry;
     VSMCMain main;
     ClickListener okClick;

    public NewPoolQryDlg(VSMCMain main)
    {
        this.main = main;
        mountEntry = new MountEntry();
        mountEntry.setTyp(MountEntry.TYP_RDONLY);
        mountEntry.setSubPath("/");
        mountEntry.setIp("127.0.0.1");
        mountEntry.setPort(8082);
        mountEntry.setMountPath(RemoteFSElem.createDir("/"));
        mountEntry.setName(VSMCMain.Txt("Manueller Mount"));
    }

    public void setOkClick( ClickListener okClick )
    {
        this.okClick = okClick;
    }


    public MountEntry getMountEntry()
    {
        return mountEntry;
    }

    public void openDlg()
    {
        List<StoragePool> list = main.getStoragePoolList();
         SelectObjectCallback cb = new SelectObjectCallback<StoragePool>()
         {
             @Override
             public void SelectedAction( StoragePool pool )
             {
                 mountEntry.setPool(pool);
                  openDlg( pool );
             }
        };
        main.SelectObject(StoragePool.class, VSMCMain.Txt("Pool"), VSMCMain.Txt("Weiter"), list, cb);
    }

    void openDlg(StoragePool pool)
    {
        Window c = buildGui(pool);
        main.getRootWin().addWindow(c);
    }

    Window buildGui(StoragePool pool)
    {
        final Window win = new Window(VSMCMain.Txt("VSM-Dateisystem Auswahl"));
        win.setWidth("550px");
        GenericEntityManager em = VSMCMain.get_util_em(pool);
        ValueChangeListener vcl = new ValueChangeListener() {

            @Override
            public void valueChange( ValueChangeEvent event )
            {
                
            }
        };

        VerticalLayout vl = new VerticalLayout();
        win.addComponent(vl);
        vl.setSpacing(true);
        vl.setSizeFull();

        JPACheckBox cbSd = new JPACheckBox(VSMCMain.Txt("Gelöschte Dateien anzeigen"), "showDeleted");
        vl.addComponent(cbSd.createGui(mountEntry));

        PoolQryEditor qryEd = new PoolQryEditor(main, em, mountEntry, vcl);
        vl.addComponent(qryEd);
       
        OkAbortPanel okPanel = new OkAbortPanel();
        okPanel.setOkText(VSMCMain.Txt("Weiter"));

        vl.addComponent(okPanel);

        okPanel.getBtOk().addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                if (!MountEntryTable.isValid(mountEntry, win))
                    return;
                event.getButton().getApplication().getMainWindow().removeWindow(win);
                okClick.buttonClick(event);
            }
        });
        okPanel.getBtAbort().addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                event.getButton().getApplication().getMainWindow().removeWindow(win);
            }
        });

        okPanel.getBtOk().focus();
        return win;
    }


}
