package org.javagems.mailunit;

import static org.junit.Assert.*;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.mail.Message;

public class MailboxTest {

    private Mailbox mailbox;

    @Before
    public void setUp() throws Exception {
        mailbox = Mailbox.get("foo@bar.com");
        mailbox.clear();
    }

    @After
    public void tearDown() throws Exception {
        mailbox.destroy();
    }

    @Test
    public void testMailbox() throws Exception {
        assertTrue(mailbox.isEmpty());

        new MailboxTest().sendTestEmail();

        assertFalse(mailbox.isEmpty());
        Message message = mailbox.get(0);

        assertEquals("Test Mail", message.getSubject());
    }

    public void sendTestEmail() throws Exception {
        Email email = new SimpleEmail();
        email.setHostName("some.smtp.host");
        email.setFrom("some@guy.com");
        email.setSubject("Test Mail");
        email.setMsg("This is a test mail ... :-)");
        email.addTo("foo@bar.com");
        email.send();
    }

}
