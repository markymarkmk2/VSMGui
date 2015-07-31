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
import de.dimm.vsm.preview.IMetaData;
import de.dimm.vsm.preview.IPreviewData;
import eu.livotov.tpt.TPTApplication;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



class LeuchtTischPreviewData implements IPreviewData {

    long attrIdx;
    File file;
    IMetaData metaData;
    String name;

    public LeuchtTischPreviewData( long attrIdx, File file, IMetaData metaData, String name ) {
        this.attrIdx = attrIdx;
        this.file = file;
        this.metaData = metaData;
        this.name = name;
    }
    
    @Override
    public File getPreviewImageFile() {
        return file;
    }

    @Override
    public IMetaData getMetaData() {
        return metaData;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getAttrIdx() {
        return attrIdx;
    }

    @Override
    public void setPreviewImageFile( File file ) {
        file = this.file;
    }    
}

class LeuchtTischMetaData implements IMetaData {

    @Override
    public String getAttribute( String key ) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAttribute( String key, String value ) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<String> getKeys() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public boolean isTimeout() {
        return false;
    }

    @Override
    public void setBusy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDone() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setError() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setTimeout() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setError( String txt ) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
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
        //initClientMain();
        initLeuchttisch();
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
    
        public void initLeuchttisch() {

        setTheme("vsm");
        /*
         * We'll build the whole UI here, since the application will not contain
         * any logic. Otherwise it would be more practical to separate parts of
         * the UI into different classes and methods.
         */

        // Main (browser) window, needed in all Vaadin applications
        final VerticalLayout rootLayout = new VerticalLayout();
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
        top.setSizeFull();
        Button bt = new Button("Bumm");
        rootLayout.addComponent(bt);
        bt.addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( Button.ClickEvent event ) {
                if (lt != null) {
                    rootLayout.removeComponent(lt);
                }
                loadLt();
                rootLayout.addComponent(lt);
            }
        });
    }
        
    void loadLt() {
            List<IPreviewData> nodes = new ArrayList<>();
        IMetaData doneMd = new LeuchtTischMetaData();
        nodes.add( new LeuchtTischPreviewData(0, new File("D:\\Bilder\\Terrassentür.png"), doneMd, "Blah"));
        nodes.add( new LeuchtTischPreviewData(0, new File("D:\\Bilder\\Terrassentür.png"), doneMd, "Blah2"));
        nodes.add( new LeuchtTischPreviewData(0, new File("D:\\Bilder\\Terrassentür.png"), doneMd, "Blah"));
        nodes.add( new LeuchtTischPreviewData(0, new File("D:\\Bilder\\Terrassentür.png"), doneMd, "Blah2"));
        
        lt = new de.dimm.vsm.vaadin.GuiElems.preview.LeuchtTisch(null, nodes);
        lt.setApp(this);
        lt.setNewNodes(nodes);
    }
    de.dimm.vsm.vaadin.GuiElems.preview.LeuchtTisch lt;
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
