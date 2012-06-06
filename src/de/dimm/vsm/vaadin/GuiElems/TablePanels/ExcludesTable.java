/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.event.ItemClickEvent.ItemClickListener;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.ClientInfo;
import de.dimm.vsm.records.Excludes;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
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
public class ExcludesTable extends BaseDataEditTable<Excludes>
{
    ClientInfo clientInfo;

    private ExcludesTable( VSMCMain main, ClientInfo clientInfo, List<Excludes> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, Excludes.class, _fieldList, listener);
        this.clientInfo = clientInfo;
    }

    public static ExcludesTable createTable( VSMCMain main, ClientInfo clientInfo, List<Excludes> list, ItemClickListener listener)
    {

        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();
        fieldList.add(new JPATextField(VSMCMain.Txt("Argument"), "argument"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Directory"), "isDir"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Kompletter Pfad"), "isFullPath"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Nur Treffer"), "includeMatches"));

        ArrayList<ComboEntry> entries = new ArrayList<ComboEntry>();
        entries.add( new ComboEntry(Excludes.MD_BEGINS_WITH, VSMCMain.Txt("beginnt mit")));
        entries.add( new ComboEntry(Excludes.MD_ENDS_WITH, VSMCMain.Txt("endet mit")));
        entries.add( new ComboEntry(Excludes.MD_EXACTLY, VSMCMain.Txt("genau")));
        entries.add( new ComboEntry(Excludes.MD_CONTAINS, VSMCMain.Txt("enthält")));
        entries.add( new ComboEntry(Excludes.MD_REGEXP, VSMCMain.Txt("Reg Exp")));
//        entries.add( new ComboEntry(Excludes.MD_OLDER_THAN, VSMCMain.Txt("älter als")));
//        entries.add( new ComboEntry(Excludes.MD_NEWER_THAN, VSMCMain.Txt("neuer als")));
        fieldList.add(new JPAComboField(VSMCMain.Txt("Modus"), "mode", entries));



        return new ExcludesTable( main, clientInfo, list, fieldList, listener);
    }


    @Override
    protected GenericEntityManager get_em()
    {
        return main.get_util_em(clientInfo.getSched().getPool());
    }


    @Override
    protected Excludes createNewObject()
    {
        Excludes p =  new Excludes();
        p.setArgument("X");
        p.setMode( Excludes.MD_BEGINS_WITH );
        p.setClinfo(clientInfo);

        
        GenericEntityManager gem = get_em();

        try
        {
            gem.check_open_transaction();
            gem.em_persist(p);

            clientInfo.getExclList().addIfRealized(p);

            
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
