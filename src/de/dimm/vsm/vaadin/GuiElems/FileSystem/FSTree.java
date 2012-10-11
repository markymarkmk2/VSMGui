/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.FileSystem;

import com.vaadin.data.Container;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import java.util.ArrayList;
import java.util.HashMap;


/**
 *
 * @author Administrator
 */
// Have a tree with some unexpanded root items

/**
 *
 * @author Administrator
 */
// Have a tree with some unexpanded root items
public class FSTree extends TreeTable
{
    ArrayList<FSTreeColumn> fields;
    
    public FSTree(ArrayList<FSTreeColumn> fields, boolean enableSort)
    {
        this.fields = fields;        
        setStyleName("TreeTable");
        setNewItemsAllowed(false);
        setSelectable(true);
        setMultiSelect(true);
        setImmediate(true); // react at once when something is selected
        setColumnReorderingAllowed(true);
        setColumnCollapsingAllowed(true);

        this.setSortDisabled(!enableSort);
        this.setSortAscending(true);
        

        
                
        setCellStyleGenerator(new Table.CellStyleGenerator()
        {
            @Override
            public String getStyle(final Object itemId, final Object propertyId)
            {
                if (propertyId != null)
                {
                    String colName = propertyId.toString();

                    if (colName.equals("name"))
                    {
                        if (itemId instanceof RemoteFSElemTreeElem)
                        {
                            RemoteFSElemTreeElem rfs = (RemoteFSElemTreeElem) itemId;
                            if (rfs.elem.isDirectory())
                                return "dir";

                            return "file";
                        }
                    }
                    else
                        return colName;
                }
                return null;
            }
        });

        addListener( new ItemClickListener() {

            @Override
            public void itemClick( ItemClickEvent event )
            {
                if (event.isDoubleClick())
                {
                    HashMap<String, Object>  mp = new HashMap<String, Object>();
                    
                    String key = itemIdMapper.key(event.getItemId());

                    mp.put("toggleCollapsed", key);
                    mp.put("selectCollapsed", key);
                    changeVariables(event.getSource(), mp);
                }
            }
        });
        if (!isSortDisabled())
        {
            addListener( new HeaderClickListener() {

                @Override
                public void headerClick( HeaderClickEvent event )
                {
                    String key = event.getPropertyId().toString();
                    HashMap<String, Object>  mp = new HashMap<String, Object>();


                    setSortAscending( !isSortAscending());

                    mp.put("sortcolumn", key);
                    mp.put("sortscending", isSortAscending());

                    setSortContainerPropertyId(key );
                    changeVariables(event.getSource(), mp);
                }
            });
        }
    }
    public void expandAndReread( Object itemId )
    {
        HashMap<String, Object>  mp = new HashMap<String, Object>();
        String key = itemIdMapper.key(itemId);

        mp.put("toggleCollapsed", key);
        mp.put("selectCollapsed", key);
        if (!isCollapsed(itemId))
        {
            changeVariables(this, mp);
        }
        changeVariables(this, mp);
    }



    @Override
    public void setContainerDataSource( Container newDataSource )
    {
        super.setContainerDataSource(newDataSource);


        if (fields != null)
        {
            ArrayList<Object> visibleList = new ArrayList<Object>();

            for (int i = 0; i < fields.size(); i++)
            {
                FSTreeColumn ftc = fields.get(i);
                setColumnHeader(ftc.fieldName, ftc.caption);
                if (ftc.size >= 0)
                    setColumnWidth(ftc.fieldName, ftc.size);
                if (ftc.exRatio >= 0)
                    setColumnExpandRatio(ftc.fieldName, ftc.exRatio);

                if (ftc.alignment != null)
                    setColumnAlignment(ftc.fieldName, ftc.alignment);

                visibleList.add(ftc.fieldName);

            }
            setVisibleColumns(visibleList.toArray());

            setItemIconPropertyId("icon");
        }
    }
    public RemoteProvider getProvider()
    {
        Container newDataSource = getContainerDataSource();
        if (newDataSource instanceof FSTreeContainer)
        {
            FSTreeContainer ftc = (FSTreeContainer)newDataSource;
            return ftc.provider;
        }
        return null;
    }

}
