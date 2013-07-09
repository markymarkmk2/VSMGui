/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.event.ItemClickEvent.ItemClickListener;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.AccountConnector;
import de.dimm.vsm.records.Role;
import de.dimm.vsm.records.RoleOption;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADBComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADBLinkField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.sql.SQLException;
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

class RoleOptionDBLinkField extends JPADBLinkField<RoleOption>
{
    ArrayList<ComboEntry> roleOptionEntries;
    public RoleOptionDBLinkField(GenericEntityManager em, ArrayList<ComboEntry> roleOptionEntries)
    {
        super( em, VSMCMain.Txt("Rollenoptionen"), "roleOptions", RoleOption.class);
        this.roleOptionEntries = roleOptionEntries;
    }
    @Override
    public String toString( Object _list )
    {
        StringBuilder sb = new StringBuilder();

        try
        {
            if (_list instanceof List)
            {
                List list = (List) _list;
                for (int i = 0; i < list.size(); i++)
                {
                    Object object = list.get(i);
                    if (sb.length() > 0)
                        sb.append(", ");

                    String txt = object.toString();
                    for (int j = 0; j < roleOptionEntries.size(); j++)
                    {
                        ComboEntry c = roleOptionEntries.get(j);
                        if (c.isDbEntry(object.toString()))
                            txt = c.getGuiEntryKey();
                    }
                    sb.append(txt);
                }
            }
            else
            {
                sb.append(_list.toString());
            }
        }
        catch (Exception noSuchFieldException)
        {
        }
        return sb.toString();
    }
}

/**
 *
 * @author Administrator
 */
public class RoleTable extends BaseDataEditTable<Role>
{
    ArrayList<ComboEntry> roleOptionEntries;

    private RoleTable( VSMCMain main, List<Role> list, ArrayList<JPAField> _fieldList, ItemClickListener listener,  ArrayList<ComboEntry> roleOptionEntries)
    {
        super(main, list, Role.class, _fieldList, listener);
        this.roleOptionEntries = roleOptionEntries;
        
    }

    public static RoleTable createTable( VSMCMain main,  List<Role> list, ItemClickListener listener)
    {
        ArrayList<JPAField> fieldList = new ArrayList<>();
        fieldList.add(new JPATextField(VSMCMain.Txt("Name"), "name"));
        fieldList.add(new JPATextField(VSMCMain.Txt("Benutzerfilter (Regexp)"), "accountmatch"));
        fieldList.add(new AccountConnectorJPADBComboField( VSMCMain.get_base_util_em() ));

        ArrayList<ComboEntry> roe = new ArrayList<>();
        roe.add( new ComboEntry(RoleOption.RL_ALLOW_VIEW_PARAM, VSMCMain.Txt("Parameter Client öffnen")));
        roe.add( new ComboEntry(RoleOption.RL_ALLOW_EDIT_PARAM, VSMCMain.Txt("Parameter bearbeiten")));
        roe.add( new ComboEntry(RoleOption.RL_ADMIN, VSMCMain.Txt("Administrator")));
        roe.add( new ComboEntry(RoleOption.RL_READ_WRITE, VSMCMain.Txt("Schreibrechte")));
        roe.add( new ComboEntry(RoleOption.RL_USERPATH, VSMCMain.Txt("Restorepfad")));
        roe.add( new ComboEntry(RoleOption.RL_FSMAPPINGFILE, VSMCMain.Txt("VSM-Filesystem Mapping")));

        setTooltipText( fieldList, "accountmatch", VSMCMain.Txt("Wildcards mit .* (z.B.) user123.*" ) );

        fieldList.add(new RoleOptionDBLinkField(VSMCMain.get_base_util_em(), roe));

        return new RoleTable( main,  list, fieldList, listener, roe);
    }

    @Override
    protected GenericEntityManager get_em()
    {
        return VSMCMain.get_base_util_em();
    }


    @Override
    protected Role createNewObject()
    {
        Role p =  new Role();
                
        GenericEntityManager gem = get_em();

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


   @Override
    public <S> BaseDataEditTable createChildTable( VSMCMain main, Role role, List<S> list, Class child, ItemClickListener listener )
    {
        if (  child.isAssignableFrom(RoleOption.class))
            return RoleOptionTable.createTable(main, role, (List) list, listener, roleOptionEntries);

        return null;
    }

}
