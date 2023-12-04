package org.sswr.util.net.email;

import java.util.ArrayList;
import java.util.List;

public class SimpleEmailMessage implements EmailMessage
{
    private String content;
    private boolean contentHTML;
    private String subject;
    private List<String> attachment;

    public SimpleEmailMessage(String subject, String content, boolean contentHTML)
    {
        this.subject = subject;
        this.content = content;
        this.contentHTML = contentHTML;
        this.attachment = new ArrayList<String>();
    }

    @Override
    public String getContent() {
        return this.content;
    }

    @Override
    public boolean isContentHTML() {
        return this.contentHTML;
    }
    
    @Override
    public String getSubject() {
        return this.subject;
    }

    @Override
    public void addAttachment(String attachmentPath) {
        this.attachment = new ArrayList<String>();
    }

    @Override
    public int getAttachmentCount() {
        return this.attachment.size();
    }

    @Override
    public String getAttachment(int index) {
        return this.attachment.get(index);
    }
}
