/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.FileSystem;

import com.thoughtworks.xstream.XStream;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.terminal.ThemeResource;
import de.dimm.vsm.Utilities.SizeStr;
import de.dimm.vsm.fsengine.FSE_Bootstrap;
import de.dimm.vsm.net.RemoteFSElem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Administrator
 */
public class BootstrapFSElemTreeElem extends RemoteFSElemTreeElem
{
    FSE_Bootstrap bootstrap;

    public BootstrapFSElemTreeElem( RemoteProvider prov, RemoteFSElem elem, RemoteFSElemTreeElem parent )
    {
        super( prov, elem, parent );
        bootstrap = null;
    }


    void readBootstrap()
    {
        if (bootstrap != null)
            return;

        File f = new File(elem.getPath());
        File btd = new File( f.getParent(), "bootstrap");
        File btf = new File( btd, "fs_" + f.getName() + ".xml");

        if (btf.exists())
        {
            XStream xs = new XStream();
            
            FileInputStream fis = null;

            try
            {
                fis = new FileInputStream(btf);
                bootstrap = (FSE_Bootstrap) xs.fromXML(fis);                
            }
            catch (IOException iOException)
            {
                iOException.printStackTrace();
            }
            finally
            {
                try
                {
                    fis.close();
                }
                catch (IOException iOException)
                {
                }
            }
        }
    }


    @Override
    protected Property getMethodProperty( String m )
    {
        readBootstrap();

        if (bootstrap == null)
            return super.getMethodProperty( m );

        if (m.equals("icon"))
        {
           String icon = "images/file.png";
           if (elem.isDirectory())
           {
               icon = "images/dir_closed.png";
           }

           return new ObjectProperty( new ThemeResource(icon), ThemeResource.class );
          
       }
       if (m.equals("name"))
          return new ObjectProperty( " " + bootstrap.getName() + " (" + getName() + ")", String.class );
       if (m.equals("size"))
       {
           if (elem.isDirectory())
               return new ObjectProperty( SizeStr.format( 0 ), String.class );
           
          return new ObjectProperty( SizeStr.format( bootstrap.getFsize() ), String.class );
       }
       if (m.equals("date"))
          return new ObjectProperty( sdf.format( new Date(bootstrap.getModificationDateMs())), String.class );
       if (m.equals("attribute"))
          return new ObjectProperty( "", String.class );

       return null;
    }


}