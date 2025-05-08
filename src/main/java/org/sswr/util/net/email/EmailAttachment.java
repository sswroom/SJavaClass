package org.sswr.util.net.email;

import java.io.File;
import java.time.ZonedDateTime;

import org.sswr.util.io.FileStream;
import org.sswr.util.io.FileStream.BufferType;
import org.sswr.util.io.FileStream.FileMode;
import org.sswr.util.io.FileStream.FileShare;
import org.sswr.util.net.MIME;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class EmailAttachment
{
	public byte[] content;
	public String contentId;
	public String fileName;
	public ZonedDateTime createTime;
	public ZonedDateTime modifyTime;
	public boolean isInline;
	public String contentType;

	@Nonnull
	public EmailAttachment clone()
	{
		EmailAttachment att = new EmailAttachment();
		att.content = this.content;
		att.contentId = this.contentId;
		att.fileName = this.fileName;
		att.createTime = this.createTime;
		att.modifyTime = this.modifyTime;
		att.isInline = this.isInline;
		att.contentType = this.contentType;
		return att;
	}

	@Nullable
	public static EmailAttachment createFromFile(@Nonnull String fileName, @Nullable String contentId)
	{
		FileStream fs = new FileStream(fileName, FileMode.ReadOnly, FileShare.DenyNone, BufferType.Normal);
		if (fs.isError())
		{
			return null;
		}
		long len = fs.getLength();
		if (len > 104857600)
		{
			return null;
		}
		EmailAttachment attachment = new EmailAttachment();
		attachment.content = new byte[(int)len];
		if (fs.read(attachment.content, 0, (int)len) != len)
		{
			return null;
		}
		attachment.createTime = fs.getCreateTime();
		attachment.modifyTime = fs.getModifyTime();
		attachment.fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
		attachment.contentId = contentId;
		attachment.isInline = false;
		attachment.contentType = MIME.getMIMEFromFileName(attachment.fileName);
		return attachment;

	}
}
