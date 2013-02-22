/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Dialogs;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.vaadin.GuiElems.OkAbortPanel;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class ComboBoxDlg<T> extends Window
{
   
     VerticalLayout vl = new VerticalLayout();
     
     Button.ClickListener okListener;
     ComboBox cb;
     CheckBox bt;
    
     HorizontalLayout targetVl;

    public ComboBoxDlg( String title, String caption, List<T> list )
    {      
        build_gui(title, caption, null, list);
    }
    public ComboBoxDlg( String title, String caption, String btCaption, List<T> list )
    {      
        build_gui(title, caption, btCaption, list);
    }

    public void setOkActionListener( ClickListener okListener )
    {
        this.okListener = okListener;
    }

    final void build_gui( String title, String caption, String btCaption, List<T> list  )
    {
        setCaption(title);

        addComponent(vl);
        setModal(true);
        setStyleName("vsm");

        //this.setSizeFull();
        this.setWidth("400px");

        vl.setSpacing(true);
        vl.setSizeFull();
        vl.setStyleName("editWin");


        HorizontalLayout buttonVl = new HorizontalLayout();
        buttonVl.setSpacing(true);

        targetVl = new HorizontalLayout();
        
        targetVl.setSpacing(true);
        targetVl.setWidth("80%");

        cb = new ComboBox(caption, list);
        cb.setImmediate( true );
        cb.setWidth( "100%");
        cb.setNewItemsAllowed( false);
        cb.setNullSelectionAllowed( false);
        if (!list.isEmpty())
            cb.setValue( list.get( 0) );
        
        
        
        targetVl.addComponent(cb);
        targetVl.setComponentAlignment(cb, Alignment.BOTTOM_LEFT);
        targetVl.setExpandRatio(cb, 1.0f);
        
        if (btCaption != null)
        {
            bt = new CheckBox( btCaption);
        }

        buttonVl.addComponent(targetVl);
        buttonVl.setComponentAlignment(targetVl, Alignment.BOTTOM_LEFT);
        buttonVl.setExpandRatio(targetVl, 1.0f);
        buttonVl.setSizeFull();


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

        cb.focus();
        
    }

    public T getValue()
    {
        return (T)cb.getValue();
    }
    public boolean isButton()
    {
        if (bt != null)
            return bt.booleanValue();
        
        return false;
    }
}
