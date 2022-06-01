package org.sswr.util.net.email;

import java.time.ZonedDateTime;

public class EmailAttachment
{
	public byte[] content;
	public String contentId;
	public String fileName;
	public ZonedDateTime createTime;
	public ZonedDateTime modifyTime;
	public boolean isInline;
	public String contentType;
}
