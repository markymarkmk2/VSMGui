/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.preview;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.Application;
import com.vaadin.event.MouseEvents;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.preview.IPreviewData;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class LeuchtTisch extends Panel implements Refresher.RefreshListener {

    List<IPreviewData> nodes;
    List<IPreviewData> busynodes;
    static int maxPerRow = 3;
    VSMCMain main;
    GridLayout array;
    Application app;

    // final Refresher refresher = new Refresher();
    public static Window createPreviewWindow( final VSMCMain main, List<IPreviewData> nodes ) {
        final LeuchtTisch lt = new LeuchtTisch(main, nodes);
        VerticalLayout vl = new VerticalLayout();
        Refresher refresher = new Refresher();
        refresher.setRefreshInterval(500);
        vl.setSizeFull();
        vl.addComponent(lt);
        vl.addComponent(refresher);
        refresher.addListener(lt);
        vl.setExpandRatio(lt, 1);

        final Window win = new Window("Leuchttisch");
        win.setContent(vl);
        win.setImmediate(true);
        if (!main.restoreWinPos(win)) {
            win.setWidth("500px");
            win.setHeight("50%");
        }
        else {
            lt.resize();
        }

        win.addListener(new Window.ResizeListener() {
            @Override
            public void windowResized( Window.ResizeEvent e ) {
                lt.resize();
                main.storeWinPos(win);
            }
        });
        lt.buildGui();

        return win;
    }

    static int getRows( List<IPreviewData> nodes ) {
        int rows = nodes.size() / maxPerRow;
        if (nodes.size() % maxPerRow != 0) {
            rows++;
        }
        return rows;
    }

    static int getCols( List<IPreviewData> nodes ) {
        if (nodes.size() > maxPerRow) {
            return maxPerRow;
        }
        return nodes.size();
    }
    private static String ERROR_FILE = "error.png";
    private static String BUSY_FILE = "loading.gif";
    private static String UNKNOWN_FILE = "norender.png";

    public LeuchtTisch( VSMCMain main, List<IPreviewData> nodes ) {
        array = new GridLayout(getCols(nodes), getRows(nodes));
        array.setWidth("450px");
        array.setStyleName("Leuchtgrid");
        
        this.addStyleName("Leuchttisch");
        this.nodes = new ArrayList<>();
        this.busynodes = new ArrayList<>();
        double f = 450.0 / getCols(nodes);
        f *= 480.0 / 640.0;
        f *= getRows(nodes);
        array.setHeight("" + f + "px");
        this.main = main;
        if (main != null) {
            this.app = main.getApp();
        }
        setNodes(nodes);
        this.setSizeFull();
        this.setImmediate(true);

        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSizeFull();
        rootLayout.addStyleName("Leuchtroot");
        this.setContent(rootLayout);
        this.setContent(array);
    }

    public void setApp( Application app ) {
        this.app = app;
    }
    
    

    @Override
    public void refresh( Refresher source ) {
        if (busynodes.isEmpty()) {
            return;
        }

        int cols = getCols(nodes);
        Iterator<IPreviewData> it = busynodes.iterator();
        while (it.hasNext()) {
            IPreviewData node = it.next();
            if (!node.getMetaData().isBusy()) {
                it.remove();
                int n = nodes.indexOf(node);
                int col = n % cols;
                int row = n / cols;
                setImage(node, col, row);
            }
        }
    }

    // Register new and Busy Nodes
    public final void setNodes( List<IPreviewData> nodes ) {
        this.nodes.clear();
        this.nodes.addAll(nodes);
        busynodes.clear();
        for (IPreviewData iPreviewData : this.nodes) {
            if (iPreviewData.getMetaData().isBusy()) {
                busynodes.add(iPreviewData);
            }
        }
    }

    public void setNewNodes( List<IPreviewData> nodes ) {
        setNodes(nodes);
        buildGui();
    }

    public void resize() {
        Window win = getWindow();
        if (win == null) {
            return;
        }
        // Verhältnis ist zu 4:3 berechnen
        // Ergibt bei B.H == 4:3 -> 1

        float ratio = win.getWidth() / win.getHeight() * (3.0f / 4.0f) / 1.2f;
        // gewünschte Höhe aus der Breite ausrechnen
        float f = (win.getWidth() - 30) / getCols(nodes);
        f *= 480.0 / 640.0;
        f *= getRows(nodes);

        // Und mit dem Formfaktor des fenster korrigieren damit die Breite gesteuerten 
        // Bilder die benötigte Höhe bekommen
        if (ratio < 1 && ratio > 0) {
            f /= ratio;
        }

        array.setWidth(win.getWidth() - 30, win.getWidthUnits());
        array.setHeight(f, win.getWidthUnits());
        this.setWidth(win.getWidth() - 10, win.getWidthUnits());
        this.setHeight(win.getHeight() - 60, win.getHeightUnits());
    }

    private void buildGui() {
        this.removeAllComponents();

        int rows = getRows(nodes);
        int cols = getCols(nodes);

        for (int row = 0; row < rows; row++) {
            //setRowExpandRatio(row, 1);
            for (int col = 0; col < cols && row * cols + col < nodes.size(); col++) {
                //setColumnExpandRatio(col, 1);
                final IPreviewData node = nodes.get(row * cols + col);
                setImage(node, col, row);
            }
        }
    }
    private class LtImage extends VerticalLayout{
        Embedded image;

        public LtImage(final IPreviewData node ) {
            createImgComponent(node);
        }
        
        
        final void createImgComponent( final IPreviewData node ) {

            final Resource res = getEffectivePreviewResource(node);
            image = new Embedded(node.getName(), res);
            image.setWidth("100%");
            image.setImmediate(true);
            image.addStyleName("Leuchtbild");
            //image.setHeight("");  
            this.addComponent(image);
            this.setSizeFull();
            this.setExpandRatio(image, 1);
            this.addStyleName("LtImage");
            
        }
        public void addListener(MouseEvents.ClickListener listener) {
            image.addListener(listener);
        }

        public void removeListener(MouseEvents.ClickListener listener) {
            image.removeListener(listener);
        }        

        public Embedded getImage() {
            return image;
        }
        
    }
    final Embedded createImgComponent( final IPreviewData node ) {
        final Resource res = getEffectivePreviewResource(node);
        Embedded image = new Embedded(node.getName(), res);
        image.setWidth("100%");
        image.setImmediate(true);
        //image.setHeight("");  
        return image;            
    }    


    public static void openSinglePreviewWin( VSMCMain main, IPreviewData node ) {

        if (main.getSinglePreviewWin() == null) {
            main.setSinglePreviewWin(createPreviewWindow(main, Arrays.asList(node)));
            main.getSinglePreviewWin().setCaption("Vorschau");
        }
        else {
            ((LeuchtTisch) ((VerticalLayout) main.getSinglePreviewWin().getContent()).getComponent(0)).setNewNodes(Arrays.asList(node));
        }
        main.getApp().getMainWindow().addWindow(main.getSinglePreviewWin());
    }
//
//    private File getEffectivePreviewFile( IPreviewData node ) {
//        if (node.getMetaData().isDone() && !node.getMetaData().isError()) {
//            return node.getPreviewImageFile();
//        }
//        if (node.getMetaData().isError()) {
//            return new File(ERROR_FILE);
//        }
//        if (!node.getMetaData().isDone()) {
//            return new File(BUSY_FILE);
//        }
//        return new File(UNKNOWN_FILE);
//    }
//
//    private void setImageOrg( final IPreviewData node, int col, int row ) {
//        final Window win = this.getWindow();
//        Embedded c = createImgComponent(node);
//        array.removeComponent(col, row);
//        array.addComponent(c, col, row);
//        array.setComponentAlignment(c, Alignment.TOP_CENTER);
//
//        // Clicklistener für Singleview falls erforderlich
//        if (nodes.size() > 1) {
//            c.addListener(new MouseEvents.ClickListener() {
//                @Override
//                public void click( MouseEvents.ClickEvent event ) {
//                    openSinglePreviewWin(main, node);
//                }
//            });
//        }
//        else {
//            c.addListener(new MouseEvents.ClickListener() {
//                @Override
//                public void click( MouseEvents.ClickEvent event ) {
//                    main.getApp().getMainWindow().removeWindow(win);
//                }
//            });
//        }
//    }
    private void setImage( final IPreviewData node, int col, int row ) {
        final Window win = this.getWindow();
        LtImage img = new LtImage(node);
        array.removeComponent(col, row);
        array.addComponent(img, col, row);
        array.setComponentAlignment(img, Alignment.TOP_CENTER);

        // Clicklistener für Singleview falls erforderlich
        if (main != null) {
            if (nodes.size() > 1) {
                img.addListener(new MouseEvents.ClickListener() {
                    @Override
                    public void click( MouseEvents.ClickEvent event ) {
                        openSinglePreviewWin(main, node);
                    }
                });
            }
            else {
                img.addListener(new MouseEvents.ClickListener() {
                    @Override
                    public void click( MouseEvents.ClickEvent event ) {
                        app.getMainWindow().removeWindow(win);
                    }
                });
            }
        }
    }
//
//    private int getColForNode( IPreviewData node ) {
//
//        return 0;
//    }
//
//    private int getRowForNode( IPreviewData node ) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

    private Resource getEffectivePreviewResource( IPreviewData node ) {
        if (node.getMetaData().isDone() && !node.getMetaData().isError()) {
            final Resource res = new FileResource(node.getPreviewImageFile(), app);
            return res;
        }
        String path = "/VAADIN/themes/vsm/images/";
        String filename;
        if (node.getMetaData().isError()) {
            filename= ERROR_FILE;
        }
        else if (!node.getMetaData().isDone()) {
            filename= BUSY_FILE;
        }
        else {
            filename= UNKNOWN_FILE;
        }
        
        JarFileResource res = new JarFileResource(path, filename, app);
        return res;
    }    
}

