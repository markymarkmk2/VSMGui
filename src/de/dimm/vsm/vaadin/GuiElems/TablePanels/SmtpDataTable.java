/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.data.Validator;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractOrderedLayout;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.SmtpLoginData;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAPasswordField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Administrator
 */
public class SmtpDataTable extends BaseDataEditTable<SmtpLoginData>
{
    private SmtpDataTable( VSMCMain main, List<SmtpLoginData> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, SmtpLoginData.class, _fieldList, listener);
    }

    public static SmtpDataTable createTable( VSMCMain main,  List<SmtpLoginData> list, ItemClickListener listener)
    {
        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();
        fieldList.add(new JPATextField(VSMCMain.Txt("Name"), "name"));
        fieldList.add(new JPATextField(VSMCMain.Txt("SMTP-Server"), "serverip"));
        fieldList.add(new JPATextField(VSMCMain.Txt("Port"), "serverport"));
        fieldList.add(new JPATextField(VSMCMain.Txt("Absender"), "smtpfrom"));
        fieldList.add(new JPATextField(VSMCMain.Txt("SMTP-Login"), "username"));
        fieldList.add(new JPAPasswordField(VSMCMain.Txt("Passwort"), "userpwd"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("TLS verwenden"), "tls"));
        fieldList.add(new JPACheckBox(VSMCMain.Txt("SSL verwenden"), "ssl"));

        Validator nameValidator = new StringLengthValidator(VSMCMain.Txt("Bitte geben Sie einen Namen ein" ),1, 255, false);
        Validator serveripValidator = new StringLengthValidator(VSMCMain.Txt("Bitte geben Sie einen Server ein" ),1, 255, false);
        Validator eMailValidator = new EmailValidator(VSMCMain.Txt("Bitte geben Sie eine eMailadresse ein" ));
        Validator portValidator = new IntegerValidator(VSMCMain.Txt("Bitte geben Sie eine Portnummer" ));

        setFieldValidator(fieldList, "name", nameValidator);
        setFieldValidator(fieldList, "serverip", serveripValidator);
        setFieldValidator(fieldList, "smtpfrom", eMailValidator);
        setFieldValidator(fieldList, "serverport", portValidator);

        setTableColumnWidth(fieldList, "serverip", 120);
        setTableFieldWidth(fieldList, "serverip", 250);
        
        setTableColumnVisible(fieldList, "smtpfrom", false);
        setTableFieldWidth(fieldList, "smtpfrom", 250);

        setTableColumnVisible(fieldList, "username", false);
        setTableColumnVisible(fieldList, "userpwd", false);
        

        return new SmtpDataTable( main,  list, fieldList, listener);
    }

    @Override
    protected GenericEntityManager get_em()
    {
        return VSMCMain.get_base_util_em();
    }


    @Override
    protected SmtpLoginData createNewObject()
    {
        SmtpLoginData p =  new SmtpLoginData();
        p.setServerport(25);
        p.setTls(true);
        p.setName("SmtpServer");
        p.setServerip("localhost");
                
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
            gem.rollback_transaction();
            VSMCMain.notify(this, "Abbruch in createNewObject", e.getMessage());
        }
        return null;
    }


    SmtpDataPreviewPanel editPanel;

    @Override
    public AbstractOrderedLayout createEditComponentPanel(  boolean readOnly )
    {
        editPanel = new SmtpDataPreviewPanel(this, readOnly);
        editPanel.recreateContent(activeElem);

        return editPanel;
    }

}
