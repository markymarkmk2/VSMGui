/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.NativeButton;
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
import javax.naming.NamingException;


/**
 *
 * @author Administrator
 */
public class AccountConnectorPreviewPanel extends PreviewPanel<AccountConnector>
{
    CheckBox checkBoxDisabled;
    CheckBox checkBoxSSL;
    
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

    final List<ComboEntry> typeList = new ArrayList<ComboEntry>();

    public AccountConnectorPreviewPanel( AccountConnectorTable j, boolean readOnly )
    {
        super(j, readOnly);
        typeList.add( new ComboEntry("dbs", X("Database")) );
        typeList.add( new ComboEntry("ldap", X("LDAP")) );
        typeList.add( new ComboEntry("ad", X("ActiveDirectory")) );
        typeList.add( new ComboEntry("imap", X("IMAP")) );
        typeList.add( new ComboEntry( "pop", X("POP3")) );
    }


    @Override
    public void recreateContent( final AccountConnector node )
    {
        removeAllComponents();


        checkBoxDisabled = new CheckBox(X("Gesperrt"));
        checkBoxSSL = new CheckBox(X("SSL"));
        checkBoxSSL.addListener(new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                setDefaultPort();
            }

        });

        comboAuthType = new ComboBox(X("Authentifizierung"), typeList);
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

        txtIp = new JPATextField(X("Server"), "ip");
        txtPort = new JPATextField(X("Port"), "port");
        txtUser = new JPATextField(X("User"), "username");
        txtUser.setExpandRatio(1.0f);
        txtPwd = new JPAPasswordField(X("Password"), "pwd");
        
        // ADD TO LAYOUT
        addComponent(txtIp.createGui(node));
        addComponent(checkBoxSSL);
        addComponent(txtPort.createGui(node));
        addComponent(txtUser.createGui(node));
        addComponent(txtPwd.createGui(node));

        // HAS TO BE AFTER createGui
        txtIp.addValidator( this, new StringLengthValidator(X("Bitte geben Sie eine IP oder einen DNS-Namen ein"), 1, 512, false));
        txtPort.addValidator( this, new IntegerValidator(X("Bitte geben Sie eine gültige Portnummer ein")));
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
 
        txtSearchBase = new JPATextField(X("Searchbase"), "searchbase");
        txtSearchBase.setExpandRatio(1.0f);
        //txtLdapDomain = new JPATextField(X("LDAP-Domain"), "ldapdomain");
        txtLdapUserAttribute = new JPATextField(X("LDAP-Userattribute"), "searchattribute");
        txtLdapFilter = new JPATextField(X("LDAP-Filter"), "ldapfilter");
        txtLdapgroupIdentifier = new JPATextField(X("Gruppenkennung"), "groupIdentifier");
        // ADD TO LAYOUT
        addComponent(txtSearchBase.createGui(node));
        //addComponent(txtLdapDomain.createGui(node));
        addComponent(txtLdapUserAttribute.createGui(node));
        addComponent(txtLdapFilter.createGui(node));
        addComponent(txtLdapgroupIdentifier.createGui(node));

        NativeButton testAD = new NativeButton( X("Test AD"));
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
            VSMCMain.notify(this, X("Verbindung okay"), "");

            try
            {
                ArrayList<String> l = auth.list_groups();
                VSMCMain.Me(this).SelectObject(String.class, "Gruppen", "Ok", l, null);

            }
            catch (NamingException namingException)
            {

            }
            auth.disconnect();
        }
        else
        {
            VSMCMain.notify(this, X("Verbindung ist fehlgeschlagen"), auth.get_error_txt());
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
        AccountConnectorTable tb = (AccountConnectorTable)this.table;

        int idx = VaadinHelpers.getSelectedIndex(comboAuthType);
        boolean isLdap = (idx == 1);
        //txtLdapDomain.getGui().setVisible(isLdap);
        getGui(txtLdapUserAttribute).setVisible(isLdap);
        getGui(txtLdapFilter).setVisible(isLdap);
        getGui(txtLdapgroupIdentifier).setVisible(isLdap);

    }

    private static String X(String key )
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
