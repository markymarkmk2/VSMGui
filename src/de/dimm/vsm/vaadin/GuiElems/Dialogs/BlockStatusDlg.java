/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.Dialogs;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.Utilities.SizeStr;
import de.dimm.vsm.net.PoolStatusResult;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.vaadin.VSMCMain;



/**
 *
 * @author Administrator
 */
public class BlockStatusDlg extends Window
{

     StoragePool pool;
     VSMCMain main;
     
     VerticalLayout vl = new VerticalLayout();
    

    TextField ddTotalBlocks;
    TextField dedupDataLen;
    TextField actFsDataLen;
    TextField actFileCnt;
    TextField totalFsDataLen;
    TextField totalFileCnt;
    TextField removeDDCnt;
    TextField removeDDLen;
    TextField dedupRatio;
    TextField totalDedupRatio;


    public BlockStatusDlg( VSMCMain main, StoragePool pool )
    {
        this.main = main;
        this.pool = pool;

        build_gui();

        updateContent();
    }
    

    final void build_gui( )
    {
        addComponent(vl);


        setModal(true);
        setStyleName("vsm");

        //this.setSizeFull();
        this.setWidth("400px");

        vl.setSpacing(true);
        vl.setSizeFull();
        vl.setImmediate(true);
        vl.setStyleName("editWin");

        this.setCaption(VSMCMain.Txt("Block_Informationen_für_StoragePool") + " " + pool.getName());

        ddTotalBlocks = new TextField(VSMCMain.Txt("Anzahl Blöcke insg."));
        vl.addComponent(ddTotalBlocks);

        dedupDataLen = new TextField(VSMCMain.Txt("Speicherplatz Blöcke"));
        vl.addComponent(dedupDataLen);

        actFsDataLen = new TextField(VSMCMain.Txt("Aktuelle Größe Dateisystem"));
        vl.addComponent(actFsDataLen);

        actFileCnt = new TextField(VSMCMain.Txt("Aktuelle Anzahl Dateien/Verzeichnise"));
        vl.addComponent(actFileCnt);

        totalFileCnt = new TextField(VSMCMain.Txt("Anzahl Dateien/Verzeichnise inkl. Historie"));
        vl.addComponent(totalFileCnt);

        totalFsDataLen = new TextField(VSMCMain.Txt("Größe Dateisystem inkl. Historie"));
        vl.addComponent(totalFsDataLen);

        removeDDCnt = new TextField(VSMCMain.Txt("Freie Blöcke insg."));
        vl.addComponent(removeDDCnt);

        removeDDLen = new TextField(VSMCMain.Txt("Speicherplatz freie Blöcke"));
        vl.addComponent(removeDDLen);

        dedupRatio = new TextField(VSMCMain.Txt("DedupRatio aktuelles Dateisystem."));
        vl.addComponent(dedupRatio);

        totalDedupRatio = new TextField(VSMCMain.Txt("DedupRatio Dateisystem inkl. Historie"));
        vl.addComponent(totalDedupRatio);


        Button calc = new Button(VSMCMain.Txt("Berechnen (dauert einige Zeit)"));
        vl.addComponent(calc);

        Button removeBlocks = new Button(VSMCMain.Txt("Freie Blöcke entfernen"));
        vl.addComponent(removeBlocks);

        Button close = new Button(VSMCMain.Txt("Ok"));

        vl.addComponent(close);
        vl.setComponentAlignment(close, Alignment.BOTTOM_RIGHT);

        final Window w = this;
        close.addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                event.getButton().getApplication().getMainWindow().removeWindow(w);
            }
        });

        calc.addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                calcStatus();
            }
        });
        removeBlocks.addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                removeBlocks();
            }
        });
    }

    private void updateContent()
    {
        PoolStatusResult psr = (PoolStatusResult)VSMCMain.callLogicControl("getPoolStatusResult", pool);
        if (psr == null)
        {
            VSMCMain.notify(vl, "Fehler beim Abholen der Statusergebnisse", pool.getName());
            return;
        }
        
        setResult( ddTotalBlocks, psr.getDdTotalBlocks() );
        setResult( dedupDataLen, psr.getDedupDataLen() );
        setResult( actFsDataLen, psr.getActFsDataLen() );
        setResult( actFileCnt, psr.getActFileCnt() );
        setResult( totalFsDataLen, psr.getTotalFsDataLen() );
        setResult( totalFileCnt, psr.getTotalFileCnt() );
        setResult( removeDDCnt, psr.getRemoveDDCnt() );
        setResult( removeDDLen, psr.getRemoveDDLen() );
        setResult( dedupRatio, psr.getDedupRatio(), true );
        setResult( totalDedupRatio, psr.getTotalDedupRatio(), true );

    }

    void calcStatus()
    {

       main.runInBusyCancel("Berechnung läuft", new Runnable() {

            @Override
            public void run()
            {
                VSMCMain.callLogicControl("calcPoolStatusResult", pool);
                updateContent();
            }
        }, new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                VSMCMain.callLogicControl("abortCalcPoolStatusResult", pool);
            }
        } );

    }

    void removeBlocks()
    {

       main.runInBusyCancel("Bereinigen läuft", new Runnable() {

            @Override
            public void run()
            {
                Object o = VSMCMain.callLogicControl("deleteFreeBlocks", pool);
                if (o != null && o instanceof Long)
                {
                    Long l = (Long)o;
                    main.Msg().info("Es wurden insgesamt " + l.longValue() + " Blöcke entfernt" , null);

                    if (l.longValue() > 0)
                    {
                        calcStatus();
                    }
                }
            }
        }, new Button.ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                VSMCMain.callLogicControl("abortDeleteFreeBlocks");
            }
        } );
    }

   


    private void setResult( TextField tf, long ddTotalBlocks0 )
    {
        setResult(tf, ddTotalBlocks0, false);
    }
    private void setResult( TextField tf, long ddTotalBlocks0, boolean percent )
    {
        String s = "-";
        if (ddTotalBlocks0 > 0)
        {
            if (percent)
                s = Long.toString(ddTotalBlocks0) + "%";
            else
                s = SizeStr.format(ddTotalBlocks0 ) + " (" + Long.toString(ddTotalBlocks0) + ")";
        }
        tf.setValue( s );        
        tf.setWidth("100%");
    }

}
