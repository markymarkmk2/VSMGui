/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.DateField;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.ArchiveJob;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAReadOnlyDateField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPASizeStr;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.SidebarPanels.ArchiveJobWin;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class ArchivJobTable  extends BaseDataEditTable<ArchiveJob>
{
    ArchiveJobWin jobWin;

    private ArchivJobTable( VSMCMain main, ArchiveJobWin jobWin, List<ArchiveJob> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, ArchiveJob.class, _fieldList, listener, /*showedit*/ true, /*showDelete*/ main.getGuiUser().isSuperUser());
        this.jobWin = jobWin;
    }

    public static ArchivJobTable createTable( VSMCMain main, ArchiveJobWin jobWin,List<ArchiveJob> list, ItemClickListener listener)
    {
        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();
        fieldList.add(new JPATextField(VSMCMain.Txt("Job"), "name"));
        fieldList.add(new JPAReadOnlyDateField(VSMCMain.Txt("Erzeugt"), "startTime", DateField.RESOLUTION_MIN));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("Ok"), "ok"));

        fieldList.add(new JPASizeStr(VSMCMain.Txt("Größe"), "totalSize"));

        setTableColumnExpandRatio(fieldList, "name", 1.0f);

        return new ArchivJobTable( main, jobWin, list, fieldList, listener);
    }

    @Override
    protected void deleteObject( ArchiveJob node )
    {
        jobWin.handleRemoveJob(node);
    }




    @Override
    protected GenericEntityManager get_em()
    {
        return main.get_base_util_em();
    }


    @Override
    protected ArchiveJob createNewObject()
    {

        return null;

    }

    @Override
    protected String getTablenameText()
    {
        return VSMCMain.Txt(this.getClass().getSimpleName());
    }
}
