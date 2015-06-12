package org.javagems.mailunit;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;

/**
 * Mock {@link Transport} to deliver to {@link Mailbox}.
 *
 * @author Kohsuke Kawaguchi
 */
public class MockTransport extends Transport {
    public MockTransport(Session session, URLName urlname) {
        super(session, urlname);
    }

    public void connect(String host, int port, String user, String password) throws MessagingException {
        // noop 
    }

    public void sendMessage(Message msg, Address[] addresses) throws MessagingException {
        for (Address a : addresses) {
            // create a copy to isolate the sender and the receiver
            Mailbox mailbox = Mailbox.get(Aliases.getInstance().resolve(a));
            if(mailbox.isError()) {
                throw new MessagingException("Simulated error sending message to "+a);
            }
            mailbox.add(msg);
        }
    }
}
