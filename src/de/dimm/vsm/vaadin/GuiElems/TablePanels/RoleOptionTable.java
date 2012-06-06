/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.event.ItemClickEvent.ItemClickListener;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.Role;
import de.dimm.vsm.records.RoleOption;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class RoleOptionTable extends BaseDataEditTable<RoleOption>
{
    Role role;

    private RoleOptionTable( VSMCMain main, Role r, List<RoleOption> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, RoleOption.class, _fieldList, listener);
        this.role = r;
    }

    public static RoleOptionTable createTable( VSMCMain main, Role r, List<RoleOption> list, ItemClickListener listener, List<ComboEntry> entries)
    {

        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();

        

        fieldList.add(new JPAComboField(VSMCMain.Txt("Option"), "token", entries));
        fieldList.add(new JPATextField(VSMCMain.Txt("Flags"), "flags"));

        setTableColumnVisible(fieldList, "flags", false);
        setFieldVisible(fieldList, "flags", false);
        
        return new RoleOptionTable( main, r, list, fieldList, listener);
    }


    @Override
    protected GenericEntityManager get_em()
    {
        return main.get_base_util_em();
    }

    @Override
    protected RoleOption createNewObject()
    {
        RoleOption p =  new RoleOption();
        p.setRole(role);
        
        GenericEntityManager gem = get_em();

        try
        {
            gem.check_open_transaction();
            gem.em_persist(p);

            role.getRoleOptions().add(gem, p);

            
            gem.commit_transaction();

            this.requestRepaint();
            return p;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            gem.rollback_transaction();

        }
        return null;

    }
    @Override
    protected String getTablenameText()
    {
        return VSMCMain.Txt(this.getClass().getSimpleName());
    }

}
