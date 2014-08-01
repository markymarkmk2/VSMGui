/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import de.dimm.vsm.Utilities.SizeStr;
import de.dimm.vsm.records.Retention;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Table.PreviewPanel;
import de.dimm.vsm.vaadin.GuiElems.VaadinHelpers;
import de.dimm.vsm.vaadin.VSMCMain;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class PositiveIntegerValidator extends IntegerValidator
{

    PositiveIntegerValidator( String msg )
    {
        super(msg);
    }

    @Override
    protected boolean isValidString( String value )
    {
        try
        {
            int i = Integer.parseInt(value);
            if (i >= 0)
            {
                return true;
            }
            return false;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}

/**
 *
 * @author Administrator
 */
public class RetentionPreviewPanel extends PreviewPanel<Retention>
{

    CheckBox checkBoxDisabled;
    CheckBox checkBoxNeg;
    ComboBox comboRetentionDim;
    TextField retentionDuration;
    TextField retentionSize;
    TextField retentionName;
    ComboBox comboArgField;
    ComboBox comboOperators;
    List<ComboEntry> opList;
    ComboBox comboSizeDim;
    List<ComboEntry> dimList;
    DateField date;
    HorizontalLayout valEntryLayout;
    RetentionTable rt;
    

    public RetentionPreviewPanel( RetentionTable j, boolean readOnly )
    {
        super(j, readOnly);
        rt = j;
    }

    @Override
    public void recreateContent( final Retention node )
    {
        removeAllComponents();

        dimList = new ArrayList<ComboEntry>();
        String[] dimArray = SizeStr.getDimArray();
        for (int i = 0; i < dimArray.length; i++)
        {
            String dim = dimArray[i];
            dimList.add(new ComboEntry(dim, dim));
        }
        comboSizeDim = new ComboBox("", dimList);
        comboSizeDim.setWidth("50px");
        comboSizeDim.setNullSelectionAllowed(false);
        comboSizeDim.setImmediate(true);


        checkBoxDisabled = new CheckBox(Txt("Gesperrt"));
        checkBoxNeg = new CheckBox(Txt("Negiert"));



        date = new DateField(Txt("Datum"));
        date.setDateFormat( "dd.MM.yyyy HH.mm");
        retentionDuration = new TextField(Txt("Wert"));
        retentionDuration.setWidth("50px");
        retentionSize = new TextField(Txt("Größe"));
        retentionSize.setWidth("50px");
        retentionName = new TextField(Txt("name"));

        comboRetentionDim = new ComboBox(Txt("Dimension"), RetentionTable.retentionDurationDim);
        comboRetentionDim.setNullSelectionAllowed(false);
        comboRetentionDim.setWidth("100px");

        comboArgField = new ComboBox(Txt("Argument"), RetentionTable.argFieldList);
        comboArgField.setNullSelectionAllowed(false);
        comboArgField.setWidth("200px");
        comboArgField.addListener(new ValueChangeListener()
        {

            @Override
            public void valueChange( ValueChangeEvent event )
            {
                updateValEntry(getGuiArgType());
            }
        });

        valEntryLayout = new HorizontalLayout();
        valEntryLayout.setSpacing(true);





        // ADD TO LAYOUT
        JPAField disabled = table.getField("disabled");
        addComponent(disabled.createGui(node));
        
        JPAField mode = table.getField("mode");
        addComponent(mode.createGui(node));



        // GET FIELDS FROM REGULAR TABLE FIELDLIST
        JPAField txtName = table.getField("name");
        addComponent(txtName.createGui(node));

        JPAField txtCreation = table.getField("creation");
        addComponent(txtCreation.createGui(node));

        addComponent(valEntryLayout);

        JPAField action = table.getField("followAction");
        addComponent(action.createGui(node));


        JPAField neg = table.getField("neg");
        addComponent(neg.createGui(node));


        // HAS TO BE AFTER createGui
        //JPAField.addValidator(this, new StringLengthValidator(X("Bitte geben Sie eine Bezeichnung ein"), 1, 255, false));
        retentionDuration.addValidator(new PositiveIntegerValidator(Txt("Bitte geben Sie gültige Zahl ein")));
        retentionSize.addValidator(new PositiveIntegerValidator(Txt("Bitte geben Sie gültige Zahl ein")));
        retentionName.addValidator(new StringLengthValidator(Txt("Bitte geben Sie eine Bezeichnung ein"), 1, 255, false));


        //txtPwd.addValidator( new StringLengthValidator(X("Bitte geben Sie das Passwort ein"), 1, 512, false));


        updateValEntry( node.getArgType() );
        setData(node);
    }

    String getGuiArgType()
    {
        int argidx = VaadinHelpers.getSelectedIndex(comboArgField);
        ComboEntry arg = RetentionTable.argFieldList.get(argidx);
        String argType = arg.getDbEntry().toString();
        return argType;
    }

    void updateValEntry(  String argType )
    {
        valEntryLayout.removeAllComponents();
        valEntryLayout.setImmediate(true);

        
        opList = RetentionTable.getOperationComboList(argType);

        if (argType == null)
            return;

        comboOperators = new ComboBox(Txt("Operator"), opList);
        comboOperators.setNullSelectionAllowed(false);
        comboOperators.setWidth("80px");
        comboOperators.setImmediate(true);




        if (Retention.isRelTSField(argType))
        {
            valEntryLayout.addComponent(comboArgField);
            valEntryLayout.addComponent(comboOperators);
            valEntryLayout.addComponent(retentionDuration);
            valEntryLayout.addComponent(comboRetentionDim);
        }

        if (Retention.isDateField(argType))
        {
            valEntryLayout.addComponent(comboArgField);
            valEntryLayout.addComponent(comboOperators);
            valEntryLayout.addComponent(date);
        }
        if (Retention.isSizeField(argType))
        {
            valEntryLayout.addComponent(comboArgField);
            valEntryLayout.addComponent(comboOperators);
            valEntryLayout.addComponent(retentionSize);
            valEntryLayout.addComponent(comboSizeDim);
        }
        if (Retention.isNameField(argType))
        {
            valEntryLayout.addComponent(comboArgField);
            valEntryLayout.addComponent(comboOperators);
            valEntryLayout.addComponent(retentionName);
        }
    }

    @Override
    public void attach()
    {
        Retention node = (Retention) getData();
        super.attach();
        setValues(node);
    }

    private static String Txt( String key )
    {
        return VSMCMain.Txt(key);
    }

    private void setValues( Retention r )
    {
        String argType = r.getArgType();

        for (int i = 0; i < RetentionTable.argFieldList.size(); i++)
        {
            ComboEntry ce = RetentionTable.argFieldList.get(i);
            if (ce.getDbEntry().equals(argType))
            {
                VaadinHelpers.setSelectedIndex(comboArgField, i);
                break;
            }
        }
        for (int i = 0; i < opList.size(); i++)
        {
            ComboEntry ce = opList.get(i);
            if (ce.getDbEntry().equals(r.getArgOp()))
            {
                VaadinHelpers.setSelectedIndex(comboOperators, i);
                break;
            }
        }

        if (argType == null)
            return;

        if (Retention.isRelTSField(argType))
        {
            String arg = r.getArgValue();
            long ts = Long.parseLong(arg);
            String dim = Retention.getNormRelSecondsDim(ts / 1000);

            for (int i = 0; i < RetentionTable.retentionDurationDim.size(); i++)
            {
                ComboEntry ce = RetentionTable.retentionDurationDim.get(i);
                if (ce.getDbEntry().equals(dim))
                {
                    VaadinHelpers.setSelectedIndex(comboRetentionDim, i);
                    retentionDuration.setValue(Retention.getNormRelSeconds(ts / 1000));
                    break;
                }
            }
        }
        if (Retention.isDateField(argType))
        {
            String arg = r.getArgValue();
            long ts = Long.parseLong(arg);
            Date d = new Date(ts);
            date.setValue(d);
        }

        if (Retention.isSizeField(argType))
        {
            String arg = r.getArgValue();
            long size = Long.parseLong(arg);

            int normSize = SizeStr.getNormSize(size);
            String dim = SizeStr.getNormSizeDim(size);

            for (int i = 0; i < dimList.size(); i++)
            {
                ComboEntry ce = dimList.get(i);
                if (ce.getDbEntry().equals(dim))
                {
                    VaadinHelpers.setSelectedIndex(comboSizeDim, i);
                    retentionSize.setValue(normSize);
                    break;
                }
            }
        }
        if (Retention.isNameField(argType))
        {
            retentionName.setValue(r.getArgValue());
        }
    }

    void updateObject( Retention r )
    {
        String argType = getGuiArgType();

        int opidx = VaadinHelpers.getSelectedIndex(comboOperators);
        
        ComboEntry op = opList.get(opidx);

        if (Retention.isRelTSField(argType))
        {
            int idx = VaadinHelpers.getSelectedIndex(comboRetentionDim);
            String dim = RetentionTable.retentionDurationDim.get(idx).getDbEntry().toString();
            String niceTxt = retentionDuration.getValue().toString() + " " + dim;
            long s = Retention.getSecondsfromNiceText(niceTxt);


            r.setArgValue(Long.toString(s * 1000));
            r.setArgType(argType);
            r.setArgOp(op.getDbEntry().toString());
        }
        if (Retention.isDateField(argType))
        {
            Date d = (Date) date.getValue();
            r.setArgValue(Long.toString(d.getTime()));
            r.setArgType(argType);
            r.setArgOp(op.getDbEntry().toString());
        }
        if (Retention.isSizeField(argType))
        {
            int idx = VaadinHelpers.getSelectedIndex(comboSizeDim);
            String dim = dimList.get(idx).getDbEntry().toString();
            long normSize = Long.parseLong(retentionSize.getValue().toString());
            long size = SizeStr.getSizeFromNormSize(normSize + " " + dim);
            r.setArgValue(Long.toString(size));
            r.setArgType(argType);
            r.setArgOp(op.getDbEntry().toString());
        }
        if (Retention.isNameField(argType))
        {
            r.setArgValue(retentionName.getValue().toString());
            r.setArgType(argType);
            r.setArgOp(op.getDbEntry().toString());
        }

    }

}
