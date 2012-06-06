/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.search;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.Date;

/**
 *
 * @author Administrator
 */
public class TimeIntervalPanel extends HorizontalLayout
{
    String field;
    DateField dt_from;
    DateField dt_till;
    CheckBox enable;

    public TimeIntervalPanel( String caption, String field, boolean isEnable)
    {
        this.field = field;

        // UNTEN
        setMargin(false, false, true, false);
        setSpacing(true);
        setImmediate(true);
        setSizeFull();
        setWidth("350px");

        enable = new CheckBox(caption, new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                updateVisiblity();
            }

        });
        dt_from = new DateField(VSMCMain.Txt("Von"));
        dt_from.setImmediate(true);
        dt_till = new DateField(VSMCMain.Txt("Bis"));
        dt_till.setImmediate(true);
        enable.setValue(isEnable);
        enable.setImmediate(true);

        addComponent(enable);
        setComponentAlignment(enable,  Alignment.MIDDLE_LEFT);
        VerticalLayout hl = new VerticalLayout();
        hl.setSpacing(true);
        hl.setSizeFull();
        hl.addComponent(dt_from);
        hl.setComponentAlignment( dt_from, Alignment.TOP_RIGHT);
        hl.addComponent(dt_till);
        hl.setComponentAlignment( dt_till, Alignment.BOTTOM_RIGHT);

        addComponent(enable);
        addComponent(hl);
        setComponentAlignment(hl, Alignment.TOP_RIGHT);
        setExpandRatio(hl, 1.0f);

        updateVisiblity();
    }
    private void updateVisiblity()
    {
        dt_from.setVisible(enable.booleanValue());
        dt_till.setVisible(enable.booleanValue());
    }
    public Date getFrom()
    {
        return (Date) dt_from.getValue();
    }
    public Date getTill()
    {
        return (Date) dt_till.getValue();
    }
    public boolean isTimeValid()
    {
        return enable.booleanValue();
    }

    public String getField()
    {
        return field;
    }

}