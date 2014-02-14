/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.TextBase;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import static de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable.setTableFieldWidth;
import de.dimm.vsm.vaadin.GuiElems.Table.PreviewPanel;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class TextBaseEntryTable extends BaseDataEditTable<TextBase>
{
   
    private TextBaseEntryTable( VSMCMain main, List<TextBase> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, TextBase.class, _fieldList, listener);               
    }

    public static TextBaseEntryTable createTable( VSMCMain main,  List<TextBase> list, ItemClickListener listener)
    {
        ArrayList<JPAField> fieldList = new ArrayList<>();
        
        fieldList.add(new JPATextField(VSMCMain.Txt("ID"), "messageId"));        
        fieldList.add(new JPATextField("Text", "messageText"));

        List<ComboEntry> langs = new ArrayList<>();
        langs.add(new ComboEntry("DE", "deutsch"));
        langs.add(new ComboEntry("EN", "english"));
        fieldList.add(new JPAComboField("Sprache", "langId", langs));
        setTableFieldWidth(fieldList, "messageId", 280);
        setTableFieldWidth(fieldList, "messageText", 280);

        return new TextBaseEntryTable( main,  list, fieldList, listener);
    }

    @Override
    public AbstractOrderedLayout createEditComponentPanel(  boolean readOnly )
    {
        final PreviewPanel panel = new TextBaseEntryPreviewPanel(this, readOnly);
        panel.recreateContent(activeElem);
        
       

        return panel;
    }

    @Override
    protected GenericEntityManager get_em()
    {
        return VSMCMain.get_txt_em();
    }

    @Override
    protected TextBase createNewObject()
    {
        if (activeElem == null)
            return null;
        
        TextBase p =  new TextBase();
        
        p.setMessageId(activeElem.getMessageId());
        p.setMessageText(activeElem.getMessageText());
              
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
            VSMCMain.notify(this, VSMCMain.Txt("Parameterfehler"), e.getMessage());
            gem.rollback_transaction();
        }
        return null;

    }

    @Override
    public boolean isValid()
    {
        return isValid(activeElem, this);
    }
    public static boolean isValid(TextBase me, Component c)
    {
        if (me.getMessageId() == null || me.getMessageId().isEmpty())
        {
            VSMCMain.notify(c, VSMCMain.Txt("Parameterfehler"), VSMCMain.Txt("Id fehlt"));
            return false;
        }
        if (me.getMessageText()== null || me.getMessageText().isEmpty())
        {
            VSMCMain.notify(c, VSMCMain.Txt("Parameterfehler"), VSMCMain.Txt("Text fehlt"));
            return false;
        }
        if (me.getLangId()== null || me.getLangId().isEmpty())
        {
            VSMCMain.notify(c, VSMCMain.Txt("Parameterfehler"), VSMCMain.Txt("Sprache fehlt"));
            return false;
        }
        return true;
    }
}
