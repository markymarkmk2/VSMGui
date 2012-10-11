/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.FileSystem;

import de.dimm.vsm.auth.User.VsmFsEntry;
import de.dimm.vsm.net.RemoteFSElem;
import java.util.ArrayList;
import java.util.List;

class RootMappingElem extends RemoteFSElemTreeElem
{
    List<VsmFsEntry> mappingList;
    List<MappingTreeElem> mappedChildren;

    public RootMappingElem(  List<VsmFsEntry> list, RemoteProvider provider, RemoteFSElem elem, RemoteFSElemTreeElem parent)
    {
        super(provider, elem, parent);
        this.mappingList = list;
       // mappedChildren
    }

    @Override
    List<RemoteFSElemTreeElem> getChildren()
    {
        if (childList != null)
            return childList;

        if (mappedChildren == null)
            return super.getChildren();

        childList = new ArrayList<RemoteFSElemTreeElem>();

        for (int i = 0; i < mappedChildren.size(); i++)
        {
            RemoteFSElemTreeElem remoteFSElemTreeElem = mappedChildren.get(i);
            remoteFSElemTreeElem.setContainer(container);
            childList.add(remoteFSElemTreeElem);
        }

        return childList;
    }
}
/**
 *
 * @author Administrator
 */
public class MappingTreeElem extends RemoteFSElemTreeElem
{
    List<RemoteFSElemTreeElem> mappedChildren;


    public MappingTreeElem(  RemoteProvider provider, RemoteFSElem elem, RemoteFSElemTreeElem parent)
    {
        super(provider, elem, parent);

    }

    public void setMappedChildren( List<RemoteFSElemTreeElem> mappedChildren )
    {
        this.mappedChildren = mappedChildren;
    }


    @Override
    List<RemoteFSElemTreeElem> getChildren()
    {
        if (childList != null)
            return childList;

        if (mappedChildren == null)
            return super.getChildren();

        childList = new ArrayList<RemoteFSElemTreeElem>();

        for (int i = 0; i < mappedChildren.size(); i++)
        {
            RemoteFSElemTreeElem remoteFSElemTreeElem = mappedChildren.get(i);
            remoteFSElemTreeElem.setContainer(container);
            childList.add(remoteFSElemTreeElem);
        }

        return childList;
    }

    MappingTreeElem getRoot()
    {
        MappingTreeElem ptr = this;
        while (parent != null)
            ptr = (MappingTreeElem) ptr.parent;

        return ptr;
    }
    @Override
    public void addChild(RemoteFSElemTreeElem remoteFSElemTreeElem)
    {
        if (childList != null)
            childList.add(remoteFSElemTreeElem);

        if (mappedChildren == null)
        {
             mappedChildren = new ArrayList<RemoteFSElemTreeElem>();
        }
        mappedChildren.add(remoteFSElemTreeElem);
    }



}
