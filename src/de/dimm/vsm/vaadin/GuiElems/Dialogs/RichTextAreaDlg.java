/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Dialogs;

import com.vaadin.event.MouseEvents;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.vaadin.GuiElems.OkAbortPanel;
import de.dimm.vsm.vaadin.VSMCMain;

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
        main.setMargin(true);
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

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setSpacing(true);
        
      
        
       
        tfText = new Label(def);
        if (isHtml)
            ((Label)tfText).setContentMode(Label.CONTENT_XHTML);
   
        tfText.setSizeUndefined();
        
         final Panel panel = new Panel();
         panel.setScrollable(true);
         panel.addComponent(tfText);
         panel.setSizeFull();
         
         
         
         vl.addComponent(panel);
         vl.setExpandRatio(panel, 1.0f);
         vl.setMargin(true);

         
         
        HorizontalLayout scrollButtons = new HorizontalLayout();
        vl.addComponent(scrollButtons);
        
        Button scrollUp = new Button("Scroll Up");
        scrollUp.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 8557421620094669457L;

            public void buttonClick(ClickEvent event) {
                int scrollPos = panel.getScrollTop() - 250;
                if (scrollPos < 0)
                    scrollPos = 0;
                panel.setScrollTop(scrollPos);
            }
        });
        scrollButtons.addComponent(scrollUp);
        
        Button scrollDown = new Button("Scroll Down");
        scrollDown.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 8557421620094669457L;

            public void buttonClick(ClickEvent event) {
                int scrollPos = panel.getScrollTop();
                if (scrollPos > 1000)
                    scrollPos = 1000;
                panel.setScrollTop(scrollPos + 250);
            }
        });
        scrollButtons.addComponent(scrollDown);    
        scrollButtons.setSizeFull();
        
     

        main.addComponent(vl);
        main.setExpandRatio(vl, 1.0f);



        OkAbortPanel okPanel = new OkAbortPanel();
        okPanel.setOkText(VSMCMain.Txt("Schließen"));
        
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
