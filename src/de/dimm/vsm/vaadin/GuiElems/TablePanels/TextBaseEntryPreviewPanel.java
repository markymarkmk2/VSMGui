/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import de.dimm.vsm.records.TextBase;
import de.dimm.vsm.vaadin.GuiElems.Table.PreviewPanel;

/**
 *
 * @author Administrator
 */
public class TextBaseEntryPreviewPanel extends PreviewPanel<TextBase>
{
    HorizontalLayout hlMM;
    

    public TextBaseEntryPreviewPanel( final TextBaseEntryTable j, boolean readOnly )
    {
        super(j, readOnly);
        setWidth("100%");
    }

    @Override
    public void recreateContent( TextBase node )
    {
        super.recreateContent(node); //To change body of generated methods, choose Tools | Templates.
        Button next = new Button("Nächster");
        final PreviewPanel<?> panel = this;
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                table.setNext();
                
                
            }
        });  
        addComponent( next);        
        Button bt = new Button("ID -> Text");
        
        bt.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                Component comp1 = table.getField("messageId").getGuiforField(panel);
                Component comp2 = table.getField("messageText").getGuiforField(panel);
                ((TextField)comp2).setValue( ((TextField)comp1).getValue());                
            }
        });  
        addComponent( bt);        
    }

    
    



}
