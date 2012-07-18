/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractOrderedLayout;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.Snapshot;
import de.dimm.vsm.records.StoragePool;
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

/**
 *
 * @author Administrator
 */
public class SnapshotTable extends BaseDataEditTable<Snapshot>
{
    StoragePool pool;
    static final String sdfFormat = "dd.MM.yyyy HH:mm";

    private SnapshotTable( VSMCMain main, StoragePool pool, List<Snapshot> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, Snapshot.class, _fieldList, listener);
        this.pool = pool;
    }

    public static SnapshotTable createTable( VSMCMain main, StoragePool pool, ItemClickListener listener) throws SQLException
    {

        List<Snapshot> list = VSMCMain.get_util_em(pool).createQuery("select T1 from Snapshot T1 where T1.pool_idx=" + pool.getIdx(), Snapshot.class);
        
        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();

        fieldList.add(new JPATextField(VSMCMain.Txt("Name"), "name"));
        fieldList.add(new JPAReadOnlyDateField(VSMCMain.Txt("Datum"), "creation", sdfFormat));

        return new SnapshotTable( main, pool, list, fieldList, listener);
    }

    @Override
    public void checkPlausibility(AbstractOrderedLayout previewTable, Snapshot t, Runnable ok, Runnable nok )
    {
        JPATextField name = (JPATextField)getField("name");
        if (name.getGuiValue(previewTable) == null || name.getGuiValue(previewTable).length() == 0)
        {
            main.Msg().errmOk(VSMCMain.Txt("Ungültiger Name"));
            nok.run();
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
    protected Snapshot createNewObject()
    {
        Snapshot n =  new Snapshot();

        SimpleDateFormat sdf = new SimpleDateFormat(sdfFormat);
        n.setCreation( new Date());
        n.setName(VSMCMain.Txt("Snapshot") + " "  + sdf.format(n.getCreation()));
        n.setPool(pool);

      
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

    

  /* @Override
    public AbstractLayout createEditComponentPanel(  boolean readOnly )
    {
        editPanel = new NodePreviewPanel(this, readOnly);
        editPanel.recreateContent(activeElem);

        return editPanel;
    }*/



}
