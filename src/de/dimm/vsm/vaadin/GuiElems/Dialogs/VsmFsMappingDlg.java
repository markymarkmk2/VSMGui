/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Dialogs;

import com.vaadin.data.Validator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.vaadin.GuiElems.OkAbortPanel;
import de.dimm.vsm.vaadin.VSMCMain;

/**
 *
 * @author Administrator
 */
public class VsmFsMappingDlg extends TextAreaDlg
{
    TextField tf;
    VSMCMain main;
    
    
    public VsmFsMappingDlg( String caption, String def, Validator val, VSMCMain main)
    {
        super( caption, def, val, true, true );
        this.main = main;
    }
   

    @Override
    final void build_gui(  String caption, String def, Validator val, boolean hasAbort, boolean rdOnly  )
    {
        VerticalLayout main = new VerticalLayout();
        setContent(main);
        main.setSpacing(true);
        main.setSizeFull();
        main.setStyleName("editWin");
        main.setImmediate(true);

        validator = val;

        setModal(true);
        setStyleName("vsm");

        //this.setSizeFull();
        this.setWidth("500px");
        this.setHeight("600px");

        this.setCaption(caption);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        hl.setSpacing(true);


      
        tfText = new TextArea("", def);
        tfText.setReadOnly(rdOnly);
        if (val != null)
            ((TextArea)tfText).addValidator( val );

        tfText.setSizeFull();
        hl.addComponent(tfText);
        hl.setExpandRatio(tfText, 1.0f);


        main.addComponent(hl);
        main.setExpandRatio(hl, 1.0f);


//        HorizontalLayout hl2 = new HorizontalLayout();
//        hl2.setSizeFull();
//        hl2.setSpacing(true);
//
//        tf = new TextField("VSM-Pfad");
//        tf.setWidth("100%");
//        Button nt = new NativeButton("...", new ClickListener() {
//
//            @Override
//            public void buttonClick( ClickEvent event )
//            {
//                searchDBPath();
//            }
//        });
//        hl2.addComponent(tf);
//        hl2.addComponent(nt);
//        hl2.setExpandRatio(tf, 1.0f);
//
//        main.addComponent(hl2);


        OkAbortPanel okPanel = new OkAbortPanel();
        okPanel.setOkText(VSMCMain.Txt("Weiter"));
        
        addComponent(okPanel);
        
        if (!hasAbort)
            okPanel.getBtAbort().setVisible(false);
      


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
                if (validator != null && !validator.isValid(getText()))
                        return;

                event.getButton().getApplication().getMainWindow().removeWindow(w);

                if (okListener != null)
                {
                    okListener.buttonClick(event);
                }
            }
        });

        okPanel.getBtOk().focus();
    }

    public String getText()
    {
        return tfText.getValue().toString();
    }

//    void searchDBPath()
//    {
//        //StoragePoolWrapper wr = main.getGuiServerApi().openPoolView(pool, true, pool.getRootDir(), main.getUser());
//
//        FSTree tr = DBFSElemEditor.createClientPathTree(main.getGuiServerApi(), wr,tf,  "/", main.getUser(),  DBFSElemEditor.ONLY_DIRS, false, null, null);
//        editFSTree( tr, wr, main.getGuiServerApi(), tf);
//
//        JPAPoolComboField poolCombo = new JPAPoolComboField( main, "poolIdx");
//
//
//        JPADBFSField dbfs = new JPADBFSField(VSMCMain.Txt("Datenbankpfad"), "basePath", main, poolCombo );
//
//        poolCombo.createGui(null);
//        dbfs.createGui(null);


//    }
//
//    void editFSTree( final FSTree treePanel, final StoragePoolWrapper wrapper, final GuiServerApi api, final TextField tf )
//    {
//        if (treePanel == null)
//            return;
//
//        final OkAbortPanel buttonPanel = new OkAbortPanel();
//
//
//        final VerticalLayout vl = new VerticalLayout();
//        vl.addComponent(treePanel);
//        vl.setExpandRatio(treePanel, 1.0f);
//        vl.addComponent(buttonPanel);
//        vl.setComponentAlignment(buttonPanel, Alignment.BOTTOM_RIGHT);
//        vl.setSizeFull();
//        vl.setImmediate(true);
//        vl.setMargin(true);
//
//
//        final Window win = new Window();
//        win.setContent(vl);
//        win.setWidth("650px");
//        win.setHeight("50%");
//        win.setCaption(VSMCMain.Txt("Pfadauswahl"));
//        win.setModal(true);
//        win.setStyleName("vsm");
//
//        this.getApplication().getMainWindow().addWindow(win);
//
//        buttonPanel.getBtOk().addListener( new Button.ClickListener()
//        {
//
//            @Override
//            public void buttonClick( ClickEvent event )
//            {
//                Object o = treePanel.getValue();
//                if (o instanceof RemoteFSElemTreeElem)
//                {
//                    RemoteFSElemTreeElem rfs = (RemoteFSElemTreeElem)o;
//                    tf.setValue(DBFSElemEditor.buildPath(rfs));
//                }
//                api.closePoolView(wrapper);
//
//
//                event.getButton().getApplication().getMainWindow().removeWindow(win);
//            }
//        });
//        buttonPanel.getBtAbort().addListener( new Button.ClickListener()
//        {
//
//            @Override
//            public void buttonClick( ClickEvent event )
//            {
//                api.closePoolView(wrapper);
//                event.getButton().getApplication().getMainWindow().removeWindow(win);
//            }
//        });
//
//
//    }
}
