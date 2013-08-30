/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Table;

import com.thoughtworks.xstream.XStream;
import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.FileSystemElemNode;
import de.dimm.vsm.vaadin.GuiElems.Fields.ColumnGeneratorField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAAbstractComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADBLinkField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.OkAbortPanel;
import de.dimm.vsm.vaadin.GuiElems.TablePanels.EmptyColumnGenerator;
import de.dimm.vsm.vaadin.VSMCMain;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.vaadin.addons.lazyquerycontainer.CompositeItem;
import org.vaadin.jouni.animator.client.ui.VAnimatorProxy.AnimType;



/**
 *
 * @author Administrator
 */
public abstract class BaseDataEditTable<T> extends Table
{
    public static final String CHECKBOX_MAGIC_PREFIX = "CHECK.";

    protected ArrayList<JPAField> fieldList;
    
    protected T activeElem;
   
    protected BeanContainer bc;

    protected boolean show_edit = true;
    protected boolean show_delete = true;



    ItemClickListener externalListener;


    BaseDataEditTable parentDB;
    List<T> parentObjlist;

    PreviewPanel<T> previewPanel;
    

    protected VSMCMain main;

    public VSMCMain getMain()
    {
        return main;
    }

    
    protected BaseDataEditTable( VSMCMain main,  List<T> parentObjlist, Class clazz, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        this(main, parentObjlist, clazz, _fieldList, listener, true, true, true);
    }
    protected BaseDataEditTable( VSMCMain main,  List<T> parentObjlist, Class clazz, ArrayList<JPAField> _fieldList, ItemClickListener listener, boolean show_edit, boolean show_delete)
    {
        this(main, parentObjlist, clazz, _fieldList, listener, show_edit, show_delete, true);
    }

    protected BaseDataEditTable( VSMCMain main,  List<T> parentObjlist, Class clazz, ArrayList<JPAField> _fieldList, ItemClickListener listener, boolean show_edit, boolean show_delete, boolean sortAscending)
    {
        this.main = main;

        if (!main.allowEdit())
        {
            show_edit = false;
            show_delete = false;
        }
       
        this.parentObjlist = parentObjlist;
        this.fieldList = _fieldList;
        this.externalListener = listener;
        this.show_edit = show_edit;
        this.show_delete = show_delete;

        // SET DEFAULT SORT MODE
        HashMap<String, Object>  mp = new HashMap<String, Object>();
        mp.put("sortascending", sortAscending);
        changeVariables(this, mp);


        bc = new BeanContainer<Long, T>(clazz);
        bc.setBeanIdProperty("idx");

        for (int i = 0; i < parentObjlist.size(); i++)
        {
            bc.addBean( parentObjlist.get(i) );
        }
        
        setSizeFull();

        setContainerDataSource(bc);
        setSelectable(true);
        setMultiSelect(false);
        setImmediate(true); // react at once when something is selected
        setColumnReorderingAllowed(true);
        setColumnCollapsingAllowed(true);

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
                        return "edit";
                    }
                    if(colName.equals("delete"))
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
                return null;
            }
        });

        int edit_columns = 0;
        if (show_edit)
            edit_columns++;
        if (show_delete)
            edit_columns++;

        int visibleFields = 0;
        for (int i = 0; i < _fieldList.size(); i++)
        {
            JPAField jPAField = _fieldList.get(i);
            if (jPAField.isTableColumnVisible())
                visibleFields++;
        }
        // BUILD COLUMNS
        String[] columns = new String[visibleFields + edit_columns];
        String[] columnLabels = new String[visibleFields + edit_columns];
        int i = 0;
        for (int f = 0; f < fieldList.size(); f++)
        {
            JPAField jPAField = fieldList.get(f);
            if (!jPAField.isTableColumnVisible())
                continue;
            
            columnLabels[i] = jPAField.getCaption();

            if (JPAAbstractComboField.class.isAssignableFrom(jPAField.getClass()))
            {
                JPAAbstractComboField fld = (JPAAbstractComboField)jPAField;

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
            else if(jPAField instanceof JPADBLinkField)
            {
                JPADBLinkField fld = (JPADBLinkField)jPAField;

                // COMBOFIELDS HAVE AN OWN COLUMN BECAUSE OF TRANSLATION BETWEEN COMBOENTRIES AND VISUAL ENTRIES
                // WE CREATE A MADB_LINK_MAGIC_PREFIXGIC COLUMNNAME AND REFER TO A GENERATED COLUMN WHICH HANDLES THE TRANSLATION
                columns[i] = DBLinkColumnGenerator.DB_LINK_MAGIC_PREFIX + jPAField.getFieldName();

                DBLinkColumnGenerator colg = new DBLinkColumnGenerator(fld);
                fld.setColumnGenerator( colg );
                addGeneratedColumn(columns[i], colg);
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
            i++;
        }
        int act_index = visibleFields;

        if (show_edit)
        {
            columns[act_index] = "edit";
            columnLabels[act_index] = "";//Main.Txt("Edit");
            addGeneratedColumn(columns[act_index], new EmptyColumnGenerator() );
            act_index++;
        }
        if (show_delete)
        {
            columns[act_index] = "delete";
            columnLabels[act_index] = "";// Main.Txt("Del");
            addGeneratedColumn(columns[act_index], new EmptyColumnGenerator() );
            act_index++;
        }

        setVisibleColumns(columns);

        setColumnHeaders(columnLabels);

        i = 0;
        for (int f = 0; f < fieldList.size(); f++)
        {
            JPAField jPAField = fieldList.get(f);
            if (!jPAField.isTableColumnVisible())
                continue;

            if (jPAField.getWidth() >= 0)
            {
                setColumnWidth(columns[i], jPAField.getWidth());
            }
            if (jPAField.getExpandRatio() >= 0)
            {
                setColumnExpandRatio(columns[i], jPAField.getExpandRatio());
            }
            i++;
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
                    if (event.isDoubleClick())
                        callTableDoubleClick( event, (T)bit.getBean() );
                    else
                        callTableClick( event, (T)bit.getBean() );
                }
            }
        };
       
       
        addListener(l);

    }
    static public void setTableColumnVisible(  ArrayList<JPAField> fieldList, String filedName, boolean visible )
    {
        for (int i = 0; i < fieldList.size(); i++)
        {
            JPAField jPAField = fieldList.get(i);
            if (jPAField.getFieldName().equals(filedName))
            {
                jPAField.setTableColumnVisible( visible );
            }
        }
    }
    static public void setFieldVisible(  ArrayList<JPAField> fieldList, String filedName, boolean visible )
    {
        for (int i = 0; i < fieldList.size(); i++)
        {
            JPAField jPAField = fieldList.get(i);
            if (jPAField.getFieldName().equals(filedName))
            {
                jPAField.setFieldVisible( visible );
            }
        }
    }
    static public void setTooltipText( ArrayList<JPAField> fieldList, String filedName, String txt )
    {
        for (int i = 0; i < fieldList.size(); i++)
        {
            JPAField jPAField = fieldList.get(i);
            if (jPAField.getFieldName().equals(filedName))
            {
                jPAField.setTooltipText( txt );
            }
        }
    }

    static public void setTableColumnWidth(  ArrayList<JPAField> fieldList, String filedName, int visible )
    {
        for (int i = 0; i < fieldList.size(); i++)
        {
            JPAField jPAField = fieldList.get(i);
            if (jPAField.getFieldName().equals(filedName))
            {
                jPAField.setWidth( visible );
            }
        }
    }
    static public void setTableFieldWidth(  ArrayList<JPAField> fieldList, String filedName, int visible )
    {
        for (int i = 0; i < fieldList.size(); i++)
        {
            JPAField jPAField = fieldList.get(i);
            if (jPAField.getFieldName().equals(filedName))
            {
                jPAField.setFieldWidth( visible );
            }
        }
    }
    static public void setTableColumnExpandRatio(  ArrayList<JPAField> fieldList, String filedName, float visible )
    {
        for (int i = 0; i < fieldList.size(); i++)
        {
            JPAField jPAField = fieldList.get(i);
            if (jPAField.getFieldName().equals(filedName))
            {
                jPAField.setExpandRatio(visible);
            }
        }
    }
    static public void setFieldValidator(  ArrayList<JPAField> fieldList, String filedName, Validator v )
    {
        for (int i = 0; i < fieldList.size(); i++)
        {
            JPAField jPAField = fieldList.get(i);
            if (jPAField.getFieldName().equals(filedName))
            {
                jPAField.setValidator(v);
            }
        }
    }

    public void setTableColumnVisible( String filedName, boolean visible )
    {
        setTableColumnVisible(fieldList, filedName, visible);
    }
    public void setTableColumnWidth( String filedName, int visible )
    {
        setTableColumnWidth(fieldList, filedName, visible);
    }
    public void setTableFieldWidth( String filedName, int visible )
    {
        setTableFieldWidth(fieldList, filedName, visible);
    }
    public void setTableColumnExpandRatio( String filedName, float visible )
    {
        setTableColumnExpandRatio(fieldList, filedName, visible);
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

    public void setParentDB( BaseDataEditTable parentDB )
    {
        this.parentDB = parentDB;
    }

    public BaseDataEditTable getParentDB()
    {
        return parentDB;
    }
    public void setValueChanged()
    {
        bc.valueChange( null );
        //setContainerDataSource(bc);
        requestRepaint();
        refreshRowCache();
        if (activeElem != null)
        {
            updateItem(activeElem);
        
            if (previewPanel != null)
            {
                previewPanel.recreateContent(activeElem);
            }
        }

        // RECURSE UP
        if (parentDB != null)
        {
            parentDB.setValueChanged();
        }

    }


    public void callTableClick( ItemClickEvent event, T item )
    {
        if (!main.checkLogin())
            return;

        activeElem = item;

        String col_id = null;
        if (event.getPropertyId() != null)
            col_id = event.getPropertyId().toString();

        if (col_id.equals("edit"))
        {
            callTableEdit(/*isNew*/false);
        }
        else if(col_id.equals("delete"))
        {
            ClickListener ok = new ClickListener() {

                @Override
                public void buttonClick( ClickEvent event )
                {
                    if (!main.allowEditGui())
                        return;
                    callTableDelete();
                }
            };
            main.Msg().errmOkCancel(VSMCMain.Txt("Wollen Sie diesen Eintrag löschen?"), ok, null);
        }
        
        if(externalListener != null)
        {
            externalListener.itemClick(event);
        }
    }

    public void callTableDoubleClick( ItemClickEvent event, T item )
    {
        if (!main.checkLogin())
            return;

        activeElem = item;

        String col_id = null;
        if (event.getPropertyId() != null)
            col_id = event.getPropertyId().toString();

        if (col_id.startsWith(DBLinkColumnGenerator.DB_LINK_MAGIC_PREFIX))
        {
            String fieldName = col_id.substring(DBLinkColumnGenerator.DB_LINK_MAGIC_PREFIX.length());
            Object obj = event.getItemId();
            try
            {
                createChildDB( fieldName, null );
            }
            catch (Exception e)
            {
                VSMCMain.notify(this, "Abbruch beim Öffnen der Tochtertabelle", e.getMessage() );
            }
        }
        else
        {
            callTableEdit(/*isNew*/false);
        }
    }

    public T getActiveElem()
    {
        return activeElem;
    }
    public T getElemBeforEdit()
    {
        return elemBeforEdit;
    }

    public boolean isNew()
    {
        return isNew;
    }
    T elemBeforEdit;
    public void callTableEdit(boolean _isNew)
    {
        if (!main.checkLogin())
            return;

        if (!main.allowEditGui())
            return;

        // SET EDIT MARKERS
        isNew = _isNew;

        XStream xs = new XStream();
        // DO NOT RECURSE INTO FSEN-CHILDREN, IS NOT RELEVANT FOR EDITOR
        xs.omitField( FileSystemElemNode.class, "children" );

        // CLEAR ALL LAZY LISTS, THEN THE SERIALIZATION DOESNT BLOCK ON LARGE CHILDLISTS
        get_em().em_refresh(activeElem);

        elemBeforEdit = (T) xs.fromXML( xs.toXML(activeElem));

        final OkAbortPanel buttonPanel = new OkAbortPanel();

        // CALLBACK TO ENABLE SAVE BUTTON
        ValueChangeListener changeListener = new ValueChangeListener()
        {
            @Override
            public void valueChange( com.vaadin.data.Property.ValueChangeEvent event )
            {
                buttonPanel.getBtOk().setCaption(VSMCMain.Txt("Speichern"));
            }
        };

        // CREATE PANEL
        final AbstractOrderedLayout editPanel = createEditComponentPanel( /*rdonly*/ false);
        
        // ADD OUR CHANGE LISTENER
        setComponentValueChangeListener(changeListener);
        

        final VerticalLayout tableLayout  = new VerticalLayout();
        tableLayout.setSizeFull();
        tableLayout.setSpacing(true);

        Label ll = new Label( getTablenameText() );
        ll.setStyleName("Tablename");
        tableLayout.addComponent( ll);
        tableLayout.addComponent(editPanel);
        tableLayout.setExpandRatio(editPanel, 1.0f);

        tableLayout.addComponent(buttonPanel);

        final Window myw = new Window(VSMCMain.Txt("Edit"));

        buttonPanel.getBtAbort().addListener( new ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                event.getButton().getApplication().getMainWindow().removeWindow(myw);
            }
        });
        buttonPanel.getBtOk().addListener( new ClickListener()
        {

            @Override
            public void buttonClick( final ClickEvent event )
            {

                Runnable okRunner = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        saveActiveObject();
                        event.getButton().getApplication().getMainWindow().removeWindow(myw);

                        updateItem( activeElem );
                        setValueChanged();
                    }
                };
                if (!checkValidators(editPanel))
                {
                    editFallbackRunner.run();
                    VSMCMain.notify(myw, VSMCMain.Txt("Bitte prüfen Sie Ihre Eingaben" ), VSMCMain.Txt("Feld:") + " " + invalidCaption);
                    return;
                }

                checkPlausibility(editPanel, activeElem, okRunner, editFallbackRunner);

                
            }
        });        

        final Application app = this.getWindow().getApplication();
        
        myw.addComponent(tableLayout);

        setEditWinLayout( myw );

        app.getMainWindow().addWindow(myw);

        main.getAnimatorProxy().animate(myw, AnimType.FADE_IN).setDuration(300);
    }

    String invalidCaption;
    protected boolean checkValidators(AbstractOrderedLayout panel)
    {
        for (int i = 0; i < fieldList.size(); i++)
        {
            JPAField jPAField = fieldList.get(i);            
            if (!jPAField.isValid(panel))
            {
                invalidCaption = jPAField.getCaption();
                return false;
            }

        }
        return true;
    }

    @Override
    protected String formatPropertyValue(Object rowId, Object colId, Property property)
    {
        // Format by property type
        if (property.getType() == Date.class && property.getValue() != null)
        {
            String dateFormat = "dd.MM.yyyy HH:mm";
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

    public ArrayList<JPAField> getFieldList()
    {
        return fieldList;
    }




    public AbstractLayout createLocalPreviewPanel( )
    {
        if (previewPanel == null)
        {
            previewPanel = new PreviewPanel<T>(this, true);            
        }
        previewPanel.recreateContent(activeElem);

        return previewPanel;
    }

    public AbstractOrderedLayout createEditComponentPanel(  boolean readOnly )
    {
        PreviewPanel panel = new PreviewPanel<T>(this, readOnly);
        panel.recreateContent(activeElem);

        return panel;
    }
    void setComponentsReadOnly(AbstractOrderedLayout panel, boolean b)
    {
        // CREATE GUI ITEMS
        for (int i = 0; i < fieldList.size(); i++)
        {
            JPAField jPAField = fieldList.get(i);
            Component gui = jPAField.getGuiforField(panel );
            jPAField.setReadOnly(gui, b);
        }
    }

    public void setComponentValueChangeListener( ValueChangeListener changeListener)
    {
        // CREATE GUI ITEMS
        for (int i = 0; i < fieldList.size(); i++)
        {
            JPAField jPAField = fieldList.get(i);
            jPAField.setValueChangeListener(changeListener);
        }
    }

    protected abstract GenericEntityManager get_em();

    protected void deleteObject( T node )
    {
        GenericEntityManager gem = get_em();
        gem.check_open_transaction();
        try
        {
            gem.em_remove(node);
            gem.commit_transaction();
            
            this.requestRepaint();
            this.refreshRowCache();
        }
        catch (Exception e)
        {
            gem.rollback_transaction();
            VSMCMain.Me(this).Msg().errmOk(VSMCMain.Txt("Das Objekt kann nicht gelöscht werden, es wird vermutlich noch verwendet"));
        }
    }

    public void callTableDelete( )
    {
        deleteActiveObject();
    }

    void deleteActiveObject()
    {
        try
        {
            if (!checkDeletePlausible(activeElem))
            {
                return;

            }
        }
        catch (SQLException e)
        {
            VSMCMain.notify(this, "Abbruch in deleteActiveObject", e.getMessage());
            return;
        }
        // ARE WE INSIDE PARENT-DB
        if (parentObjlist != null)
        {
            // REMOVE FROM PARENT-OBJECT-LIST
            parentObjlist.remove(activeElem);

            // UPDATE PARENT-OBJECT
//            if (parentDB != null)
//                parentDB.saveActiveObject();
        }

        // REMOVE VIA ITEM-ID FROM OWN CONTAINER
        Object itemId = getItemId( activeElem );

        bc.removeItem( itemId );
        
        deleteObject( activeElem );

        activeElem = null;

        setValueChanged();
    }

    protected abstract T createNewObject();

    boolean isNew = false;
    protected void registerNewObject( T obj )
    {
        activeElem = obj;
        callTableEdit(/*isNew*/true);
        bc.addBean(activeElem);

        setValueChanged();

    }

    public Component createNewButton()
    {
        if (!main.allowEdit())
            return null;

        ClickListener cl = new ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {

                if (!main.checkLogin())
                    return;

                T obj = createNewObject();

                if (obj != null)
                {
                    registerNewObject(obj);
                }
            }
        };
        NativeButton bt = new NativeButton(VSMCMain.Txt("Neu") + "...", cl);
        bt.setStyleName("DbNewButton");
        
        return bt;
    }

    protected void saveActiveObject()
    {
        if (!main.allowEdit())
            return;

        GenericEntityManager gem = get_em();
        gem.check_open_transaction();
        try
        {
            activeElem = gem.em_merge(activeElem);
            gem.commit_transaction();

            updateItem( activeElem );
            this.requestRepaint();
            this.refreshRowCache();
//            updateValues();
        }
        catch (Exception e)
        {
            gem.rollback_transaction();
            VSMCMain.notify(this, "Fehler beim Sichern", e.getMessage() );
        }
    }

    void fallbackEdit()
    {
        if (activeElem != null)
        {
            long idx = -1;
            Object o = getItemId( activeElem );
            if (o != null)
            {
                idx = ((Long)o).longValue();
                Class<T> cl = (Class<T>)activeElem.getClass();
                activeElem = get_em().em_find(cl, idx);
            }
        }
    }
    Runnable editFallbackRunner = new Runnable() {

        @Override
        public void run()
        {
            fallbackEdit();
        }
    };

    public void checkPlausibility(AbstractOrderedLayout editPanel, T t, Runnable okListener, Runnable nokListener)
    {
        if (okListener != null)
            okListener.run();               
    }

    Object getItemId( Object o )
    {
        try
        {
            Method getIdx = o.getClass().getMethod("getIdx");
            Object itemId = getIdx.invoke(o, (Object[]) null);
            return itemId;
        }
        catch (Exception exception)
        {
        }
        return null;
    }
    public void updateItem(Object o)
    {
        Object itemId = null;


        // UPDATE BY SETTING PROPERTY AGAIN
        try
        {
            itemId = getItemId( o );

            if (bc.containsId(itemId))
            {
                BeanItem oldItem = bc.getItem(itemId);
                if (oldItem != null)
                {
                    for (int i = 0; i < fieldList.size(); i++)
                    {
                         fieldList.get(i).update( oldItem );
                    }
                }
            }
        }
        catch (Exception e)
        {
            VSMCMain.notify(this, "Fehler beim Update", e.getMessage() );
        }
    }
    public Component createHeader( String caption )
    {
        Label ll = new Label(caption);
        ll.setStyleName("Tablename");

        HorizontalLayout head = new HorizontalLayout();
        head.setWidth("100%");
        head.setSpacing(true);
        head.addComponent(ll);
        head.setComponentAlignment(ll, Alignment.MIDDLE_LEFT);
        
        Component bt = createNewButton();
        if (bt != null)
        {
            head.addComponent(bt);
            head.setComponentAlignment(bt, Alignment.MIDDLE_RIGHT);
        }

        return head;

    }
    
    public Component createNoNewButtonHeader( String caption )
    {
        Label ll = new Label(caption);
        ll.setStyleName("Tablename");

        HorizontalLayout head = new HorizontalLayout();
        head.setWidth("100%");
        head.setSpacing(true);
        head.addComponent(ll);
        head.setComponentAlignment(ll, Alignment.MIDDLE_LEFT);

        return head;

    }

    public void createChildDB( JPADBLinkField dbl, final ClickListener okActionClick )
    {

                ItemClickListener listener = new ItemClickListener() {

                    @Override
                    public void itemClick( ItemClickEvent event )
                    {
                        //throw new UnsupportedOperationException("Not supported yet.");
                    }
                };

                List<T> list = dbl.getList( activeElem);
                if (list == null)
                    throw new RuntimeException("Liste für " + dbl.getFieldName() + " ist nicht initialisiert!");




                BaseDataEditTable childTable = createChildTable( main, activeElem, list, dbl.getClient(), listener);

                childTable.setParentDB(this);

                final VerticalLayout tableWin  = new VerticalLayout();
                tableWin.setSizeFull();
                tableWin.setSpacing(true);

                Component head = childTable.createHeader( getHeaderText(dbl) );

                tableWin.addComponent(head);
                tableWin.addComponent(childTable);
                tableWin.setExpandRatio(childTable, 1.0f);

                HorizontalLayout bt_hl = new HorizontalLayout();
                bt_hl.setSpacing(true);
                bt_hl.setWidth("100%");

                final Button bt_ok = new Button(VSMCMain.Txt("Schließen"));
                bt_hl.addComponent(bt_ok);
                bt_hl.setComponentAlignment(bt_ok, Alignment.MIDDLE_RIGHT);
                tableWin.addComponent(bt_hl);

                final Window win = new Window( dbl.getCaption());

                bt_ok.addListener( new ClickListener() {

                    @Override
                    public void buttonClick( ClickEvent event )
                    {
                        VSMCMain.Me(win).getRootWin().removeWindow(win);

                        if (okActionClick != null)
                            okActionClick.buttonClick(event);
                    }
                });


                win.addComponent(tableWin);


                childTable.setDBWinLayout( win );

                // OPEN ADJUSTED
                Window parentWin = this.getWindow();
                if (parentWin != null && parentWin != this.getApplication().getMainWindow())
                {
                    win.setPositionX( parentWin.getPositionX() + 25 );
                    win.setPositionY( parentWin.getPositionY() + 25 );
                }

                VSMCMain.Me(this).getRootWin().addWindow(win);

    }

    public void createChildDB( String fieldName, final ClickListener okActionClick )
    {
        if (!main.checkLogin())
            return;


        for (int i = 0; i < fieldList.size(); i++)
        {
            JPAField jPAField = fieldList.get(i);
            if (jPAField.getFieldName().equals(fieldName) && jPAField instanceof JPADBLinkField)
            {
                JPADBLinkField dbl = (JPADBLinkField)jPAField;

                createChildDB(dbl, okActionClick);

                break;
            }
        }
    }
    protected String getHeaderText(JPAField dbl)
    {
        String s = VSMCMain.Txt("Liste der" + " " + dbl.getCaption());
        return s;
    }
    public String getTablenameText()
    {
        return VSMCMain.Txt(this.getClass().getSimpleName());
    }

    protected void setDBWinLayout(Window win)
    {
        win.setModal(true);
        win.setStyleName("vsm");
        //win.setHeight("500px");
        win.setWidth("500px");
    }
    protected void setEditWinLayout(Window win)
    {
        win.setModal(true);
        win.setStyleName("vsm");
        //win.setHeight("500px");
        win.setWidth("500px");
    }


    public <S> BaseDataEditTable createChildTable( VSMCMain main, T sched, List<S> list, Class child, ItemClickListener listener)
    {
        return null;
    }

    protected boolean checkDeletePlausible(T t) throws SQLException
    {
        return true;
    }



}
