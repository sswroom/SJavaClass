package org.sswr.util.net.email;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class SimpleEmailMessage extends EmailMessage
{
	private String content;
	private boolean contentHTML;
	private String subject;
	private List<String> headerName;
	private List<String> headerValue;
	private List<EmailAttachment> attachment;

	public SimpleEmailMessage(@Nonnull String subject, @Nonnull String content, boolean contentHTML)
	{
		this.subject = subject;
		this.content = content;
		this.contentHTML = contentHTML;
		this.headerName = new ArrayList<String>();
		this.headerValue = new ArrayList<String>();
		this.attachment = new ArrayList<EmailAttachment>();
	}

	@Override
	@Nullable
	public String getContent() {
		return this.content;
	}

	@Override
	public boolean isContentHTML() {
		return this.contentHTML;
	}
	
	@Override
	@Nullable
	public String getSubject() {
		return this.subject;
	}

	public void addCustomHeader(@Nonnull String name, @Nonnull String value)
	{
		this.headerName.add(name);
		this.headerValue.add(value);
	}

	@Override
	public int getCustomHeaderCount() {
		return this.headerName.size();
	}

	@Override
	@Nullable
	public String getCustomHeaderName(int index) {
		return this.headerName.get(index);
	}

	@Override
	@Nullable
	public String getCustomHeaderValue(int index) {
		return this.headerValue.get(index);
	}

	@Override
	public boolean addAttachmentFile(@Nonnull String attachmentPath) {
		EmailAttachment att = EmailAttachment.createFromFile(attachmentPath, "attach"+(this.attachment.size() + 1));
		if (att != null)
		{
			this.attachment.add(att);
			return true;
		}
		return false;
	}

	@Override
	public boolean addAttachment(@Nonnull byte[] bytes, @Nonnull String contentType, @Nonnull String fileName, @Nullable String contentId) {
		EmailAttachment att = new EmailAttachment();
		att.content = bytes;
		if (contentId == null)
		{
			att.contentId = "attach"+(this.attachment.size() + 1);
		}
		else
		{
			att.contentId = contentId;
		}
		att.contentType = contentType;
		att.fileName = fileName;
		this.attachment.add(att);
		return true;
	}

	public void addAttachment(@Nonnull EmailAttachment attachment)
	{
		this.attachment.add(attachment);
	}

	@Override
	public int getAttachmentCount() {
		return this.attachment.size();
	}

	@Override
	@Nullable
	public EmailAttachment getAttachment(int index) {
		return this.attachment.get(index);
	}

	@Override
	@Nonnull
	public List<EmailAttachment> getAttachments()
	{
		return this.attachment;
	}
}
