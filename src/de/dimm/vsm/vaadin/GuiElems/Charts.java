/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems;

import com.vaadin.ui.Component;
import com.vaadin.ui.ProgressIndicator;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.Timer;



/**
 *
 * @author Administrator
 */
public class Charts
{

    String selectedNode = "";

   

    public Component init_ProgressChart()
    {
        final ProgressIndicator pi = new ProgressIndicator();
        pi.setWidth(300, Component.UNITS_PIXELS);
        pi.setHeight(40, Component.UNITS_PIXELS);
        pi.setIndeterminate(true);

        Timer timer = new Timer(1000, new ActionListener()
        {

            @Override
            public void actionPerformed( ActionEvent e )
            {
                Random r = new Random();
                int i = r.nextInt(100);

                pi.setValue(i);

                pi.setImmediate(true);
                pi.requestRepaint();


            }
        });
        timer.start();
        return pi;
    }
}
