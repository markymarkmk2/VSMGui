/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.ApplicationServlet;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Administrator
 */
public class AppServlet extends ApplicationServlet
{

    static URL[] getGuiUrls()
    {
        try
        {
            ArrayList<URL> l = new ArrayList<URL>();

            File libDir = new File("dist\\lib");
            if (!libDir.exists())
                libDir = new File("lib");

            if (!libDir.exists())
            {
                System.out.println("Cannot load GUI URLS");
                return null;
            }
            File[] libs = libDir.listFiles();
            for (int i = 0; i < libs.length; i++)
            {
                File file1 = libs[i];
                if (!file1.getName().endsWith(".jar"))
                    continue;

                URL u = file1.toURI().toURL();//new URL("file", "localhost", file.getAbsolutePath());

                l.add(u);
            }


            return l.toArray( new URL[0]);
        }
        catch (MalformedURLException malformedURLException)
        {
        }
        return null;
    }

    @Override
    protected ClassLoader getClassLoader() throws ServletException
    {
        ClassLoader ldr = null;
        try
        {
            ldr = URLClassLoader.newInstance(getGuiUrls(), this.getClass().getClassLoader());
        }
        catch (Exception malformedURLException)
        {
            malformedURLException.printStackTrace();
        }
        return ldr;
    }

    @Override
    public void destroy()
    {
        super.destroy();
        if (app != null)
        {
            app.close();
        }
    }


    Application app;

    @Override
    protected Application getNewApplication(HttpServletRequest request)            throws ServletException
    {
        app = super.getNewApplication(request);
        return app;
    }

    public Application getApp() {
        return app;
    }
    
   
    
}
