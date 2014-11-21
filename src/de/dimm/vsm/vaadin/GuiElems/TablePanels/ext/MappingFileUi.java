/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.TablePanels.ext;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import de.dimm.vsm.auth.User;
import de.dimm.vsm.records.RoleOption;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.TextAreaDlg;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.TextFieldDlg;
import de.dimm.vsm.vaadin.VSMCMain;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public abstract class MappingFileUi extends VerticalLayout
{
    ComboBox cb;
    List<ComboEntry> cdelist;
    abstract protected String getCbName();
    abstract protected String getDirName();
    abstract protected String getNewContent();

    
    
    public void buildUi( final TextField optionString, final RoleOption node)
    {
        optionString.setVisible(false );
        cdelist = new ArrayList<>();
        cdelist.add( new ComboEntry("", ""));
        File dir = new File(getMappingFolder());
        if (!dir.exists())
            dir.mkdir();

        File[] fs = new File(getMappingFolder()).listFiles();
        int actIndex = -1;

        for (int i = 0; i < fs.length; i++)
        {
            File file = fs[i];
            String name = file.getName();
            if (name.endsWith(User.MAPPING_EXT))
                name = name.substring(0, name.lastIndexOf(User.MAPPING_EXT));
            
            ComboEntry cbe = new ComboEntry(file, name);
            cdelist.add(cbe);
            if (name.equals(node.getOptionStr()))
                actIndex = i;
        }
        cb = new ComboBox(getCbName(), cdelist);
        cb.setNewItemsAllowed(false);
        cb.setNullSelectionAllowed(false);

        cb.addListener( new Property.ValueChangeListener() {

            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                optionString.setValue("");
                ComboEntry sel = (ComboEntry)cb.getValue();
                if (sel == null)
                   return;

                if (sel.getDbEntry() == null)
                    return;

                optionString.setValue(sel.getGuiEntryKey());
            }
        });
        this.addComponent(cb);
        if (actIndex != -1)
            cb.select(cdelist.get(actIndex + 1));

        Button newEntry = new Button("Neu", new Button.ClickListener() {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                newFsmapping();
            }
        });
        this.addComponent(newEntry);
        Button delEntry = new Button("Löschen", new Button.ClickListener() {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                delFsmapping();
            }
        });
        Button editEntry = new Button("Bearbeiten", new Button.ClickListener() {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                editFsmapping();
            }
        });
        this.addComponent(newEntry);
        this.addComponent(delEntry);
        this.addComponent(editEntry);        
    }

    String getMappingFolder()
    {
        return getDirName();
    }
    void newFsmapping()
    {
        Validator fsv = new AbstractValidator("Bitte geben Sie einen gültigen und eindeutigen Namen für das Mapping ein") {

            @Override
            public boolean isValid( Object value )
            {
                File f = new File( getMappingFolder(), value.toString() + User.MAPPING_EXT );
                if (f.exists())
                {
                    VSMCMain.notify(MappingFileUi.this, "Mappingdatei existiert bereits: ", f.toString());
                    return false;
                }
                
                try
                {
                    if (!f.createNewFile())
                    {
                        VSMCMain.notify(MappingFileUi.this, "Mappingdatei kann nicht angelegt werden: ", f.toString());
                        return false;
                    }
                    
                    f.delete();
                }
                catch (Exception exc)
                {
                    VSMCMain.notify(MappingFileUi.this, "Mappingdatei kann nicht angelegt werden: ", f.toString());
                    return false;
                }
                return true;                
            }
        };
        final TextFieldDlg dlg = new TextFieldDlg("Neues Mapping", "Name", "Mapping " + cdelist.size(), fsv);

        dlg.setOkActionListener( new Button.ClickListener() {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                createMapping( dlg.getText() );
            }
        });

        this.getApplication().getMainWindow().addWindow(dlg);
        
    }

    void createMapping(String name)
    {
        File folder = new File( getMappingFolder());
        if (!folder.exists()) {
            folder.mkdir();
        }
        File f = new File( getMappingFolder(), name + User.MAPPING_EXT);
        String txt = getNewContent();

        try (FileWriter fw = new FileWriter(f))
        {
            fw.write(txt);
            ComboEntry cbe = new ComboEntry(f, name);
            cdelist.add(cbe);
            cb.addItem(cbe);
            cb.setValue(cbe);
            cb.select(cdelist.size() - 1);
        }
        catch (IOException iOException)
        {
            VSMCMain.notify(this, "Fehler beim Erzeugen der Mappingdatei" , iOException.getMessage());
        }
    }
    void delFsmapping()
    {
        ComboEntry sel = (ComboEntry)cb.getValue();
        if (sel == null)
            return;

        if (sel.getDbEntry() == null)
            return;


        File f = new File( getMappingFolder(), sel.getGuiEntryKey() + User.MAPPING_EXT);
        if (f.exists())
        {
            f.delete();
            cdelist.remove(sel);
            cb.removeItem(sel);
            cb.select(cdelist.size() - 1);
        }
        else {
            f = new File( getMappingFolder(), sel.getGuiEntryKey());
            if (f.exists())
            {
                f.delete();
                cdelist.remove(sel);
                cb.removeItem(sel);
                cb.select(cdelist.size() - 1);
            }
            else
                VSMCMain.notify(this, "Fehler beim Löschen der Mappingdatei" , "Datei existiert nicht");
        }
    }
    void editFsmapping()
    {
        final ComboEntry sel = (ComboEntry)cb.getValue();
        if (sel == null)
            return;

        if (sel.getDbEntry() == null)
            return;

        String content = null;
        File f = new File( getMappingFolder(), sel.getGuiEntryKey()+ User.MAPPING_EXT );
        if (!f.exists()) {
            f = new File( getMappingFolder(), sel.getGuiEntryKey());
        }
            
        try (FileReader fw = new FileReader(f))
        {
            char[] cbuff = new char[(int)f.length()];
            int len = fw.read(cbuff);
            content = new String(cbuff, 0, len);
        }
        catch (Exception iOException)
        {
            VSMCMain.notify(this, "Fehler beim Lesen der Mappingdatei" , iOException.getMessage());
        }
        if (content == null)
            return;

        Validator vsmmappingValidator = new AbstractValidator("Das Mapping ist fehlerfaft") {

            @Override
            public boolean isValid( Object value )
            {
                boolean ret = checkMapping( value.toString() );
                if (!ret)
                    this.setErrorMessage("Fehler in Zeile " + lastParsedLine + ": " + parseErrText);
                
                return ret;
            }
        };
        
        final TextAreaDlg dlg = new TextAreaDlg("Mappinginhalt", content, vsmmappingValidator, true, false);

        dlg.setOkActionListener( new Button.ClickListener() {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {   File f = new File( getMappingFolder(), sel.getGuiEntryKey()+ User.MAPPING_EXT );
                writeFsMapping( f, dlg.getText()  );
                f = new File( getMappingFolder(), sel.getGuiEntryKey());
                if (f.exists())
                    f.delete();
            }
        });
        this.getApplication().getMainWindow().addWindow(dlg);
    }
    String lastParsedLine = "";
    String parseErrText = "";

    protected abstract boolean checkMapping( String s );
    
    void  writeFsMapping( File f, String txt )
    {
        try (FileWriter fw = new FileWriter(f))
        {
            fw.write(txt);
        }
        catch (IOException iOException)
        {
            VSMCMain.notify(this, "Fehler beim Schreiben der Mappingdatei" , iOException.getMessage());
        }
    }
    
}
