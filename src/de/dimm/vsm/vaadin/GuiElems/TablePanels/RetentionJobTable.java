/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.DateField;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.fsengine.LazyList;
import de.dimm.vsm.records.Retention;
import de.dimm.vsm.records.RetentionJob;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADateField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPASizeStr;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */

public class RetentionJobTable extends BaseDataEditTable<RetentionJob>
{
    Retention retention;
    RetentionJobPreviewPanel editPanel;

    private RetentionJobTable( VSMCMain main, Retention r, List<RetentionJob> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, RetentionJob.class, _fieldList, listener, /*edit*/ false, /*del*/ true);
        this.retention = r;
    }

    public static RetentionJobTable createTable( VSMCMain main, Retention r, List<RetentionJob> list, ItemClickListener listener)
    {
        ArrayList<JPAField> fieldList = new ArrayList<>();
        
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Beendet"), "finished"));
        fieldList.add(new JPADateField(VSMCMain.Txt("Gestartet"), "start", DateField.RESOLUTION_MIN));
        fieldList.add(new JPASizeStr(VSMCMain.Txt("Dateien"), "statNodes"));
        fieldList.add(new JPASizeStr(VSMCMain.Txt("Hashes"), "statHashes"));
        fieldList.add(new JPASizeStr(VSMCMain.Txt("Blöcke"), "statDedups"));
        fieldList.add(new JPASizeStr(VSMCMain.Txt("Speicher"), "dedupSize"));
        fieldList.add(new JPASizeStr(VSMCMain.Txt("Attribute"), "statAttribs"));
               
        return new RetentionJobTable( main, r, list, fieldList, listener);
    }
    
    @Override
    protected GenericEntityManager get_em()
    {
        return VSMCMain.get_util_em(retention.getPool());
    }

    @Override
    protected RetentionJob createNewObject()
    {
        return null;
    }
}
