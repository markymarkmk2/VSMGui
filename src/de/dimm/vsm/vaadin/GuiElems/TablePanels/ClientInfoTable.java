/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.net.interfaces.AgentApi;
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
import java.util.Properties;

class AgentOptValidator extends AbstractValidator
{
    String key;
    String val;
    ClientInfoTable clit;
    

    public AgentOptValidator( ClientInfoTable clit, String key, String val )
    {
        super(VSMCMain.Txt("Dieser Agent unterstützt dieses Feature nicht"));
        this.key = key;
        this.val = val;
        this.clit = clit;
    }

   

    @Override
    public boolean isValid( Object value )
    {
        if (value instanceof Boolean)
        {
            Boolean b = (Boolean) value;
            if (b.booleanValue())
            {
                Properties p = getProperties();
                if (p != null)
                    return  (p.getProperty(key, "sdhaks").equals(val));
            }
        }
        return true;
    }
    
    Properties getProperties()
    {
        return clit.getAgentProperties();
    }

}
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
        GenericEntityManager em = VSMCMain.get_util_em(sched.getPool());

        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();
        fieldList.add(new JPATextField(VSMCMain.Txt("IP"), "ip"));
        fieldList.add(new JPATextField(VSMCMain.Txt("Port"), "port"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Gesperrt"), "disabled"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Komprimierung"), "compression"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Verschlüsselung"), "encryption"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Nur neue Dateien"), "onlyNewer"));
        fieldList.add(new JPADBLinkField( em, VSMCMain.Txt("Volumes"), "volumeList", ClientVolume.class));
        fieldList.add(new JPADBLinkField( em, VSMCMain.Txt("Excludes"), "exclList", Excludes.class));

        setTableColumnVisible(fieldList, "compression", false);
        setTableColumnVisible(fieldList, "encryption", false);
        setTableColumnVisible(fieldList, "onlyNewer", false);

        
        ClientInfoTable clit = new ClientInfoTable( main, sched, list, fieldList, listener);

        setFieldValidator(fieldList, "compression", new AgentOptValidator(clit, AgentApi.OP_AG_COMP, "true"));
        setFieldValidator(fieldList, "encryption", new AgentOptValidator(clit, AgentApi.OP_AG_ENC, "true"));

        return clit;
    }

    @Override
    protected GenericEntityManager get_em()
    {
        return VSMCMain.get_util_em(sched.getPool());
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

    String lastIP = null;
    int lastPort = -1;
    Properties props;
    Properties getAgentProperties()
    {
        if (activeElem == null)
            return null;

        if (activeElem.getIp() == null)
            return null;

        if (activeElem.getIp().isEmpty())
            return null;

        if (lastIP == null || lastPort != getActiveElem().getPort() || !lastIP.equals(getActiveElem().getIp()))
        {
            props = main.getGuiServerApi().getAgentProperties( activeElem.getIp(), activeElem.getPort(), /*withMsg*/ false );
            if (props == null)
                VSMCMain.notify(this, VSMCMain.Txt("Dieser Agent ist nicht online"), "Optionen können nicht abgefragt werden");
            lastIP = activeElem.getIp();
            lastPort = activeElem.getPort();
        }
        return props;
    }

    @Override
    public AbstractOrderedLayout createEditComponentPanel( boolean readOnly )
    {
        props = null;
        lastIP = null;
        return super.createEditComponentPanel(readOnly);
    }

}
