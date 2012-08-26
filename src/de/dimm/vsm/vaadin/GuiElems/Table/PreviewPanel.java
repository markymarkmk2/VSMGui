/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Table;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADBLinkField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;

/**
 *
 * @author Administrator
 */
public class PreviewPanel<T> extends VerticalLayout
{
    protected BaseDataEditTable<T> table;
    protected boolean rdOnly;

    public PreviewPanel( BaseDataEditTable<T> table, boolean rdOnly )
    {
        this.table = table;
        this.rdOnly = rdOnly;
        setMargin(true);
        setSpacing(true);
        setSizeFull();
        setStyleName("editWin");
        setImmediate(true);
    }
    public Component getGui( String field )
    {
        return JPAField.getGuiforField(this, field);
    }
    public Component getGui( JPAField field )
    {
        return JPAField.getGuiforField(this, field.getFieldName());
    }
    public void addDBLinkClickListener( final Component gui, final JPADBLinkField linkField )
    {
        linkField.addGuiClickListener( this, new ClickListener()
        {

            @Override
            public void buttonClick( ClickEvent event )
            {
                ClickListener ok = new ClickListener()
                {
                    @Override
                    public void buttonClick( ClickEvent event )
                    {
                        linkField.reload(gui);
                    }
                };

                table.createChildDB( linkField, ok );
            }
        } );
    }

    public void recreateContent( T node)
    {
        removeAllComponents();

        // CREATE GUI ITEMS
        for (int i = 0; i < table.fieldList.size(); i++)
        {
            JPAField jPAField = table.fieldList.get(i);
            final Component gui = jPAField.createGui(node);
            addComponent( gui );
            if (jPAField instanceof JPADBLinkField)
            {
                final JPADBLinkField linkField = (JPADBLinkField)jPAField;
                addDBLinkClickListener( gui, linkField );
            }
            jPAField.setReadOnly( gui, rdOnly);
        }
    }
}
