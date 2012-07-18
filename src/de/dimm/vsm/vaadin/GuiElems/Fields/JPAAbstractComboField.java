/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Fields;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Select;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Administrator
 */
public abstract class JPAAbstractComboField extends JPAField {

    public JPAAbstractComboField( String caption, String fieldName, String tooltip )
    {
        super(caption, fieldName, tooltip);
    }
    public JPAAbstractComboField( String caption, String fieldName )
    {
        this(caption, fieldName, null);
    }



    @Override
    public Component createGui(Object _node)
    {
        node = _node;

        ValueChangeListener vcl = new ValueChangeListener()
        {
            @Override
            public void valueChange( ValueChangeEvent event )
            {
                if (changeListener != null)
                    changeListener.valueChange(event);
            }
        };

        List<ComboEntry> entries = null;
        try
        {
            entries = getEntries();
        }
        catch (SQLException sQLException)
        {
        }
        final MethodProperty p = new JPAComboMethodProperty(node,fieldName, entries);
        p.addListener(vcl);


        ComboBox comboBox = new ComboBox(caption, entries);
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(fieldName, String.class, null);

        for (int i = 0; i < entries.size(); i++)
        {
            ComboEntry comboEntry = entries.get(i);
            Item it = container.addItem(comboEntry.getGuiEntryKey());
            if (it == null)
                throw new RuntimeException("Doppelter Comboeintrag");
            it.getItemProperty(fieldName).setValue(comboEntry.getDbEntry());
        }


        comboBox.setContainerDataSource(container);
        comboBox.setPropertyDataSource(p);
        comboBox.setItemCaptionMode(Select.ITEM_CAPTION_MODE_EXPLICIT_DEFAULTS_ID);

        comboBox.setNullSelectionAllowed(false);
        comboBox.setReadOnly(false);
        comboBox.setInvalidAllowed(false);
        comboBox.setNewItemsAllowed(false);
        comboBox.setImmediate(true);
        
        if (toolTip != null)
            comboBox.setDescription(toolTip);
        
        if (validator != null)
        {
            comboBox.addValidator(validator);
            comboBox.setValidationVisible(true);
            comboBox.setRequired(true);
        }



        for (int i = 0; i < entries.size(); i++)
        {
            ComboEntry comboEntry = entries.get(i);
            if (p.getValue() != null && p.getValue().equals(comboEntry.getDbEntry()))
            {
                comboBox.setValue(comboEntry.getGuiEntryKey());
                break;
            }
        }
        comboBox.setVisible(isFieldVisible());


        comboBox.setData(this);

        comboBox.addListener( vcl);

        return comboBox;
    }

    public abstract List<ComboEntry> getEntries() throws SQLException;

    public ComboEntry getSelectedEntry(AbstractOrderedLayout panel)
    {
        Component gui = getGuiforField(panel, fieldName);
        ComboEntry entry = null;
        if (gui == null)
        {
            return null;
        }
        try
        {
            String val = ((ComboBox) gui).getValue().toString();
            entry = null;
            for (int i = 0; i < getEntries().size(); i++)
            {
                ComboEntry _entry = getEntries().get(i);
                if (_entry.isGuiEntry(val))
                {
                    entry = _entry;
                    break;
                }
            }
        }
        catch (Exception e)
        {
        }
        return entry;
    }

    @Override
    public boolean isValid( AbstractOrderedLayout panel )
    {
        Component gui = getGuiforField(panel, fieldName);
        if (gui != null && gui.isVisible())
        {
            ComboBox tf = (ComboBox)gui;
            return tf.isValid();
        }
        // MAYBE NOT VISIUAL, WE USSUME UNCHANGED OR IRRELEVANT
        return true;
    }
}
