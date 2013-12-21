/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.FileSystem;

import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteFSElemTreeElem;
import java.util.List;
import org.vaadin.peter.contextmenu.ContextMenu;

/**
 *
 * @author Administrator
 */
public interface IContextMenuCallback
{
     public void handleRestoreTargetDialog( List<RemoteFSElemTreeElem> rfstreeelems );
     public void handleDownload( RemoteFSElemTreeElem singleRfstreeelem );
}
