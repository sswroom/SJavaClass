package org.sswr.util.net.email;

public interface EmailMessage
{
	public String getContent();
	public boolean isContentHTML();
	public String getSubject();
	public void addAttachment(String attachmentPath);
	public int getAttachmentCount();
	public String getAttachment(int index);
}
