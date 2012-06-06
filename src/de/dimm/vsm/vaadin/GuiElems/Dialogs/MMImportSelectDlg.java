/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Dialogs;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.records.HotFolder;
import de.dimm.vsm.vaadin.GuiElems.OkAbortPanel;
import de.dimm.vsm.vaadin.VSMCMain;

/**
 *
 * @author Administrator
 */
public class MMImportSelectDlg extends Window
{
     
     HotFolder hotfolder = null;
     VerticalLayout vl = new VerticalLayout();
     TextField from;
     TextField till;
     CheckBox deleted;
     boolean okay = false;
     Button.ClickListener okListener;

    public MMImportSelectDlg(  HotFolder hf)
    {
        hotfolder = hf;        

        build_gui();
    }

    public void setOkListener( ClickListener okListener )
    {
        this.okListener = okListener;
    }


    final void build_gui()
    {
        addComponent(vl);
        setModal(true);
        setStyleName("vsm");

        //this.setSizeFull();
        this.setWidth("400px");

        vl.setSpacing(true);
        vl.setSizeFull();
        vl.setStyleName("editWin");

        this.setCaption(VSMCMain.Txt("Auswahl MM Importoptionen"));

        from = new TextField(VSMCMain.Txt("ab Job-Nr."), "" );
        vl.addComponent(from);
        till = new TextField(VSMCMain.Txt("bis Job-Nr."), "" );
        vl.addComponent(till);

        deleted = new CheckBox(VSMCMain.Txt("Ältere Versionen ebenfalls importieren"));
        vl.addComponent(deleted);


        OkAbortPanel pn = new OkAbortPanel();
        pn.getBtRetry().setVisible(false);
        pn.setOkText(VSMCMain.Txt("Weiter"));
        vl.addComponent(pn);

        final Window w = this;
        pn.getBtOk().addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                okay = true;
                event.getButton().getApplication().getMainWindow().removeWindow(w);
                if (okListener != null)
                    okListener.buttonClick(event);

            }
        });
        pn.getBtAbort().addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                okay = false;
                event.getButton().getApplication().getMainWindow().removeWindow(w);
            }
        });
    }

    public boolean isOkay()
    {
        return okay;
    }

    public boolean isImportDeleted()
    {
        return deleted.booleanValue();
    }
    public long getFrom()
    {
        try
        {
            return Long.parseLong(from.getValue().toString());
        }
        catch (NumberFormatException numberFormatException)
        {
        }
        return 0;
    }
    public long getTill()
    {
        try
        {
            return Long.parseLong(till.getValue().toString());
        }
        catch (NumberFormatException numberFormatException)
        {
        }
        return 0;
    }

}
