/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.FileSystemElemNode;
import de.dimm.vsm.records.MountEntry;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADBFSField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
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
    StoragePool pool;
    
    private MountEntryTable( VSMCMain main, StoragePool pool, List<MountEntry> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, MountEntry.class, _fieldList, listener);
        this.pool = pool;
    }

    public static MountEntryTable createTable( VSMCMain main,  StoragePool pool, List<MountEntry> list, ItemClickListener listener)
    {
        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();
        fieldList.add(new JPATextField(VSMCMain.Txt("Name"), "name"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Gesperrt"), "disabled"));
        

        fieldList.add(new JPATextField(VSMCMain.Txt("Ziel-IP"), "ip"));
        fieldList.add(new JPATextField(VSMCMain.Txt("Ziel-Port"), "port"));

        fieldList.add(new JPACheckBox(VSMCMain.Txt("AutoMount"), "autoMount"));
       // fieldList.add(new JPACheckBox(VSMCMain.Txt("Mounted"), "mounted"));
        
        GenericEntityManager em = VSMCMain.get_util_em(pool);

        JPAPoolQrySelectField poolQryField = new JPAPoolQrySelectField( main, em);
        fieldList.add(poolQryField);
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Gelöschte Dateien anzeigen"), "showDeleted"));

        JPARemoteFSField remMountPath = new JPARemoteFSField(VSMCMain.Txt("Ziel-Pfad"), "mountPath", "ip", "port", null );
        remMountPath.setMountPointMode(true);
        fieldList.add(remMountPath);
        JPADBFSField subPath = new JPADBFSField(VSMCMain.Txt("VSM-Pfad"), "subPath", main, pool);
        subPath.setOnlyDirs(true);
        fieldList.add(subPath);

        setTableColumnVisible(fieldList, "port", false);
        setTableColumnVisible(fieldList, "showDeleted", false);

        setTooltipText(fieldList, "subPath", VSMCMain.Txt("Pfad des VSM-Dateisystems, der im Zielpfad sichtbar wird"));
        

        return new MountEntryTable( main, pool, list, fieldList, listener);
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
        return VSMCMain.get_util_em(pool);
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
        p.setPool( pool );
        p.setSubPath("/127.0.0.1/8082");

        
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

    @Override
    public boolean isValid()
    {
        return isValid(activeElem, this);
    }
    public static boolean isValid(MountEntry me, Component c)
    {
        if (me.getName() == null || me.getName().isEmpty())
        {
            VSMCMain.notify(c, VSMCMain.Txt("Parameterfehler"), VSMCMain.Txt("Name fehlt"));
            return false;
        }
        if (me.getMountPath() == null || me.getMountPath().getPath().isEmpty())
        {
            VSMCMain.notify(c, VSMCMain.Txt("Parameterfehler"), VSMCMain.Txt("Ziel-Pfad fehlt"));
            return false;
        }
        if (me.getSubPath() == null || me.getSubPath().isEmpty())
        {
            VSMCMain.notify(c, VSMCMain.Txt("Parameterfehler"), VSMCMain.Txt("VSM-Pfad fehlt"));
            return false;
        }
        
        if (!me.getSubPath().startsWith(me.getIp() + "/" + me.getPort()))
        {
             VSMCMain.notify(c, VSMCMain.Txt("Warnung"), VSMCMain.Txt("Der VSM-Pfad entspricht nicht dem ausgewählten Agenten, Sie könnten dadurch eventuell Daten anderer Agenten überschreiben!"));
        }

        return true;
    }




}
