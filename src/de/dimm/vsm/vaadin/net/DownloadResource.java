/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;

import com.vaadin.Application;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.StreamResource;
import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import java.util.Collection;

@SuppressWarnings("serial")
public class DownloadResource extends StreamResource
{
    static
    {
        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
    }

    private final String filename;

    public DownloadResource( File fileToDownload, Application application )
            throws FileNotFoundException
    {
        super(new FileStreamResource(fileToDownload), fileToDownload.getName(),
                application);

        this.filename = fileToDownload.getName();
    }

    public DownloadResource( InputStream is, String name, Application application )            
    {
        super(new InputStreamResource(is), name,
                application);

        this.filename = name;
    }

    @Override
    public DownloadStream getStream()
    {
        MimeType mt = new MimeType("application/octet-stream");
        Collection<MimeType> col = MimeUtil.getMimeTypes(filename);
        if (!col.isEmpty())
            mt = col.iterator().next();


        DownloadStream stream = new DownloadStream(getStreamSource().getStream(), mt.getMediaType(), filename);
        stream.setParameter("Content-Disposition", "attachment;filename="
                + filename);
        return stream;
    }

    private static class FileStreamResource implements StreamResource.StreamSource, Serializable
    {

        private final InputStream inputStream;

        public FileStreamResource( File fileToDownload )
                throws FileNotFoundException
        {
            inputStream = new MyFileInputStream(fileToDownload);
        }

        @Override
        public InputStream getStream()
        {
            return inputStream;
        }

        public class MyFileInputStream extends FileInputStream implements Serializable
        {

            public MyFileInputStream( File fileToDownload ) throws FileNotFoundException
            {
                super(fileToDownload);
            }
        }
    }

    private static class InputStreamResource implements StreamResource.StreamSource, Serializable
    {
        private final InputStream inputStream;

        public InputStreamResource( InputStream inputStream )
        {
            this.inputStream = inputStream;
        }

        @Override
        public InputStream getStream()
        {
            return inputStream;
        }
    }
}
