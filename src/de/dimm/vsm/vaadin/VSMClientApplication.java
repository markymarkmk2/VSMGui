package de.dimm.vsm.vaadin;

import com.vaadin.Application;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import eu.livotov.tpt.TPTApplication;
import javax.servlet.http.HttpServletRequest;

public class VSMClientApplication extends TPTApplication implements ClickListener 
{
    private static final long serialVersionUID = 1L;


    boolean isLoggedin;
    VSMCMain main;
    
    @Override
    public void applicationInit() 
    {

        initClientMain();

    }
    public void initClientMain()
    {
        setTheme("vsm");
       
        main = new VSMCMain(this);

        main.createMenu();
        main.createHeader();
        main.initMainScreen();
        
        setMainWindow(main.getRoot());

        if (!main.isLoggedIn())
        {
            main.checkLogin( new ClickListener()
            {
                @Override
                public void buttonClick( ClickEvent event )
                {
                   // main.setMainComponent( main.btstatus.getPanel());
                }
            });
        }


    }

    @Override
    public void firstApplicationStartup()
    {
        // TODO Auto-generated method stub
        System.out.println("firstApplicationStartup");
        
    }

    @Override
    public void transactionStart( Application application, Object transactionData )
    {
        if (transactionData instanceof HttpServletRequest)
        {
            HttpServletRequest httpServletRequest = (HttpServletRequest) transactionData;
            String ip = httpServletRequest.getRemoteAddr();
            String host = httpServletRequest.getRemoteHost();

            String request = httpServletRequest.getRequestURI();
            int idx = request.lastIndexOf('/');
            if (idx > 0)
                request = request.substring(0, idx);
            if (request.equals(httpServletRequest.getServletPath()) )
            {
                String qry = httpServletRequest.getQueryString();
                main.setClient(  ip, host, qry );
            }

        }

        super.transactionStart(application, transactionData);
        //System.out.println("transactionStart");
    }

    @Override
    public void transactionEnd( Application application, Object o )
    {
        super.transactionEnd(application, o);
        //System.out.println("transactionEnd");
    }


    @Override
    public void buttonClick(ClickEvent event)
    {
        // TODO Auto-generated method stub
        
    }    
}
