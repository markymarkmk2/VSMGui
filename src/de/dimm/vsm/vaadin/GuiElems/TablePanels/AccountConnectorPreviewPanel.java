/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import de.dimm.vsm.auth.GenericRealmAuth;
import de.dimm.vsm.records.AccountConnector;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAPasswordField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.PreviewPanel;
import de.dimm.vsm.vaadin.GuiElems.VaadinHelpers;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Administrator
 */
public class AccountConnectorPreviewPanel extends PreviewPanel<AccountConnector>
{
    CheckBox checkBoxDisabled;
    CheckBox checkBoxSSL;
    CheckBox checkBoxEmptyPwd;
    
    ComboBox comboAuthType;
    JPATextField txtIp;
    JPATextField txtPort;
    JPATextField txtUser;
    JPATextField txtPwd;

    JPATextField txtSearchBase;
    //JPATextField txtLdapDomain;
    JPATextField txtLdapFilter;
    JPATextField txtLdapUserAttribute;
    JPATextField txtLdapgroupIdentifier;
    JPATextField txtLdapDomain;

    JPATextField txtNtDomainName;

    final List<ComboEntry> typeList = new ArrayList<ComboEntry>();

    public AccountConnectorPreviewPanel( AccountConnectorTable j, boolean readOnly )
    {
        super(j, readOnly);
        typeList.add( new ComboEntry("dbs", Txt("Database")) );
        typeList.add( new ComboEntry("ldap", Txt("LDAP")) );
        typeList.add( new ComboEntry("ad", Txt("ActiveDirectory")) );
        typeList.add( new ComboEntry("smtp", Txt("SMTP")) );
        typeList.add( new ComboEntry("imap", Txt("IMAP")) );
        typeList.add( new ComboEntry( "pop", Txt("POP3")) );
    }


    @Override
    public void recreateContent( final AccountConnector node )
    {
        removeAllComponents();


        checkBoxDisabled = new CheckBox(Txt("Gesperrt"));
        checkBoxSSL = new CheckBox(Txt("SSL"));
        checkBoxSSL.addListener(new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                setDefaultPort();
            }

        });
        checkBoxEmptyPwd = new CheckBox(Txt("Leere Passworte erlaubt"));

        comboAuthType = new ComboBox(Txt("Authentifizierung"), typeList);
        comboAuthType.setNullSelectionAllowed(false);
        comboAuthType.addListener( new ValueChangeListener() {

            @Override
            public void valueChange( ValueChangeEvent event )
            {
                setDefaultPort();
            }
        });
        
        
        // ADD TO LAYOUT
        addComponent(checkBoxDisabled);
        addComponent(comboAuthType);

        txtIp = new JPATextField(Txt("Server"), "ip");
        txtPort = new JPATextField(Txt("Port"), "port");
        txtUser = new JPATextField(Txt("User"), "username");
        txtUser.setExpandRatio(1.0f);
        txtPwd = new JPAPasswordField(Txt("Password"), "pwd");

        
        // ADD TO LAYOUT
        addComponent(txtIp.createGui(node));
        addComponent(checkBoxSSL);
        addComponent(checkBoxEmptyPwd);
        addComponent(txtPort.createGui(node));
        addComponent(txtUser.createGui(node));
        addComponent(txtPwd.createGui(node));

        // HAS TO BE AFTER createGui
        txtIp.addValidator( this, new StringLengthValidator(Txt("Bitte geben Sie eine IP oder einen DNS-Namen ein"), 1, 512, false));
        txtPort.addValidator( this, new IntegerValidator(Txt("Bitte geben Sie eine gültige Portnummer ein")));
        //txtPwd.addValidator( new StringLengthValidator(X("Bitte geben Sie das Passwort ein"), 1, 512, false));


        // SET LISTENERS
        ValueChangeListener vc = new ValueChangeListener() {

            @Override
            public void valueChange( ValueChangeEvent event )
            {
                check_visibility();
            }
        };

        comboAuthType.setImmediate(true);
        comboAuthType.addListener( vc);
 
        txtSearchBase = new JPATextField(Txt("Searchbase"), "searchbase");
        txtSearchBase.setExpandRatio(1.0f);
        //txtLdapDomain = new JPATextField(X("LDAP-Domain"), "ldapdomain");
        txtLdapUserAttribute = new JPATextField(Txt("LDAP-Userattribute"), "searchattribute");
        txtLdapFilter = new JPATextField(Txt("LDAP-Filter"), "ldapfilter");
        txtLdapgroupIdentifier = new JPATextField(Txt("Gruppenkennung"), "groupIdentifier");
        txtNtDomainName = new JPATextField(Txt("NT-Domain"), "ntDomainName");
        txtLdapDomain = new JPATextField(Txt("Login-Domain"), "ldapdomain");

        // ADD TO LAYOUT
        addComponent(txtSearchBase.createGui(node));
        //addComponent(txtLdapDomain.createGui(node));
        addComponent(txtLdapUserAttribute.createGui(node));
        addComponent(txtLdapFilter.createGui(node));
        addComponent(txtLdapgroupIdentifier.createGui(node));
        addComponent(txtNtDomainName.createGui(node));
        addComponent(txtLdapDomain.createGui(node));

        Button testAD = new Button( Txt("Test Connect"));
        testAD.addListener( new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                 checkAD(node);
            }
        });
        addComponent(testAD);

        check_visibility();
        setData(node);
    }
    private void checkAD(AccountConnector node)
    {
        GenericRealmAuth auth = GenericRealmAuth.factory_create_realm( node );

        if (auth.connect())
        {            
            VSMCMain.notify(this, Txt("Verbindung okay"), "");

//            try
//            {
//                ArrayList<String> l = auth.list_groups();
//                VSMCMain.Me(this).SelectObject(String.class, "Gruppen", "Ok", l, null);
//
//            }
//            catch (NamingException namingException)
//            {
//
//            }
            auth.disconnect();
        }
        else
        {
            VSMCMain.notify(this, Txt("Verbindung ist fehlgeschlagen"), auth.get_error_txt());
        }
    }

    @Override
    public void attach()
    {
        AccountConnector node = (AccountConnector)getData();
        super.attach();
        setValues(node );
        check_visibility();
    }


    void check_visibility()
    {
        //AccountConnectorTable tb = (AccountConnectorTable)this.table;

        int idx = VaadinHelpers.getSelectedIndex(comboAuthType);
        boolean isLdap = (idx == 1);
        boolean isAd = (idx == 2);
        
        //txtLdapDomain.getGui().setVisible(isLdap);
        getGui(txtLdapUserAttribute).setVisible(isLdap);
        getGui(txtLdapFilter).setVisible(isLdap);
        getGui(txtLdapgroupIdentifier).setVisible(isLdap);

        getGui(txtNtDomainName).setVisible(isAd);
        getGui(txtLdapDomain).setVisible(isAd || isLdap);



    }

    private static String Txt(String key )
    {
        return VSMCMain.Txt(key);
    }

    private void setValues(AccountConnector node)
    {
        String typ = node.getType();

        for (int i = 0; i < typeList.size(); i++)
        {
            ComboEntry ce = typeList.get(i);
            if (ce.getDbEntry().equals(typ))
            {
                VaadinHelpers.setSelectedIndex(comboAuthType, i);
                break;
            }
        }
        checkBoxDisabled.setValue((node.getFlags() & AccountConnector.FL_DISABLED) == AccountConnector.FL_DISABLED );
        checkBoxSSL.setValue((node.getFlags() & AccountConnector.FL_SSL) == AccountConnector.FL_SSL );
        checkBoxSSL.setValue((node.getFlags() & AccountConnector.FL_ALLOW_EMPTY_PWD) == AccountConnector.FL_ALLOW_EMPTY_PWD );

    }

 
    void updateObject( AccountConnector activeElem )
    {
        int idx = VaadinHelpers.getSelectedIndex(comboAuthType);
        activeElem.setType( typeList.get( idx ).getDbEntry().toString());

        long flags = activeElem.getFlags();

        if (((Boolean)checkBoxDisabled.getValue()).booleanValue())
            flags |= AccountConnector.FL_DISABLED;
        else
            flags &= ~AccountConnector.FL_DISABLED;

       if (((Boolean)checkBoxSSL.getValue()).booleanValue())
            flags |= AccountConnector.FL_SSL;
       else
            flags &= ~AccountConnector.FL_SSL;

       if (((Boolean)checkBoxEmptyPwd.getValue()).booleanValue())
            flags |= AccountConnector.FL_ALLOW_EMPTY_PWD;
       else
            flags &= ~AccountConnector.FL_ALLOW_EMPTY_PWD;

        activeElem.setFlags(flags);

    }

    private void setDefaultPort()
    {
        int idx = VaadinHelpers.getSelectedIndex(comboAuthType);
        if (idx >= 0)
        {
            String type = typeList.get(idx).getDbEntry().toString();
            int port = get_dflt_port(type, ((Boolean)checkBoxSSL.getValue()).booleanValue());
            ((TextField)getGui(txtPort)).setValue(new Integer(port));
        }
    }


    public static int get_dflt_port( String type, boolean secure )
    {
        if (type.compareTo("smtp") == 0)
            return  (secure ? 465 : 25);
        if (type.compareTo("pop") == 0)
            return (secure ? 995 : 110);
        if (type.compareTo("imap") == 0)
            return (secure ? 993 : 143);
        if (type.compareTo("ldap") == 0 || type.compareTo("ad") == 0)
            return (secure ? 636 : 389);

        return 0;
    }

}
