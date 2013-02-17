/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Fields;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.fsengine.LazyList;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.Table.DBLinkColumnGenerator;
import de.dimm.vsm.vaadin.VSMCMain;
import java.lang.reflect.Field;
import java.util.List;


/**
 *
 * @author Administrator
 */
public class JPADBLinkField<T> extends JPAField<T>
{
    Class client;
    DBLinkColumnGenerator colg;
    protected GenericEntityManager em;

    public JPADBLinkField(GenericEntityManager em, String caption, String fieldName, Class client)
    {
        super( caption, fieldName );
        this.em = em;
        this.client = client;
    }

    @Override
    public void setReadOnly( Component gui, boolean b )
    {
        if (gui instanceof DBLinkTextField)
        {
            DBLinkTextField tf = (DBLinkTextField)gui;
            tf.setReadOnly(b);
        }
        else
        {
            super.setReadOnly( gui, b );
        }
    }

    @Override
    public Component createGui( T _node)
    {
        node = _node;
        
       
        String s = toString(getList( node));

        DBLinkTextField tf = new DBLinkTextField(caption);
//        TextField tf = new TextField(caption);
        tf.setValue(s);
        if (toolTip != null)
            tf.setDescription(toolTip);
        
        tf.setData(this);
        tf.setWidth("250px");

        return tf;
    }
    public void reload(Component tf)
    {
        List<T> list = getList( node);
        if (list instanceof LazyList)
        {
            LazyList ll = (LazyList)list;
            ll.unRealize();
        }
        ((DBLinkTextField)tf).setValue(toString(getList( node)));
    }
    public List<T> getList( Object parent)
    {
        try
        {            
            Field f = parent.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object o = f.get(parent);
            if (o instanceof LazyList)
            {
                LazyList<T> list = (LazyList<T>) o;
                
                GenericEntityManager local_em = em;
                if (local_em == null && parent instanceof StoragePool)
                {
                    local_em = VSMCMain.get_util_em((StoragePool)parent);
                }
                if (local_em != null)
                {
                    // FORCE DIREKT READ OF DB
                    list.unRealize();

                    return list.getList(local_em);
                }

                if (!list.isRealized())
                    throw new RuntimeException("Lazy List is not realized");
                
                // THIS LIST MUST HAVE ALREADY BEEN FILLED
                return list.getList();
            }
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
        }
        return null;
    }
    public String toString( Object _list )
    {
        StringBuilder sb = new StringBuilder();


        try
        {
            List list = null;
            if (_list instanceof List)
            {
                if (_list instanceof LazyList)
                {
                    LazyList<T> lazyList = (LazyList<T>) _list;
                    if (em != null)
                    {
                        // FORCE DIREKT READ OF DB
                        lazyList.unRealize();

                        list = lazyList.getList(em);
                    }
                    else
                    {
                        if (!lazyList.isRealized())
                            throw new RuntimeException("Lazy List is not realized");

                        // THIS LIST MUST HAVE ALREADY BEEN FILLED
                        list =  lazyList.getList();
                    }
                }
                else
                {
                    list = (List) _list;
                }


                for (int i = 0; i < list.size(); i++)
                {
                    Object object = list.get(i);
                    if (sb.length() > 0)
                        sb.append(", ");
                    sb.append(object.toString());
                }
            }
            else
            {
                sb.append(_list.toString());
            }
        }
        catch (Exception noSuchFieldException)
        {
        }
        return sb.toString();
    }

    public Class getClient()
    {
        return client;
    }

    public void setColumnGenerator( DBLinkColumnGenerator colg )
    {
        this.colg = colg;
    }

    public void addGuiClickListener( AbstractOrderedLayout panel, ClickListener clickListener )
    {
        Component gui = getGuiforField(panel, fieldName);
        if (gui != null)
            ((DBLinkTextField)gui).addGuiClickListener( clickListener );
    }

    @Override
    public int getFieldWidth()
    {
        return 250;
    }
    

}
