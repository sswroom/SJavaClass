package org.sswr.util.net.email;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.data.XmlUtil;
import org.sswr.util.data.textenc.FormEncoding;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.sswr.util.io.UTF8Reader;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class EmailTemplate2 {
	private String subject;
	private String content;
	private boolean htmlContent;

	private boolean parseTemplate(@Nonnull StringBuilder sbOut, @Nonnull String tpl, @Nonnull Map<String, String> items, @Nonnull LogTool log)
	{
		int i;
		int j;
		StringBuilder sbParam = new StringBuilder();
		String paramName;
		String param;
		i = 0;
		while (true)
		{
			j = tpl.indexOf('[', i);
			if (j == -1)
			{
				sbOut.append(tpl.substring(i));
				return true;
			}
			if (i != j)
			{
				sbOut.append(tpl.substring(i, j));
			}
			i = j + 1;
			if (tpl.charAt(i) == '[')
			{
				sbOut.append('[');
				i++;
			}
			else
			{
				j = tpl.indexOf(']', i);
				if (j == -1)
				{
					log.logMessage("EmailTemplate ']' not found after '['", LogLevel.ERROR);
					return false;
				}
				sbParam.setLength(0);
				sbParam.append(tpl.substring(i, j));
				paramName = sbParam.toString();
				while ("@#^$".indexOf(paramName.charAt(0)) >= 0)
				{
					paramName = paramName.substring(1);
				}
				if ((param = items.get(paramName)) == null)
				{
					log.logMessage("EmailTemplate item [" + tpl.substring(i, j) + "] not found", LogLevel.ERROR);
					return false;
				}
				sbParam.setLength(0);
				sbParam.append(param);
				while (true)
				{
					if (tpl.charAt(i) == '@')
					{
						param = XmlUtil.toXMLText(sbParam.toString());
						sbParam.setLength(0);
						sbParam.append(param);
					}
					else if (tpl.charAt(i) == '#')
					{
						param = XmlUtil.toHTMLBodyText(sbParam.toString());
						sbParam.setLength(0);
						sbParam.append(param);
					}
					else if (tpl.charAt(i) == '^')
					{
						param = FormEncoding.formEncode(sbParam.toString());
						sbParam.setLength(0);
						sbParam.append(param);
					}
					else if (tpl.charAt(i) == '$')
					{
					}
					else
					{
						sbOut.append(sbParam);
						break;
					}
					i++;
				}
				i = j + 1;
			}
		}
	}

	public EmailTemplate2(@Nonnull String subject, @Nonnull String content, boolean htmlContent)
	{
		this.subject = subject;
		this.content = content;
		this.htmlContent = htmlContent;
	}
	
	@Nullable
	public EmailMessage createEmailMessage(@Nonnull Map<String, String> items, @Nonnull LogTool log)
	{
		StringBuilder sbSubject = new StringBuilder();
		if (!parseTemplate(sbSubject, this.subject, items, log))
			return null;
		StringBuilder sbContent = new StringBuilder();
		if (!parseTemplate(sbContent, this.content, items, log))
			return null;
		return new SimpleEmailMessage(sbSubject.toString(), sbContent.toString(), htmlContent);
	}

	@Nullable
	public static EmailTemplate2 loadFromFile(@Nonnull String fileName, boolean htmlContent)
	{
		try
		{
			FileInputStream fs = new FileInputStream(fileName);
			StringBuilderUTF8 sbSubject = new StringBuilderUTF8();
			StringBuilderUTF8 sbContent = new StringBuilderUTF8();
			UTF8Reader reader = new UTF8Reader(fs);
			while (true)
			{
				if (!reader.readLine(sbSubject, 2048))
					return null;
				if (reader.isLineBreak())
					break;
			}
			reader.readToEnd(sbContent);
			if (sbSubject.getLength() > 0 && sbContent.getLength() > 0)
			{
				return new EmailTemplate2(sbSubject.toString(), sbContent.toString(), htmlContent);
			}
			return null;
		}
		catch (FileNotFoundException ex)
		{
			return null;
		}
	}
}
