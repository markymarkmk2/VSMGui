/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.FileSystem;

import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.terminal.ThemeResource;
import de.dimm.vsm.Utilities.SizeStr;
import de.dimm.vsm.net.RemoteFSElem;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class RemoteFSElemTreeElem
{
    protected RemoteFSElemTreeElem parent;
    protected RemoteFSElem elem;
    protected boolean collapsed = true;
    protected List<RemoteFSElemTreeElem> childList;
    protected RemoteProvider provider;
    FSTreeContainer container;
    int level;

    public static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy  HH.mm");

    public RemoteFSElemTreeElem( RemoteProvider provider, RemoteFSElem elem, RemoteFSElemTreeElem parent )
    {
        this.provider = provider;
        this.elem = elem;
        this.parent = parent;
        if (parent != null)
            level = parent.level + 1;
        else
            level = 0;
    }

    public int getLevel()
    {
        return level;
    }
    

    public RemoteFSElem getElem()
    {
        return elem;
    }
    public RemoteFSElemTreeElem getParent()
    {
        return parent;
    }

    @Override
    public String toString()
    {
        return elem.toString();
    }


    

    protected Property getMethodProperty( String m )
    {
       if (m.equals("icon"))
       {
           String icon;
           if (elem.isDirectory())
           {
               if (elem.isDeleted())
                   icon = "images/dir_closed_deleted.png";
               else
                   icon = "images/dir_closed.png";
/*               if (collapsed)
                   icon = "images/dir_closed.png";
               else
                   icon = "images/dir_open.png";*/
           }
           else
           {
               if (elem.isDeleted())
                   icon = "images/file_deleted.png";
               else
                   icon = "images/file.png";
           }


           return new ObjectProperty( new ThemeResource(icon), ThemeResource.class );
          
       }
       if (m.equals("name"))
       {
          return new ObjectProperty( " " + elem.getName(), String.class );
       }
       if (m.equals("size"))
       {
           if (elem.isDirectory())
               return new ObjectProperty( SizeStr.format( 0 ), String.class );
           
          return new ObjectProperty( SizeStr.format( elem.getDataSize() ), String.class );
       }
       if (m.equals("date"))
          return new ObjectProperty( sdf.format(elem.getMtime()), String.class );
       if (m.equals("attribute"))
          return new ObjectProperty( "", String.class );

       return null;
    }

    public boolean isCollapsed()
    {
        return collapsed;
    }

    public void setCollapsed( boolean collapsed )
    {
        this.collapsed = collapsed;
        if (collapsed)
        {
            childList.clear();
            childList = null;
        }
    }


    List<RemoteFSElemTreeElem> getChildren()
    {
        if (childList != null)
            return childList;

        childList = provider.getChildren(this);
        for (int i = 0; i < childList.size(); i++)
        {
            RemoteFSElemTreeElem remoteFSElemTreeElem = childList.get(i);
            remoteFSElemTreeElem.setContainer(container);
        }
        

        return childList;
    }

//    public String getVsmName()
//    {
//        String path = container.mapVsmToUserPath(elem.getPath());
//        String s = path;
//        int idx = s.lastIndexOf(elem.getSeparatorChar());
//        if (idx > 0 && idx < s.length() - 1)
//            s = s.substring(idx + 1);
//        return s;
//    }
    public String getName()
    {
        String path = elem.getPath();
        String s = path;
        int idx = s.lastIndexOf(elem.getSeparatorChar());
        if (idx > 0 && idx < s.length() - 1)
            s = s.substring(idx + 1);
        return s;
    }

    void setContainer( FSTreeContainer aThis )
    {
        container = aThis;
    }

    public FSTreeContainer getContainer()
    {
        return container;
    }
    public void addChild(RemoteFSElemTreeElem remoteFSElemTreeElem)
    {
        if (childList != null)
            childList.add(remoteFSElemTreeElem);
    }
    
}