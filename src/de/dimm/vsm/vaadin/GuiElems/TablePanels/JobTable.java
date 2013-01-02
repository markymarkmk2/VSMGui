/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.fsengine.checks.ICheck;
import de.dimm.vsm.jobs.CheckJobInterface;
import de.dimm.vsm.jobs.InteractionEntry;
import de.dimm.vsm.jobs.JobEntry;
import de.dimm.vsm.jobs.JobInterface;
import de.dimm.vsm.jobs.JobInterface.JOBSTATE;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.JobInfoWindow;
import de.dimm.vsm.vaadin.GuiElems.Fields.ColumnGeneratorField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADateField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.OkAbortPanel;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.GuiElems.Table.ComboColumnGenerator;
import de.dimm.vsm.vaadin.VSMCMain;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.vaadin.addons.lazyquerycontainer.CompositeItem;


class JobStateColumnGenerator implements Table.ColumnGenerator
{
    Label label;
    @Override
    public Component generateCell( Table source, Object itemId, Object columnId )
    {
        BeanItem it = (BeanItem) source.getItem(itemId);
        JobEntry job = (JobEntry)it.getBean();
        label = new Label(getJobStatusStr(job.getJobStatus()));
        return label;
    }

    public String getJobStatusStr(JOBSTATE st)
    {
        if (st != null)
            return VSMCMain.Txt( st.toString());
        return "";
    }
}



class JobPercentColumnGenerator implements Table.ColumnGenerator
{
    Label label;
    @Override
    public Component generateCell( Table source, Object itemId, Object columnId )
    {
        BeanItem it = (BeanItem) source.getItem(itemId);
        JobEntry job = (JobEntry)it.getBean();
        label = new Label(Integer.toString(job.getProcessPercent()) + " " + job.getProcessPercentDimension());
        return label;
    }


}


class StatusField extends JPATextField implements ColumnGeneratorField
{

    JobStateColumnGenerator colgen;

    public StatusField()
    {
        super("Status", "jobStatus");
    }

    @Override
    public Component createGui(Object _node)
    {
        node = _node;

        JobEntry job = (JobEntry)node;

        TextField tf = new TextField("Status");

        tf.setValue( colgen.label.getValue());
        tf.setData(this);

        return tf;
    }

    @Override
    public ColumnGenerator getColumnGenerator()
    {
        colgen = new JobStateColumnGenerator();
        return colgen;
    }
}



class PercentField extends JPATextField implements ColumnGeneratorField
{

    JobPercentColumnGenerator colgen;

    public PercentField()
    {
        super("Speed", "processPercent");
    }

    @Override
    public Component createGui(Object _node)
    {
        node = _node;

        JobEntry job = (JobEntry)node;

        TextField tf = new TextField("Speed");

        tf.setValue( colgen.label.getValue());
        tf.setData(this);

        return tf;
    }

    @Override
    public ColumnGenerator getColumnGenerator()
    {
        colgen = new JobPercentColumnGenerator();
        return colgen;
    }
}

/**
 *
 * @author Administrator
 */
public class JobTable extends Table
{
    VSMCMain main;
    public static final String CHECKBOX_MAGIC_PREFIX = "CHECK.";
    static String dateFormat = "dd.MM.yyyy HH:mm";

    protected ArrayList<JPAField> fieldList;

    JobEntry activeElem;
    BeanContainer<Long, JobEntry> bc;
    //Timer timer;


    public JobTable( VSMCMain main )
    {
        this.main = main;


        ArrayList<JPAField> fl = new ArrayList<JPAField>();
        fl.add( new StatusField());
        fl.add(new JPATextField(VSMCMain.Txt("Status"), "statusStr"));
        fl.add(new JPATextField(VSMCMain.Txt("Statistik"), "statistic"));
        fl.add(new JPADateField(VSMCMain.Txt("Erzeugt"), "started", DateField.RESOLUTION_MIN));
        fl.add( new PercentField());

        BaseDataEditTable.setTableColumnExpandRatio(fl, "statusStr", 1.0f);
        BaseDataEditTable.setTableColumnWidth(fl, "jobStatus", 80);
        BaseDataEditTable.setTableColumnWidth(fl, "processPercent", 50);
        BaseDataEditTable.setTableColumnWidth(fl, "statistic", 280);


        initTable( fl, true, true );


    }

    public BeanContainer<Long, JobEntry> getBc()
    {
        return bc;
    }

    

    public void refresh()
    {
        requestStatus();
    }

    final void initTable( ArrayList<JPAField> _fieldList, boolean show_edit, boolean show_abort)
    {
        fieldList = _fieldList;
        bc = new BeanContainer<Long, JobEntry>(JobEntry.class);
        bc.setBeanIdProperty("idx");

        setSizeFull();

        setContainerDataSource(bc);
        setSelectable(true);
        setMultiSelect(false);
        setImmediate(true); // react at once when something is selected
        setColumnReorderingAllowed(true);
        setColumnCollapsingAllowed(true);


        listJobs();

        // CREATE EDIT AND DELET COLUMN STYLES -> SHOW BUTTONS VIA CSS
        setCellStyleGenerator(new Table.CellStyleGenerator()
        {
            @Override
            public String getStyle(final Object itemId, final Object propertyId)
            {
                if (propertyId != null)
                {
                    String colName = propertyId.toString();
                    if(colName.equals("edit"))
                    {
                        return "zoom";
                    }
                    if(colName.equals("abort"))
                    {
                        return "delete";
                    }
                    if(colName.equals("statusStr"))
                    {
                        BeanItem item = (BeanItem) getItem(itemId);
                        if (item != null && item.getBean() instanceof JobEntry)
                        {
                            JobEntry je = (JobEntry)item.getBean();
                            if (je.getJobStatus() == JOBSTATE.NEEDS_INTERACTION)
                            {
                                return "userInteraction";
                            }
                        }
                    }
                    if (colName.startsWith(CHECKBOX_MAGIC_PREFIX))
                    {
                        colName = colName.substring(CHECKBOX_MAGIC_PREFIX.length());
                        JPAField jPAField = getField( colName );

                        if (jPAField instanceof JPACheckBox)
                        {
                            Object valueGetter = getItem(itemId).getItemProperty(colName);
                            if (valueGetter != null && valueGetter instanceof MethodProperty && ((MethodProperty)valueGetter).getValue() != null)
                            {
                                Object value = ((MethodProperty)valueGetter).getValue();
                                if (value instanceof Boolean && ((Boolean)value).booleanValue())
                                    return "ok";
                            }
                            return "nok";
                        }
                    }
                }
                return null;
            }
        });

        int edit_columns = 0;
        if (show_edit)
            edit_columns++;
        if (show_abort)
            edit_columns++;

        // BUILD COLUMNS
        String[] columns = new String[fieldList.size() + edit_columns];
        String[] columnLabels = new String[fieldList.size() + edit_columns];
        for (int i = 0; i < fieldList.size(); i++)
        {
            JPAField jPAField = fieldList.get(i);
            columnLabels[i] = jPAField.getCaption();

            if (jPAField instanceof JPAComboField)
            {
                JPAComboField fld = (JPAComboField)jPAField;

                // COMBOFIELDS HAVE AN OWN COLUMN BECAUSE OF TRANSLATION BETWEEN COMBOENTRIES AND VISUAL ENTRIES
                // WE CREATE A MAGIC COLUMNNAME AND REFER TO A GENERATED COLUMN WHICH HANDLES THE TRANSLATION
                columns[i] = ComboColumnGenerator.COMBO_MAGIC_PREFIX + jPAField.getFieldName();

                addGeneratedColumn(columns[i], new ComboColumnGenerator(fld));
            }
            else if(jPAField instanceof JPACheckBox)
            {
                JPACheckBox fld = (JPACheckBox)jPAField;

                // COMBOFIELDS HAVE AN OWN COLUMN BECAUSE OF TRANSLATION BETWEEN COMBOENTRIES AND VISUAL ENTRIES
                // WE CREATE A MAGIC COLUMNNAME AND REFER TO A GENERATED COLUMN WHICH HANDLES THE TRANSLATION
                columns[i] = CHECKBOX_MAGIC_PREFIX + jPAField.getFieldName();

                addGeneratedColumn(columns[i], new EmptyColumnGenerator());
            }
            else if(jPAField instanceof ColumnGeneratorField)
            {
                // ALLOW COMPLETELY ARTIFICIAL FIELDS
                ColumnGeneratorField fld = (ColumnGeneratorField)jPAField;
                columns[i] = jPAField.getFieldName();
                addGeneratedColumn(columns[i], fld.getColumnGenerator());
            }
            else
            {
                columns[i] = jPAField.getFieldName();
            }

            setItemCaption(items, ALIGN_LEFT);
        }
        int act_index = fieldList.size();

        if (show_edit)
        {
            columns[act_index] = "edit";
            columnLabels[act_index] = "";//Main.Txt("Edit");
            addGeneratedColumn(columns[act_index], new EmptyColumnGenerator() );
            act_index++;
        }
        if (show_abort)
        {
            columns[act_index] = "abort";
            columnLabels[act_index] = "";// Main.Txt("Del");
            addGeneratedColumn(columns[act_index], new EmptyColumnGenerator() );
            act_index++;
        }

        setVisibleColumns(columns);

        setColumnHeaders(columnLabels);

        for (int i = 0; i < fieldList.size(); i++)
        {
            JPAField jPAField = fieldList.get(i);
            if (jPAField.getWidth() >= 0)
            {
                setColumnWidth(columns[i], jPAField.getWidth());
            }
            if (jPAField.getExpandRatio() >= 0)
            {
                setColumnExpandRatio(columns[i], jPAField.getExpandRatio());
            }
        }


        ItemClickListener l = new ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                Item item = event.getItem();
                if (item instanceof CompositeItem)
                {
                    item = ((CompositeItem)item).getItem("bean");
                }

                if (item instanceof BeanItem)
                {
                    BeanItem bit = (BeanItem)item;
                    callTableClick( event, (JobEntry)bit.getBean() );
                }
            }
        };

        addListener(l);
    }

    void listJobs()
    {
        JobEntry[] arr = main.getDummyGuiServerApi().listJobs(main.getUser());
        

        HashMap<Long, JobEntry> newMap = new  HashMap<Long, JobEntry>();
        for (int i = 0; i < arr.length; i++)
        {
            JobEntry jobEntry = arr[i];
            newMap.put(jobEntry.getIdx(), jobEntry);
        }

        bc.removeAllItems();
/*        // HANDLE EXISTING OR OLD
        for (int b = 0; b < bc.size(); b++)
        {
            long id = bc.getIdByIndex(b);
            if (!newMap.containsKey(id))
            {
                bc.removeItem(id);
                b = b--;
            }
            else
            {
                BeanItem bi = new BeanItem(newMap.get(id));
                updateItem(id, bi);
                newMap.remove(id);
            }
        }
*/
       // HANDLE NEW
        Collection<JobEntry> c = newMap.values();
        for (Iterator<JobEntry> it = c.iterator(); it.hasNext();)
        {
            JobEntry jobEntry = it.next();
            bc.addBean( jobEntry );
        }  
        if (activeElem != null)
        {
            if (!bc.containsId(activeElem.getIdx()))
            {
                activeElem = null;
            }
        }
    }

    @Override
    protected String formatPropertyValue(Object rowId, Object colId, Property property)
    {
        // Format by property type
        if (property.getType() == Date.class && property.getValue() != null)
        {
            SimpleDateFormat df = new SimpleDateFormat(dateFormat);
            return df.format((Date)property.getValue());
        }
        // Format by property type
        if (property.getType() == Boolean.class && property.getValue() != null)
        {
            Boolean b = (Boolean) property.getValue();
            return b.booleanValue() ? "Ja" : "Nein" ;
        }

        return super.formatPropertyValue(rowId, colId, property);
    }


    public JPAField getField( String name )
    {
        for (int i = 0; i < fieldList.size(); i++)
        {
            JPAField jPAField = fieldList.get(i);

            if (jPAField.getFieldName().equals(name))
            {
                return jPAField;
            }
        }
        return null;
    }

    public void callTableClick( ItemClickEvent event, final JobEntry item )
    {
        activeElem = item;

        String col_id = null;
        if (event.getPropertyId() != null)
            col_id = event.getPropertyId().toString();

        if (col_id.equals("edit"))
        {
            callJobDisplay(item);
        }
        else if(col_id.equals("abort"))
        {
            ClickListener ok = new ClickListener() {

                @Override
                public void buttonClick( ClickEvent event )
                {
                    callJobAbort(item);
                }
            };
            if (item.getJobStatus() != JOBSTATE.FINISHED_ERROR && item.getJobStatus() != JOBSTATE.FINISHED_OK)
            {
                main.Msg().errmOkCancel(VSMCMain.Txt("Wollen Sie diesen Job abbrechen?"), ok, null);
            }
            else
            {
                callJobAbort(item);
            }
        }
        else if (item.getJobStatus() == JOBSTATE.NEEDS_INTERACTION)
        {
            callJobInteraction( item );
        }
    }
    public JobEntry getActiveElem()
    {
        return activeElem;
    }

    private void callJobDisplay( JobEntry item )
    {
        JobInfoWindow dlg = new JobInfoWindow(main, item.getJob());

        this.getApplication().getMainWindow().addWindow(dlg);        
    }
    private void callJobAbort( JobEntry item )
    {
        item.getJob().abortJob();
    }


    private void requestStatus()
    {
        listJobs();
    }

    public void updateItem(Object itemId, BeanItem newItem )
    {
        // UPDATE BY SETTING PROPERTY AGAIN
        try
        {
            if (bc.containsId(itemId))
            {
                BeanItem oldItem = bc.getItem(itemId);
                if (oldItem != null)
                {
                    for (int i = 0; i < fieldList.size(); i++)
                    {
                         //fieldList.get(i).update( oldItem, newItem );
                    }
                }
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public void activate()
    {
//        timer.start();
    }

    public void deactivate()
    {
        activeElem = null;
//        timer.stop();
    }
    
    Window createUserSelect( final CheckJobInterface cjob, String caption, final List<String> userSelect )
    {
        final Window win = new Window("Auswahl treffen");
        win.setModal(true);
        win.setStyleName("vsm");

        VerticalLayout vl = new VerticalLayout();
        win.addComponent(vl);

        vl.setSpacing(true);
        vl.setSizeFull();
        vl.setImmediate(true);
        vl.setStyleName("editWin");
        Label lb = new Label(caption);
        vl.addComponent(lb);

        final ComboBox cbSelect  = new ComboBox("Was tun", userSelect);
        cbSelect.setNullSelectionAllowed(false);
        cbSelect.setNewItemsAllowed(false);
        cbSelect.setValue(userSelect.get(0));

        vl.addComponent(cbSelect);

        OkAbortPanel panel = new OkAbortPanel();
        vl.addComponent(panel);

        panel.setOkText("Start");
        panel.getBtOk().addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                String val = cbSelect.getValue().toString();
                for (int i = 0; i < userSelect.size(); i++)
                {
                    String string = userSelect.get(i);
                    if (string.equals(val))
                    {                       
                        event.getButton().getApplication().getMainWindow().removeWindow(win);
                        cjob.getInteractionEntry().setAnswer(InteractionEntry.INTERACTION_ANSWER.OK);                        
                    }
                }
            }
        });
        panel.getBtAbort().addListener( new Button.ClickListener() {
            @Override
            public void buttonClick( ClickEvent event )
            {
                event.getButton().getApplication().getMainWindow().removeWindow(win);
            }
        });
        return win;

    }

    void handleUserChoice( final ICheck check, final String caption, final int i )
    {
        Runnable r = new Runnable() {

            @Override
            public void run()
            {
                StringBuffer sb = new StringBuffer();
                if (!check.handleUserChoice(i, sb))
                {
                    VSMCMain.Me(JobTable.this).Msg().errmOk(sb.toString());
                }
            }
        };
        Button.ClickListener abortClick = new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                check.abort();
            }
        };

        VSMCMain.Me(this).runInBusyCancel(check.getName() + " " + caption, r, abortClick );
    }

     private void handleSelectJobInteraction(JobInterface job) {
        
         if (job instanceof CheckJobInterface) {
             final CheckJobInterface cjob = (CheckJobInterface)job;
             List<String> options = new ArrayList<String>();
             String caption = cjob.getCheck().fillUserOptions(options);
             if (!options.isEmpty())
             {
                Window win = createUserSelect(cjob, caption, options );
                getApplication().getMainWindow().addWindow(win);
             }
             else
             {
                 String msg = cjob.getCheck().getErrText();
                 Button.ClickListener okClick = new Button.ClickListener() {

                    @Override
                    public void buttonClick( ClickEvent event )
                    {
                        cjob.abortJob();
                    }
                 };


                 main.Msg().errmOk(msg, okClick);
             }
         }
    }

    private void callJobInteraction( final JobEntry jobEntry )
    {
        final InteractionEntry ie = jobEntry.getJob().getInteractionEntry();
        if (ie.getInteractionType() == InteractionEntry.INTERACTION_TYPE.SELECT) {
            handleSelectJobInteraction(jobEntry.getJob());
            return;
        }
            
        OkAbortPanel okPanel = new OkAbortPanel();

        if (ie.getInteractionType() == InteractionEntry.INTERACTION_TYPE.OK)
            okPanel.getBtAbort().setVisible(false);
        if (ie.getInteractionType() == InteractionEntry.INTERACTION_TYPE.OK_RETRY_CANCEL)
            okPanel.getBtRetry().setVisible(true);
        

        okPanel.setOkText(VSMCMain.Txt("Weiter"));


        final Window win = new Window(ie.getShortText());
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setSpacing(true);

        Label plainText = new Label(ie.getText());
        plainText.setContentMode(Label.CONTENT_XHTML);
        vl.addComponent(plainText);
        vl.setExpandRatio(plainText, 1);
        vl.addComponent(okPanel);

        win.addComponent(vl);

        okPanel.getBtOk().addListener( new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                win.getApplication().getMainWindow().removeWindow(win);
                ie.setAnswer(InteractionEntry.INTERACTION_ANSWER.OK);
                jobEntry.setJobStatus(JOBSTATE.RUNNING);
            }
        });
        okPanel.getBtAbort().addListener( new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                win.getApplication().getMainWindow().removeWindow(win);
                ie.setAnswer(InteractionEntry.INTERACTION_ANSWER.CANCEL);
                jobEntry.setJobStatus(JOBSTATE.RUNNING);
            }
        });
        okPanel.getBtRetry().addListener( new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                win.getApplication().getMainWindow().removeWindow(win);
                ie.setAnswer(InteractionEntry.INTERACTION_ANSWER.RETRY);
                jobEntry.setJobStatus(JOBSTATE.RUNNING);
            }
        });


        this.getApplication().getMainWindow().addWindow(win);
    }    

   
}
