/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import de.dimm.vsm.net.LogQuery;

/**
 *
 * @author Administrator
 */
public interface LoqQueryProvider
{
    public LogQuery getLogQuery();
    int getCnt();
    int getOffset();

}
