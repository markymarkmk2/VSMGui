/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Table;

import com.vaadin.data.Property;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAAbstractComboField;
import de.dimm.vsm.vaadin.VSMCMain;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class ComboColumnGenerator implements Table.ColumnGenerator
{
    JPAAbstractComboField field; /* Format string for the Double values. */

    static public final String COMBO_MAGIC_PREFIX = "COMBO.";

    /**
     * Creates double value column formatter with the given
     * format string.
     */
    public ComboColumnGenerator(JPAAbstractComboField fld)
    {
        this.field = fld;
    }

    
    @Override
    public Component generateCell(Table source, Object itemId,
                                  Object columnId) {
        // Get the object stored in the cell as a property
        if (columnId.toString().startsWith(COMBO_MAGIC_PREFIX))
            columnId = columnId.toString().substring(COMBO_MAGIC_PREFIX.length());

        Property prop = source.getItem(itemId).getItemProperty(columnId);
        if (prop != null && prop.getValue() != null)
        {
            // SHOW THE CONTENT OF toString AS COLUMN VALUE
            String txt = prop.getValue().toString();
            List<ComboEntry> entries = null;
            try
            {
                entries = field.getEntries();
            }
            catch (SQLException sQLException)
            {
                VSMCMain.notify(source, "Fehler beim Lesen der Felder", sQLException.getMessage());
                return new Label("");
            }
            for (int i = 0; i < entries.size(); i++)
            {
                ComboEntry ce = entries.get(i);
                if (ce.getDbEntry().equals(prop.getValue()) )
                {
                    txt =ce.getGuiEntryKey();
                    break;
                }
            }
            String s = source.getStyleName();
            Label label = new Label(txt);

            // Set styles for the column: one indicating that it's
            // a value and a more specific one with the column
            // name in it. This assumes that the column name
            // is proper for CSS.

            label.addStyleName("ComboColumn");

            return label;
        }
        return null;
    }
}