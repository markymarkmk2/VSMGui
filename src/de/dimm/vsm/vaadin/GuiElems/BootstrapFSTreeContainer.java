/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems;

import de.dimm.vsm.auth.User;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTreeColumn;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.FSTreeContainer;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.RemoteProvider;
import java.util.ArrayList;

/**
 *
 * @author Administrator
 */
public class BootstrapFSTreeContainer extends FSTreeContainer
{

    public BootstrapFSTreeContainer( RemoteProvider provider, ArrayList<FSTreeColumn> fields)
    {
        super( provider, fields );
    }


}
