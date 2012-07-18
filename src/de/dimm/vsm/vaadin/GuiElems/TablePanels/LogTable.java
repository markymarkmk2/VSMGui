/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.net.LogQuery;
import de.dimm.vsm.records.MessageLog;
import de.dimm.vsm.vaadin.GuiElems.Fields.ColumnGeneratorField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPAReadOnlyDateField;
import de.dimm.vsm.vaadin.GuiElems.Fields.JPATextField;
import de.dimm.vsm.vaadin.GuiElems.Table.BaseDataEditTable;
import de.dimm.vsm.vaadin.VSMCMain;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.Timer;



class LevelColumnGenerator implements Table.ColumnGenerator
{
    Label label;
    @Override
    public Component generateCell( Table source, Object itemId, Object columnId )
    {
        BeanItem it = (BeanItem) source.getItem(itemId);
        MessageLog job = (MessageLog)it.getBean();
        label = new Label(MessageLog.getErrLevelName(job.getErrLevel()));

        return label;
    }
}

class LevelField extends JPATextField implements ColumnGeneratorField
{

    public LevelField()
    {
        super("ErrLevel", "errLevel");
    }

    @Override
    public Component createGui(Object _node)
    {
        node = _node;

        MessageLog job = (MessageLog)node;
        TextField tf = new TextField("ErrLevel");
        tf.setValue(MessageLog.getErrLevelName(job.getErrLevel()));

        tf.setData(this);

        return tf;
    }

    @Override
    public ColumnGenerator getColumnGenerator()
    {
        return new LevelColumnGenerator();
    }

    @Override
    public int getWidth()
    {
        return 80;
    }

}

class MessageColumnGenerator implements Table.ColumnGenerator
{
    Label label;
    @Override
    public Component generateCell( Table source, Object itemId, Object columnId )
    {
        BeanItem it = (BeanItem) source.getItem(itemId);
        MessageLog job = (MessageLog)it.getBean();
        String txt = VSMCMain.Txt(job.getMessageId());
        if (job.getAdditionText() != null)
        {
            txt += " " + job.getAdditionText();
        }
        label = new Label(txt);

        return label;
    }
}

class MessageField extends JPATextField implements ColumnGeneratorField
{

    MessageColumnGenerator colgen;
    public MessageField()
    {
        super("Message", "messageId");
    }

    @Override
    public Component createGui(Object _node)
    {
        node = _node;

        TextField tf = new TextField("Message");
        tf.setValue( VSMCMain.Txt(((MessageLog)node).getMessageId()));

        tf.setData(this);
        tf.setWidth("" + getFieldWidth() + "px");


        return tf;
    }

    @Override
    public ColumnGenerator getColumnGenerator()
    {
        colgen = new MessageColumnGenerator();
        return colgen;
    }

    @Override
    public int getWidth()
    {
        return 100;
    }

    @Override
    public int getFieldWidth()
    {
        return 400;
    }


    @Override
    public float getExpandRatio()
    {
        return 1.0f;
    }
}


class ExceptionField extends JPATextField implements ColumnGeneratorField
{

    EmptyColumnGenerator colgen;
    public ExceptionField()
    {
        super("Exception", "exceptionName");
    }

    @Override
    public Component createGui(Object _node)
    {
        node = _node;
        MessageLog job = (MessageLog)node;

        TextField tf = new TextField("Exception");
        if (job.getExceptionName() != null)
            tf.setValue( job.getExceptionName() );

        tf.setWidth("" + getFieldWidth() + "px");
        tf.setData(this);


        return tf;
    }

    @Override
    public ColumnGenerator getColumnGenerator()
    {
        colgen = new EmptyColumnGenerator();
        return colgen;
    }

    @Override
    public int getWidth()
    {
        return 80;
    }

    @Override
    public int getFieldWidth()
    {
        return 400;
    }


}


/**
 *
 * @author Administrator
 */
public class LogTable extends BaseDataEditTable<MessageLog>
{
    
    static String dateFormat = "dd.MM.yyyy HH:mm";

    Timer timer;
    

    LoqQueryProvider provider;


    public LogTable( VSMCMain main, List<MessageLog> list, ArrayList<JPAField> _fieldList, ItemClickListener listener, LoqQueryProvider provider )
    {
        super(main, list, MessageLog.class, _fieldList, listener, false, false, /*sort ascending*/false);
        this.provider = provider;


        timer = new Timer(1000, new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                timer.stop();
                try
                {

                    requestStatus();
                }
                catch (Exception ex)
                {
                }
                timer.start();
            }
        });
    }

    public static LogTable createTable( VSMCMain main,ItemClickListener listener, LoqQueryProvider provider )
    {
        ArrayList<JPAField> fl = new ArrayList<JPAField>();
        fl.add(new JPAReadOnlyDateField(VSMCMain.Txt("Wann"), "creation", DateField.RESOLUTION_SEC));
        fl.add(new LevelField());
        fl.add(new JPATextField(VSMCMain.Txt("moduleName"), "moduleName"));

        JPATextField fld = new JPATextField(VSMCMain.Txt("MessageId"), "messageId");
        fld.setTableColumnVisible(false);
        fl.add(fld);


        fl.add( new MessageField());
        fl.add(new JPATextField(VSMCMain.Txt("additionText"), "additionText"));
        fl.add( new ExceptionField() );
        fl.add(new JPATextField(VSMCMain.Txt("ExceptionText"), "exceptionText"));
        fl.add(new JPATextField(VSMCMain.Txt("Stack"), "exceptionStack"));

        setTableColumnVisible(fl, "additionText", false);
        setTableColumnVisible(fl, "exceptionText", false);
        setTableColumnVisible(fl, "exceptionStack", false);

        setTableFieldWidth(fl, "moduleName", 400);
        setTableFieldWidth(fl, "messageId", 400);
        setTableFieldWidth(fl, "additionText", 400);
        setTableFieldWidth(fl, "exceptionText", 400);
        //setTableFieldWidth(fl, "exceptionStack", 400);
        setTableColumnExpandRatio(fl, "exceptionStack", 1.0f);

        List<MessageLog> list = new ArrayList<MessageLog>();

        return new LogTable(main, list, fl, listener, provider);

    }

    long lastCounter;
    MessageLog lastLog;
    void listLogs( int cnt, long  offsetIdx, LogQuery lq)
    {
        // QUERY HAS CHANGED -> RESET LIST
        if (!lq.equals(last_lq))
            lastLog = null;


        // LAST LOGS SINCE ONLY IF WE UPDATE ONLINE (OLDER THAN NULL)
        if (lastLog != null && lq.getOlderThan() == null)
        {
            MessageLog[] arr = main.getDummyGuiServerApi().listLogsSince( lastLog.getIdx(), lq);

            if (arr == null || arr.length == 0)
                return;

            lastLog = arr[0];

            for (int i = 0; i < arr.length; i++)
            {
                MessageLog messageLog = arr[i];
                bc.addBeanAt(i, messageLog);
            }
            return;
        }

        MessageLog[] arr = main.getDummyGuiServerApi().listLogs( cnt, offsetIdx, lq);

        bc.removeAllItems();
        lastLog = null;

        if (arr == null || arr.length == 0)
              return;

        lastLog = arr[0];

        for (int i = 0; i < arr.length; i++)
        {
            MessageLog messageLog = arr[i];
            bc.addBean(messageLog);
        }
    }



    LogQuery last_lq;

    private void requestStatus()
    {
        LogQuery lq = provider.getLogQuery();
        boolean reReadLogs = false;

        long cnt = main.getDummyGuiServerApi().getLogCounter();

        // QRY CHANGED THEN RELOAD
        if (last_lq == null || !last_lq.equals(lq))
            reReadLogs = true;

        // NEW DATA
        if (!reReadLogs && cnt != lastCounter)
        {
            // QRY ON OLD DATA, IGNORE NEW ENTRIES
            if (lq.getOlderThan() == null)
            {
                reReadLogs = true;
            }
        }
        if (!reReadLogs)
            return;



        listLogs(lq.getMaxLen(), provider.getOffset(), lq);

        lastCounter = cnt;
        last_lq = lq;
    }


    public void activate()
    {
        requestStatus();
        timer.start();
    }

    public void deactivate()
    {
        timer.stop();
    }

    @Override
    protected GenericEntityManager get_em()
    {
        return VSMCMain.get_base_util_em();
    }

    @Override
    protected MessageLog createNewObject()
    {
        return null;
    }



    boolean inside_reset;
    public void resetLog()
    {
        this.lastLog = null;
        lastCounter = -1;
        
        if (inside_reset)
            return;
        inside_reset = true;
        try
        {
            timer.stop();
            requestStatus();
            timer.start();
        }
        finally
        {
            inside_reset = false;
        }
    }


     
}
