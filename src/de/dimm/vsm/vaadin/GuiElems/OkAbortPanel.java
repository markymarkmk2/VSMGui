/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import de.dimm.vsm.vaadin.VSMCMain;

/**
 *
 * @author Administrator
 */
public class OkAbortPanel extends HorizontalLayout
{
    NativeButton btOk;
    NativeButton btAbort;
    NativeButton btRetry;

    public OkAbortPanel()
    {
        setWidth("100%");
        HorizontalLayout bt_hl = new HorizontalLayout();
        bt_hl.setSpacing(true);
        //bt_hl.setWidth("100%");

        btOk = new NativeButton(VSMCMain.Txt("Schließen"));
        btAbort = new NativeButton(VSMCMain.Txt("Abbruch"));
        btRetry = new NativeButton(VSMCMain.Txt("Wiederholen"));
        btRetry.setVisible(false);
        
        bt_hl.addComponent(btRetry);
        bt_hl.setComponentAlignment(btRetry, Alignment.MIDDLE_LEFT);
        bt_hl.addComponent(btAbort);
        bt_hl.setComponentAlignment(btAbort, Alignment.MIDDLE_RIGHT);
        bt_hl.addComponent(btOk);
        bt_hl.setComponentAlignment(btOk, Alignment.MIDDLE_RIGHT);
        addComponent(bt_hl);
        setComponentAlignment(bt_hl, Alignment.MIDDLE_RIGHT);
    }
    public void setOkText(String txt )
    {
        btOk.setCaption(txt);
    }
    public void setAbortText(String txt )
    {
        btAbort.setCaption(txt);
    }

    public NativeButton getBtOk()
    {
        return btOk;
    }

    public NativeButton getBtAbort()
    {
        return btAbort;
    }

    public NativeButton getBtRetry()
    {
        return btRetry;
    }

}
