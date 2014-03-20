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
public class FsMappingUi extends VerticalLayout
{
    ComboBox cb;
    List<ComboEntry> cdelist;
    public void buildUi( final TextField optionString, final RoleOption node)
{
        optionString.setVisible(false );
        cdelist = new ArrayList<>();
        cdelist.add( new ComboEntry("", ""));
        File dir = new File(getFsMappingFolder());
        if (!dir.exists())
            dir.mkdir();

        File[] fs = new File(getFsMappingFolder()).listFiles();
        int actIndex = -1;

        for (int i = 0; i < fs.length; i++)
        {
            File file = fs[i];

            ComboEntry cbe = new ComboEntry(file, file.getName());
            cdelist.add(cbe);
            if (file.getName().equals(node.getOptionStr()))
                actIndex = i;
        }
        cb = new ComboBox("Filesystemmapping", cdelist);
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

    String getFsMappingFolder()
    {
        return User.FS_MAPPINGFOLDER;
    }
    void newFsmapping()
    {
        Validator fsv = new AbstractValidator("Bitte geben Sie einen gültigen und eindeutigen Namen für das Mapping ein") {

            @Override
            public boolean isValid( Object value )
            {
                File f = new File( getFsMappingFolder(), value.toString() );
                if (f.exists())
                    return false;
                
                try
                {
                    if (!f.createNewFile())
                        return false;
                    
                    f.delete();
                }
                catch (Exception exc)
                {
                    return false;
                }
                return true;                
            }
        };
        final TextFieldDlg dlg = new TextFieldDlg("Neues VSM-Dateisystemmapping", "Name", "Mapping " + cdelist.size() + 1, fsv);

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
        File f = new File( getFsMappingFolder(), name);
        String txt = "# Diese Datei stellt ein Mapping zwischen VSM-Dateisystem <-> Benutzersicht dar.\n";
        txt += "# Linke Spalte ist der reale VSM-Systempfad, rechte Spalte der Pfad aus der Sicht des Benutzers\n";
        txt += "# Trennung der Spalten mit einem ',' Leerzeichen um den Trenner herum werden ignoriert\n"
                + "# \n"
                + "# \n"
                + "# Beispiel:\n"
                + "# Rolle 'Standardnutzer' hat folgendes Mapping: \n"
                + "# /192.168.2.1/8082/raid/daten/BenutzerDaten/Piet,    /Benutzerdaten/Piet\n"
                + "# /192.168.2.42/8082/raid/daten1/BenutzerDaten/Mark,   /Benutzerdaten/Mark\n"
                + "# 192.168.2.47/8082/raid/daten99/BenutzerDaten,   /Benutzerdaten\n"
                + "# Im Dateibaum ist dann für die Benutzer Piet und Mark nur noch folgendes zu sehen:\n"
                + "# \n"
                + "# /Benutzerdaten\n"
                + "#         /Piet\n"
                + "#         /Mark\n"
                + "#         /< alle Benutzer von 192.168.2.47>\n"
                + "# ";

        try (FileWriter fw = new FileWriter(f))
        {
            fw.write(txt);
            ComboEntry cbe = new ComboEntry(f, f.getName());
            cdelist.add(cbe);
            cb.setValue(cbe);
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


        File f = new File( getFsMappingFolder(), sel.getGuiEntryKey());
        if (f.exists())
            f.delete();
        else
            VSMCMain.notify(this, "Fehler beim Löschen der Mappingdatei" , "Datei existiert nicht");

    }
    void editFsmapping()
    {
        ComboEntry sel = (ComboEntry)cb.getValue();
        if (sel == null)
            return;

        if (sel.getDbEntry() == null)
            return;

        String content = null;
        final File f = new File( getFsMappingFolder(), sel.getGuiEntryKey());
            
        try (FileReader fw = new FileReader(f))
        {
            char[] cbuff = new char[(int)f.length()];
            fw.read(cbuff);
            content = new String(cbuff);
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
                    this.setErrorMessage("Fehler in Zeile " + lastParsedLine);
                
                return ret;
            }
        };
        
        final TextAreaDlg dlg = new TextAreaDlg("Mappinginhalt", content, vsmmappingValidator, true, false);

        dlg.setOkActionListener( new Button.ClickListener() {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                writeFsMapping( f, dlg.getText()  );
            }
        });
        this.getApplication().getMainWindow().addWindow(dlg);
    }
    String lastParsedLine = "";

    boolean checkMapping( String s )
    {
        s = s.replace('\r', '\n');
        String[] arr = s.split("\n");
        for (int i = 0; i < arr.length; i++)
        {
            String string = arr[i];
            if (string.trim().isEmpty())
                continue;
            if (string.charAt(0) == '#')
                continue;
            
            lastParsedLine = string;
            if (string.startsWith("Exclude"))
            {
                String[] entry = string.split(",");
                if (entry.length < 1)
                    return false;
                
                String mask = entry[0].trim();
                if (mask.isEmpty())
                    return false;
                
                continue;
            }
            else
            {
                String[] entry = string.split(",");
                if (entry.length < 2)
                    return false;
                String v = entry[0].trim();
                String u = entry[1].trim();
                if (v.isEmpty())
                    return false;
                if (u.isEmpty())
                    return false;
                if (v.charAt(0) != '/')
                    return false;
            }
           
        }
        return true;

    }
    void  writeFsMapping( File f, String txt )
    {
        try (FileWriter fw = new FileWriter(f))
        {
            fw.write(txt);
        }
        catch (IOException iOException)
        {
            VSMCMain.notify(this, "Fehler beim Erzeugen der Mappingdatei" , iOException.getMessage());
        }
    }
    
}
