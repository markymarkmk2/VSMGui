/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.FileSystem;

/**
 *
 * @author Administrator
 */
public class FSTreeColumn
{
    String fieldName;
    String caption;
    int size;
    float exRatio;
    Class clazz;
    String alignment;

    public FSTreeColumn( String fieldName, String caption, int size, float exRatio, String alignment, Class clazz )
    {
        this.fieldName = fieldName;
        this.caption = caption;
        this.size = size;
        this.exRatio = exRatio;
        this.alignment = alignment;
        this.clazz = clazz;
    }


}
