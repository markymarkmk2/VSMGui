/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Fields;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.MountEntry;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.PoolQryEditor;
import de.dimm.vsm.vaadin.VSMCMain;
import java.sql.SQLException;
import java.util.List;



class JPAPoolQryMethodProperty extends MethodProperty
{

    public JPAPoolQryMethodProperty( Object node,String fieldName)
    {
        super(node,fieldName);
    }

    @Override
    public Object getValue()
    {
        Object dbVal = super.getValue();
        if (dbVal == null)
            return null;


        return "";
    }
}


/**
 *
 * @author Administrator
 */
public class JPAPoolQrySelectField extends JPAField<MountEntry>  implements ColumnGeneratorField
{
    VSMCMain main;

     GenericEntityManager em;

   
    

    public JPAPoolQrySelectField( VSMCMain main, GenericEntityManager em )
    {
        super( VSMCMain.Txt("Zugriffsrechte"), "PoolQrySelect" );
        this.main = main;

        this.em = em;
       
    }


    @Override
    public Component createGui(MountEntry node) {
        this.node = node;
        PoolQryEditor ed = new PoolQryEditor(main, em, node, changeListener);
  
        return ed;
    }

    @Override
    public ColumnGenerator getColumnGenerator() {
        return new PoolQrySelectColumnGenerator();
    }


    @Override
    public void update( BeanItem<MountEntry> oldItem )
    {
        MountEntry me = oldItem.getBean();
        String property = getFieldName();
        if (oldItem.getItemProperty(property) == null)
        {
            System.out.println("No property: " + property);
            return;
        }
        Object v = oldItem.getItemProperty(property).getValue();
        oldItem.getItemProperty(property).setValue(v);
    }

   

    
class PoolQrySelectColumnGenerator implements Table.ColumnGenerator
{
    Label label;
    @Override
    public Component generateCell( Table source, Object itemId, Object columnId )
    {
        BeanItem it = (BeanItem) source.getItem(itemId);
        MountEntry job = (MountEntry)it.getBean();

        String str = PoolQryEditor.getNiceStr( job );
       
        Label lb = new Label( str );
        return lb;
    }
}
  
}
