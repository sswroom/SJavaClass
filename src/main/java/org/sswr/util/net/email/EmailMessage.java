package org.sswr.util.net.email;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;

import org.sswr.util.crypto.CRC32R;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.StringUtil;
import org.sswr.util.net.WebUtil;

public abstract class EmailMessage
{
	public abstract String getContent();
	public abstract boolean isContentHTML();
	public abstract String getSubject();
	public abstract int getCustomHeaderCount();
	public abstract String getCustomHeaderName(int index);
	public abstract String getCustomHeaderValue(int index);
	public abstract void addCustomHeader(String name, String value);
	public abstract int getAttachmentCount();
	public abstract EmailAttachment getAttachment(int index);
	public abstract List<EmailAttachment> getAttachments();
	public abstract boolean addAttachmentFile(String attachmentPath);

	public void setSentDate(ZonedDateTime dt)
	{
		this.addCustomHeader("Date", WebUtil.date2Str(dt));
	}

	public void setSentDate(Timestamp date)
	{
		setSentDate(DateTimeUtil.newZonedDateTime(date));
	}

	public void setMessageId(String id)
	{
		this.addCustomHeader("Message-ID", "<"+id+">");
	}

	public static String generateMessageID(String fromEmail)
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
