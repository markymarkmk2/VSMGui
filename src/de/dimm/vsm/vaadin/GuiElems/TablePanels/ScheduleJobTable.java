/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;


import com.vaadin.data.Container.Sortable;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ItemSorter;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.Job;
import de.dimm.vsm.records.Schedule;
import de.dimm.vsm.vaadin.GuiElems.Fields.ColumnGeneratorField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.List;

class JobFieldProperty extends ObjectProperty<String>
{
    Job job;
    Component label;

    public JobFieldProperty(Job node)
    {
        super("?", String.class, false);
        this.job = node;
        label = null;
    }

    public void setComponent( Component label )
    {
        this.label = label;
    }

    @Override
    public String getValue()
    {
        return JPAJobField.getJobNiceText(job);
    }

    @Override
    public void setValue( Object newValue ) throws ReadOnlyException, ConversionException
    {
        if (label != null)
        {
            if (label instanceof Label)
            {
                ((Label)label).setValue(newValue);
            }
            if (label instanceof TextField)
            {
                ((TextField)label).setValue(newValue);
            }
        }
    }
}

class JPAJobField extends JPATextField implements ColumnGeneratorField
{

    Schedule sched;
    JobColumnGenerator colgen;
    Table table;
    

    public JPAJobField(Schedule _sched)
    {
        super("Job", "job");
        sched = _sched;

    }

    @Override
    public Component createGui(Object _node)
    {
        node = _node;

        Job job = (Job)node;


        JobFieldProperty p = new JobFieldProperty(job);

        TextField tf = new TextField("Job", p);
        p.setComponent( tf );


        tf.setValue( getJobNiceText(job) );
        tf.setData(this);
        tf.setPropertyDataSource(p);

        return tf;
    }

    

    public static String getJobNiceText(Job job)
    {
        StringBuilder sb = new StringBuilder();
//        sb.append( job.getIdx());
//            sb.append(" ");

        if (job.getDisabled())
        {
            sb.append(VSMCMain.Txt("Gesperrt"));
            sb.append(" ");
        }
        if (job.getSched().getCycleLengthMs() > 86400000)
        {
            if (job.getDayNumber() >= 0)
            {
                sb.append(VSMCMain.Txt("Tag"));
                sb.append(" ");
                sb.append(job.getDayNumber());
                sb.append(" ");
            }
        }
        long offsetS = job.getOffsetStartMs() / 1000;
        sb.append(offsetS / 3600);
        sb.append(":");
        sb.append((offsetS/60) % 60);

       
        
        return sb.toString();
    }

    @Override
    public ColumnGenerator getColumnGenerator()
    {
        colgen =  new JobColumnGenerator( this );
        return colgen;
    }
}

class JobColumnGenerator implements Table.ColumnGenerator
{
    JPAJobField field; /* Format string for the Double values. */
    

    public JobColumnGenerator(JPAJobField fld)
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


        JobFieldProperty p = new JobFieldProperty((Job) bi.getBean());
        bi.addItemProperty(field.getFieldName(), p);

        Object _job = bi.getBean();
        if (_job instanceof Job)
        {
            String txt = p.getValue().toString();
            
            Label label = new Label(txt);
            label.setImmediate(true);

            p.setComponent(label);
            return label;
        }
        return null;
    }
}

/**
 *
 * @author Administrator
 */
public class ScheduleJobTable extends BaseDataEditTable<Job>
{
    Schedule sched;


    private ScheduleJobTable( VSMCMain main, Schedule sched, List<Job> _list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, _list, Job.class, _fieldList, listener);
        this.sched = sched;
    }

    public static ScheduleJobTable createTable( VSMCMain main, Schedule sched, List<Job> list, ItemClickListener listener)
    {
        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();
        fieldList.add(new JPAJobField( sched ));
        
        ScheduleJobTable jt = new ScheduleJobTable( main, sched, list, fieldList, listener);

        final BeanContainer bc = jt.bc;
        bc.setItemSorter( new ItemSorter() {

            @Override
            public void setSortProperties( Sortable container, Object[] propertyId, boolean[] ascending )
            {
               
            }

            @Override
            public int compare( Object itemId1, Object itemId2 )
            {
                Job o1 = (Job) bc.getItem(itemId1).getBean();
                Job o2 = (Job) bc.getItem(itemId2).getBean();
                 long n1 = o1.getDayNumber() * 86400000l + o1.getOffsetStartMs();
                 long n2 = o2.getDayNumber() * 86400000l + o2.getOffsetStartMs();
                 if (n1 == n2)
                     return 0;
                 return (n1 < n2) ? -1 : 1;

            }
        });

        return jt;
    }

    JPAJobField getJobField()
    {
        return (JPAJobField)fieldList.get(0);
    }

    @Override
    protected GenericEntityManager get_em()
    {
        return main.get_util_em(sched.getPool());
    }

    @Override
    protected Job createNewObject()
    {
        Job p =  new Job();
        p.setDayNumber(0);
        
        p.setSched(sched);

        GenericEntityManager gem = get_em();

        try
        {
            gem.check_open_transaction();
            gem.em_persist(p);
            gem.commit_transaction();
            sched.getJobs().addIfRealized(p);
            
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
    protected String getTablenameText()
    {
        return VSMCMain.Txt(this.getClass().getSimpleName());
    }

    ScheduleJobPreviewPanel editPanel;

    @Override
    protected void saveActiveObject()
    {
        // EXCERPT CHANGES FROM NEW GUIFIELDS
        editPanel.updateObject( activeElem );

        super.saveActiveObject();       
    }

    @Override
    public AbstractOrderedLayout createEditComponentPanel(  boolean readOnly )
    {
        editPanel = new ScheduleJobPreviewPanel(this, readOnly);
        editPanel.recreateContent(activeElem);

        return editPanel;
    }

    @Override
    public boolean checkPlausibility( AbstractOrderedLayout editPanel, Job job )
    {
        ScheduleJobPreviewPanel panel = (ScheduleJobPreviewPanel)editPanel;

        

        long cycleLenS = job.getSched().getCycleLengthMs() / 1000;
        int dayNumber = panel.getDayNumber();

       

        if (cycleLenS >86400 && dayNumber < 0 || dayNumber >= cycleLenS / 86400)
        {
            VSMCMain.notify(this, VSMCMain.Txt("Ungültige Tagnummer"), VSMCMain.Txt("Bitte geben Sie eine gültige Tagnummer ein") );
            return false;
        }

        return true;

    }


}
