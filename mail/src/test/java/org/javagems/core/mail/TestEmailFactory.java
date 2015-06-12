package org.javagems.core.mail;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.commons.configuration.Configuration;
import org.javagems.core.configuration.ConfigurationFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;

import java.io.File;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;

public class TestEmailFactory {

    private Configuration configuration;

    @Before
    public void setUp() throws Exception {
        Mailbox.clearAll();

        configuration = mock(Configuration.class);
        when(configuration.getString("mail.smtp.host")).thenReturn("smtp.keyade.com");
        when(configuration.getString("mail.from")).thenReturn("test@keyade.com");
        when(configuration.getString("mail.from.alias")).thenReturn("Mr Test");
        when(configuration.getString("mail.replyto")).thenReturn("test@keyade.com");
    }

    @After
    public void tearDown() throws Exception {
        Mailbox.clearAll();
    }

    @Test
    public void test() throws Exception {
        InternetAddress address = new InternetAddress("destinataire@yourdomain.com");
        Mailbox mailbox = Mailbox.get(address);
        assertTrue(mailbox.isEmpty());

        EmailFactory
                .getInstance(configuration)
                .addTo(address)
                .subject("A simple test email")
                .text("A simple content with a PATTERN")
                .replace("PATTERN", "value inside")
                .make()
                .send();

        mailbox = Mailbox.get(address);
        assertFalse(mailbox.isEmpty());
        Message message = mailbox.get(0);

        assertArrayEquals(new InternetAddress[] {
                new InternetAddress("destinataire@yourdomain.com")},
                message.getAllRecipients());

        String content = ((MimeMultipart) message.getContent()).getBodyPart(0).getContent().toString();
        assertEquals("A simple content with a value inside", content);
    }
    
    public static void main(String[] args) throws Exception {
        File file = new File("myFile.jpg");

        EmailFactory.getInstance(ConfigurationFactory.getConfiguration())
                .addTo(new InternetAddress("destinataire@keyade.com"))
                .subject("test subject")
                .embed(file, "My logo", "MY_CID")
                .html("<html><body><div style=\"color:red;\">PATTERN</div><img src=\"cid:MY_CID\"/></body></html>")
                .replace("PATTERN", "value")
                .attach(file, "myFileWithAnotherName.jpg", "My Big File")
                .make()
                .send();
    }
}
