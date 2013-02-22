/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.FileSystem;

import com.vaadin.ui.AbstractOrderedLayout;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAPoolComboField;
import de.dimm.vsm.vaadin.VSMCMain;

/**
 *
 * @author mw
 */
public class ComboPoolGetter implements IActPoolGetter{

    JPAPoolComboField poolCombo;
    AbstractOrderedLayout panel;

    public ComboPoolGetter(JPAPoolComboField poolCombo, AbstractOrderedLayout panel) {
        this.poolCombo = poolCombo;
        this.panel = panel;
    }
    
    
    @Override
    public StoragePool getActStoragePool() {
        ComboEntry sel = poolCombo.getSelectedEntry(panel);
        if (sel == null)
            return null;
        Long idx = (Long) sel.getDbEntry();
        VSMCMain main = VSMCMain.Me(panel);
        StoragePool pool = main.resolveStoragePool( idx );
        return pool;
    }
    
}
