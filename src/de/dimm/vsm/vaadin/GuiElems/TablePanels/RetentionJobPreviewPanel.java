/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import de.dimm.vsm.records.RetentionJob;
import de.dimm.vsm.vaadin.GuiElems.Table.PreviewPanel;

/**
 *
 * @author Administrator
 */
public class RetentionJobPreviewPanel extends PreviewPanel<RetentionJob>
{
    public RetentionJobPreviewPanel( RetentionJobTable j, boolean readOnly )
    {
        super(j, readOnly);       
    }

    @Override
    public void recreateContent( final RetentionJob node )
    {
        super.recreateContent( node );
    }
    
    
    void updateVisibility(final RetentionJob node )
    {
    }
}
