/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractOrderedLayout;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.FileSystemElemNode;
import de.dimm.vsm.records.HotFolder;
import de.dimm.vsm.records.MountEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADBFSField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAPoolComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAPoolQrySelectField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPARemoteFSField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.GuiElems.Table.PreviewPanel;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class MountEntryTable extends BaseDataEditTable<MountEntry>
{
    private MountEntryTable( VSMCMain main, List<MountEntry> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, MountEntry.class, _fieldList, listener);
    }

    public static MountEntryTable createTable( VSMCMain main, List<MountEntry> list, ItemClickListener listener)
    {
        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();
        fieldList.add(new JPATextField(VSMCMain.Txt("Name"), "name"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Gesperrt"), "disabled"));
        JPAPoolComboField poolCombo = new JPAPoolComboField( main, "poolIdx");
        fieldList.add(poolCombo);

        fieldList.add(new JPATextField(VSMCMain.Txt("Ziel-IP"), "ip"));
        fieldList.add(new JPATextField(VSMCMain.Txt("Ziel-Port"), "port"));

        fieldList.add(new JPACheckBox(VSMCMain.Txt("Mounted"), "mounted"));

        JPAPoolQrySelectField poolQryField = new JPAPoolQrySelectField( main, "typ", "username", "ts", "snapshotIdx");
        fieldList.add(poolQryField);
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Gelöschte Dateien anzeigen"), "showDeleted"));

        JPARemoteFSField remMountPath = new JPARemoteFSField(VSMCMain.Txt("Ziel-Pfad"), "mountPath", "ip", "port" );
        remMountPath.setMountPointMode(true);
        fieldList.add(remMountPath);

        setTableColumnVisible(fieldList, "port", false);
        setTableColumnVisible(fieldList, "showDeleted", false);
        

        return new MountEntryTable( main, list, fieldList, listener);
    }

    @Override
       public AbstractOrderedLayout createEditComponentPanel(  boolean readOnly )
    {
        PreviewPanel panel = new MountEntryPreviewPanel(this, readOnly);
       

        panel.recreateContent(activeElem);

        return panel;
    }



    @Override
    protected GenericEntityManager get_em()
    {
        return VSMCMain.get_base_util_em();
    }


    @Override
    protected MountEntry createNewObject()
    {
        MountEntry p =  new MountEntry();
        
        p.setName(VSMCMain.Txt("MountEintrag"));
        p.setIp("127.0.0.1");
        p.setPort(8082);
        p.setDisabled(true);
        p.setTyp(MountEntry.TYP_RDONLY);

        
        // CREATE ROOT DIR
        FileSystemElemNode root_node = FileSystemElemNode.createDirNode();
        root_node.getAttributes().setName("/");
        

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
            e.printStackTrace();
            gem.rollback_transaction();
        }
        return null;

    }



}
