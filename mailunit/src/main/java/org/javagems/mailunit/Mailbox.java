package org.javagems.mailunit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.mail.util.MimeMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * files mailbox that hosts messages.
 * 
 * @author Christophe Cassagnabere
 */
public class Mailbox {
    private static final Logger LOGGER = LoggerFactory.getLogger(Mailbox.class);

    private static final int THRESHOLD = 64;
    private final Address address;

    private boolean error;

    private File directory;

    private Mailbox(InternetAddress address) {
        this.address = address;
        try {
            File tmpFile = File.createTempFile("mailbox", null);
            FileUtils.deleteQuietly(tmpFile);
            this.directory = new File(
                    new File(tmpFile.getParentFile(), "mailbox"),
                    address.getAddress().replace("@", "(at)"));
            FileUtils.forceMkdir(directory);

            LOGGER.info("Email has not been sent to " + address.toString() + " (mock javamail). The content is available in " + directory);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Address getAddress() {
        return address;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    private static final Map<Address, Mailbox> mailboxes = new HashMap<Address, Mailbox>();

    public synchronized static Mailbox get(InternetAddress a) {
        Mailbox inbox = mailboxes.get(a);
        if (inbox == null) {
            mailboxes.put(a, inbox = new Mailbox(a));
        }
        return inbox;
    }

    public static Mailbox get(Address address) throws AddressException {
        return get(new InternetAddress(address.toString()));
    }
    
    public static Mailbox get(String address) throws AddressException {
        return get(new InternetAddress(address));
    }

    public static void clearAll() throws Exception {
        File tmpFile = File.createTempFile("mailbox", null);
        FileUtils.deleteQuietly(tmpFile);
        FileUtils.deleteDirectory(new File(tmpFile.getParentFile(), "mailbox"));
        mailboxes.clear();
    }

    public Message get(int i) {
        return this.read(this.list(true).get(i));
    }

    public int size() {
        return this.list(false).size();
    }

    public int getNewMessageCount() {
        return this.list(false).size();
    }

    public List<Message> getMessages() {
        List<Message> messages = new ArrayList<Message>();
        for (File file : this.list(true)) {
            messages.add(this.read(file));
        }
        return messages;
    }

    public void add(Message message) {
        this.purge();
        this.write(message);
    }

    public void addAll(List<Message> messages) {
        this.purge();
        for (Message message : messages) {
            this.write(message);
        }
    }

    public void removeAll(List<Message> expunged) {
        this.clear();
    }

    public void clear() {
        for (File file : this.list(false)) {
            FileUtils.deleteQuietly(file);
        }
    }

    private List<File> list(boolean sorted) {
        try {
            List<File> files = new ArrayList<File>(FileUtils.listFiles(directory, new String[] {"eml"}, false));
            if (sorted) {
                Collections.sort(files, new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                    }
                });
            }
            return files;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Message read(File file) {
        try {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            return MimeMessageUtils.createMimeMessage(session, file);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private File write(Message message) {
        try {
            File file = File.createTempFile("email", ".eml", this.directory);
            MimeMessageUtils.writeMimeMessage(new MimeMessage((MimeMessage) message), file);
            return file;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void purge() {
        List<File> files = this.list(true);
        if (files.size() > THRESHOLD) {
            for (int i = 0; i < THRESHOLD / 4; i++) {
                FileUtils.deleteQuietly(files.get(i));
            }
        }
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    public void destroy() {
        try {
            FileUtils.deleteDirectory(this.directory);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
