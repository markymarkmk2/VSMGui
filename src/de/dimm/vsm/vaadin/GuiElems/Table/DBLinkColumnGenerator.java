/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Table;

import com.vaadin.data.Property;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADBLinkField;

/**
 *
 * @author Administrator
 */
public class DBLinkColumnGenerator<T> implements Table.ColumnGenerator
{
    JPADBLinkField<T> field; /* Format string for the Double values. */

    public static final String DB_LINK_MAGIC_PREFIX = "DBLINK.";


    /**
     * Creates double value column formatter with the given
     * format string.
     */
    public DBLinkColumnGenerator(JPADBLinkField fld)
    {
        this.field = fld;
    }

    /**
     * Generates the cell containing the Double value.
     * The column is irrelevant in this use case.
     */
    @Override
    public Component generateCell(Table source, Object itemId,
                                  Object columnId)
    {
        // Get the object stored in the cell as a property
        if (columnId.toString().startsWith(DB_LINK_MAGIC_PREFIX))
            columnId = columnId.toString().substring(DB_LINK_MAGIC_PREFIX.length());

        Property prop = source.getItem(itemId).getItemProperty(columnId);
        if (prop != null && prop.getValue() != null)
        {
            String txt = field.toString((T)prop.getValue());

            String s = source.getStyleName();
            Label label = new Label(txt);

            // Set styles for the column: one indicating that it's
            // a value and a more specific one with the column
            // name in it. This assumes that the column name
            // is proper for CSS.

            label.addStyleName("DBLinkColumn");

            return label;
        }
        return null;
    }
}