/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Window;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.BackupJobResult;
import de.dimm.vsm.records.BackupVolumeResult;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAReadOnlyDateField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class BackupResultTable extends BaseDataEditTable<BackupJobResult>
{
    StoragePool pool;

    private BackupResultTable( VSMCMain main,StoragePool pool, List<BackupJobResult> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, BackupJobResult.class, _fieldList, listener, false, false, /*sort ascending*/false);
        this.pool = pool;
        

    }

    public static BackupResultTable createTable( VSMCMain main, StoragePool pool, List<BackupJobResult> list, ItemClickListener listener)
    {
        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();
        fieldList.add(new JPAReadOnlyDateField(VSMCMain.Txt("Start"), "startTime", DateField.RESOLUTION_MIN));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("OK"), "ok"));
        fieldList.add(new JPATextField(VSMCMain.Txt("Status"), "status"));


        setTableColumnWidth(fieldList, "status", 250);
        setTableFieldWidth(fieldList, "status", 350);

        setTableColumnExpandRatio(fieldList, "status", 1.0f);

        return new BackupResultTable( main, pool, list, fieldList, listener);
    }

    @Override
    protected GenericEntityManager get_em()
    {
        return main.get_util_em(pool);
    }


    @Override
    protected BackupJobResult createNewObject()
    {
       
        return null;

    }
   @Override
    public <S> BaseDataEditTable createChildTable( VSMCMain main, BackupJobResult sched, List<S> list, Class child, ItemClickListener listener )
    {        
        return BackupVolumeResultTable.createTable(main, sched, (List<BackupVolumeResult>) list, listener);
    }
    @Override
    protected String getTablenameText()
    {
        return VSMCMain.Txt(this.getClass().getSimpleName());
    }
    
    @Override
    protected void setDBWinLayout( Window win )
    {
        super.setDBWinLayout(win);
        win.setWidth("700px");
    }

    @Override
    public Component createHeader( String caption )
    {
        return super.createNoNewButtonHeader(caption);
    }




}
