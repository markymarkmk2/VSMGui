/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Window;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.records.ClientInfo;
import de.dimm.vsm.records.ClientVolume;
import de.dimm.vsm.records.FileSystemElemNode;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPARemoteFSField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class ClientVolumeTable extends BaseDataEditTable<ClientVolume>
{
    ClientInfo clientInfo;

    private ClientVolumeTable( VSMCMain main, ClientInfo clientInfo, List<ClientVolume> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, ClientVolume.class, _fieldList, listener);
        this.clientInfo = clientInfo;
    }

    public static ClientVolumeTable createTable( VSMCMain main, ClientInfo clientInfo, List<ClientVolume> list, ItemClickListener listener)
    {
        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();
        
        JPACheckBox cbStayLocal = new JPACheckBox(VSMCMain.Txt("StayLocal"), "staylocal");
        fieldList.add(new JPARemoteFSField(VSMCMain.Txt("Pfad"), "volumePath", clientInfo.getIp(), clientInfo.getPort(), cbStayLocal));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Gesperrt"), "disabled"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("CDP"), "cdp"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Snapshot"), "snapshot"));
        fieldList.add(cbStayLocal);

        return new ClientVolumeTable( main, clientInfo, list, fieldList, listener);
    }


    @Override
    protected GenericEntityManager get_em()
    {
        return VSMCMain.get_util_em(clientInfo.getSched().getPool());
    }

    @Override
    protected ClientVolume createNewObject()
    {
        ClientVolume p =  new ClientVolume();
        p.setClinfo(clientInfo);
        p.setVolumePath( new RemoteFSElem("", FileSystemElemNode.FT_DIR, 0, 0, 0, 0, 0));

        
        GenericEntityManager gem = get_em();

        try
        {
            gem.check_open_transaction();
            gem.em_persist(p);

            clientInfo.getVolumeList().addIfRealized(p);

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
    protected void setDBWinLayout( Window win )
    {
        super.setDBWinLayout(win);
        win.setWidth("700px");
    }

    @Override
    protected void deleteObject( ClientVolume node )
    {
        // GET RID OF BACKUPRESULTS FIRST

        GenericEntityManager gem = get_em();
        gem.nativeCall( "delete from BackupVolumeResult where volume_idx=" + node.getIdx());

        super.deleteObject(node);
    }


}
