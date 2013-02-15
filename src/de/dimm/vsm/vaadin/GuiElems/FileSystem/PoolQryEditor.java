/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.FileSystem;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.MountEntry;
import de.dimm.vsm.records.Snapshot;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.tasks.TaskEntry;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.VSMCMain;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;



/**
 *
 * @author Administrator
 */



public class PoolQryEditor extends HorizontalLayout
{
    public static final String DEFAULTWIDTH = "250px";

    ComboBox cbTyp;
    ComboBox cbSnapShot;
    DateField dtTimestamp;
    AbstractField tfUser;

    Object node;
  

    public final void rebuildSnapshotList(VSMCMain main, MountEntry me) {
        StoragePool pool = main.getStoragePool(me.getPoolIdx());   
        if (pool == null)
            return;
        
        GenericEntityManager em = VSMCMain.get_util_em(pool);
        try {
            List<Snapshot> list = em.createQuery("select t1 from Snapshot t1", Snapshot.class);
            
            BeanContainer bc = new BeanContainer<Long, Snapshot>(Snapshot.class);
            bc.setBeanIdProperty("idx");
            bc.addAll(list);
            cbSnapShot.setContainerDataSource(bc);
        } catch (SQLException sQLException) {
        } catch (IllegalStateException illegalStateException) {
        }
        
    }

    public PoolQryEditor( VSMCMain main, MountEntry me)
    {
        List<ComboEntry> typeList = new ArrayList<ComboEntry>();
        typeList.add( new ComboEntry(MountEntry.TYP_SNAPSHOT, VSMCMain.Txt("Snapshot")));
        typeList.add( new ComboEntry(MountEntry.TYP_TIMESTAMP, VSMCMain.Txt("Timestamp")));
        typeList.add( new ComboEntry(MountEntry.TYP_RDONLY, VSMCMain.Txt("Aktuell nur lesen")));
        typeList.add( new ComboEntry(MountEntry.TYP_RDWR, VSMCMain.Txt("Aktuell lesen/schreiben")));
        
        
        cbTyp = new ComboBox("Art", typeList);
        cbTyp.setImmediate(true);
        tfUser= new TextField("Benutzer");
        tfUser.setImmediate(true);
        dtTimestamp = new DateField("Timestamp");
        dtTimestamp.setImmediate(true);
        
        
        
        cbSnapShot = new ComboBox("Snapshot");
        cbSnapShot.setImmediate(true);
 
        cbTyp.addListener( new Property.ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                String typ = cbTyp.getValue().toString();
                dtTimestamp.setVisible(typ.equals(MountEntry.TYP_TIMESTAMP));
                cbSnapShot.setVisible(typ.equals(MountEntry.TYP_SNAPSHOT));
            }
        });

        rebuildSnapshotList( main, me);
        
        addComponent(tfUser);
        addComponent(cbTyp);
        addComponent(dtTimestamp);
        addComponent(cbSnapShot);
        
      
    }
   

  
    @Override
    public void setReadOnly( boolean readOnly )
    {
        cbTyp.setReadOnly(readOnly);
        tfUser.setReadOnly(readOnly);
    }

}
