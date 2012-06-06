/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.fsengine.LazyList;
import de.dimm.vsm.records.ClientInfo;
import de.dimm.vsm.records.Job;
import de.dimm.vsm.records.Schedule;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.Fields.ColumnGeneratorField;
import de.dimm.vsm.vaadin.GuiElems.Fields.DBLinkTextField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADBLinkField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADateField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAReadOnlyDateField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;



class CycleLenColumnGenerator implements Table.ColumnGenerator
{
    Label label;
    @Override
    public Component generateCell( Table source, Object itemId, Object columnId )
    {
        BeanItem it = (BeanItem) source.getItem(itemId);
        Schedule job = (Schedule)it.getBean();
        label = new Label( toString( job) );

        return label;
    }

    static String toString(  Schedule node )
    {
        List<String> dim = new ArrayList<String>();
        dim.add(VSMCMain.Txt("Stunde(n)"));
        dim.add(VSMCMain.Txt("Tag(e)"));
        dim.add(VSMCMain.Txt("Woche(n)"));

        long cs = node.getCycleLengthMs() / 1000;
        int f;
        String dimStr = "?";

        if (cs < 86400)
        {
            dimStr = dim.get(0);
            f = 3600;
        }
        else if(cs < 7*86400)
        {
            dimStr = dim.get(1);
            f = 86400;
        }
        else
        {
            dimStr = dim.get(2);
            f = 7*86400;
        }
        long n = cs / f;

        return Long.toString(n) + " " + dimStr;
    }
}

class CycleLenField extends JPATextField implements ColumnGeneratorField
{

    CycleLenColumnGenerator colgen;
    public CycleLenField()
    {
        super(VSMCMain.Txt("Zyklus"), "cycleLengthMs");

    }


    @Override
    public Component createGui(Object _node)
    {
        node = _node;

        Schedule job = (Schedule)node;
        TextField tf = new TextField(VSMCMain.Txt("Zyklus"));

        tf.setVisible(false);

        tf.setData(this);

        return tf;
    }

    @Override
    public ColumnGenerator getColumnGenerator()
    {
        colgen =  new CycleLenColumnGenerator();
        return colgen;
    }

    @Override
    public int getWidth()
    {
        return 100;
    }
}

class NextStartField extends JPAField implements ColumnGeneratorField
{
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    SchedStartColumnGenerator colgen;


    public NextStartField()
    {
        super(VSMCMain.Txt("Nächster Start"), "schedstart");
    }

    @Override
    public Component createGui( Object _node)
    {
        node = _node;

        Schedule sched = (Schedule)node;


        TextField tf = new TextField(VSMCMain.Txt("Nächster Start"));

        Object nextStart = VSMCMain.callLogicControl("getNextStart", sched);

        tf.setValue( getStartString(nextStart) );
        tf.setData(this);
        tf.setReadOnly(true);

        return tf;
    }


    @Override
    public ColumnGenerator getColumnGenerator()
    {
        colgen =  new SchedStartColumnGenerator( this );
        return colgen;
    }

    static String getStartString( Object val )
    {
        if (val == null)
            return "-";
        if (val instanceof Date)
        {
            return sdf.format((Date)val);
        }
        return val.toString();
    }
}

class SchedStartColumnGenerator implements Table.ColumnGenerator
{
    NextStartField field; /* Format string for the Double values. */


    public SchedStartColumnGenerator(NextStartField fld)
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


       
        Object sched = bi.getBean();
        if (sched instanceof Schedule)
        {
            Object nextStart = VSMCMain.callLogicControl("getNextStart", (Schedule)sched);

            Label label = new Label();
            label.setValue(NextStartField.getStartString(nextStart));
            label.setImmediate(true);

            return label;
        }
        return null;
    }
}


class JPAJobDBLinkField extends JPADBLinkField
{

    public JPAJobDBLinkField(GenericEntityManager em)
    {
        super( em, VSMCMain.Txt("Jobs"), "jobs", Job.class);
    }

    @Override
    public String toString( Object _list )
    {
        StringBuilder sb = new StringBuilder();

        try
        {
            if (_list instanceof List)
            {
                List<Job> list = (List<Job>) _list;
                for (int i = 0; i < list.size(); i++)
                {
                    Job j = list.get(i);

                    String s = JPAJobField.getJobNiceText(j);
                    if (sb.length() > 0)
                        sb.append(", ");
                    sb.append(s);
                }
            }
            else
            {
                sb.append(_list.toString());
            }
        }
        catch (Exception noSuchFieldException)
        {
        }
        return sb.toString();
    }

    @Override
    public void reload( Component tf)
    {
        List<Job> list = getList( node);

        ((DBLinkTextField)tf).setValue(toString(list));

    }

    @Override
    public List<Job> getList( Object parent)
    {
        try
        {
            Field f = parent.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object o = f.get(parent);
            if (o instanceof LazyList)
            {
                o = ((LazyList)o).getList(em);
            }

            if (o instanceof List)
            {
                List<Job> list = (List<Job>) o;

                // RETURN SORTED LIST 
                Collections.sort(list, new Comparator<Job>()
                {

                    @Override
                    public int compare( Job o1, Job o2 )
                    {
                         long n1 = o1.getDayNumber() * 86400000l + o1.getOffsetStartMs();
                         long n2 = o2.getDayNumber() * 86400000l + o2.getOffsetStartMs();
                         if (n1 == n2)
                             return 0;
                         return (n1 < n2) ? -1 : 1;
                    }
                });
                return list;
            }
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
        }
        return null;
    }



}

/**
 *
 * @author Administrator
 */
public class ScheduleTable extends BaseDataEditTable<Schedule>
{
    StoragePool pool;

    private ScheduleTable( VSMCMain main, StoragePool pool, List<Schedule> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, Schedule.class, _fieldList, listener);
        this.pool = pool;
    }

    public static ScheduleTable createTable( VSMCMain main, StoragePool pool, List<Schedule> list, ItemClickListener listener)
    {
        GenericEntityManager em = main.get_util_em(pool);

        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();
        fieldList.add(new JPATextField(VSMCMain.Txt("Name"), "name"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Gesperrt"), "disabled"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Zyklus"), "isCycle"));
        fieldList.add(new CycleLenField() );
        fieldList.add(new NextStartField() );
        fieldList.add(new JPAReadOnlyDateField(VSMCMain.Txt("Erzeugt"), "creation", DateField.RESOLUTION_DAY));
        fieldList.add(new JPADateField(VSMCMain.Txt("Gültig ab"), "scheduleStart", DateField.RESOLUTION_MIN));
        //fieldList.add(new JPAJobDBLinkField());
        fieldList.add(new JPADBLinkField(em, VSMCMain.Txt("Rechner"), "clientList", ClientInfo.class));

        setTableColumnVisible( fieldList, "scheduleStart", false );
        setTableColumnVisible( fieldList, "creation", false );
        setTableColumnVisible( fieldList, "isCycle", false );
        
        return new ScheduleTable( main, pool, list, fieldList, listener);
    }

    @Override
    protected GenericEntityManager get_em()
    {
        return main.get_util_em(pool);
    }


    @Override
    protected Schedule createNewObject()
    {
        Schedule p =  new Schedule();
        p.setCreation( new Date() );
        p.setName(VSMCMain.Txt("Neuer Sicherungsplan"));
        p.setPool( pool );
        p.setCycleLengthMs(24*3600*1000l);
        p.setIsCycle(true);

        GenericEntityManager gem = get_em();

        try
        {
            gem.check_open_transaction();
            gem.em_persist(p);
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
    public <S> BaseDataEditTable createChildTable( VSMCMain main, Schedule sched, List<S> list, Class child, ItemClickListener listener )
    {
        if (  child.isAssignableFrom(ClientInfo.class))
            return ClientInfoTable.createTable(main, sched, (List) list, listener);
        if (  child.isAssignableFrom(Job.class))
            return ScheduleJobTable.createTable(main, sched, (List) list, listener);

        return null;
    }
    
    @Override
    protected String getTablenameText()
    {
        return VSMCMain.Txt(this.getClass().getSimpleName());
    }

    @Override
    public Component createHeader( String caption )
    {
        Label ll = new Label(caption);
        ll.setStyleName("Tablename");
        Component bt = createNewButton();

        final HorizontalLayout head = new HorizontalLayout();
        head.setWidth("100%");
        head.setSpacing(true);
        head.addComponent(ll);
        head.setComponentAlignment(ll, Alignment.MIDDLE_LEFT);

        ClickListener cl = new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                main.initSchedules();
                setValueChanged();
                VSMCMain.info(head, VSMCMain.Txt("Die Zeitpläne wurden aktualisiert"), "");
            }
        };

        NativeButton initBt = new  NativeButton(VSMCMain.Txt("Initialisieren") + "...", cl);
        head.addComponent(initBt);
        head.setComponentAlignment(initBt, Alignment.MIDDLE_CENTER);


        if (bt != null)
        {
            head.addComponent(bt);
            head.setComponentAlignment(bt, Alignment.MIDDLE_RIGHT);
        }

        return head;

    }

    @Override
    public boolean checkPlausibility( AbstractOrderedLayout previewTable, Schedule t )
    {
        if (t.getScheduleStart() == null)
            {
                VSMCMain.notify(this, VSMCMain.Txt("Ungültiger Gültigkeitszeitpunkt"), VSMCMain.Txt("Bitte geben Sie ein gültiges Datum ein") );
                return false;
            }
        return true;
    }

    public static String getNiceText(Schedule sched)
    {
        StringBuilder sb = new StringBuilder();

        if (sched.getDisabled())
        {
            sb.append(VSMCMain.Txt("Gesperrt"));
            sb.append(" ");
        }


        long l = sched.getCycleLengthMs();
        l /= 1000;

        if (l < 86400)  // < 1 TAG
        {
            int h = (int)(l /3600);
            if (h > 1)
            {
                sb.append(h);
                sb.append("-");
            }
            sb.append(VSMCMain.Txt("stündlich"));
            sb.append(" ");
        }
        else if (l >= 86400) // == 1 TAG
        {
            int d = (int)(l /86400);
            if (d < 7)
            {
                if (d > 1)
                {
                    sb.append(d);
                    sb.append("-");
                }
                sb.append(VSMCMain.Txt("täglich"));
                sb.append(" ");
            }
            else
            {
                int w = d/7;
                if (w > 1)
                {
                    sb.append(w);
                    sb.append("-");
                }
                sb.append(VSMCMain.Txt("wöchentlich"));
                sb.append(" ");
            }
        }
        return sb.toString();
    }
    SchedulePreviewPanel editPanel;

    @Override
    public AbstractOrderedLayout createEditComponentPanel(  boolean readOnly )
    {
        editPanel = new SchedulePreviewPanel(this, readOnly);
        editPanel.recreateContent(activeElem);

        return editPanel;
    }

    @Override
    protected void saveActiveObject()
    {
        // EXCERPT CHANGES FROM NEW GUIFIELDS
        editPanel.updateObject( activeElem );

        super.saveActiveObject();
    }

    @Override
    protected void deleteObject( Schedule node )
    {
        // GET RID OF BACKUPRESULTS FIRST

        GenericEntityManager gem = get_em();
        gem.nativeCall( "delete from BackupJobResult where schedule_idx=" + node.getIdx());

        super.deleteObject(node);
    }

}
