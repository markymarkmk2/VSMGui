/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm;



import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import eu.livotov.tpt.TPTApplication;
import java.io.File;

/**
 * Sample application layout, similar (almost identical) to Apple iTunes.
 *
 * @author IT Mill Ltd.
 *
 */
@SuppressWarnings("serial")
public class LeuchtTisch extends TPTApplication  {

    
@Override
    public void firstApplicationStartup()
    {
        // TODO Auto-generated method stub0
        System.out.println("firstApplicationStartup");
        
    }    

@Override
    public void applicationInit() 
    {
        initClientMain();
        System.out.println("applicationInit");
    }
    
    public void initClientMain() {

        setTheme("vsm");
        /*
         * We'll build the whole UI here, since the application will not contain
         * any logic. Otherwise it would be more practical to separate parts of
         * the UI into different classes and methods.
         */

        // Main (browser) window, needed in all Vaadin applications
        VerticalLayout rootLayout = new VerticalLayout();
        final Window root = new Window("LeuchtTisch", rootLayout);

        /*
         * We'll attach the window to the browser view already here, so we won't
         * forget it later.
         */
        setMainWindow(root);

        

        // Our root window contains one VerticalLayout, let's make
        // sure it's 100% sized, and remove unwanted margins
        rootLayout.setSizeFull();
        rootLayout.setMargin(false);

        // Top area, containing playback and volume controls, play status, view
        // modes and search
        VerticalLayout top = new VerticalLayout();
        top.setWidth("100%");
        top.setHeight("50px");
        
        top.setSpacing(true);
        Button bt = new Button("Halloooo");
        top.addComponent(bt);

        // Let's attach that one straight away too
        rootLayout.addComponent(top);
        
       /* ImageScaler scaler = new ImageScaler();
        Resource res = new FileResource(new File("D:\\Bilder\\Terrassentür.png"), this);
        scaler.setImage(res, 640, 480);        
        top.addComponent(scaler);*/
       /* ImageScaler scaler2 = new ImageScaler();
        res = new FileResource(new File("D:\\Bilder\\Terrassentür.png"), this);
        scaler2.setImage(res, 640, 480);        
        top.addComponent(scaler2);*/
        
        Button bt2 = new Button(  "Exit", new Button.ClickListener() {

            @Override
            public void buttonClick( Button.ClickEvent event ) {
                LeuchtTisch.this.close();
            }
        });
         top.addComponent(bt2);
         /*
         ImageScaler scaler2 = new ImageScaler();
        Resource res = new FileResource(new File("D:\\Bilder\\Terrassentür.png"), this);
        scaler2.setImage(res, 640, 480);        
        top.addComponent(scaler2);
        * */
                
         HorizontalLayout gallery =  creategallery();
         rootLayout.addComponent(gallery);
         rootLayout.setExpandRatio(gallery, 1);
         HorizontalLayout gallery2 =  creategallery();
         rootLayout.addComponent(gallery2);
         rootLayout.setExpandRatio(gallery2, 1);
         HorizontalLayout gallery3 =  creategallery();
         rootLayout.addComponent(gallery3);
         rootLayout.setExpandRatio(gallery3, 1);
         
    }
    HorizontalLayout creategallery() {
         HorizontalLayout gallery = new HorizontalLayout();
         gallery.setSizeFull();
         
         
         Resource res = new FileResource(new File("D:\\Bilder\\Terrassentür.png"), this);
         Embedded image = new Embedded("Yes, logo:", res);
         image.setWidth("100%");
         gallery.addComponent(image);
         
         res = new FileResource(new File("D:\\Bilder\\Terrassentür.png"), this);
         image = new Embedded("Yes, logo:", res);
         image.setWidth("100%");
         
         gallery.addComponent(image);    
         
         res = new FileResource(new File("D:\\Bilder\\Terrassentür.png"), this);
         image = new Embedded("Yes, logo:", res);
         image.setWidth("100%");
         gallery.addComponent(image);    
         return gallery;
    }

}
