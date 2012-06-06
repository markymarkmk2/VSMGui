/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Fields;

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
public class JPAPoolComboField extends JPAAbstractComboField
{

    VSMCMain main;

   
    public JPAPoolComboField( VSMCMain main, String fieldName)
    {
        super( VSMCMain.Txt("StoragePool"), fieldName );
        this.main = main;        
    }

    @Override
    public List<ComboEntry> getEntries()
    {
        List<ComboEntry> entries = new ArrayList<ComboEntry>();

        List<StoragePool> list = main.getStoragePoolList();
                
        for (int i = 0; i < list.size(); i++)
        {
            StoragePool object = list.get(i);

            String name =  object.getName();

            // AVOID DOUBLE NAMES, ADD IDX AS DATE ( IDX OF POOL IS MADE FROM CURRENTMS)
            if (entriesContain( entries, name))
            {
                Date d = new Date();
                d.setTime(object.getIdx());
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH.mm");
                name = object.getName() + "(" + sdf.format(d) + ")";
            }

            ComboEntry ce = new ComboEntry(object.getIdx(), name);
            entries.add(ce);
        }

        return entries;
    }

    private boolean entriesContain( List<ComboEntry> entries, String name )
    {
        for (int i = 0; i < entries.size(); i++)
        {
            ComboEntry comboEntry = entries.get(i);
            if (comboEntry.getGuiEntryKey().equals(name))
                return true;
        }

        return false;
    }

    
  
}
