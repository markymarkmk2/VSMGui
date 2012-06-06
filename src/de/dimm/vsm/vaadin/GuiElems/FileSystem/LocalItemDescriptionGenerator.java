/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.FileSystem;

import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Component;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Administrator
 */
public class LocalItemDescriptionGenerator implements ItemDescriptionGenerator
{
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    @Override
    public String generateDescription( Component source, Object itemId, Object propertyId )
    {
        if (propertyId != null)
        {
            String colName = propertyId.toString();

            if (colName.equals("name"))
            {
                if (itemId instanceof RemoteFSElemTreeElem)
                {
                    RemoteFSElemTreeElem rfs = (RemoteFSElemTreeElem) itemId;
                    String path = rfs.getElem().getPath();
                    return path;
                }
            }
            if (colName.equals("size"))
            {
                if (itemId instanceof RemoteFSElemTreeElem)
                {
                    RemoteFSElemTreeElem rfs = (RemoteFSElemTreeElem) itemId;
                    String path = rfs.getElem().getPath();
                    File f = new File(path);
                    if (f.exists())
                        return Long.toString( f.length());
                }
            }
            if (colName.equals("date"))
            {
                if (itemId instanceof RemoteFSElemTreeElem)
                {
                    RemoteFSElemTreeElem rfs = (RemoteFSElemTreeElem) itemId;
                    String path = rfs.getElem().getPath();
                    File f = new File(path);
                    if (f.exists())
                        return sdf.format(new Date(f.lastModified()));
                }
            }
        }
        return "";
    }
}
