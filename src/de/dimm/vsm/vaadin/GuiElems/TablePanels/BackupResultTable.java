/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.fsengine.LazyList;
import de.dimm.vsm.records.BackupJobResult;
import de.dimm.vsm.records.BackupVolumeResult;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.Fields.ColumnGeneratorField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAReadOnlyDateField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


class StatusColumnGenerator implements Table.ColumnGenerator
{
    Label label;
    StoragePool pool;
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");

    public StatusColumnGenerator(StoragePool pool ) {
        this.pool = pool;       
    }
    
    
    @Override
    public Component generateCell( Table source, Object itemId, Object columnId )
    {
        BeanItem it = (BeanItem) source.getItem(itemId);
        BackupJobResult job = (BackupJobResult)it.getBean();
        label = new Label( toString(job) );
        return label;
    }
    String toString(  BackupJobResult job )
    {
        String status =  "";
         
        LazyList<BackupVolumeResult> backupVolumeResults = job.getBackupVolumeResults();
        
        if (!backupVolumeResults.isEmpty(VSMCMain.get_util_em(pool))
                && backupVolumeResults.get(0).getVolume() != null
                && backupVolumeResults.get(0).getVolume().getClinfo() != null) {
            status = backupVolumeResults.get(0).getVolume().getClinfo().toString();
            
        }

        if (job.getStatus() != null) {
            status +=   " : " + job.getStatus();
        }

        return status;
    }
}

class DurationColumnGenerator implements Table.ColumnGenerator
{
    Label label;
    StoragePool pool;
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");

    public DurationColumnGenerator(StoragePool pool ) {
        this.pool = pool;       
    }
    
    
    @Override
    public Component generateCell( Table source, Object itemId, Object columnId )
    {
        BeanItem it = (BeanItem) source.getItem(itemId);
        BackupJobResult job = (BackupJobResult)it.getBean();
        label = new Label( toString(job) );
        return label;
    }
    static String toString(  BackupJobResult job )
    {
        String status =  "-";
         
        if ( job.getEndTime() != null && job.getStartTime()!= null) {            
            long dur = job.getEndTime().getTime() - job.getStartTime().getTime();
            dur /= 1000;
            long h = dur / 3600;
            int m = (int)(dur%3600) / 60;
            int s = (int)(dur%60);
            
            status =  String.format("%2d:%02d:%02d", h, m, s);
        }

        return status;
    }
}
class JobStatusField extends JPATextField implements ColumnGeneratorField
{

    StoragePool pool;
    StatusColumnGenerator colgen;
    public JobStatusField(StoragePool pool )
    {
         super(VSMCMain.Txt("Status"), "status");
         this.pool = pool;
    }


    @Override
    public Component createGui(Object _node)
    {
        node = _node;

        BackupJobResult job = (BackupJobResult)node;
        TextField tf = new TextField(VSMCMain.Txt("Status"));
        tf.setValue(job.getStatus() != null ? job.getStatus() : "-");

        tf.setWidth("350px");

        tf.setData(this);

        return tf;
    }

    @Override
    public Table.ColumnGenerator getColumnGenerator()
    {
        colgen =  new StatusColumnGenerator(pool);
        return colgen;
    }

    @Override
    public int getWidth()
    {
        return 180;
    }
}
class JobDurationField extends JPATextField implements ColumnGeneratorField
{

    StoragePool pool;
    DurationColumnGenerator colgen;
    public JobDurationField(StoragePool pool )
    {
         super(VSMCMain.Txt("Duration"), "duration");
         this.pool = pool;
    }


    @Override
    public Component createGui(Object _node)
    {
        node = _node;

        BackupJobResult job = (BackupJobResult)node;
        TextField tf = new TextField(VSMCMain.Txt("Duration"));
        tf.setValue(DurationColumnGenerator.toString(job));

        tf.setWidth("80px");

        tf.setData(this);

        return tf;
    }

    @Override
    public Table.ColumnGenerator getColumnGenerator()
    {
        colgen =  new DurationColumnGenerator(pool);
        return colgen;
    }

    @Override
    public int getWidth()
    {
        return 80;
    }
}
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
        ArrayList<JPAField> fieldList = new ArrayList<>();
        fieldList.add(new JPAReadOnlyDateField(VSMCMain.Txt("Start"), "startTime", DateField.RESOLUTION_MIN));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("OK"), "ok"));
        fieldList.add(new JobDurationField(pool));
        fieldList.add(new JobStatusField(pool));


        setTableColumnWidth(fieldList, "status", 250);
        setTableFieldWidth(fieldList, "status", 350);

        setTableColumnExpandRatio(fieldList, "status", 1.0f);

        return new BackupResultTable( main, pool, list, fieldList, listener);
    }

    @Override
    protected GenericEntityManager get_em()
    {
        return VSMCMain.get_util_em(pool);
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
