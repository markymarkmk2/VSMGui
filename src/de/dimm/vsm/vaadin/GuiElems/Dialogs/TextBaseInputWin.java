/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.Dialogs;

import com.thoughtworks.xstream.XStream;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.records.TextBase;
import de.dimm.vsm.text.MissingTextException;
import de.dimm.vsm.vaadin.VSMCMain;
import static de.dimm.vsm.vaadin.VSMCMain.insideTextBaseEdit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class TextBaseInputWin extends Window
{
     
     VSMCMain main;
     
     VerticalLayout vl = new VerticalLayout();
     
     MissingTextException actExc;
     TextField key;
     TextField text;
     ComboBox lang;

    public TextBaseInputWin( VSMCMain _main )
    {        
        this.main = _main;
        
        actExc = main.missingKeys.get(0);
        
        build_gui();
        
        setVals(actExc);  

        Button abort = new Button("Abbruch");
        Button close = new Button("Übernehmen");
        
        vl.addComponent(abort);
        vl.addComponent(close);
        vl.setComponentAlignment(abort, Alignment.BOTTOM_LEFT);
        vl.setComponentAlignment(close, Alignment.BOTTOM_LEFT);        
        
        
        final Window w = this;
        abort.addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                event.getButton().getApplication().getMainWindow().removeWindow(w);
                 VSMCMain.insideTextBaseEdit = false;
            }
        });
        close.addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                addNewTextBase(key.getValue().toString(), text.getValue().toString(), lang.getValue().toString());
                VSMCMain.missingKeys.remove(actExc);
                
                if (VSMCMain.missingKeys.isEmpty())
                {
                    event.getButton().getApplication().getMainWindow().removeWindow(w);
                    VSMCMain.insideTextBaseEdit = false;
                }
                else
                {
                    actExc = VSMCMain.missingKeys.get(0);
                    setVals(actExc);                    
                }
            }
        });        
    }
    

    final void build_gui( )
    {
        addComponent(vl);
        setModal(true);
        setStyleName("vsm");

        //this.setSizeFull();
        this.setWidth("400px");

        vl.setSpacing(true);
        vl.setSizeFull();
        vl.setStyleName("editWin");

        this.setCaption("Texcteingabe für unbekannte Texte" );

        key = new TextField("Key" );
        vl.addComponent(key);
        key.setWidth("100%");
        vl.setExpandRatio(key, 1.0f);
        key.setReadOnly(true);

        List<String> langlist = new ArrayList<>();
        langlist.add("DE");
        langlist.add("EN");
        lang = new ComboBox("Sprach", langlist);
        vl.addComponent(lang);
               
        text = new TextField("Text" );
        vl.addComponent(text);        

        vl.addComponent(new Label(" "));
        
        Button copy = new Button("Kopie");
        
        vl.addComponent(copy);
        vl.setComponentAlignment(copy, Alignment.BOTTOM_LEFT);
        
        final Window w = this;
        copy.addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                text.setValue(key.getValue());
            }
        });        
        
    }

    final void setVals( MissingTextException exc )
    {
        key.setReadOnly(false);
        key.setValue(exc.getMessage() );
        key.setReadOnly(true);
        text.setValue("");
        
    }

    private void addNewTextBase( String id, String msg, String lang )
    {
        TextBase tb = new TextBase();
        tb.setMessageText(msg);
        tb.setMessageId(id);
        tb.setLangId(lang);
        
        List<TextBase> list = new ArrayList<>();
        list.add(tb);
        
        XStream xs = new XStream();

        File f = new File("VsmTextBaseUpdate.txt");
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(f);
            xs.toXML(list, fos);
        }
        catch (IOException iOException)
        {
            VSMCMain.notify(this, "Kann Textdatenbank nicht schreiben", iOException.toString());
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException iOException1)
            {
            }
            VSMCMain.notify(this, "SetTextBase", VSMCMain.Txt("$UPD$"));            
        }                    
    }
    
}
