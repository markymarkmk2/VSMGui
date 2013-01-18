/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Fields;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import de.dimm.vsm.records.AbstractStorageNode;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.VSMCMain;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 *
 * @author Administrator
 */
public class JPACloneNodeComboField extends JPAAbstractComboField
{

    VSMCMain main;
    StoragePool pool;

   
    public JPACloneNodeComboField( VSMCMain main, StoragePool pool, String fieldName)
    {
        super( VSMCMain.Txt("CloneStorageNode"), fieldName );
        this.main = main;
        this.pool = pool;
    }

    @Override
    public List<ComboEntry> getEntries()
    {
        List<ComboEntry> entries = new ArrayList<ComboEntry>();

        List<AbstractStorageNode> list = pool.getStorageNodes(VSMCMain.get_util_em(pool));

        AbstractStorageNode actNode = (AbstractStorageNode)node;

        // ADD ALL NODES BIGGER AS OUR OWN NODE
        for (int i = 0; i < list.size(); i++)
        {
            AbstractStorageNode object = list.get(i);
            
            // Den eigenen CloneNode immer dazupacken
            if (actNode != null && actNode.getCloneNode() != null && object.getIdx() == actNode.getCloneNode().getIdx())
            {
                ComboEntry ce = new ComboEntry(object, object.getName());
                entries.add(ce);
                continue;
            }

            if (!object.isOnline() )
                continue;

            if (node != null && node instanceof AbstractStorageNode)
            {
               
                if (object.getIdx() <= actNode.getIdx())
                    continue;
            }

            String name =  object.getName();

            ComboEntry ce = new ComboEntry(object, name);
            entries.add(ce);
        }

        return entries;
    }

    @Override
    public Component createGui( Object _node )
    {
        // ALLOW DESELCTION OF COMBONODE
        ComboBox cb = (ComboBox) super.createGui(_node);
        cb.setNullSelectionAllowed(true);
        return cb;
    }



    
  
}
