/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.data.validator.NullValidator;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.mail.NotificationEntry;
import de.dimm.vsm.records.MailGroup;
import de.dimm.vsm.records.MailNotifications;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAAbstractComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;



class JPAMailGroupComboField extends JPAAbstractComboField
{

    VSMCMain main;

    public JPAMailGroupComboField( VSMCMain main)
    {
        super( VSMCMain.Txt("Mailgruppe"), "group" );
        this.main = main;
    }

    @Override
    public List<ComboEntry> getEntries()
    {
        List<ComboEntry> entries = new ArrayList<ComboEntry>();

        try
        {
            List<MailGroup> list = VSMCMain.get_base_util_em().createQuery("select T1 from MailGroup T1", MailGroup.class);

            for (int i = 0; i < list.size(); i++)
            {
                MailGroup object = list.get(i);

                String name = object.getName();

                ComboEntry ce = new ComboEntry(object, name);
                entries.add(ce);
            }
        }
        catch (SQLException sQLException)
        {
        }
        return entries;
    }
}
class JPANotificationEntryComboField extends JPAAbstractComboField
{

    VSMCMain main;

    public JPANotificationEntryComboField( VSMCMain main)
    {
        super( VSMCMain.Txt("Benachrichtigung"), "keyString" );
        this.main = main;
    }

    @Override
    public List<ComboEntry> getEntries()
    {
        List<ComboEntry> entries = new ArrayList<ComboEntry>();

        try
        {
            List<NotificationEntry> list = main.getNotificationServer().listNotificationEntries();

            Collections.sort(list, new Comparator<NotificationEntry>() {

                @Override
                public int compare( NotificationEntry o1, NotificationEntry o2 )
                {
                    int c = o1.getLeveltext().compareTo(o2.getLeveltext());
                    if (c != 0)
                        return c;
                    
                    return o1.getSubject(null).compareTo(o2.getSubject(null));
                }
            });

            for (int i = 0; i < list.size(); i++)
            {
                NotificationEntry object = list.get(i);

                String name = object.getLeveltext() + ": " + object.getSubject(null);

                ComboEntry ce = new ComboEntry(object.getKey(), name);
                entries.add(ce);
            }
        }
        catch (Exception sQLException)
        {
        }
        return entries;
    }

}


/**
 *
 * @author Administrator
 */
public class MailNotificationTable extends BaseDataEditTable<MailNotifications>
{
    private MailNotificationTable( VSMCMain main, List<MailNotifications> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, MailNotifications.class, _fieldList, listener);
       
    }

    public static MailNotificationTable createTable( VSMCMain main,  List<MailNotifications> list, ItemClickListener listener)
    {
        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();

        fieldList.add(new JPAMailGroupComboField( main ) );
        fieldList.add(new JPANotificationEntryComboField( main ) );
        fieldList.add(new JPACheckBox( "Deaktiviert", "disabled" ) );

        setTableFieldWidth(fieldList, "keyString", 400);
        setTableColumnWidth(fieldList, "keyString", 200);
        setTableColumnExpandRatio(fieldList, "keyString", 1.0f);

        setFieldValidator(fieldList, "group", new NullValidator(VSMCMain.Txt("Bitte wählen Sie eine Gruppe aus" ),false));
        setFieldValidator(fieldList, "keyString", new NullValidator(VSMCMain.Txt("Bitte wählen Sie eine Benachrichtigung aus" ), false));
        
        return new MailNotificationTable( main,  list, fieldList, listener);
    }

    @Override
    protected GenericEntityManager get_em()
    {
        return VSMCMain.get_base_util_em();
    }


    @Override
    protected MailNotifications createNewObject()
    {
        MailNotifications p =  new MailNotifications();


        GenericEntityManager gem = get_em();

        List<MailGroup> list = null;
        try
        {
            list = VSMCMain.get_base_util_em().createQuery("select p from MailGroup p order by T1.idx desc", MailGroup.class);
            if (list.isEmpty())
            {
                VSMCMain.notify(this, "Bitte legen Sie zuerst MailGruppen an", "");
                return null;
            }
            p.setGroup(list.get(0));
        }
        catch (Exception sQLException)
        {
        }


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
            gem.rollback_transaction();
            VSMCMain.notify(this, "Abbruch in createNewObject", e.getMessage());
        }
        return null;
    }




}
