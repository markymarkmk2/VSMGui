/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.TablePanels.ext;

import de.dimm.vsm.auth.User;
import de.dimm.vsm.hash.StringUtils;
import de.dimm.vsm.log.LogManager;
import java.util.Arrays;

/**
 *
 * @author Administrator
 */
public class AclMappingFileUi extends MappingFileUi{

    @Override
    public String getCbName() {
        return "ACL-Mapping";
    }
    
    @Override
    protected String getNewContent() {
    String txt =  "# Diese Datei stellt ein Mapping zwischen Usern / Gruppen und ACL-Einträgen her.\n# Aufbau:\n";
            txt+= "# [User|Group]:<Name>:[Allow:Deny]:[User|Group]:<Name>[,<Name>...]\n";
            txt+= "# \n"
                + "# \n"
                + "# Beispiele:\n"
                + "# Benutzer Anne soll auch auf Dokumente von Bernd und Clara zugreifen können: \n"
                + "# User:Anne:Allow:User:Bernd,Clara\n\n"
                + "# Benutzer Anne soll keine Dokumente der Gruppe Einkauf sehen, obwohl sie zum Zeitpunkt der Sicherung Mitglied der Gruppe war:\n"
                + "# User:Anne:Deny:Group:Einkauf\n\n"
                + "# Alle Benutzer der Gruppe Einkauf sollen auch Dokumente der Gruppen Entwicklung und Support sehen\n"
                + "# Group:Einkauf:Allow:Group:Entwicklung,Support\n"
                + "# \n"
                + "# Um die von Posix automatisch gesetzten ACLs (wie z.B. 'Everyone@') zu ignorieren: \n"
                + "# In einer eigenen Zeile den Text \n"
                + "# " + User.SKIP_POSIX_ACL_OPT + " \n"
                + "# angeben"    ;
        
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
            {
                continue;
            }
            if (string.charAt(0) == '#')
                continue;
            
            lastParsedLine = string;
            String[] entry = string.split(":");
            if (entry.length == 1 && entry[0].equalsIgnoreCase(User.SKIP_POSIX_ACL_OPT))
                continue;
            
            if (entry.length < 5) {
                parseErrText = "Zu wenige Argumente";
                return false;
            }
            String type =  entry[0].trim().toLowerCase();
            String entity =  entry[1].trim();
            String action = entry[2].trim().toLowerCase();
            String argsType = entry[3].trim().toLowerCase();
            String argsArr = entry[4];
            String[] args = argsArr.split(",");
            if (args.length < 1) {
                parseErrText = "Zu wenige Werte";
                return false;
            }
            if (!Arrays.asList("user","group").contains(type))
                return false;
            if (!Arrays.asList("user","group").contains(argsType))
                return false;
            if (!Arrays.asList("allow","deny").contains(action))
                return false;
            
            if (entity.isEmpty())
                return false;
        }
        return true;

    }    

    @Override
    protected String getDirName() {
        return User.GROUP_MAPPINGFOLDER;
    }
}
