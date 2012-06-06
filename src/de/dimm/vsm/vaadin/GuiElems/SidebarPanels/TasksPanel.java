/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import de.dimm.vsm.vaadin.GuiElems.TablePanels.EmptyColumnGenerator;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.tasks.InteractionEntry;
import de.dimm.vsm.tasks.TaskEntry;
import de.dimm.vsm.tasks.TaskInterface.TASKSTATE;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.TaskInfoWindow;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.Timer;
import org.vaadin.addons.lazyquerycontainer.CompositeItem;



//
//class JobPercentColumnGenerator implements Table.ColumnGenerator
//{
//    Label label;
//    @Override
//    public Component generateCell( Table source, Object itemId, Object columnId )
//    {
//        BeanItem it = (BeanItem) source.getItem(itemId);
//        JobEntry job = (JobEntry)it.getBean();
//        label = new Label(Integer.toString(job.getProcessPercent()) + " " + job.getProcessPercentDimension());
//        return label;
//    }
//
//
//}
class TaskStateColumnGenerator implements Table.ColumnGenerator
{
    Label label;
    @Override
    public Component generateCell( Table source, Object itemId, Object columnId )
    {
        BeanItem it = (BeanItem) source.getItem(itemId);
        TaskEntry job = (TaskEntry)it.getBean();
        label = new Label(getTaskStatusStr(job.getTaskStatus()));
        return label;
    }

    public String getTaskStatusStr(TASKSTATE st)
    {
        return VSMCMain.Txt( st.toString());
    }
}


//
//class JobPercentColumnGenerator implements Table.ColumnGenerator
//{
//    Label label;
//    @Override
//    public Component generateCell( Table source, Object itemId, Object columnId )
//    {
//        BeanItem it = (BeanItem) source.getItem(itemId);
//        JobEntry job = (JobEntry)it.getBean();
//        label = new Label(Integer.toString(job.getProcessPercent()) + " " + job.getProcessPercentDimension());
//        return label;
//    }
//
//
//}


class TaskStatusField extends JPATextField implements ColumnGeneratorField
{

    TaskStateColumnGenerator colgen;

    public TaskStatusField()
    {
        super("TaskStatus", "taskStatus");
    }

    @Override
    public Component createGui(Object _node)
    {
        node = _node;

        TaskEntry job = (TaskEntry)node;

        TextField tf = new TextField("TaskStatus");

        tf.setValue( colgen.label.getValue());
        tf.setData(this);
        return tf;
    }

    @Override
    public ColumnGenerator getColumnGenerator()
    {
        return new TaskStateColumnGenerator();
    }
}


/**
 *
 * @author Administrator
 */
public class TasksPanel extends Table
{
    VSMCMain main;
    public static final String CHECKBOX_MAGIC_PREFIX = "CHECK.";
    static String dateFormat = "dd.MM.yyyy HH:mm";

    protected ArrayList<JPAField> fieldList;

    TaskEntry activeElem;
    final BeanContainer<Long, TaskEntry> bc;
//    Timer timer;


    public TasksPanel( VSMCMain main )
    {
        this.main = main;
        bc = new BeanContainer<Long, TaskEntry>(TaskEntry.class);
        bc.setBeanIdProperty("idx");


        ArrayList<JPAField> fl = new ArrayList<JPAField>();
        fl.add(new JPATextField(VSMCMain.Txt("Name"), "name"));
        fl.add( new TaskStatusField());
        fl.add(new JPATextField(VSMCMain.Txt("Status"), "statusStr"));
        fl.add(new JPATextField(VSMCMain.Txt("Statistik"), "statistic"));
        
        
        initTable( fl, true, true );

    }

    void refresh()
    {
        requestStatus();
    }


    final void initTable( ArrayList<JPAField> _fieldList, boolean show_edit, boolean show_pause)
    {
        fieldList = _fieldList;        

        setSizeFull();

        setContainerDataSource(bc);
        setSelectable(true);
        setMultiSelect(false);
        setImmediate(true); // react at once when something is selected
        setColumnReorderingAllowed(true);
        setColumnCollapsingAllowed(true);


        listTasks();

        // CREATE EDIT AND DELET COLUMN STYLES -> SHOW BUTTONS VIA CSS
        setCellStyleGenerator(new Table.CellStyleGenerator()
        {
            @Override
            public String getStyle(final Object itemId, final Object propertyId)
            {
                synchronized(bc)
                {
                    if (propertyId != null)
                    {
                        String colName = propertyId.toString();
                        if(colName.equals("edit"))
                        {
                            return "zoom";
                        }
                        if(colName.equals("pause"))
                        {
                            BeanItem item = (BeanItem) getItem(itemId);
                            if (item != null && item.getBean() instanceof TaskEntry)
                            {
                                TaskEntry te = (TaskEntry)item.getBean();
                                if (te.getTaskStatus() == TASKSTATE.PAUSED)
                                {
                                    return "paused";
                                }
                            }
                            else
                            {
                                return null;
                            }

                            return "running";
                        }
                        if(colName.equals("statusStr"))
                        {
                            BeanItem item = (BeanItem) getItem(itemId);

                            if (item != null && item.getBean() instanceof TaskEntry)
                            {
                                TaskEntry je = (TaskEntry)item.getBean();
                                if (je.getTaskStatus() == TASKSTATE.NEEDS_INTERACTION)
                                {
                                    return "userInteraction";
                                }
                            }
                            else
                            {
                                return null;
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
                }
                return null;
            }
        });

        int edit_columns = 0;
        if (show_edit)
            edit_columns++;
        if (show_pause)
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
        if (show_pause)
        {
            columns[act_index] = "pause";
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
                    callTableClick( event, (TaskEntry)bit.getBean() );
                }
            }
        };

        addListener(l);
    }
    HashMap<Long, String> lastStateMap = new  HashMap<Long, String>();

    void listTasks()
    {
        TaskEntry[] arr = main.getDummyGuiServerApi().listTasks();
        

        HashMap<Long, TaskEntry> newMap = new  HashMap<Long, TaskEntry>();
        for (int i = 0; i < arr.length; i++)
        {
            TaskEntry jobEntry = arr[i];
            newMap.put(jobEntry.getIdx(), jobEntry);
        }

        synchronized(bc)
        {
            boolean changed = false;
            for (int b = 0; b < bc.size(); b++)
            {
                long id = bc.getIdByIndex(b);
                String lastState = lastStateMap.get(id);
                TaskEntry newEntry = newMap.get(id);
                if (lastState == null || !lastState.equals(newEntry.hash()) )
                {
                    changed = true;
                    break;
                }
                if (newMap.containsKey(id))
                {
                    newMap.remove(id);
                }

            }
            if (!changed && newMap.isEmpty())
            {
                return;
            }



            bc.removeAllItems();

            for (int i = 0; i < arr.length; i++)
            {
                TaskEntry jobEntry = arr[i];
                bc.addBean( jobEntry );
                lastStateMap.put(jobEntry.getIdx(), jobEntry.hash());
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

    public void callTableClick( ItemClickEvent event, final TaskEntry item )
    {
        activeElem = item;

        String col_id = null;
        if (event.getPropertyId() != null)
            col_id = event.getPropertyId().toString();

        if (col_id.equals("edit"))
        {
            callTaskDisplay(item);
        }
        else if(col_id.equals("pause"))
        {
            ClickListener ok = new ClickListener() {

                @Override
                public void buttonClick( ClickEvent event )
                {
                    callTaskPause(item);
                }
            };
            if (item.getTaskStatus() != TASKSTATE.PAUSED)
            {
                main.Msg().errmOkCancel(VSMCMain.Txt("Wollen Sie diese Task pausieren?"), ok, null);
            }
            else
            {
                callTaskPause(item);
            }
        }
        else if (item.getTaskStatus() == TASKSTATE.NEEDS_INTERACTION)
        {
            callTaskInteraction( item );
        }
    }
    public TaskEntry getActiveElem()
    {
        return activeElem;
    }

    private void callTaskDisplay( TaskEntry item )
    {
        TaskInfoWindow dlg = new TaskInfoWindow(main, item);

        this.getApplication().getMainWindow().addWindow(dlg);

        
    }
    private void callTaskPause( TaskEntry item )
    {
        if (item.getTaskStatus() == TASKSTATE.PAUSED)
            item.setTaskStatus(TASKSTATE.RUNNING);
        else
            item.setTaskStatus(TASKSTATE.PAUSED);

    }


    private void requestStatus()
    {
        listTasks();
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
//        timer.stop();
    }

    private void callTaskInteraction( final TaskEntry taskEntry )
    {
        final InteractionEntry ie = taskEntry.getTask().getInteractionEntry();
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
                taskEntry.setTaskStatus(TASKSTATE.RUNNING);
            }
        });
        okPanel.getBtAbort().addListener( new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                win.getApplication().getMainWindow().removeWindow(win);
                ie.setAnswer(InteractionEntry.INTERACTION_ANSWER.CANCEL);
                taskEntry.setTaskStatus(TASKSTATE.RUNNING);
            }
        });
        okPanel.getBtRetry().addListener( new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                win.getApplication().getMainWindow().removeWindow(win);
                ie.setAnswer(InteractionEntry.INTERACTION_ANSWER.RETRY);
                taskEntry.setTaskStatus(TASKSTATE.RUNNING);
            }
        });


        this.getApplication().getMainWindow().addWindow(win);
    }

}
