/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.BlockStatusDlg;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.CheckObjectDlg;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPADBLinkField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Table.PreviewPanel;
import de.dimm.vsm.vaadin.VSMCMain;

/**
 *
 * @author Administrator
 */
public class StoragePoolPreviewPanel extends PreviewPanel<StoragePool>
{

    public StoragePoolPreviewPanel( StoragePoolTable pool, boolean readOnly )
    {
        super(pool, readOnly);
    }



    @Override
    public void recreateContent( final StoragePool node )
    {
        removeAllComponents();

         // CREATE GUI ITEMS
        for (int i = 0; i < table.getFieldList().size(); i++)
        {
            JPAField jPAField = table.getFieldList().get(i);

            final Component gui =  jPAField.createGui(node);
            addComponent( gui );

            if (jPAField instanceof JPADBLinkField)
            {
                JPADBLinkField linkField = (JPADBLinkField)jPAField;
                addDBLinkClickListener( gui, linkField );
            }
            if (jPAField.getFieldName().equals("mountPoint"))
            {
                jPAField.setReadOnly(gui, rdOnly || !table.isNew() );
            }
            else
            {
                jPAField.setReadOnly(gui, rdOnly);
            }
        }

        setData(node);


        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        
        Button statusBt = new Button(VSMCMain.Txt("Status_anzeigen"), new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                showBlockStatusDlg( table.getActiveElem() );
            }

        });
        Button checkPool = new Button(VSMCMain.Txt("Pool prüfen"), new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                showCheckPoolDlg( node );
            }
        });

        Button checkBootstrap = new Button(VSMCMain.Txt("Recoverystatus prüfen"), new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                showCheckBootstrap( node );
            }
        });
       
        hl.addComponent(statusBt);
        hl.addComponent(checkPool);
        hl.addComponent(checkBootstrap);
        
        addComponent(hl);
    }
    
    private void showBlockStatusDlg( StoragePool activeElem )
    {
        BlockStatusDlg dlg = new BlockStatusDlg(table.getMain(), activeElem);
        table.getApplication().getMainWindow().addWindow(dlg);
    }

    private void showCheckPoolDlg( StoragePool pool )
    {
        CheckObjectDlg dlg = new CheckObjectDlg(table.getMain(), pool, "StoragePool");
        table.getApplication().getMainWindow().addWindow(dlg);
    }

    private void showCheckBootstrap( StoragePool pool )
    {
        table.getMain().getGuiServerApi().rebuildBootstraps(table.getMain().getGuiUser().getUser(), pool);
        VSMCMain.notify(this, VSMCMain.Txt("Job gestartet"), VSMCMain.Txt("Sie können den Fortstritt des Auftrags in der JobAnzeige verfolgen"));
    }


}
