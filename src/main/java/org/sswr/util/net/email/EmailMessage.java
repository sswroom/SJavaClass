package org.sswr.util.net.email;

import java.util.List;

public interface EmailMessage
{
	public String getContent();
	public boolean isContentHTML();
	public String getSubject();
	public int getCustomHeaderCount();
	public String getCustomHeaderName(int index);
	public String getCustomHeaderValue(int index);
	public int getAttachmentCount();
	public EmailAttachment getAttachment(int index);
	public List<EmailAttachment> getAttachments();
	public boolean addAttachmentFile(String attachmentPath);
}
