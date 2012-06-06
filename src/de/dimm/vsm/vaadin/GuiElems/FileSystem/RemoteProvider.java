/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.FileSystem;

import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import de.dimm.vsm.net.RemoteFSElem;
import java.util.List;

/**
 *
 * @author Administrator
 */
public interface RemoteProvider
{
    RemoteFSElemTreeElem createNode( RemoteProvider provider, RemoteFSElem elem, RemoteFSElemTreeElem parent);

    List<RemoteFSElemTreeElem> getChildren(RemoteFSElemTreeElem elem);

    boolean createDir(RemoteFSElemTreeElem elem);

    ItemDescriptionGenerator getItemDescriptionGenerator();

}
