/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Fields;

/**
 *
 * @author Administrator
 */
public class JPAPasswordField extends JPATextField
{
    public JPAPasswordField(String caption, String fieldName)
    {
        super( caption, fieldName );
        password = true;
    }
}
