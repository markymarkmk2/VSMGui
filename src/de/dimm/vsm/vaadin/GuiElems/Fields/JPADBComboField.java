/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Fields;

import com.vaadin.data.util.MethodProperty;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//
//class JPAComboMethodProperty extends MethodProperty
//{
//    List<ComboEntry> entries;
//
//    public JPAComboMethodProperty( Object node,String fieldName, List<ComboEntry> entries)
//    {
//        super(node,fieldName);
//        this.entries = entries;
//    }
//
//    @Override
//    public Object getValue()
//    {
//        Object dbVal = super.getValue();
//        if (dbVal == null)
//            return null;
//
//        for (int i = 0; i < entries.size(); i++)
//        {
//            ComboEntry comboEntry = entries.get(i);
//            if (comboEntry.isDbEntry(dbVal.toString()))
//            {
//                System.out.println("JPAComboMethodProperty.getValue returns " + comboEntry.getGuiEntryKey());
//                return comboEntry.getGuiEntryKey();
//            }
//        }
//        System.out.println("JPAComboMethodProperty.getValue returns empty");
//        return "";
//    }
//
//    @Override
//    public void setValue( Object newValue ) throws ReadOnlyException, ConversionException
//    {
//        if (newValue != null)
//        {
//
//            for (int i = 0; i < entries.size(); i++)
//            {
//                ComboEntry comboEntry = entries.get(i);
//                if (comboEntry.isGuiEntry(newValue.toString()))
//                    newValue = comboEntry.getDbEntry();
//            }
//        }
//
//        System.out.println("JPAComboMethodProperty.setValue " + newValue);
//        super.setValue(newValue);
//    }
//
//    @Override
//    protected void invokeSetMethod( Object value )
//    {
//        super.invokeSetMethod(value);
//    }
//
//    @Override
//    public String toString()
//    {
//        return super.toString();
//    }
//}


/**
 *
 * @author Administrator
 */
public class JPADBComboField extends JPAAbstractComboField
{
    protected Class clazz;
    protected String qry;
    protected String textField;
    protected GenericEntityManager em;

   
    public JPADBComboField(String caption, String fieldName, GenericEntityManager em, Class clazz, String qry, String textField)
    {
        super( caption, fieldName );
        this.clazz = clazz;
        this.qry = qry;
        this.textField = textField;
        this.em = em;
        
    }

    @Override
    public List<ComboEntry> getEntries() throws SQLException
    {
            List<ComboEntry> entries = new ArrayList<ComboEntry>();
            List list = em.createQuery(qry, clazz);
            for (int i = 0; i < list.size(); i++)
            {
                Object object = list.get(i);
                final MethodProperty p = new MethodProperty(object, textField);
                ComboEntry ce = new ComboEntry(object, p.getValue().toString());
                entries.add(ce);
            }
            return entries;
    }
   

}
