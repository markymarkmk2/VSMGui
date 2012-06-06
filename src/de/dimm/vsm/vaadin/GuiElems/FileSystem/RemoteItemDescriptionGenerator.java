/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.FileSystem;

import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Component;
import de.dimm.vsm.net.SearchWrapper;
import de.dimm.vsm.net.StoragePoolWrapper;
import de.dimm.vsm.vaadin.VSMCMain;
import java.text.SimpleDateFormat;

/**
 *
 * @author Administrator
 */
public class RemoteItemDescriptionGenerator implements ItemDescriptionGenerator
{
    StoragePoolWrapper pwrapper;
    SearchWrapper swrapper;
    VSMCMain main;

    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public RemoteItemDescriptionGenerator( StoragePoolWrapper wrapper, VSMCMain main )
    {
        this.pwrapper = wrapper;
        this.main = main;
    }
    public RemoteItemDescriptionGenerator( SearchWrapper swrapper, VSMCMain main )
    {
        this.swrapper = swrapper;
        this.main = main;
    }


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
                    if (rfs.getElem() != null)
                    {
                        try
                        {
                            if (pwrapper != null)
                            {
                                String path = main.getGuiServerApi().resolvePath(pwrapper, rfs.getElem());
                                return path;
                            }
                            if (swrapper != null)
                            {
                                String path = main.getGuiServerApi().resolvePath(swrapper, rfs.getElem());
                                return path;
                            }
                        }
                        catch (Exception sQLException)
                        {
                        }
                    }
                }
            }
            if (colName.equals("size"))
            {
                if (itemId instanceof RemoteFSElemTreeElem)
                {
                    RemoteFSElemTreeElem rfs = (RemoteFSElemTreeElem) itemId;
                    String r = Long.toString( rfs.elem.getDataSize() );
                    if (rfs.elem.getStreamSize() > 0)
                        r = "Data: " + r +  " EA: " + rfs.elem.getStreamSize();

                    return r;
                }
            }
            if (colName.equals("date"))
            {
                if (itemId instanceof RemoteFSElemTreeElem)
                {
                    RemoteFSElemTreeElem rfs = (RemoteFSElemTreeElem) itemId;
                    String path = rfs.getElem().getPath();
                    return "Access: " + sdf.format(rfs.getElem().getAtime()) + " Creation: " + sdf.format(rfs.getElem().getCtime()) + " Modif.: " + sdf.format(rfs.getElem().getMtime());
                }
            }

        }
        return "";
    }
}