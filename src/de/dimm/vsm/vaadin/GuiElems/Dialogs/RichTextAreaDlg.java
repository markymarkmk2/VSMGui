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
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.vaadin.GuiElems.OkAbortPanel;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;

/**
 *
 * @author Administrator
 */
public class RichTextAreaDlg extends Window
{    

     Label tfText;
    


    public RichTextAreaDlg( String caption, String def, boolean isHtml )
    {
        build_gui(caption, def, isHtml );
    }
    


    final void build_gui(  String caption, String def, boolean isHtml )
    {
        VerticalLayout main = new VerticalLayout();
        setContent(main);
        main.setSpacing(true);
        main.setSizeFull();
        main.setStyleName("editWin");
        main.setImmediate(true);


        setModal(true);
        setStyleName("vsm");

        //this.setSizeFull();
        this.setWidth("500px");
        this.setHeight("600px");

        this.setCaption(caption);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        hl.setSpacing(true);

       
        tfText = new Label(def);
        if (isHtml)
            ((Label)tfText).setContentMode(Label.CONTENT_XHTML);
   
        tfText.setSizeFull();

        hl.addComponent(tfText);
        hl.setExpandRatio(tfText, 1.0f);
        hl.setMargin(true);


        main.addComponent(hl);
        main.setExpandRatio(hl, 1.0f);



        OkAbortPanel okPanel = new OkAbortPanel();
        okPanel.setOkText(VSMCMain.Txt("Okay"));
        
        addComponent(okPanel);
        
        okPanel.getBtAbort().setVisible(false);
      


        final Window w = this;
        
        okPanel.getBtOk().addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                event.getButton().getApplication().getMainWindow().removeWindow(w);
            }
        });

        okPanel.getBtOk().focus();
    }  
}
