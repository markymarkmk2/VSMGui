/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.FileSystem;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.MountEntry;
import de.dimm.vsm.records.Snapshot;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAAbstractComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADateField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.VSMCMain;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;



class JPASnapShotComboField extends JPAAbstractComboField<MountEntry>
{

    VSMCMain main;

   
  

    public JPASnapShotComboField( VSMCMain main,  MountEntry me, String fieldName)
    {
        super( VSMCMain.Txt("Snapshot"), fieldName );
        this.main = main;
        node = me;

    }



    @Override
    public List<ComboEntry> getEntries()
    {
        return getEntries(node);
    }

    public static List<ComboEntry> getEntries(MountEntry me)
    {
        List<ComboEntry> snapShotList = new ArrayList<ComboEntry>();

        if (me == null)
            return snapShotList;

        StoragePool pool = VSMCMain.getStoragePool(me.getPoolIdx());
        if (pool == null)
            return snapShotList;

        GenericEntityManager em = VSMCMain.get_util_em(pool);
        try {
            List<Snapshot> list = em.createQuery("select t1 from Snapshot t1", Snapshot.class);

            for (int i = 0; i < list.size(); i++)
            {
                Snapshot snapshot = list.get(i);
                snapShotList.add( new ComboEntry(snapshot.getIdx(), snapshot.getName()));
            }

        } catch (SQLException sQLException) {
        } catch (IllegalStateException illegalStateException) {
        }
        
        return snapShotList;
    }

}

/**
 *
 * @author Administrator
 */

public class PoolQryEditor extends HorizontalLayout
{
    public static final String DEFAULTWIDTH = "450px";

    String typFieldName;
     String userFieldName;
     String tsFieldName;
     
     String snFieldName;
    

    //ComboBox cbTyp;
    JPASnapShotComboField cbSnapShot;
   // DateField dtTimestamp;
   // AbstractField tfUser;

    JPAAbstractComboField cbTyp;

    MountEntry node;
    //Object node;

    JPATextField tfUser;
    JPADateField dtTimestamp;
    static final List<ComboEntry> typeList = new ArrayList<ComboEntry>();
    static
    {
        typeList.add( new ComboEntry(MountEntry.TYP_SNAPSHOT, VSMCMain.Txt("Snapshot")));
        typeList.add( new ComboEntry(MountEntry.TYP_TIMESTAMP, VSMCMain.Txt("Timestamp")));
        typeList.add( new ComboEntry(MountEntry.TYP_RDONLY, VSMCMain.Txt("Aktuell nur lesen")));
        typeList.add( new ComboEntry(MountEntry.TYP_RDWR, VSMCMain.Txt("Aktuell lesen/schreiben")));
    }
    
  

    public PoolQryEditor( VSMCMain main, final MountEntry me, final ValueChangeListener changeListener, String typFieldName, String userFieldName, String tsFieldName, String snFieldName )
    {
        this.node = me;
        this.typFieldName = typFieldName;
        this.userFieldName = userFieldName;
        this.tsFieldName = tsFieldName;       
        this.snFieldName = snFieldName;
       
        
        this.setSpacing(true);
        
        
       

        cbTyp = new JPAComboField("Art", typFieldName, typeList);
        cbSnapShot = new JPASnapShotComboField(main, node, snFieldName);
        tfUser = new JPATextField("Benutzer", userFieldName);
        dtTimestamp = new JPADateField("Timestamp", tsFieldName, DateField.RESOLUTION_MIN);
      
        
                
        addComponent(tfUser.createGui(me));
        addComponent(cbTyp.createGui(me));
        addComponent(dtTimestamp.createGui(me));
        addComponent(cbSnapShot.createGui(me));

        ((ComboBox)cbTyp.getGuiforField(this)).addListener( new ValueChangeListener() {

            @Override
            public void valueChange( ValueChangeEvent event )
            {
                updateVisibility(me);
            }
        });
        updateVisibility(me);
        
    }
    final void updateVisibility( final MountEntry me)
    {
        ComboEntry ce = cbTyp.getSelectedEntry(this);

        String typ = ce.getDbEntry().toString();
        dtTimestamp.getGuiforField(this).setVisible(typ.equals(MountEntry.TYP_TIMESTAMP));

        ComboBox snap = (ComboBox)cbSnapShot.getGuiforField(this);
        snap.setVisible(typ.equals(MountEntry.TYP_SNAPSHOT));
        if (snap.isVisible()) 
        {
            Container container = snap.getContainerDataSource();
            container.removeAllItems();

            List<ComboEntry> entries = cbSnapShot.getEntries(me);
            for (ComboEntry comboEntry : entries)
            {
                Item it = container.addItem(comboEntry.getGuiEntryKey());
                if (it == null)
                    throw new RuntimeException("Doppelter Comboeintrag");
                it.getItemProperty(snFieldName).setValue(comboEntry.getDbEntry());
            }
        }
    }

  
    @Override
    public void setReadOnly( boolean readOnly )
    {
       /* cbTyp.setReadOnly(readOnly);
        tfUser.setReadOnly(readOnly);
        dtTimestamp.setReadOnly(readOnly);
        cbSnapShot.setReadOnly(readOnly);*/
    }

    public static String getNiceStr( MountEntry me )
    {
        StringBuilder sb = new StringBuilder();
        sb.append(me.getUsername());

        sb.append(me.getUsername());

        for (ComboEntry ge : typeList)
        {
            if (ge.isDbEntry(me.getTyp()))
            {
                sb.append(" ");
                sb.append(ge.getGuiEntryKey());
                break;
            }
        }
        if (me.getTyp(). equals(MountEntry.TYP_SNAPSHOT))
        {
            List<ComboEntry> snList = JPASnapShotComboField.getEntries(me);
            for (ComboEntry ge : snList)
            {
                if (ge.isDbEntry(me.getSnapshotIdx()))
                {
                    sb.append(" ");
                    sb.append(ge.getGuiEntryKey());
                    break;
                }
            }
        }
        if (me.getTyp(). equals(MountEntry.TYP_TIMESTAMP) && me.getTs() != null)
        {
            sb.append(" ");
            SimpleDateFormat sdfg = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            sb.append(sdfg.format(me.getTs()));
        }
        return sb.toString();
    }

}
