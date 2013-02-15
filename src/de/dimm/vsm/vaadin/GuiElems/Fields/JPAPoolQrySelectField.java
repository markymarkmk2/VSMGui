/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Fields;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import de.dimm.vsm.records.MessageLog;
import de.dimm.vsm.records.MountEntry;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.PoolQryEditor;
import de.dimm.vsm.vaadin.VSMCMain;


/**
 *
 * @author Administrator
 */
public class JPAPoolQrySelectField extends JPAField  implements ColumnGeneratorField
{

    VSMCMain main;

   
    public JPAPoolQrySelectField( VSMCMain main, String userFieldName, String tsFieldName, String rdFieldName, String showDeletedFieldName)
    {
        super( VSMCMain.Txt("Zugriffsrechte"), "PoolQrySelect" );
        this.main = main;        
    }

    @Override
    public Component createGui(Object node) {
        if (!(node instanceof MountEntry))
            return null;
        
        MountEntry me = (MountEntry)node;
        PoolQryEditor ed = new PoolQryEditor(main, me);
        return ed;
    }

    @Override
    public ColumnGenerator getColumnGenerator() {
        return new PoolQrySelectColumnGenerator();
    }

    

    
class PoolQrySelectColumnGenerator implements Table.ColumnGenerator
{
    Label label;
    @Override
    public Component generateCell( Table source, Object itemId, Object columnId )
    {
        BeanItem it = (BeanItem) source.getItem(itemId);
        MountEntry job = (MountEntry)it.getBean();

        Label lb = new Label(job.getTyp());
        return lb;
    }
}
  
}
