package org.sswr.util.net.email;

import java.util.ArrayList;
import java.util.List;

public class SimpleEmailMessage implements EmailMessage
{
	private String content;
	private boolean contentHTML;
	private String subject;
	private List<String> headerName;
	private List<String> headerValue;
	private List<EmailAttachment> attachment;

	public SimpleEmailMessage(String subject, String content, boolean contentHTML)
	{
		this.subject = subject;
		this.content = content;
		this.contentHTML = contentHTML;
		this.headerName = new ArrayList<String>();
		this.headerValue = new ArrayList<String>();
		this.attachment = new ArrayList<EmailAttachment>();
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

	public void addCustomHeader(String name, String value)
	{
		this.headerName.add(name);
		this.headerValue.add(value);
	}

	@Override
	public int getCustomHeaderCount() {
		return this.headerName.size();
	}

	@Override
	public String getCustomHeaderName(int index) {
		return this.headerName.get(index);
	}

	@Override
	public String getCustomHeaderValue(int index) {
		return this.headerValue.get(index);
	}

	@Override
	public boolean addAttachmentFile(String attachmentPath) {
		EmailAttachment att = EmailAttachment.createFromFile(attachmentPath, "attach"+(this.attachment.size() + 1));
		if (att != null)
		{
			this.attachment.add(att);
			return true;
		}
		return false;
	}

	@Override
	public int getAttachmentCount() {
		return this.attachment.size();
	}

	@Override
	public EmailAttachment getAttachment(int index) {
		return this.attachment.get(index);
	}
}
