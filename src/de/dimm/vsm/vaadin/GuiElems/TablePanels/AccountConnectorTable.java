/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;


import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.records.AccountConnector;
import de.dimm.vsm.vaadin.GuiElems.Fields.ColumnGeneratorField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.List;


class JPAAccountConnectorField extends JPATextField implements ColumnGeneratorField
{
    
    AccountConnectorColumnGenerator colgen;

    public JPAAccountConnectorField()
    {
        super("AccountConnector", "accountConnector");
        
    }

    @Override
    public Component createGui(Object _node)
    {
        node = _node;

        AccountConnector job = (AccountConnector)node;

        TextField tf = new TextField(VSMCMain.Txt("AccountConnector"));

        tf.setValue( getNiceText(job) );
        tf.setData(this);

        return tf;
    }

    // THIS IS CALLED WHEN OBJECT WAS CHANGED TO REFLECT CHANGES GENERATED COLUMN
    @Override
    public void update( BeanItem oldItem )
    {
        AccountConnector job = (AccountConnector)oldItem.getBean();

        if ( colgen != null )
        {
            colgen.label.setValue(getNiceText(job));
        }
    }

    public static String getNiceText(AccountConnector job)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(job.toString());

        if ((job.getFlags() & AccountConnector.FL_DISABLED) == AccountConnector.FL_DISABLED)
        {
            sb.append(" (" + VSMCMain.Txt("Gesperrt"));
            sb.append(") ");
        }

        
        return sb.toString();
    }

    @Override
    public ColumnGenerator getColumnGenerator()
    {
        colgen =  new AccountConnectorColumnGenerator( this );
        return colgen;
    }
}

class AccountConnectorColumnGenerator implements Table.ColumnGenerator
{
    JPAAccountConnectorField field; /* Format string for the Double values. */
    Label label;

    public AccountConnectorColumnGenerator(JPAAccountConnectorField fld)
    {
        this.field = fld;
    }

    /**
     * Generates the cell containing the value.
     * The column is irrelevant in this use case.
     */
    @Override
    public Component generateCell(Table source, Object itemId,Object columnId)
    {        
        BeanItem bi = (BeanItem)source.getItem(itemId);
        Object _a = bi.getBean();
        if (_a instanceof AccountConnector)
        {
            String txt = JPAAccountConnectorField.getNiceText((AccountConnector)_a);
            label = new Label(txt);
            return label;
        }
        return null;
    }
}



/**
 *
 * @author Administrator
 */
public class AccountConnectorTable extends BaseDataEditTable<AccountConnector>
{
    


    private AccountConnectorTable( VSMCMain main, List<AccountConnector> _list, ArrayList<JPAField> _fieldList, ItemClickListener listener)
    {
        super(main, _list, AccountConnector.class, _fieldList, listener);
        
    }

    public static AccountConnectorTable createTable( VSMCMain main,  List<AccountConnector> list, ItemClickListener listener)
    {
        ArrayList<JPAField> fieldList = new ArrayList<JPAField>();
        fieldList.add(new JPAAccountConnectorField(  ));
        
        AccountConnectorTable jt = new AccountConnectorTable( main,  list, fieldList, listener);

        return jt;
    }

    JPAJobField getJobField()
    {
        return (JPAJobField)fieldList.get(0);
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

    @Override
    protected GenericEntityManager get_em()
    {
        return VSMCMain.get_base_util_em();
    }

    @Override
    protected AccountConnector createNewObject()
    {
        AccountConnector p =  new AccountConnector();
        p.setUsername("user");
        p.setType(AccountConnector.TY_LDAP);
        p.setIp("127.0.0.1");
        p.setPort(AccountConnectorPreviewPanel.get_dflt_port(AccountConnector.TY_LDAP, false));
       

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



    AccountConnectorPreviewPanel editPanel;

    @Override
    protected void saveActiveObject()
    {
        // EXCERPT CHANGES FROM NEW GUIFIELDS
        editPanel.updateObject( activeElem );

        super.saveActiveObject();       
    }

    @Override
    public AbstractOrderedLayout createEditComponentPanel(  boolean readOnly )
    {
        editPanel = new AccountConnectorPreviewPanel(this, readOnly);
        editPanel.recreateContent(activeElem);

        return editPanel;
    }


}
