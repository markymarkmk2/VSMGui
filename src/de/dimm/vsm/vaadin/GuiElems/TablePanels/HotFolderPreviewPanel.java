/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;
import de.dimm.vsm.MMapi.MMAnswer;
import de.dimm.vsm.MMapi.MMapi;
import de.dimm.vsm.Utilities.ParseToken;
import de.dimm.vsm.records.HotFolder;
import de.dimm.vsm.vaadin.GuiElems.ComboEntry;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.MMImportSelectDlg;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPACheckBox;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAComboField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADBLinkField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.PreviewPanel;
import de.dimm.vsm.vaadin.VSMCMain;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class HotFolderPreviewPanel extends PreviewPanel<HotFolder>
{
    HorizontalLayout hlMM;

    public HotFolderPreviewPanel( HotFolderTable j, boolean readOnly )
    {
        super(j, readOnly);
        setWidth("100%");
    }

    

    int getActualPort()
    {
        JPATextField portfield = (JPATextField)table.getField("port");
        try
        {
            return Integer.parseInt(portfield.getGuiValue(this));
        }
        catch (NumberFormatException numberFormatException)
        {
        }
        return 0;
    }

    @Override
    public void recreateContent( final HotFolder node )
    {
        removeAllComponents();

         // CREATE GUI ITEMS
        for (int i = 0; i < table.getFieldList().size(); i++)
        {
            JPAField jPAField = table.getFieldList().get(i);

            if (jPAField.getFieldName().equals("mmVerify") || jPAField.getFieldName().equals("mmMediaType") ||
                    jPAField.getFieldName().equals("mmIP") || jPAField.getFieldName().equals("mmMountPath")  )
                continue;

            final Component gui =  jPAField.createGui(node);
            addComponent( gui );

            if (jPAField instanceof JPADBLinkField)
            {
                JPADBLinkField linkField = (JPADBLinkField)jPAField;
                addDBLinkClickListener( gui, linkField );
            }
            jPAField.setReadOnly(gui, rdOnly);
        }


        setData(node);


        hlMM = new HorizontalLayout();
        hlMM.setImmediate(true);
        hlMM.setSpacing(true);
        // BOTTOM MARGIN
        hlMM.setMargin(false, false, true, false);

        setMMVisible(node, node.isMmArchive());
        
        // SET CALLBACK FOR VISIBILITY MM-PARAMS ON/OFF
        JPACheckBox mmArchive = (JPACheckBox)table.getField("mmArchive");
        mmArchive.getCheckBox(this).addListener(  new ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                setMMVisible( node, ((CheckBox)event.getSource()).booleanValue() );
            }
        });

        


        // ADD MM-PANEL TO PREVIEW
        addComponent(hlMM);


        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);

        NativeButton importNode = new NativeButton(VSMCMain.Txt("MediaManager Archiv imortieren"), new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                importMMArchiv( node );
            }
        });
        hl.addComponent(importNode);

        addComponent(hl);

      
    }

    void setMMVisible( final HotFolder node, boolean b )
    {
        if (!b)
        {
            hlMM.setVisible(false);
            hlMM.removeAllComponents();
            return;
        }
        setMMMediaTypes( node );

        VerticalLayout mmvl = new VerticalLayout();
        hlMM.addComponent(mmvl);

        HorizontalLayout hl1 = new HorizontalLayout();
        Component c = table.getField("mmMediaType").createGui(node);
        hl1.addComponent(c);
        hl1.setComponentAlignment(c, Alignment.BOTTOM_LEFT);
        hl1.setSpacing(true);
        
        c = table.getField("mmVerify").createGui(node);
        hl1.addComponent(c);
        hl1.setComponentAlignment(c, Alignment.BOTTOM_CENTER);

        mmvl.addComponent(hl1);

        HorizontalLayout hl2 = new HorizontalLayout();
        hl2.setSpacing(true);
        c = table.getField("mmIP").createGui(node);
        hl2.addComponent(c);
        hl2.setComponentAlignment(c, Alignment.BOTTOM_LEFT);
        c = table.getField("mmMountPath").createGui(node);
        hl2.addComponent(c);
        hl2.setComponentAlignment(c, Alignment.BOTTOM_RIGHT);
        hl2.setSizeFull();

        mmvl.addComponent(hl2);

        NativeButton btCheckMMOnline = new NativeButton(VSMCMain.Txt("Test MM-Verbindung") , new ClickListener()
        {
            @Override
            public void buttonClick( ClickEvent event )
            {
                String ip = getMMIP(node);
                checkMMOnline( ip, 11112 );
            }

        });
        hl1.addComponent(btCheckMMOnline);
        hl1.setComponentAlignment(btCheckMMOnline, Alignment.BOTTOM_RIGHT);

        hlMM.setVisible(true);

    }
    String getMMIP(final HotFolder node)
    {
        String ip = null;
        JPAField jPAField = table.getField("mmIP");
        if (jPAField instanceof JPATextField)
        {
            JPATextField tf = (JPATextField) jPAField;
            ip = tf.getGuiValue(this);
        }
        if (ip != null && !ip.isEmpty())
            return ip;

        ip = node.getMmIP();

        if (ip != null && !ip.isEmpty())
            return ip;
        

        jPAField = table.getField("ip");
        JPATextField tf = (JPATextField) jPAField;
        ip = tf.getGuiValue(this);

        return ip;
    }

    static List<ComboEntry> getMMMediaTypes(String actualIp, int actualPort)
    {
        MMapi api = new MMapi(actualIp, actualPort);
        try
        {
            api.connect();
            if (api.isConnected())
            {
                MMAnswer ma = api.sendMM("list_media_types");
                if (ma.getCode() == 0)
                {
                    List<ComboEntry> ceList = new ArrayList<ComboEntry>();
                    List<String> list = MMapi.getAnswerList(ma);
                    for (int i = 0; i < list.size(); i++)
                    {
                        String string = list.get(i);
                        if (string.isEmpty())
                            continue;
                        
                        ParseToken pt = new ParseToken(string);
                        ComboEntry ce = new ComboEntry(pt.GetString("NA:"), pt.GetString("DE:"));
                        ceList.add(ce);
                    }
                    return ceList;
                }
            }
        }
        catch (IOException iOException)
        {
        }
        finally
        {
            api.disconnect();
        }
        
        return null;

    }

    void setMMMediaTypes(HotFolder node)
    {
        String ip = getMMIP(node);
        if (ip == null || ip.isEmpty())
            return;
        
         List<ComboEntry> entries = getMMMediaTypes(ip, 11112);
         if (entries == null)
         {
             VSMCMain.notify(this, VSMCMain.Txt("MediaManger ist nicht online"),
                     VSMCMain.Txt("Bitte prüfen Sie, ob der MM-Dienst auf dem Rechner gestartet ist") + ": "
                     + ip + ":" + 11112);
             return;
        }

         JPAField jPAField = table.getField("mmMediaType");
         if (jPAField instanceof JPAComboField)
         {
             JPAComboField cbf = (JPAComboField) jPAField;
             cbf.setEntries(entries, this);
             ComboBox cb = (ComboBox)cbf.getGuiforField(this);
             if (cb != null)
             {
                 Property p = cb.getPropertyDataSource();
                 for (int i = 0; i < entries.size(); i++)
                 {
                     ComboEntry comboEntry = entries.get(i);
                     if (p.getValue() != null && p.getValue().equals(comboEntry.getDbEntry()))
                     {
                         cb.setValue(comboEntry.getGuiEntryKey());
                         break;
                     }
                 }
             }
        }
    }
    
    private void checkMMOnline( String actualIp, int actualPort )
    {
        MMapi api = new MMapi(actualIp, actualPort);
        try
        {
            api.connect();
            if (api.isConnected())
            {
                VSMCMain.info(this, VSMCMain.Txt("MediaManger ist online"), "");
                return;
            }
        }
        catch (IOException iOException)
        {
        }
        finally
        {
            api.disconnect();
        }
        VSMCMain.notify(this, VSMCMain.Txt("MediaManger ist nicht online"), VSMCMain.Txt("Bitte prüfen Sie, ob der MM-Dienst auf dem Rechner gestartet ist") + ": " + actualIp + ":" + actualPort);
    }

    void importMMArchiv( final HotFolder node )
    {
        final MMImportSelectDlg dlg = new MMImportSelectDlg( node);
        final Component c = this;

        Button.ClickListener okListener = new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                long from = dlg.getFrom();
                long till = dlg.getTill();
                if (till < from)
                    till = from;
                
                boolean deleteted = dlg.isImportDeleted();
                try
                {
                    ((HotFolderTable) table).importMMArchiv(node, from, till, deleteted);
                    VSMCMain.info(c, VSMCMain.Txt("Der Import wurde gestartet"), VSMCMain.Txt("Details zum Import unter Jobs"));
                }
                catch (Exception exception)
                {
                    VSMCMain.notify(c, VSMCMain.Txt("Der Import schlug fehl"), exception.getMessage());
                }
            }
        };

        dlg.setOkListener(okListener);

        this.getApplication().getMainWindow().addWindow(dlg);

    }


}
