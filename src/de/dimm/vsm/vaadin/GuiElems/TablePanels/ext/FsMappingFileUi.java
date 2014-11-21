/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.TablePanels.ext;

import de.dimm.vsm.auth.User;

/**
 *
 * @author Administrator
 */
public class FsMappingFileUi extends MappingFileUi{

    @Override
    public String getCbName() {
        return "VSM-FsMapping";
    }
    
    @Override
    protected String getNewContent() {
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
                + "#         /< alle Benutzer von 192.168.2.47>\n";
        return txt;
    }
    
    @Override
    protected boolean checkMapping( String s )
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

    @Override
    protected String getDirName() {
        return User.FS_MAPPINGFOLDER;
    }
}
