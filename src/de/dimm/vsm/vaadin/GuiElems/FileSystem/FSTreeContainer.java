/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.FileSystem;


import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.treetable.Collapsible;
import de.dimm.vsm.net.RemoteFSElem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 *
 * @author Administrator
 */
public class FSTreeContainer implements Collapsible, Container.Sortable
{
    ArrayList<RemoteFSElemTreeElem> root_list;
    private ArrayList<FSTreeColumn> fields;
    ArrayList<RemoteFSElemTreeElem> visibleList;
    RemoteProvider provider;
   
    boolean skipEmptyDirs;
//    ArrayList<RemoteFSElem> rootNodes;

    public FSTreeContainer( RemoteProvider provider, ArrayList<FSTreeColumn> fields/*, ArrayList<RemoteFSElem> rootNodes*/ )
    {
        this.provider = provider;
        
        this.root_list = new ArrayList<>();
        visibleList = new ArrayList<>();

//        this.rootNodes = rootNodes;
        this.fields = fields;
    }

    public void setSkipEmptyDirs( boolean skipEmptyDirs )
    {
        this.skipEmptyDirs = skipEmptyDirs;
    }

    public boolean isSkipEmptyDirs()
    {
        return skipEmptyDirs;
    }

    public void initRootTree(RemoteFSElemTreeElem elem)
    {
        root_list.add( elem);
        visibleList.add(elem);
    }
    
    public void initRootlist(List<RemoteFSElem> root_fselem_list)
    {
        for (int i = 0; i < root_fselem_list.size(); i++)
        {
            RemoteFSElem elem = root_fselem_list.get(i);

            RemoteFSElemTreeElem root = provider.createNode(provider, elem, null);
            root.setContainer( this );

            // SKIP EMPTY ROOT DIRECTORIES
            if (skipEmptyDirs && root.getElem().isDirectory())
            {
                List<RemoteFSElemTreeElem> ch = provider.getChildren(root);
                if (ch == null || ch.isEmpty())
                {
                    continue;
                }
            }

            root_list.add( root);
            visibleList.add(root);
        }        
    }

  
    


    @Override
    public Collection<?> getContainerPropertyIds()
    {
        ArrayList list = new ArrayList();

        for (int i = 0; i < fields.size(); i++)
        {
            FSTreeColumn object = fields.get(i);
            list.add( object.fieldName );
        }
        list.add("icon");

        return list;
    }

    FSTreeColumn getColumnField( Object itemId )
    {
        for (int i = 0; i < fields.size(); i++)
        {
            FSTreeColumn ftc = fields.get(i);
            if (ftc.fieldName.equals(itemId.toString()))
                return ftc;
        }
        return null;
    }


    @Override
    public Collection<?> getItemIds()
    {
        return visibleList;
    }

    @Override
    public Property getContainerProperty( Object itemId, Object propertyId )
    {
        if (itemId instanceof RemoteFSElemTreeElem)
        {
            RemoteFSElemTreeElem i = (RemoteFSElemTreeElem)itemId;

            return i.getMethodProperty(propertyId.toString());
        }
        return null;
    }

   
    @Override
    public Collection<?> getChildren( Object itemId )
    {
        if (itemId instanceof RemoteFSElemTreeElem)
        {
            RemoteFSElemTreeElem i = (RemoteFSElemTreeElem)itemId;
            return i.getChildren();
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getParent( Object itemId )
    {
        if (itemId instanceof RemoteFSElemTreeElem)
        {
            RemoteFSElemTreeElem i = (RemoteFSElemTreeElem)itemId;
            return i.parent;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<?> rootItemIds()
    {
        return root_list;
    }

    @Override
    public boolean setParent( Object itemId, Object newParentId ) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean areChildrenAllowed( Object itemId )
    {
        if (itemId instanceof RemoteFSElemTreeElem)
        {
            RemoteFSElemTreeElem i = (RemoteFSElemTreeElem)itemId;
            return i.elem.isDirectory();
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean setChildrenAllowed( Object itemId, boolean areChildrenAllowed ) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRoot( Object itemId )
    {
        if (itemId == null)
            return false;
        if (itemId instanceof RemoteFSElemTreeElem)
        {
            return root_list.contains((RemoteFSElemTreeElem)itemId);
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasChildren( Object itemId )
    {
        if (itemId instanceof RemoteFSElemTreeElem)
        {
            RemoteFSElemTreeElem i = (RemoteFSElemTreeElem)itemId;
            return (i.getChildren().size() > 0);
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeItem( Object itemId ) throws UnsupportedOperationException
    {
        if (itemId instanceof RemoteFSElemTreeElem)
        {
            RemoteFSElemTreeElem item = (RemoteFSElemTreeElem)itemId;
            if (!isCollapsed(item))
            {
                for (int j = 0; j < item.getChildren().size(); j++)
                {
                    RemoteFSElemTreeElem child = item.getChildren().get(j);
                    removeItem(child);
                }
            }
            // REMOVE FROM VISIBLE LIST
            visibleList.remove(item);
           
            // REMOVE FROM PARENT LIST
            return item.getParent().getChildren().remove(item);
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Item getItem( Object itemId )
    {
        return new BeanItem(itemId);
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class<?> getType( Object propertyId )
    {
        FSTreeColumn ftc = getColumnField( propertyId );
        if (ftc != null)
            return ftc.clazz;

        if (propertyId.toString().equals("icon"))
            return ThemeResource.class;
        
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int size()
    {
        return visibleList.size();
    }

    @Override
    public boolean containsId( Object itemId )
    {
        return visibleList.indexOf(itemId) >= 0;
    }

    @Override
    public Item addItem( Object itemId ) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object addItem() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addContainerProperty( Object propertyId, Class<?> type, Object defaultValue ) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeContainerProperty( Object propertyId ) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException
    {
        visibleList.clear();

        for (int i = 0; i < root_list.size(); i++)
        {
            RemoteFSElemTreeElem root = root_list.get(i);
            root.setCollapsed(true);
            visibleList.add(root);
        }
        return true;
    }

    @Override
    public Object nextItemId( Object itemId )
    {
        int index = visibleList.indexOf(itemId);
        if (index < visibleList.size() - 1)
            return visibleList.get(index + 1);

        return null;
    }

    @Override
    public Object prevItemId( Object itemId )
    {
        int index = visibleList.indexOf(itemId);
        if (index > 0)
            return visibleList.get(index -1);

        return null;
    }

    @Override
    public Object firstItemId()
    {
        return visibleList.get(0);
    }

    @Override
    public Object lastItemId()
    {
        return visibleList.get(size() - 1);
    }

    @Override
    public boolean isFirstId( Object itemId )
    {
        return itemId == visibleList.get(0);
    }

    @Override
    public boolean isLastId( Object itemId )
    {
        return itemId == visibleList.get(size() - 1);
    }

    @Override
    public Object addItemAfter( Object previousItemId ) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Item addItemAfter( Object previousItemId, Object newItemId ) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setCollapsed( Object itemId, boolean collapsed )
    {
        if (itemId instanceof RemoteFSElemTreeElem)
        {
            RemoteFSElemTreeElem item = (RemoteFSElemTreeElem)itemId;
            int start_idx = visibleList.indexOf(item);
            List<RemoteFSElemTreeElem> childs = item.getChildren();
            for (int j = 0; j < childs.size(); j++)
            {

                RemoteFSElemTreeElem child = childs.get(j);
                if (collapsed)
                {
                    // IF WE CLOSE, WE CLOSE RECURSIVELY
                    if (child.getElem().isDirectory() && !child.isCollapsed())
                    {
                        setCollapsed( child, true );
                    }

                    if(!visibleList.remove(child))
                        System.out.println("Remove failed: " + child.elem.getPath());
                }
                else
                {
                    visibleList.add(start_idx + 1 + j, child);
                }
            }
            item.setCollapsed( collapsed);
            //System.out.println("Length: " + visibleList.size());

            return;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCollapsed( Object itemId )
    {
        if (itemId instanceof RemoteFSElemTreeElem)
        {
            RemoteFSElemTreeElem i = (RemoteFSElemTreeElem)itemId;
            return i.isCollapsed( );
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void setFields( ArrayList<FSTreeColumn> fields )
    {
        this.fields = fields;
    }

    @Override
    public void sort( Object[] propertyId, boolean[] ascending )
    {
        final Object[] cols = propertyId;
        final boolean[] asc = ascending;



        Collections.sort(visibleList, new Comparator<RemoteFSElemTreeElem>()
        {
            @Override
            public int compare( RemoteFSElemTreeElem o1, RemoteFSElemTreeElem o2 )
            {
                RemoteFSElem e1 = o1.getElem();
                RemoteFSElem e2 = o2.getElem();


                for (int i = 0; i < asc.length; i++)
                {
                    boolean b = asc[i];
                    String col = cols[i].toString();
                    if (col.equals("name"))
                    {
                        int c = e1.getName().compareTo(e2.getName());
                        if (c != 0)
                            return (b) ? c : -c;
                    }
                    if (col.equals("size"))
                    {
                        if (e1.getDataSize() != e2.getDataSize())
                        {
                            int c = (e1.getDataSize() > e2.getDataSize()) ? 1 : -1;
                            return (b) ? c : -c;
                        }
                    }
                    if (col.equals("date"))
                    {
                        int c = e1.getMtime().compareTo( e2.getMtime() );
                        if (c != 0)
                            return (b) ? c : -c;
                    }
                }
                return 0;
            }
        } );
        
    }

    @Override
    public Collection<?> getSortableContainerPropertyIds()
    {
        List l = new ArrayList();
        l.add("name");
        l.add("size");
        l.add("date");
        return l;
    }

   

}