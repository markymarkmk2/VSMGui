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
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADBFSField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAPoolComboField;
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
public class HotFolderTable extends BaseDataEditTable<HotFolder>
{
    private HotFolderTable( VSMCMain main, List<HotFolder> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, HotFolder.class, _fieldList, listener);
    }

    public static HotFolderTable createTable( VSMCMain main, List<HotFolder> list, ItemClickListener listener)
    {
        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();
        fieldList.add(new JPATextField(VSMCMain.Txt("Name"), "name"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Gesperrt"), "disabled"));

        fieldList.add(new JPATextField(VSMCMain.Txt("IP"), "ip"));
        fieldList.add(new JPATextField(VSMCMain.Txt("Port"), "port"));
        fieldList.add(new JPATextField(VSMCMain.Txt("Ruhezeit (s)"), "settleTime"));
        fieldList.add(new JPATextField(VSMCMain.Txt("Filter"), "filter"));

        List<ComboEntry> entries = new ArrayList<ComboEntry>();
        entries.add( new ComboEntry(HotFolder.HF_ALL, VSMCMain.Txt("Alles")));
        entries.add( new ComboEntry(HotFolder.HF_DIRS, VSMCMain.Txt("Nur_Verzeichnisse")));
        entries.add( new ComboEntry(HotFolder.HF_FILES, VSMCMain.Txt("Nur_dateien")));
        fieldList.add(new JPAComboField(VSMCMain.Txt("Typ"), "acceptString", entries));
       
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Komprimierung"), "hfcompression"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Verschlüsselung"), "hfencryption"));

        JPAPoolComboField poolCombo = new JPAPoolComboField( main, "poolIdx");
        fieldList.add(poolCombo);
        fieldList.add(new JPARemoteFSField(VSMCMain.Txt("Überwachter Ordner"), "mountPath", "ip", "port" ));

        fieldList.add(new JPADBFSField(VSMCMain.Txt("VSM-Pfad"), "basePath", main, poolCombo ));

        //fieldList.add(new JPACheckBox(VSMCMain.Txt("Unterordner mit Datum erzeugen"), "createDateSubdir"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("MM-Archiv"), "mmArchive"));
//        entries = HotFolderPreviewPanel.getMMMediaTypes("192.168.2.145", 11112);//new ArrayList<ComboEntry>();
        entries = new ArrayList<ComboEntry>();
        //entries.add( new ComboEntry("A", "Archiv"));
        fieldList.add(new JPAComboField(VSMCMain.Txt("Medientyp"), "mmMediaType", entries ));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Mit Verify"), "mmVerify"));

        fieldList.add(new JPATextField(VSMCMain.Txt("MediaManager-IP (opt.)"), "mmIP"));
        fieldList.add(new JPATextField(VSMCMain.Txt("MediaManager-Pfad (UNC) (opt.)"), "mmMountPath"));

        setTableColumnVisible(fieldList, "typ", false);
        setTableColumnVisible(fieldList, "filter", false);
        setTableColumnVisible(fieldList, "port", false);
        setTableColumnVisible(fieldList, "hfcompression", false);
        setTableColumnVisible(fieldList, "hfencryption", false);
        setTableColumnVisible(fieldList, "settleTime", false);
        setTableColumnVisible(fieldList, "basePath", false);
        setTableColumnVisible(fieldList, "mmVerify", false);
        setTableColumnVisible(fieldList, "mmIP", false);
        setTableColumnVisible(fieldList, "mmMountPath", false);
        setTableFieldWidth(fieldList, "mmMountPath", 250);

        setTooltipText(fieldList, "basePath", VSMCMain.Txt("Pfad unter dem die gesicherten Daten im VSM-Dateisystem sichtbar sind"));
        
        //setTableColumnVisible(fieldList, "createDateSubdir", false);



        return new HotFolderTable( main, list, fieldList, listener);
    }

    @Override
       public AbstractOrderedLayout createEditComponentPanel(  boolean readOnly )
    {
        PreviewPanel panel = new HotFolderPreviewPanel(this, readOnly);
        

        // THIS IS NEEDED TO RESOLVE POOL ENTRY INSIDE FSFIELD EDITOR (HE NEEDS TO OPEN POOLWRAPPER)
        JPADBFSField dbfsField = (JPADBFSField) getField("basePath");

        dbfsField.setActualEditPanel( panel );

        panel.recreateContent(activeElem);

        return panel;
    }



//    @Override
//    public <HotFolder> BaseDataEditTable createChildTable( VSMCMain main, HotFolder parent, List<HotFolder> list, Class child, ItemClickListener listener )
//    {
//        return null;
//    }

    @Override
    protected GenericEntityManager get_em()
    {
        return VSMCMain.get_base_util_em();
    }


    @Override
    protected HotFolder createNewObject()
    {
        HotFolder p =  new HotFolder();
        
        p.setName(VSMCMain.Txt("Neuer HotFolder"));
        p.setFilter("");
        p.setIp("127.0.0.1");
        p.setPort(8082);
        p.setSettleTime(10);
        p.setCreateDateSubdir(true);
        p.setMmMountPath("");
        p.setBasePath("");
        p.setDisabled(true);

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



    void importMMArchiv( HotFolder node, long fromIdx, long tillIdx, boolean withOldJobs) throws Exception
    {
        main.getGuiServerApi().importMMArchiv(node, fromIdx, tillIdx, withOldJobs, main.getUser());
    }

}
