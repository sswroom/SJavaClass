package org.sswr.util.net.email;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ReceivedEmail
{
	private ArrayList<String> headers;
	private HashMap<String, String> headerMap;
	private String contentType;
	private byte[] content;
	private HashMap<String, EmailAttachment> attachmentMap;
	private ArrayList<EmailAttachment> attachments;
	private boolean signVerified;

	public ReceivedEmail()
	{
		this.signVerified = false;
		this.headerMap = new HashMap<String, String>();
		this.headers = new ArrayList<String>();
		this.contentType = null;
		this.content = null;
		this.attachments = new ArrayList<EmailAttachment>();
		this.attachmentMap = new HashMap<String, EmailAttachment>();
	}

	public void addHeader(@Nonnull String name, @Nonnull String value)
	{
		this.headerMap.put(name, value);
		this.headers.add(name+": "+value);
		if (name.equalsIgnoreCase("Content-Type"))
		{
			int i = value.indexOf(';');
			if (i >= 0)
			{
				this.contentType = value.substring(0, i);
			}
			else
			{
				this.contentType = value;
			}
		}
	}

	public void setContent(@Nonnull byte[] content)
	{
		this.content = content;
	}

	public void setContentType(@Nonnull String contentType)
	{
		this.contentType = contentType;
	}

	public void setSignVerified(boolean signVerified)
	{
		this.signVerified = signVerified;
	}

	public void addAttachment(boolean isInline, @Nonnull String fileName, @Nonnull String contentType, @Nonnull byte[] content)
	{
		EmailAttachment attachment = new EmailAttachment();
		attachment.isInline = isInline;
		attachment.fileName = fileName;
		attachment.contentType = contentType;
		attachment.content = content;
//		public String contentId;
//		public ZonedDateTime createTime;
//		public ZonedDateTime modifyTime;
		this.attachments.add(attachment);
		this.attachmentMap.put(attachment.fileName, attachment);
	}

	@Nullable
	public byte[] getContent()
	{
		return this.content;
	}

	@Nullable
	public EmailAttachment getAttachment(@Nonnull String fileName)
	{
		return this.attachmentMap.get(fileName);
	}

	@Nonnull
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Headers---------------------------------\r\n");
		int i = 0;
		int j = this.headers.size();
		while (i < j)
		{
			sb.append(headers.get(i));
			sb.append("\r\n");
			i++;
		}
		sb.append("\r\n");
		sb.append("Content---------------------------------\r\n");
		if (contentType != null)
		{
			sb.append("Content-Type: ");
			sb.append(this.contentType);
			sb.append("\r\n");
			sb.append("Sign Verified: ");
			sb.append(this.signVerified);
			sb.append("\r\n");
			sb.append("\r\n");
		}
		if (content != null)
		{
			sb.append(new String(content, StandardCharsets.UTF_8));
		}
		sb.append("\r\n");
		sb.append("Attachments-----------------------------\r\n");
		i = 0;
		j = this.attachments.size();
		while (i < j)
		{
			EmailAttachment attachment = this.attachments.get(i);
			sb.append("Content ID: "+attachment.contentId+"\r\n");
			sb.append("File Name: "+attachment.fileName+"\r\n");
			sb.append("Create Time: "+attachment.createTime+"\r\n");
			sb.append("Modify Time: "+attachment.modifyTime+"\r\n");
			sb.append("Content Type: "+attachment.contentType+"\r\n");
			sb.append("Inline: "+attachment.isInline+"\r\n");
			sb.append("Size: "+content.length+"\r\n");
			sb.append("-----------------------------\r\n");					
			i++;
		}
		return sb.toString();
	}
}
