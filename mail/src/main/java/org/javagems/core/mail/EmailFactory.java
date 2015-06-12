package org.javagems.core.mail;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.FileDataSource;
import javax.mail.internet.InternetAddress;

public class EmailFactory {
    private class EmbbededImg {
        String description;
        File image;

        public EmbbededImg(File image, String description) {
            this.description = description;
            this.image = image;
        }

        public String getDescription() {
            return description;
        }

        public File getImage() {
            return image;
        }
    }

    protected static final String SMTP_HOST = "mail.smtp.host";
    protected static final String FROM = "mail.from";
    protected static final String FROM_ALIAS = "mail.from.alias";
    protected static final String REPLYTO = "mail.replyto";

    private List<InternetAddress> to;
    private List<InternetAddress> cc;
    private List<InternetAddress> bcc;
    private String subject;
    private String text;
    private String html;
    private List<EmailAttachment> attachments;
    private Map<String, EmbbededImg> embedded;
    private Map<String, String> replacements;
    private Configuration configuration;

    public static EmailFactory getInstance(Configuration configuration) {
        return new EmailFactory(configuration);
    }

    private EmailFactory(Configuration configuration) {
        this.configuration = configuration;
        this.to = new ArrayList<InternetAddress>();
        this.cc = new ArrayList<InternetAddress>();
        this.bcc = new ArrayList<InternetAddress>();
        this.attachments = new ArrayList<EmailAttachment>();
        this.embedded = new HashMap<String, EmbbededImg>();
        this.replacements = new HashMap<String, String>();
    }

    public EmailFactory to(Collection<InternetAddress> to) {
        this.to = new ArrayList<InternetAddress>(to);
        return this;
    }

    public EmailFactory addTo(Collection<InternetAddress> to) {
        this.to.addAll(to);
        return this;
    }

    public EmailFactory addTo(InternetAddress... to) {
        this.to.addAll(Arrays.asList(to));
        return this;
    }

    public EmailFactory cc(Collection<InternetAddress> cc) {
        this.cc = new ArrayList<InternetAddress>(cc);
        return this;
    }

    public EmailFactory addCc(Collection<InternetAddress> cc) {
        this.cc.addAll(cc);
        return this;
    }

    public EmailFactory addCc(InternetAddress... cc) {
        this.cc.addAll(Arrays.asList(cc));
        return this;
    }

    public EmailFactory bcc(Collection<InternetAddress> bcc) {
        this.bcc = new ArrayList<InternetAddress>(bcc);
        return this;
    }

    public EmailFactory addBcc(Collection<InternetAddress> bcc) {
        this.bcc.addAll(bcc);
        return this;
    }

    public EmailFactory addBcc(InternetAddress... bcc) {
        this.bcc.addAll(Arrays.asList(bcc));
        return this;
    }

    public EmailFactory subject(String subject) {
        this.subject = subject;
        return this;
    }

    public EmailFactory text(String text) {
        this.text = text;
        return this;
    }

    public EmailFactory html(String html) {
        this.html = html;
        return this;
    }

    public EmailFactory setAttachments(Collection<EmailAttachment> attachments) {
        this.attachments = new ArrayList<EmailAttachment>(attachments);
        return this;
    }

    public EmailFactory addAttachments(Collection<EmailAttachment> attachments) {
        this.attachments.addAll(attachments);
        return this;
    }

    public EmailFactory addAttachments(EmailAttachment... attachments) {
        this.attachments.addAll(Arrays.asList(attachments));
        return this;
    }

    public EmailFactory attach(File file) {
        this.attach(file, null);
        return this;
    }

    public EmailFactory attach(File file, String name) {
        this.attach(file, name, null);
        return this;
    }

    public EmailFactory attach(File file, String name, String description) {
        EmailAttachment attachment = new EmailAttachment();
        attachment.setPath(file.getAbsolutePath());
        attachment.setDisposition(EmailAttachment.ATTACHMENT);
        attachment.setName(name);
        attachment.setDescription(description);

        this.addAttachments(attachment);
        return this;
    }

    public EmailFactory embed(File image, String description, String cid) {
        this.embedded.put(cid, new EmbbededImg(image, description));
        return this;
    }

    public HtmlEmail make() throws Exception {
        HtmlEmail email = new HtmlEmail();
        email.setHostName(this.configuration.getString(SMTP_HOST));
        email.setFrom(this.configuration.getString(FROM), this.configuration.getString(FROM_ALIAS));
        email.setReplyTo(Arrays.asList(new InternetAddress(this.configuration.getString(REPLYTO))));

        for (Entry<String, EmbbededImg> entry : this.embedded.entrySet()) {
            email.embed(new FileDataSource(entry.getValue().getImage()), entry.getValue().getDescription(), entry.getKey());
        }
        if (!StringUtils.isEmpty(this.subject)) {
            String subject = this.subject;
            for (Entry<String, String> entry : this.replacements.entrySet()) {
                String value = StringUtils.isEmpty(entry.getValue()) ? "" : entry.getValue();
                subject = subject.replaceAll(entry.getKey(), value);
            }
            email.setSubject(subject);
        }
        if (!StringUtils.isEmpty(this.text)) {
            String text = this.text;
            for (Entry<String, String> entry : this.replacements.entrySet()) {
                String value = StringUtils.isEmpty(entry.getValue()) ? "" : entry.getValue();
                text = text.replaceAll(entry.getKey(), value);
            }
            email.setTextMsg(text);
        }
        if (!StringUtils.isEmpty(this.html)) {
            String html = this.html;
            for (Entry<String, String> entry : this.replacements.entrySet()) {
                String value = StringUtils.isEmpty(entry.getValue()) ? "" : entry.getValue();
                html = html.replaceAll(entry.getKey(), value);
            }
            email.setHtmlMsg(html);
        }
        if (!CollectionUtils.isEmpty(this.attachments)) {
            for (EmailAttachment attachment : this.attachments) {
                email.attach(attachment);
            }
        }

        if (!CollectionUtils.isEmpty(this.to)) {
            email.setTo(this.to);
        }
        if (!CollectionUtils.isEmpty(this.cc)) {
            email.setCc(this.cc);
        }
        if (!CollectionUtils.isEmpty(this.bcc)) {
            email.setBcc(this.bcc);
        }
        return email;
    }

    public EmailFactory replace(String key, String value) {
        this.replacements.put(key, value);
        return this;
    }
}
