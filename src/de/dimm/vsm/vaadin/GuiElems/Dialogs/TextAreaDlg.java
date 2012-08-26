/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Dialogs;

import com.vaadin.data.Validator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.vaadin.GuiElems.OkAbortPanel;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;

/**
 *
 * @author Administrator
 */
public class TextAreaDlg extends Window
{
     protected static ArrayList<String> ipList = new ArrayList<String>();
     protected static ArrayList<String> pathList = new ArrayList<String>();

     
     
     protected Button.ClickListener okListener;
     protected TextArea tfText;

     protected Validator validator;
    
    
     protected boolean hasAbort;
     protected boolean rdOnly;

    public TextAreaDlg( String caption, String def, Validator val, boolean hasAbort, boolean rdOnly )
    {
        this.build_gui(caption, def, val, hasAbort, rdOnly);
    }
    public TextAreaDlg( String caption, String def  )
    {
        this.build_gui(caption, def, null, /*hasAbort*/false, /*rdOnly*/ true);
    }

    public void setOkActionListener( ClickListener okListener )
    {
        this.okListener = okListener;
    }

    void build_gui(  String caption, String def, Validator val, boolean hasAbort, boolean rdOnly  )
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
}
