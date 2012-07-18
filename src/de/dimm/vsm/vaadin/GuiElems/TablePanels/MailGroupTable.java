/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.data.Validator;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.MailGroup;
import de.dimm.vsm.records.SmtpLoginData;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAAbstractComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextAreaField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.List;


class EMailListValidator extends StringLengthValidator
{

    public EMailListValidator(String txt)
    {
        super(txt, 1, 255, false);
    }

    @Override
    public boolean isValid( Object value )
    {
        boolean ret = super.isValid(value);
        if (!ret)
            return false;
        List<String> list = MailGroup.getEmails(value.toString());
        if (list.isEmpty())
            return false;

        EmailValidator ev = new EmailValidator(null);
        for (int i = 0; i < list.size(); i++)
        {
            String string = list.get(i);
            if (!ev.isValid(string))
                return false;
        }
        return true;
    }
}


class JPASmtpDataComboField extends JPAAbstractComboField
{

    VSMCMain main;

    public JPASmtpDataComboField( VSMCMain main)
    {
        super( VSMCMain.Txt("SMTP-Server"), "smtpdata" );
        this.main = main;
    }

    @Override
    public List<ComboEntry> getEntries()
    {
        List<ComboEntry> entries = new ArrayList<ComboEntry>();

        try
        {
            List<SmtpLoginData> list = VSMCMain.get_base_util_em().createQuery("select T1 from SmtpLoginData T1", SmtpLoginData.class);

            for (int i = 0; i < list.size(); i++)
            {
                SmtpLoginData object = list.get(i);

                String name = object.getName() + " " + object.getServerip() + ":" + object.getServerport();

                ComboEntry ce = new ComboEntry(object, name);
                entries.add(ce);
            }
        }
        catch (Exception sQLException)
        {
        }
        return entries;
    }

}


/**
 *
 * @author Administrator
 */
public class MailGroupTable extends BaseDataEditTable<MailGroup>
{
    private MailGroupTable( VSMCMain main, List<MailGroup> list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, list, MailGroup.class, _fieldList, listener);
       
    }

    public static MailGroupTable createTable( VSMCMain main,  List<MailGroup> list, ItemClickListener listener)
    {
        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();
        fieldList.add(new JPATextField(VSMCMain.Txt("Name"), "name"));
        fieldList.add(new JPATextAreaField(VSMCMain.Txt("eMailadressen"), "emailText"));
        fieldList.add(new JPASmtpDataComboField( main ) );

        Validator nameValidator = new StringLengthValidator(VSMCMain.Txt("Bitte geben Sie einen Namen ein" ),1, 255, false);
        Validator emailTextValidator = new EMailListValidator(VSMCMain.Txt("Bitte geben Sie eine oder mehrere durch Leerzeichen getrennte eMail-Adressen ein" ));


        setFieldValidator(fieldList, "name", nameValidator);
        setFieldValidator(fieldList, "emailText", emailTextValidator);

        return new MailGroupTable( main,  list, fieldList, listener);
    }

    @Override
    protected GenericEntityManager get_em()
    {
        return VSMCMain.get_base_util_em();
    }


    @Override
    protected MailGroup createNewObject()
    {
        MailGroup p =  new MailGroup();
        p.setName(VSMCMain.Txt("Neue Gruppe"));
        p.setEmailText("root@localhost");

        List<SmtpLoginData> list = null;
        try
        {
            list = VSMCMain.get_base_util_em().createQuery("select p from SmtpLoginData p", SmtpLoginData.class);
            if (list.isEmpty())
            {
                VSMCMain.notify(this, "Bitte legen Sie zuerst SMTP-Daten an", "");
                return null;
            }
            p.setSmtpdata(list.get(0));
        }
        catch (Exception sQLException)
        {            
        }
                
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




}
