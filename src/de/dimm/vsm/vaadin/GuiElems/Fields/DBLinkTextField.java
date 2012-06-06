/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Fields;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;

/**
 *
 * @author Administrator
 */
public class DBLinkTextField extends CustomComponent
{
    TextField tf;
    Button bt;
    public DBLinkTextField(String c)
    {
        tf = new TextField(c);
        
        tf.setImmediate(true);
        setImmediate(true);


        bt = new NativeButton("");

        bt.setStyleName("edit");


        HorizontalLayout hl = new HorizontalLayout();

        hl.addComponent(tf);
        hl.addComponent(bt);

        setCompositionRoot(hl);
    }
    public void setValue( String v)
    {
        boolean wasReadOnly = tf.isReadOnly();
        if (wasReadOnly)
            tf.setReadOnly(false);

        tf.setValue(v);

        if (wasReadOnly)
            tf.setReadOnly(true);
    }
    @Override
    public void setReadOnly( boolean b)
    {
        tf.setReadOnly(b);
        bt.setVisible(!b);
    }

    void addGuiClickListener( ClickListener clickListener )
    {
        bt.addListener(clickListener);
    }

    @Override
    public void setWidth( String width )
    {
        tf.setWidth(width);
    }



}