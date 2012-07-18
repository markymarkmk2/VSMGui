/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.TablePanels;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.NativeButton;
import de.dimm.vsm.mail.NotificationEntry;
import de.dimm.vsm.mail.SmtpNotificationServer;
import de.dimm.vsm.records.MailGroup;
import de.dimm.vsm.records.SmtpLoginData;
import de.dimm.vsm.vaadin.GuiElems.Dialogs.TextFieldDlg;
import de.dimm.vsm.vaadin.GuiElems.Table.PreviewPanel;
import de.dimm.vsm.vaadin.VSMCMain;
import java.net.ConnectException;

/**
 *
 * @author Administrator
 */
public class SmtpDataPreviewPanel extends PreviewPanel<SmtpLoginData>
{

    public SmtpDataPreviewPanel( SmtpDataTable j, boolean readOnly )
    {
        super(j, readOnly);
    }



    @Override
    public void recreateContent( final SmtpLoginData node )
    {
        super.recreateContent( node );


        NativeButton bt = new NativeButton( VSMCMain.Txt("Testmail"), new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                sendTestMail();
            }
        });
        
        addComponent(bt);

    }

    String lastemail = "";
    void sendTestMail()
    {
        final NotificationEntry noentry  = new NotificationEntry("TEST", VSMCMain.Txt("Dies ist eine TestMail"), "Blah", NotificationEntry.Level.INFO, false);
        final MailGroup group = new MailGroup();
        group.setSmtpdata(table.getActiveElem());
        final Component _this = this;

        final TextFieldDlg dlg = new TextFieldDlg("Senden an", "Emailadresse", lastemail, null);
        dlg.setOkActionListener(new ClickListener() {

            @Override
            public void buttonClick( ClickEvent event )
            {
                group.setEmailText(dlg.getText());
                lastemail = dlg.getText();
                SmtpNotificationServer smtpServer = (SmtpNotificationServer)VSMCMain.getNotificationServer();
                try
                {
                    smtpServer.rawFireNotification(noentry, group, "", null);
                    VSMCMain.notify(_this, "Die Testmail wurde erfolgreich versendet", "" );
                }
                catch (Exception e)
                {
                    if (e.getCause() instanceof ConnectException)
                    {
                        VSMCMain.notify(_this, "Die Anmeldung beim SMTP-Server schlug fehl:", e.getMessage() );
                    }
                    else
                    {
                        VSMCMain.notify(_this, "Das Versenden schlug fehl:", e.getMessage() );
                    }
                }
            }
        });
        this.getApplication().getMainWindow().addWindow(dlg);

    }
    


}
