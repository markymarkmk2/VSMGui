/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Window;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.ClientInfo;
import de.dimm.vsm.records.ClientVolume;
import de.dimm.vsm.records.Excludes;
import de.dimm.vsm.records.Schedule;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADBLinkField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class ClientInfoTable extends BaseDataEditTable<ClientInfo>
{
    Schedule sched;

    private ClientInfoTable( VSMCMain main, Schedule sched, List<ClientInfo> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, ClientInfo.class, _fieldList, listener);
        this.sched = sched;
    }

    public static ClientInfoTable createTable( VSMCMain main, Schedule sched, List<ClientInfo> list, ItemClickListener listener)
    {
        GenericEntityManager em = main.get_util_em(sched.getPool());

        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();
        fieldList.add(new JPATextField(VSMCMain.Txt("IP"), "ip"));
        fieldList.add(new JPATextField(VSMCMain.Txt("Port"), "port"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Gesperrt"), "disabled"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Compression"), "compression"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("NurNeuereDateien"), "onlyNewer"));
        fieldList.add(new JPADBLinkField( em, VSMCMain.Txt("Volumes"), "volumeList", ClientVolume.class));
        fieldList.add(new JPADBLinkField( em, VSMCMain.Txt("Excludes"), "exclList", Excludes.class));
        
        return new ClientInfoTable( main, sched, list, fieldList, listener);
    }

    @Override
    protected GenericEntityManager get_em()
    {
        return main.get_util_em(sched.getPool());
    }


    @Override
    protected ClientInfo createNewObject()
    {
        ClientInfo p =  new ClientInfo();
        p.setSched(sched);
        p.setIp("localhost");
        p.setPort(8082);
        
        
        GenericEntityManager gem = get_em();

        try
        {
            gem.check_open_transaction();
            gem.em_persist(p);
            sched.getClientList().addIfRealized(p);

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
    public <S> BaseDataEditTable createChildTable( VSMCMain main, ClientInfo sched, List<S> list, Class child, ItemClickListener listener )
    {
        if (  child.isAssignableFrom(Excludes.class))
            return ExcludesTable.createTable(main, sched, (List) list, listener);
        if (  child.isAssignableFrom(ClientVolume.class))
            return ClientVolumeTable.createTable(main, sched, (List) list, listener);

        return null;
    }
    @Override
    protected String getTablenameText()
    {
        return VSMCMain.Txt(this.getClass().getSimpleName());
    }
    
    @Override
    protected void setDBWinLayout( Window win )
    {
        super.setDBWinLayout(win);
        win.setWidth("700px");
    }

    @Override
    protected boolean checkDeletePlausible( ClientInfo t ) throws SQLException
    {
        if (!super.checkDeletePlausible(t))
            return false;
        

        if (!t.getVolumeList().getList(get_em()).isEmpty())
        {
            main.Msg().errmOk(VSMCMain.Txt("Bitte löschen Sie zunächst die zugehörigen Volumes") );
            return false;
        }
        return true;
    }


}
