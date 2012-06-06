/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Fields;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;

/**
 *
 * @author Administrator
 */
public class JPAReadOnlyDateField extends JPADateField
{
    public JPAReadOnlyDateField(String caption, String fieldName, String format)
    {
        super( caption, fieldName, format );
    }
    public JPAReadOnlyDateField(String caption, String fieldName, int res )
    {
        super( caption, fieldName, res );
    }

    @Override
    public Component createGui(Object _node)
    {
        Component gui = super.createGui( _node);


        gui.setReadOnly(true);

        return gui;
    }

    @Override
    public void setReadOnly( Component gui, boolean b )
    {
        return;
    }

}
