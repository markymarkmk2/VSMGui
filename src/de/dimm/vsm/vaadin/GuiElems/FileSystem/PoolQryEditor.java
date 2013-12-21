/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.FileSystem;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import de.dimm.vsm.auth.GuiUser;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.AccountConnector;
import de.dimm.vsm.records.MountEntry;
import de.dimm.vsm.records.RoleOption;
import de.dimm.vsm.records.Snapshot;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAAbstractComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADBComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADateField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.VSMCMain;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

class AccountConnectorJPADBComboField extends JPADBComboField
{
    public AccountConnectorJPADBComboField(GenericEntityManager em)
    {
        super( VSMCMain.Txt("Authentifizierer"), "accountConnector", em, AccountConnector.class, "select T1 from AccountConnector T1", "");
    }

    @Override
    public List<ComboEntry> getEntries() throws SQLException
    {
        List<ComboEntry> entries = new ArrayList<>();

        List<AccountConnector> list = em.createQuery(qry, clazz);

        for (int i = 0; i < list.size(); i++)
        {
            AccountConnector acct = list.get(i);
            String s = acct.getType() + ":" + acct.getIp();

            ComboEntry ce = new ComboEntry(acct, s);
            entries.add(ce);
        }

        return entries;    
    }
}


class JPASnapShotComboField extends JPADBComboField
{
    VSMCMain main;

    public JPASnapShotComboField( GenericEntityManager em, String fieldName)
    {
        super( VSMCMain.Txt("Snapshot"), fieldName, em, Snapshot.class, "select T1 from snapshot T1", "");
    }


   @Override
    public List<ComboEntry> getEntries() throws SQLException
    {
        List<ComboEntry> entries = new ArrayList<>();

        List<Snapshot> list = em.createQuery(qry, clazz);

        for (int i = 0; i < list.size(); i++)
        {
            Snapshot acct = list.get(i);
            String s = acct.getName();

            ComboEntry ce = new ComboEntry(acct, s);
            entries.add(ce);
        }

        return entries;    
    }

}

/**
 *
 * @author Administrator
 */

public class PoolQryEditor extends HorizontalLayout
{
    public static final String DEFAULTWIDTH = "450px";
  
    
    JPADBComboField cbSnapShot;
    JPAAbstractComboField cbTyp;
    MountEntry node;
    

    JPATextField tfUser;
    JPADateField dtTimestamp;
    static final List<ComboEntry> typeList = new ArrayList<>();
    static
    {
        typeList.add( new ComboEntry(MountEntry.TYP_SNAPSHOT, VSMCMain.Txt("Snapshot")));
        typeList.add( new ComboEntry(MountEntry.TYP_TIMESTAMP, VSMCMain.Txt("Timestamp")));
        typeList.add( new ComboEntry(MountEntry.TYP_RDONLY, VSMCMain.Txt("Aktuell nur lesen")));
        typeList.add( new ComboEntry(MountEntry.TYP_RDONLYMULTI, VSMCMain.Txt("Versionen nur lesen")));
        typeList.add( new ComboEntry(MountEntry.TYP_RDWR, VSMCMain.Txt("Aktuell lesen/schreiben")));
    }
    public static String getTypeText( String typ)
    {
        for (ComboEntry ce : typeList)
        {
            if (ce.isDbEntry( typ))
                return ce.getGuiEntryKey();            
        }
        return "";
    }
    
    public static ComboEntry getTypeComboEntry( String typ)
    {
        for (ComboEntry ce : typeList)
        {
            if (ce.isDbEntry( typ))
                return ce;            
        }
        return null;
    }    
  
    public PoolQryEditor( VSMCMain main, GenericEntityManager em, final MountEntry me, final ValueChangeListener changeListener )
    {
        this.node = me;               
        this.setSpacing(true);
                       
        List<ComboEntry> localTypelist = new ArrayList<>(typeList);
        
        // Normale User ohne RDWR-RoleOption dürfen nicht selbsttätig RW-Mounts erstellen, nur SU
        boolean allowRW = false;       
        
        if (main.isSuperUser())
            allowRW = true;
        
        if (main.getUser().hasRoleOption(RoleOption.RL_READ_WRITE))
            allowRW = true;
        
        
        if (!allowRW)
        {
            localTypelist.remove( getTypeComboEntry(MountEntry.TYP_RDWR));
        }
           
        
        cbTyp = new JPAComboField("Art", "typ", localTypelist);
        cbSnapShot = new JPASnapShotComboField(em, "snapShot");
        tfUser = new JPATextField("Benutzer", "username");
        dtTimestamp = new JPADateField("Timestamp", "ts", DateField.RESOLUTION_MIN);

        // ONLY ROOT IS ALLOWED TO CHANGE USER
        if (main.isSuperUser())
        {
            addComponent(tfUser.createGui(me));
        }
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
        if (me.getUsername() != null)
        {
            sb.append(me.getUsername());                
        }
        for (ComboEntry ge : typeList)
        {
            if (ge.isDbEntry(me.getTyp()))
            {
                sb.append(" ");
                sb.append(ge.getGuiEntryKey());
                break;
            }
        }
        if (me.getTyp(). equals(MountEntry.TYP_SNAPSHOT) && me.getSnapShot() != null)
        {
            sb.append(" ");
            sb.append(me.getSnapShot().getName());
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
