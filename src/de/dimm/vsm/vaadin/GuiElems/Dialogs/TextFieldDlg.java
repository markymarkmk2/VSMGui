/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Dialogs;

import com.vaadin.data.Validator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
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
public class TextFieldDlg extends Window
{
     static ArrayList<String> ipList = new ArrayList<String>();
     static ArrayList<String> pathList = new ArrayList<String>();

     VerticalLayout vl = new VerticalLayout();
     
     Button.ClickListener okListener;
     TextField tfText;
    
     HorizontalLayout targetVl;

    public TextFieldDlg( String title, String caption, String def, Validator val )
    {      
        build_gui(title, caption, def, val);
    }

    public void setOkActionListener( ClickListener okListener )
    {
        this.okListener = okListener;
    }

    final void build_gui( String title, String caption, String def, Validator val )
    {
        setCaption(title);

        addComponent(vl);
        setModal(true);
        setStyleName("vsm");

        //this.setSizeFull();
        this.setWidth("500px");

        vl.setSpacing(true);
        vl.setSizeFull();
        vl.setStyleName("editWin");


        HorizontalLayout buttonVl = new HorizontalLayout();
        buttonVl.setSpacing(true);

        targetVl = new HorizontalLayout();
        
        targetVl.setSpacing(true);
        targetVl.setWidth("60%");

        tfText = new TextField(caption, def);

        if (val != null)
            tfText.addValidator( val );
        
        targetVl.addComponent(tfText);
        targetVl.setComponentAlignment(tfText, Alignment.BOTTOM_LEFT);
        targetVl.setExpandRatio(tfText, 1.0f);

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

                if (okListener != null)
                {
                    okListener.buttonClick(event);
                }
            }
        });

        tfText.focus();
        tfText.selectAll();

    }

    public String getText()
    {
        return tfText.getValue().toString();
    }
}
