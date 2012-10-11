/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractOrderedLayout;
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
        fieldList.add(new JPATextField(VSMCMain.Txt("Parameter"), "optionStr"));
        fieldList.add(new JPATextField(VSMCMain.Txt("Flags"), "flags"));

        setTableColumnVisible(fieldList, "flags", false);
        setTableFieldWidth(fieldList, "optionStr", 300);
        //setTableColumnVisible(fieldList, "option", false);
        //setFieldVisible(fieldList, "flags", false);
        
        return new RoleOptionTable( main, r, list, fieldList, listener);
    }


    RoleOptionPreviewPanel editPanel;

    @Override
    public AbstractOrderedLayout createEditComponentPanel(  boolean readOnly )
    {
        editPanel = new RoleOptionPreviewPanel(this, readOnly);
        editPanel.recreateContent(activeElem);

        return editPanel;
    }


    @Override
    protected GenericEntityManager get_em()
    {
        return VSMCMain.get_base_util_em();
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
    public void checkPlausibility( AbstractOrderedLayout editPanel, RoleOption t, Runnable ok, Runnable nok )
    {
        if (t.getToken().equals(RoleOption.RL_USERPATH))
        {
            if (!t.isValidUserPath())
            {
                main.Msg().errmOk("Bitte geben Sie bite den Pfad im Format <Rechner:Port/Pfad> an\n\nBeispiele:\n"
                        + "10.1.1.1:8082:G:\\restore\n"
                        + "dataserver:8082:/opt/volumes/restore\nrestoreserver:8082:/");
                nok.run();
                return;
            }
        }
        ok.run();
        
    }

    


}
