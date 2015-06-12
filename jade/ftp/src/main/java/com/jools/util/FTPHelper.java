package com.jools.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

public class FTPHelper {

    private static final Logger logger = Logger.getLogger(FTPHelper.class);
    private final String username;
    private final String password;
    private final InetAddress host;
    private final FTPClient ftp;
    private final boolean verbose;

    private FTPHelper(String username, String password, InetAddress host, boolean verbose) {
        this.ftp = new FTPClient();
        this.username = username;
        this.password = password;
        this.host = host;
        this.verbose = verbose;
    }

    public static FTPHelper getInstance(String username, String password, InetAddress host, boolean verbose) {
        return new FTPHelper(username, password, host, verbose);
    }

    public boolean upload(File src, String dst) {
        //TODO gestion du paramètre dst.
        if (this.verbose) {
            ftp.addProtocolCommandListener(new ProtocolCommandListener() {

                @Override
                public void protocolCommandSent(ProtocolCommandEvent event) {
                    logger.info(event.getMessage());
                }

                @Override
                public void protocolReplyReceived(ProtocolCommandEvent event) {
                    logger.info(event.getMessage());
                }
            });
        }
        try {
            this.connect();
            if (this.ftp.login(this.username, this.password)) {

                if (this.verbose) {
                    logger.info("Remote system is " + ftp.getSystemType());
                }

                this.ftp.setFileType(FTP.BINARY_FILE_TYPE);
                this.ftp.enterLocalPassiveMode();

                if (src.isFile()) {
                    this.storeFile(src);
                } else {
                    this.storeDirectory(src);
                }
            }
            this.ftp.logout();
            return true;
        } catch (IOException e) {
            logger.fatal("Problem during FTP upload", e);
            return false;
        } finally {
            disconnect();
        }
    }

    private void disconnect() {
        if (this.ftp.isConnected()) {
            try {
                this.ftp.disconnect();
                if (this.verbose) {
                    logger.info("Disconnected from " + this.host);
                }
            } catch (IOException f) {
                logger.warn("Pb during disconnection from " + this.host, f);
            }
        }
    }

    private void storeDirectory(File directory) throws IOException {
        Collection<File> files = Arrays.asList(directory.listFiles());
        if (files.isEmpty()) {
            throw new IOException("Failed to list contents of " + directory);
        }
        for (File file : files) {
            if (file.isDirectory()) {
                this.ftp.makeDirectory(file.getName());
                if (!this.ftp.changeWorkingDirectory(file.getName())) {
                    throw new IOException("CWD " + file.getName() + ": 550 Failed to change directory.");
                }
                storeDirectory(file);
                this.ftp.changeToParentDirectory();
            } else {
                this.storeFile(file);
            }
        }
    }

    private void storeFile(File file) throws IOException {
        InputStream input = null;
        try {
            input = new FileInputStream(file);
            if (!this.ftp.storeFile(file.getName(), input)) {
                throw new IOException("STOR " + file.getName() + ": 553 Could not create file.");
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException f) {
                }
                input = null;
            }
        }
    }

    private void connect() throws IOException {
        try {
            this.ftp.connect(this.host);

            if (this.verbose) {
                logger.info("Connected to " + this.host + ".");
            }

            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new IOException("Could not connect properly.");
            }
        } catch (IOException e) {
            this.disconnect();
            throw e;
        }
    }
}
