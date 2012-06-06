/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Dialogs;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteFSElemEditor;
import de.dimm.vsm.vaadin.GuiElems.OkAbortPanel;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class RestoreLocationDlg extends Window implements Property.ValueChangeListener
{
     static ArrayList<String> ipList = new ArrayList<String>();

     VerticalLayout vl = new VerticalLayout();
     OptionGroup select;
     Button.ClickListener okListener;
     TextField tfPort;
     ComboBox tfIP;
     RemoteFSElemEditor editor;
     HorizontalLayout targetVl;

    public RestoreLocationDlg( String ip, int port, String path, boolean allowOrig )
    {
        build_gui(ip, port, path, allowOrig);
    }

    public void setOkListener( ClickListener okListener )
    {
        this.okListener = okListener;
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
        if (!ipList.contains(ip))
            ipList.add(ip);


        tfIP = new ComboBox(VSMCMain.Txt("IP"), ipList );
        tfIP.setNullSelectionAllowed(false);
        tfIP.setInvalidAllowed(true);
        tfIP.setNewItemsAllowed(true);
        tfIP.setValue(ip);
        targetVl.addComponent(tfIP);
        targetVl.setComponentAlignment(tfIP, Alignment.BOTTOM_LEFT);
        tfPort = new TextField(VSMCMain.Txt("Port"), Integer.toString(port) );
        tfPort.addValidator( new IntegerValidator(""));
        
        targetVl.addComponent(tfPort);
        targetVl.setComponentAlignment(tfPort, Alignment.BOTTOM_LEFT);


        editor = new RemoteFSElemEditor(  VSMCMain.Txt("Zielpfad"), path, tfIP, tfPort, RemoteFSElemEditor.ONLY_DIRS);
        targetVl.addComponent(editor);
        buttonVl.addComponent(select);
        buttonVl.addComponent(targetVl);
        buttonVl.setComponentAlignment(targetVl, Alignment.BOTTOM_RIGHT);


        OkAbortPanel okPanel = new OkAbortPanel();
        okPanel.setOkText(VSMCMain.Txt("Weiter"));
        vl.addComponent(buttonVl);
        vl.addComponent(okPanel);

        addComponent(vl);


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
        return editor.getTf().getValue().toString();
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
}
