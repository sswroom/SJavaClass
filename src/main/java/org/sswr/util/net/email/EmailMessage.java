package org.sswr.util.net.email;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;

import org.sswr.util.crypto.CRC32R;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.StringUtil;
import org.sswr.util.net.WebUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class EmailMessage
{
	@Nullable
	public abstract String getContent();
	public abstract boolean isContentHTML();
	@Nullable
	public abstract String getSubject();
	public abstract int getCustomHeaderCount();
	@Nullable
	public abstract String getCustomHeaderName(int index);
	@Nullable
	public abstract String getCustomHeaderValue(int index);
	public abstract void addCustomHeader(@Nonnull String name, @Nonnull String value);
	public abstract int getAttachmentCount();
	@Nullable
	public abstract EmailAttachment getAttachment(int index);
	@Nonnull
	public abstract List<EmailAttachment> getAttachments();
	public abstract boolean addAttachmentFile(@Nonnull String attachmentPath);
	public abstract boolean addAttachment(@Nonnull byte[] bytes, @Nonnull String contentType, @Nonnull String fileName);

	public void setSentDate(@Nonnull ZonedDateTime dt)
	{
		this.addCustomHeader("Date", WebUtil.date2Str(dt));
	}

	public void setSentDate(@Nonnull Timestamp date)
	{
		setSentDate(DateTimeUtil.newZonedDateTime(date));
	}

	public void setMessageId(@Nonnull String id)
	{
		this.addCustomHeader("Message-ID", "<"+id+">");
	}

	@Nonnull
	public static String generateMessageID(@Nonnull String fromEmail)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtil.toHex64(System.currentTimeMillis()));
		sb.append('.');
		CRC32R crc = new CRC32R();
		int i;
		i = fromEmail.indexOf('@');
		crc.calc(fromEmail.substring(0, i).getBytes(StandardCharsets.UTF_8));
		sb.append(StringUtil.toHex(crc.getValue()));
		sb.append(fromEmail.substring(i));
		return sb.toString();
	} 
}
