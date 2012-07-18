/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.HotFolder;
import de.dimm.vsm.records.HotFolderError;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPARemoteFSField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class HotFolderErrorTable extends BaseDataEditTable<HotFolderError>
{
    HotFolder hotFolder;

    private HotFolderErrorTable( VSMCMain main, HotFolder clientInfo, List<HotFolderError> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, HotFolderError.class, _fieldList, listener, /*edit*/false, /*del*/true);
        this.hotFolder = clientInfo;
    }

    public static HotFolderErrorTable createTable( VSMCMain main, HotFolder clientInfo, List<HotFolderError> list, ItemClickListener listener)
    {
        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();
        fieldList.add(new JPARemoteFSField(VSMCMain.Txt("Pfad"), "elem", "ip", "port" ));
        fieldList.add(new JPATextField(VSMCMain.Txt("Fehler"), "errtext"));
        

        return new HotFolderErrorTable( main, clientInfo, list, fieldList, listener);
    }


    @Override
    protected GenericEntityManager get_em()
    {
        return VSMCMain.get_base_util_em();
    }
    
    @Override
    public Component createNewButton()
    {
         return null;
    }

    @Override
    protected HotFolderError createNewObject()
    {
        HotFolderError p =  new HotFolderError();
        
        GenericEntityManager gem = get_em();

        try
        {
            gem.check_open_transaction();
            gem.em_persist(p);

            hotFolder.getErrlist().addIfRealized(p);

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
    protected void deleteObject( HotFolderError node )
    {
        super.deleteObject(node);
    }
    public void deleteHotFolderError( HotFolderError node )
    {
        deleteObject(node);
    }


}
