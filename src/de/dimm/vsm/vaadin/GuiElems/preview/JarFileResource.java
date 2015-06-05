/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.preview;

import com.vaadin.Application;
import com.vaadin.service.FileTypeResolver;
import com.vaadin.terminal.ApplicationResource;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.Terminal;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 *
 * @author Administrator
 */
public class JarFileResource implements ApplicationResource {

    String path;
    String filename;
    Application app;

    public JarFileResource( String path, String filename, Application app ) {
        this.path = path;
        this.filename = filename;
        this.app = app;
        app.addResource(this);
    }

    @Override
    public DownloadStream getStream() {
        try {
            final DownloadStream ds = new DownloadStream(this.getClass().getResourceAsStream(path + filename), getMIMEType(), getFilename());
            ds.setParameter("Content-Length", String.valueOf(getRsrcLength()));

            ds.setCacheTime(getCacheTime());
            return ds;
        }
        catch (final Exception e) {
            // Log the exception using the application error handler
            getApplication().getErrorHandler().terminalError(new Terminal.ErrorEvent() {
                @Override
                public Throwable getThrowable() {
                    return e;
                }
            });

            return null;
        }
    }

    @Override
    public Application getApplication() {
        return app;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public long getCacheTime() {
        return DownloadStream.DEFAULT_CACHETIME;
    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public String getMIMEType() {
        return FileTypeResolver.getMIMEType(filename);
    }

    long getRsrcLength() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[4096];

        try (InputStream is = this.getClass().getResourceAsStream(path + filename)) {
            while (true) {
                int rlen = is.read(buff);
                if (rlen < 0) {
                    break;
                }
                baos.write(buff, 0, rlen);
            }
            baos.close();
        }
        catch (Exception exc) {
        }
        return baos.size();
    }
}
