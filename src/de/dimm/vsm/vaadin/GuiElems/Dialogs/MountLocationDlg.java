/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Dialogs;

import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteFSElemEditor;
import de.dimm.vsm.vaadin.GuiElems.OkAbortPanel;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Administrator
 */
public class MountLocationDlg extends Window
{
     static ArrayList<String> ipList = new ArrayList<String>();
     static ArrayList<String> pathList = new ArrayList<String>();

     VerticalLayout vl = new VerticalLayout();
     
     Button.ClickListener okListener;
     TextField tfPort;
     ComboBox tfIP;
     RemoteFSElemEditor editor;
     DateField timestamp;
     HorizontalLayout targetVl;

    public MountLocationDlg( String ip, int port, String path )
    {
        build_gui(ip, port, path);
    }

    public void setOkListener( ClickListener okListener )
    {
        this.okListener = okListener;
    }

    final void build_gui( String ip, int port, String path)
    {
        addComponent(vl);
        setModal(true);
        setStyleName("vsm");

        //this.setSizeFull();
        this.setWidth("700px");

        vl.setSpacing(true);
        vl.setSizeFull();
        vl.setStyleName("editWin");


        HorizontalLayout buttonVl = new HorizontalLayout();
        buttonVl.setSpacing(true);

        targetVl = new HorizontalLayout();
        
        targetVl.setSpacing(true);
        if (!ipList.contains(ip))
            ipList.add(ip);
        if (!pathList.contains(path))
            pathList.add(path);


        tfIP = new ComboBox(VSMCMain.Txt("IP"), ipList );
        tfIP.setNullSelectionAllowed(false);
        tfIP.setNewItemsAllowed(true);
        tfIP.select(ipList.get(0));

        targetVl.addComponent(tfIP);
        targetVl.setComponentAlignment(tfIP, Alignment.BOTTOM_LEFT);
        tfPort = new TextField(VSMCMain.Txt("Port"), Integer.toString(port) );
        tfPort.addValidator( new IntegerValidator(""));
        
        targetVl.addComponent(tfPort);
        targetVl.setComponentAlignment(tfPort, Alignment.BOTTOM_LEFT);

        timestamp = new DateField(VSMCMain.Txt("Timestamp"), new Date());
        targetVl.addComponent(timestamp);

        editor = new RemoteFSElemEditor(VSMCMain.Txt("Mountpfad"), pathList, tfIP, tfPort, RemoteFSElemEditor.ONLY_DIRS);
        targetVl.addComponent(editor);       
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
                String path = getPath();

                if (!ipList.contains(ip))
                    ipList.add(ip);
                if (!pathList.contains(path))
                    pathList.add(path);

                if (okListener != null)
                {
                    okListener.buttonClick(event);
                }
            }
        });
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

    public Date getDate()
    {
        return (Date) timestamp.getValue();
    }

    public void setIP( String host )
    {
        if (!ipList.contains(host))
            ipList.add(host);
        
        tfIP.setValue(host);
    }
}
