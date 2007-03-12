// Copyright (C) 2007 Google Inc.

package com.google.enterprise.connector.otex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.otex.client.Client;

/**
 * This content handler implementation uses <code>FetchVersion</code>
 * with an output stream and returns a
 * <code>ByteArrayInputStream</code> that shares a buffer with the
 * output stream passed to <code>FetchVersion</code>.
 * 
 * @see ContentHandler
 */
class ByteArrayContentHandler implements ContentHandler {
    /** The connector contains configuration information. */
    private LivelinkConnector connector;

    /** The client provides access to the server. */
    private Client client;

    ByteArrayContentHandler() {
    }

    /** {@inheritDoc} */
    public void initialize(LivelinkConnector connector, Client client,
            Logger logger) throws RepositoryException {
        this.connector = connector;
        this.client = client;
    }
    
    /** {@inheritDoc} */
    public InputStream getInputStream(final Logger logger,
            int volumeId, int objectId, int versionNumber, int size)
            throws RepositoryException {
        try {
            SharedByteArrayOutputStream out =
                new SharedByteArrayOutputStream(size);
            client.FetchVersion(logger, volumeId, objectId, versionNumber,
                out);
            return out.getInputStream();
        } catch (IOException e) {
            throw new LivelinkException(e, logger);
        }
    }


    /**
     * This implementation adds a {@link #getInputStream} method that
     * shares the buffer with this output stream. The input stream
     * cannot be obtained until the output stream is closed.
     */
    private static class SharedByteArrayOutputStream
            extends ByteArrayOutputStream {
        public SharedByteArrayOutputStream() {
            super();
        }

        public SharedByteArrayOutputStream(int size) {
            super(size);
        }

        /** Is this output stream open? */
        private boolean isOpen = true;

        /** Marks this output stream as closed. */
        public void close() throws IOException {
            isOpen = false;
            super.close();
        }

        /*
         * Gets a <code>ByteArrayInputStream</code> that shares the
         * output buffer, without copying it.
         *
         * @return a <code>ByteArrayInputStream</code>
         * @throws IOException if the output stream is open
         */
        public ByteArrayInputStream getInputStream() throws IOException {
            if (isOpen)
                throw new IOException("Output stream is open.");
            return new ByteArrayInputStream(buf, 0, count);
        }
    }
}