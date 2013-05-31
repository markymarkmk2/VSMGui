/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.SidebarPanels;

import de.dimm.vsm.vaadin.GuiElems.TablePanels.EmptyColumnGenerator;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
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
import de.dimm.vsm.auth.User;
import de.dimm.vsm.hash.StringUtils;
import de.dimm.vsm.records.MountEntry;
import de.dimm.vsm.tasks.TaskEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.ColumnGeneratorField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.FileSystem.PoolQryEditor;
import de.dimm.vsm.vaadin.GuiElems.Table.ComboColumnGenerator;
import de.dimm.vsm.vaadin.VSMCMain;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.vaadin.addons.lazyquerycontainer.CompositeItem;




class MountEntryColumnGenerator implements Table.ColumnGenerator
{
    Label label;
    @Override
    public Component generateCell( Table source, Object itemId, Object columnId )
    {
        BeanItem it = (BeanItem) source.getItem(itemId);
        MountEntry job = (MountEntry)it.getBean();
        label = new Label(job.getName() + " " + PoolQryEditor.getNiceStr( job )  + " " + job.getIp() + ":" + job.getMountPath().getPath());
        return label;
    }
}

class MountEntryField extends JPATextField implements ColumnGeneratorField
{

    MountEntryColumnGenerator colgen;

    public MountEntryField()
    {
        super("MountEntry", "mountEntry");
    }

    @Override
    public Component createGui(Object _node)
    {
        node = _node;

        TaskEntry job = (TaskEntry)node;

        TextField tf = new TextField("MountEntry");

        tf.setValue( colgen.label.getValue());
        tf.setData(this);
        return tf;
    }

    @Override
    public ColumnGenerator getColumnGenerator()
    {
        colgen = new MountEntryColumnGenerator();
        return colgen;
    }
}


/**
 *
 * @author Administrator
 */
public class MountEntryViewTable extends Table
{
    VSMCMain main;
    public static final String CHECKBOX_MAGIC_PREFIX = "CHECK.";
    static String dateFormat = "dd.MM.yyyy HH:mm";

    protected ArrayList<JPAField> fieldList;

    MountEntry activeElem;
    final BeanItemContainer< MountEntry> bc;


    public MountEntryViewTable( VSMCMain main )
    {
        this.main = main;
        bc = new BeanItemContainer<>(MountEntry.class);

        ArrayList<JPAField> fl = new ArrayList<>();
        //fl.add(new JPATextField(VSMCMain.Txt("Name"), "name"));
        
        MountEntryField mef = new MountEntryField();
        mef.setExpandRatio( 1);
        fl.add( mef);
        
        
        initTable( fl, false, true );

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
                            
                            return "delete";
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
            columnLabels[act_index] = "Unmount";// Main.Txt("Del");
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
                    callTableClick( event, (MountEntry)bit.getBean() );
                }
            }
        };

        addListener(l);
    }
    
    void listTasks()
    {
        List<MountEntry> allEntries = main.getDummyGuiServerApi().getAllMountEntries();
        List<MountEntry> mountedEntries = main.getDummyGuiServerApi().getMountedMountEntries();
        if (mountedEntries.isEmpty())
        {
            activeElem = null;
            synchronized(bc)
            {
                bc.removeAllItems();
            }
            
            return;
        }
        
        List<MountEntry> newMap = new ArrayList<>();        
        newMap.addAll( mountedEntries);

        synchronized(bc)
        {
            Collection<MountEntry> coll = bc.getItemIds();
            for (MountEntry mountEntry : coll)
            {
                if (newMap.contains(mountEntry))
                {
                    newMap.remove(mountEntry);
                }
            }
            
            if (newMap.isEmpty())
            {
                return;
            }

            bc.removeAllItems();
            User usr = main.getUser();
            
            // Remove all entries of other users
            FileSystemViewer.filterUserEntries( main.getGuiUser().getUser(), mountedEntries);
            bc.addAll( mountedEntries );
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

    public void callTableClick( ItemClickEvent event, final MountEntry item )
    {
        activeElem = item;

        String col_id = null;
        if (event.getPropertyId() != null)
            col_id = event.getPropertyId().toString();

        if (col_id.equals("edit"))
        {
            callDisplay(item);
        }
        else if(col_id.equals("pause"))
        {
            ClickListener ok = new ClickListener() {

                @Override
                public void buttonClick( ClickEvent event )
                {
                    callUnmount(item);
                }
            };
            main.Msg().errmOkCancel(VSMCMain.Txt("Wollen Sie diesen Eintrag unmounten?"), ok, null);                        
        }        
    }

    public MountEntry getActiveElem()
    {
        return activeElem;
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

    private void callUnmount( MountEntry val )
    {                   
         main.getDummyGuiServerApi().unMountEntry( val );
    }

    private void callDisplay( MountEntry item )
    {
        
    }   
}
