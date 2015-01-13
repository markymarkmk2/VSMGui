/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import static com.vaadin.terminal.Sizeable.SIZE_UNDEFINED;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import static com.vaadin.ui.Table.ALIGN_LEFT;
import com.vaadin.ui.TextField;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.fsengine.LazyList;
import de.dimm.vsm.hash.StringUtils;
import de.dimm.vsm.records.Retention;
import de.dimm.vsm.records.RetentionWindow;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.ColumnGeneratorField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import static de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable.setTableColumnExpandRatio;
import de.dimm.vsm.vaadin.GuiElems.VaadinHelpers;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */


class RetentionIntervallField extends JPAField<RetentionWindow> implements ColumnGeneratorField
{
    RetentionStartColumnGenerator colgen;

    public RetentionIntervallField()
    {
        super(VSMCMain.Txt("Start"), "retintervall");
    }

    @Override
    public Component createGui( RetentionWindow _node)
    {
        node = _node;
        TextField tf = new TextField(VSMCMain.Txt((node.isNegated() ? "Inaktiv" : "Aktiv")));
        tf.setValue( node.toString() );
        tf.setData(this);
        tf.setReadOnly(true);
        return tf;
    }

    @Override
    public Table.ColumnGenerator getColumnGenerator()
    {
        colgen =  new RetentionStartColumnGenerator( this );
        return colgen;
    }
}

class RetentionStartColumnGenerator implements Table.ColumnGenerator
{
    RetentionIntervallField field; /* Format string for the Double values. */
    public RetentionStartColumnGenerator(RetentionIntervallField fld)
    {
        this.field = fld;
    }

    /**
     * Generates the cell containing the value.
     * The column is irrelevant in this use case.
     */
    @Override
    public Component generateCell(Table source, Object itemId,Object columnId)
    {
        BeanItem bi = (BeanItem)source.getItem(itemId);
        Object obj = bi.getBean();
        if (obj instanceof RetentionWindow)
        {
            String txt = ((RetentionWindow)obj).toString();
            Label label = new Label();
            label.setValue(txt);
            label.setImmediate(true);
            return label;
        }
        return null;
    }
}

public class RetentionWindowTable extends BaseDataEditTable<RetentionWindow>
{
    Retention retention;
    RetentionWindowPreviewPanel editPanel;

    private RetentionWindowTable( VSMCMain main, Retention r, List<RetentionWindow> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, RetentionWindow.class, _fieldList, listener);
        this.retention = r;
    }

    public static RetentionWindowTable createTable( VSMCMain main, Retention r, List<RetentionWindow> list, ItemClickListener listener)
    {
        ArrayList<JPAField> fieldList = new ArrayList<>();
        
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Gesperrt"), "disabled"));                
        fieldList.add(new JPAComboField(VSMCMain.Txt("Zyklus"), "cycleString", getCycleComboList() ));
        fieldList.add(new RetentionIntervallField());
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Negiert"), "negated"));
        
        setTableColumnExpandRatio(fieldList, "retintervall", 1);

        
        return new RetentionWindowTable( main, r, list, fieldList, listener);
    }

    static ArrayList<ComboEntry> getCycleComboList() {
        ArrayList<ComboEntry> entries = new ArrayList<>();
        entries.add( new ComboEntry(RetentionWindow.DAILY, VSMCMain.Txt("täglich")));
        entries.add( new ComboEntry(RetentionWindow.WEEKLY, VSMCMain.Txt("wöchentlich")));
        entries.add( new ComboEntry(RetentionWindow.YEARLY, VSMCMain.Txt("jährlich")));
        return entries;
    }


    @Override
    public AbstractOrderedLayout createEditComponentPanel(  boolean readOnly )
    {
        editPanel = new RetentionWindowPreviewPanel(this, readOnly);
        editPanel.recreateContent(activeElem);
        return editPanel;
    }


    @Override
    protected GenericEntityManager get_em()
    {
        return VSMCMain.get_util_em(retention.getPool());
    }

    @Override
    protected RetentionWindow createNewObject()
    {
        RetentionWindow p =  new RetentionWindow();
        p.setCycleString(RetentionWindow.DAILY);
        p.setRetention(retention);
       
        
        GenericEntityManager gem = get_em();
        try
        {
            gem.check_open_transaction();
            gem.em_persist(p);

            List<RetentionWindow> windows = retention.getRetentionWindows();
            if (windows instanceof LazyList)
                ((LazyList<RetentionWindow>)windows).add(gem, p);
            else
                windows.add(p);
            
            gem.commit_transaction();

            this.requestRepaint();
            return p;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            gem.rollback_transaction();

        }
        return null;
    }


    @Override
    public void checkPlausibility( AbstractOrderedLayout _editPanel, RetentionWindow t, Runnable ok, Runnable nok )
    {
        boolean fail = editPanel.checkPlausibility(main, t);
        if (fail) {
            nok.run();
        }
        else {
            ok.run();
        }      
    }
    @Override
    protected void saveActiveObject()
    {
        // EXCERPT CHANGES FROM NEW GUIFIELDS
        editPanel.updateObject( activeElem );
        super.saveActiveObject();       
    }
}
