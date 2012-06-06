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
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.BackupJobResult;
import de.dimm.vsm.records.BackupVolumeResult;
import de.dimm.vsm.vaadin.GuiElems.Fields.ColumnGeneratorField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAReadOnlyDateField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPASizeStr;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.List;



class VolumeColumnGenerator implements Table.ColumnGenerator
{
    Label label;
    @Override
    public Component generateCell( Table source, Object itemId, Object columnId )
    {
        BeanItem it = (BeanItem) source.getItem(itemId);
        BackupVolumeResult job = (BackupVolumeResult)it.getBean();
        label = new Label( toString( job) );

        return label;
    }
    static String toString(  BackupVolumeResult job )
    {
        return job.getVolume().getClinfo().toString() + "->" + job.getVolume().toString();
    }
}

class VolumeField extends JPATextField implements ColumnGeneratorField
{

    VolumeColumnGenerator colgen;
    public VolumeField()
    {
        super("Volume", "volume");
    }


    @Override
    public Component createGui(Object _node)
    {
        node = _node;

        BackupVolumeResult job = (BackupVolumeResult)node;
        TextField tf = new TextField("Volume");
        tf.setValue(VolumeColumnGenerator.toString(job));

        tf.setWidth("350px");

        tf.setData(this);

        return tf;
    }

    @Override
    public ColumnGenerator getColumnGenerator()
    {
        colgen =  new VolumeColumnGenerator();
        return colgen;
    }

    @Override
    public int getWidth()
    {
        return 180;
    }


}

/**
 *
 * @author Administrator
 */
public class BackupVolumeResultTable extends BaseDataEditTable<BackupVolumeResult>
{
    BackupJobResult jobResult;

    private BackupVolumeResultTable( VSMCMain main, BackupJobResult jobResult, List<BackupVolumeResult> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, BackupVolumeResult.class, _fieldList, listener, false, false);
        this.jobResult = jobResult;
    }

    public static BackupVolumeResultTable createTable( VSMCMain main, BackupJobResult jobresult, List<BackupVolumeResult> list, ItemClickListener listener)
    {
        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();
        
        fieldList.add(new VolumeField());
        fieldList.add(new JPAReadOnlyDateField(VSMCMain.Txt("Start"), "startTime", DateField.RESOLUTION_MIN));
        fieldList.add(new JPAReadOnlyDateField(VSMCMain.Txt("Ende"), "endTime", DateField.RESOLUTION_MIN));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("OK"), "ok"));
        fieldList.add(new JPATextField(VSMCMain.Txt("Status"), "status"));
        fieldList.add(new JPASizeStr(VSMCMain.Txt("New Data"), "dataTransfered"));
        fieldList.add(new JPASizeStr(VSMCMain.Txt("New Files"), "filesTransfered"));
        fieldList.add(new JPASizeStr(VSMCMain.Txt("Checked Data"), "dataChecked"));
        fieldList.add(new JPASizeStr(VSMCMain.Txt("Checked Files"), "filesChecked"));




        setTableColumnWidth(fieldList, "status", 250);
        setTableFieldWidth(fieldList, "status", 350);

        setTableColumnExpandRatio(fieldList, "status", 1.0f);
        setTableColumnVisible(fieldList, "filesTransfered", false);
        setTableColumnVisible(fieldList, "dataChecked", false);
        setTableColumnVisible(fieldList, "filesChecked", false);

        return new BackupVolumeResultTable( main, jobresult, list, fieldList, listener);
    }


    @Override
    protected GenericEntityManager get_em()
    {
        return main.get_util_em(jobResult.getSchedule().getPool());
    }

    @Override
    protected BackupVolumeResult createNewObject()
    {
        
        return null;

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
