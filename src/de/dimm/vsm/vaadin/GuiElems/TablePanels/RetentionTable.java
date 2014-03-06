/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.Retention;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.ColumnGeneratorField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAReadOnlyDateField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


class JPARetentionField extends JPATextField implements ColumnGeneratorField
{

    RetentionColumnGenerator colgen;

    public JPARetentionField()
    {
        super("Gültigkeitsdauer", "retention");

    }

    @Override
    public Component createGui(Object _node)
    {
        node = _node;

        Retention r = (Retention)node;

        TextField tf = new TextField(VSMCMain.Txt("Gültigkeitsdauer"));
        tf.setWidth("250px");

        tf.setValue( getNiceText(r) );
        tf.setData(this);
        return tf;
    }

    // THIS IS CALLED WHEN OBJECT WAS CHANGED TO REFLECT CHANGES GENERATED COLUMN
    @Override
    public void update( BeanItem oldItem )
    {
        Retention job = (Retention)oldItem.getBean();

        if ( colgen != null )
        {
            colgen.label.setValue(getNiceText(job));
        }
    }

    public static String getNiceText(Retention r)
    {
        if (r.getArgType() == null)
            return r.toString();

        String argType = RetentionTable.getNiceArgtype( r.getArgType() );
        String niceOp = RetentionTable.getNiceOpString( r.getArgType(), r.getArgOp() );
        String niceValue = RetentionTable.getNiceValString(r);

        return argType + " " + niceOp + " " + niceValue;
    }

    @Override
    public ColumnGenerator getColumnGenerator()
    {
        colgen =  new RetentionColumnGenerator( this );
        return colgen;
    }

    @Override
    public int getWidth()
    {
        return 250;
    }

}

class RetentionColumnGenerator implements Table.ColumnGenerator
{
    JPARetentionField field; /* Format string for the Double values. */
    Label label;

    public RetentionColumnGenerator(JPARetentionField fld)
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
        Object _a = bi.getBean();
        if (_a instanceof Retention)
        {
            String txt = JPARetentionField.getNiceText((Retention)_a);
            label = new Label(txt);
            return label;
        }
        return null;
    }
}
/**
 *
 * @author Administrator
 */
public class RetentionTable extends BaseDataEditTable<Retention>
{
    StoragePool pool;
    

    static final List<ComboEntry> retentionDurationDim = new ArrayList<ComboEntry>();

    static final List<ComboEntry> argFieldList = new ArrayList<ComboEntry>();

    static
    {
        retentionDurationDim.add( new ComboEntry("m", Txt("Minuten(n)")) );
        retentionDurationDim.add( new ComboEntry("h", Txt("Stunde(n)")) );
        retentionDurationDim.add( new ComboEntry("d", Txt("Tag(e)")) );
        retentionDurationDim.add( new ComboEntry("w", Txt("Wochen(n)")) );
        retentionDurationDim.add( new ComboEntry("y", Txt("Jahr(e)")) );

        argFieldList.add( new ComboEntry(Retention.ARG_TS, Txt("Zeitpunkt der Sicherung")) );
        argFieldList.add( new ComboEntry(Retention.ARG_NAME, Txt("Name")) );
        argFieldList.add( new ComboEntry(Retention.ARG_SIZE, Txt("Größe")) );
        argFieldList.add( new ComboEntry(Retention.ARG_CDATE, Txt("Erstellungsdatum")) );
        argFieldList.add( new ComboEntry(Retention.ARG_MDATE, Txt("Änderungsdatum")) );
        argFieldList.add( new ComboEntry(Retention.ARG_ADATE, Txt("Letztes Zugriffsdatum")) );
        argFieldList.add( new ComboEntry(Retention.ARG_UID, Txt("Benutzer ID")) );
        argFieldList.add( new ComboEntry(Retention.ARG_GID, Txt("Gruppen ID")) );
    }

    static String getNiceOpString( String argType, String op )
    {
        if (Retention.isDateField(argType) || Retention.isRelTSField(argType) )
        {
            if (op.equals(Retention.OP_LT))
                return Txt("älter als");
            if (op.equals(Retention.OP_GT))
                return Txt("jünger als");
        }
        return Txt(Retention.getSqlOpString(op));
    }

    static String getNiceValString( Retention r )
    {
        if ( Retention.isRelTSField(r.getArgType()) )
        {
            long s = Long.parseLong(r.getArgValue()) / 1000;
            return Retention.getNormRelSeconds(s) + " " + getNiceDim(Retention.getNormRelSecondsDim(s) );
        }
        return r.getNiceValue();
    }

    static List<ComboEntry> getOperationComboList( String argType )
    {
        List<ComboEntry> l = new ArrayList<ComboEntry>();

        if (argType == null)
            return l;

        String[] ops = Retention.getAllowedOps(argType);
        for (int i = 0; i < ops.length; i++)
        {
            String op = ops[i];
            String nicerOp = getNiceOpString( argType, op ); 
            l.add(  new ComboEntry( op, nicerOp) );
        }

        return l;
    }


    private RetentionTable( VSMCMain main, StoragePool pool, List<Retention> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, Retention.class, _fieldList, listener);
        this.pool = pool;

    }

    static String getNiceDim(String normDim)
    {
        for (int i = 0; i < retentionDurationDim.size(); i++)
        {
            ComboEntry comboEntry = retentionDurationDim.get(i);
            if (comboEntry.isDbEntry(normDim))
                return comboEntry.getGuiEntryKey();
        }
        return normDim;
    }

    static String getNiceArgtype(String argtype)
    {
        for (int i = 0; i < argFieldList.size(); i++)
        {
            ComboEntry comboEntry = argFieldList.get(i);
            if (comboEntry.isDbEntry(argtype))
                return comboEntry.getGuiEntryKey();
        }
        return argtype;
    }

    private static String Txt(String key )
    {
        return VSMCMain.Txt(key);
    }

    public static RetentionTable createTable( VSMCMain main, StoragePool pool, ItemClickListener listener) throws SQLException
    {

        List<Retention> list = VSMCMain.get_util_em(pool).createQuery("select T1 from Retention T1 where T1.pool_idx=" + pool.getIdx(), Retention.class);
        
        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();

        List<ComboEntry> modeList = new ArrayList<ComboEntry>();
        modeList.add( new ComboEntry(Retention.MD_BACKUP, Txt("Backup")));
        modeList.add( new ComboEntry(Retention.MD_ARCHIVE, Txt("Archiv")));
        fieldList.add( new JPAComboField(Txt("Modus"), "mode", modeList, 
                Txt("Backup: Mindestens eine Version bleibt nach Ablauf der Gültigkeit erhalten\nArchiv: Dateien werden nach Ablauf der Gültigkeit gelöscht") ) );
        
        fieldList.add(new JPATextField(VSMCMain.Txt("Name"), "name"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Gesperrt"), "disabled"));
        fieldList.add(new JPAReadOnlyDateField(VSMCMain.Txt("Estellt"), "creation", DateField.RESOLUTION_DAY));
        fieldList.add(new JPARetentionField());

        List<ComboEntry> actionList = new ArrayList<ComboEntry>();
        actionList.add( new ComboEntry(Retention.AC_DELETE, Txt("Löschen")));
        actionList.add( new ComboEntry(Retention.AC_MOVE, Txt("Verschieben")));
        actionList.add( new ComboEntry(Retention.AC_SCRIPT, Txt("Script starten")));
        fieldList.add( new JPAComboField(Txt("Aktion"), "followAction", actionList ) );


        return new RetentionTable( main, pool, list, fieldList, listener);
    }

    @Override
    public void checkPlausibility( AbstractOrderedLayout previewTable, Retention t, Runnable ok, Runnable nok)
    {
        JPATextField name = (JPATextField)getField("name");
        if (name.getGuiValue(previewTable) == null || name.getGuiValue(previewTable).length() == 0)
        {
            main.Msg().errmOk(VSMCMain.Txt("Ungültiger Name"));
            return;
        }

        JPAComboField ca = (JPAComboField)getField("followAction");
        if (!ca.getSelectedEntry(previewTable).getDbEntry().equals(Retention.AC_DELETE))
        {
            main.Msg().errmOk(VSMCMain.Txt("Aktion ist noch nicht implementiert"));
            return;
        }

        ok.run();
    }

    @Override
    protected GenericEntityManager get_em()
    {
        return VSMCMain.get_util_em(pool);
    }

    @Override
    protected Retention createNewObject()
    {
        Retention n =  new Retention();

        String format = "dd.MM.yyyy HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        n.setCreation( new Date());
        n.setName(VSMCMain.Txt("Retention") + " "  + sdf.format(n.getCreation()));
        n.setPool(pool);
        n.setArgType( Retention.ARG_TS);
        n.setArgOp(Retention.OP_LT);
        // 4 WOCHEN
        n.setArgValue(Long.toString(4 * 7 * 24 * 3600 * 1000l));
        n.setFollowAction(Retention.AC_DELETE);

      
        GenericEntityManager em = get_em();
        
        try
        {
            em.check_open_transaction();
            em.em_persist(n);
      
            em.commit_transaction();

            this.requestRepaint();
            return n;
        }
        catch (Exception e)
        {
            em.rollback_transaction();
            VSMCMain.notify(this, "Abbruch in createNewObject", e.getMessage());

        }
        return null;

    }


    RetentionPreviewPanel editPanel;
    

   @Override
    public AbstractOrderedLayout createEditComponentPanel(  boolean readOnly )
    {
        editPanel = new RetentionPreviewPanel(this, readOnly);
        editPanel.recreateContent(activeElem);
        

        return editPanel;
    }

    @Override
    protected void setEditWinLayout(Window win)
    {
        win.setModal(true);
        win.setStyleName("vsm");
        
        win.setWidth("800px");
    }

    @Override
    protected void saveActiveObject()
    {
        // EXCERPT CHANGES FROM NEW GUIFIELDS
        editPanel.updateObject( activeElem );

        super.saveActiveObject();
    }



}
