package org.sswr.util.net.email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sswr.util.data.XmlUtil;
import org.sswr.util.data.textenc.FormEncoding;

/***
 * Template format:
 * [@xxxx] Attribute Text
 * [^xxxx] Form Encoded Text
 * [#xxxx] HTML Text
 * [$xxxx] Direct HTML Tag
 * [xxxx]  Subject Text
 * [[ output [ character
 */
public class EmailTemplate implements EmailMessage
{
	public class TemplateFormatException extends Exception
	{
		private static final long serialVersionUID = 1L;
		public TemplateFormatException(String message)
		{
			super(message);
		}
	}

	public class TemplateItemException extends Exception
	{
		private static final long serialVersionUID = 1L;
		public TemplateItemException(String message)
		{
			super(message);
		}
	}

	public static class ItemGroup
	{
		public String itemTemplate;
		public StringBuilder sbItem;
		public String postTemplate;
		public StringBuilder sbPost;
		public String noItem;
		public String hasItemPre;
		public String hasItemPost;

		public String groupTemplate;
		public String groupPostTemplate;
		public String groupName;
		public String groupLast;
	}

	private String subjTemplate;
	private String contTemplate;
	private StringBuilder sbSubj;
	private StringBuilder sbPre;
	private List<ItemGroup> groups;
	private List<EmailAttachment> attachments;
	private List<String> headerName;
	private List<String> headerValue;

	public EmailTemplate(InputStream templateStm, Map<String, String> vars) throws IOException, TemplateFormatException, TemplateItemException
	{
		this.sbSubj = new StringBuilder();
		this.attachments = new ArrayList<EmailAttachment>();
		this.groups = new ArrayList<ItemGroup>();
		this.headerName = new ArrayList<String>();
		this.headerValue = new ArrayList<String>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(templateStm, StandardCharsets.UTF_8));
		this.subjTemplate = reader.readLine();
		this.parseTemplate(this.subjTemplate, this.sbSubj, vars);
		StringBuilder sb = new StringBuilder();
		String line;
		while (true)
		{
			line = reader.readLine();
			if (line == null)
			{
				break;
			}
			sb.append(line);
			sb.append("\r\n");
		}
		this.contTemplate = parseItemGroup(sb.toString(), vars);
		this.sbPre = new StringBuilder();
		this.parseTemplate(this.contTemplate, this.sbPre, vars);
	}

	public EmailTemplate(String subject, String content, Map<String, String> vars) throws IOException, TemplateFormatException, TemplateItemException
	{
		this.sbSubj = new StringBuilder();
		this.groups = new ArrayList<ItemGroup>();
		this.subjTemplate = subject;
		this.parseTemplate(this.subjTemplate, this.sbSubj, vars);
		this.contTemplate = parseItemGroup(content, vars);
		this.sbPre = new StringBuilder();
		this.parseTemplate(this.contTemplate, this.sbPre, vars);
	}

	private String parseItemGroup(String content, Map<String, String> vars) throws IOException, TemplateFormatException, TemplateItemException
	{
		int itemOfst = content.indexOf("[item]");
		if (itemOfst >= 0)
		{
			int j = content.indexOf("[/item]");
			if (j < itemOfst)
			{
				throw new IOException("Template format invalid: [/item] not found");
			}
			ItemGroup group = new ItemGroup();
			group.sbPost = new StringBuilder();
			group.sbItem = new StringBuilder();
			group.itemTemplate = content.substring(itemOfst + 6, j);
			group.postTemplate = content.substring(j + 7);
			content = content.substring(0, itemOfst);
			this.groups.add(group);
			j = group.itemTemplate.indexOf("[--noitems--]");
			if (j >= 0)
			{
				group.noItem = group.itemTemplate.substring(j + 13);
				group.itemTemplate = group.itemTemplate.substring(0, j);
			}
			j = group.itemTemplate.indexOf("[--hasItemPost--]");
			if (j >= 0)
			{
				group.hasItemPost = group.itemTemplate.substring(j + 17);
				group.itemTemplate = group.itemTemplate.substring(0, j);
			}
			j = group.itemTemplate.indexOf("[--hasItemPre--]");
			if (j >= 0)
			{
				group.hasItemPre = group.itemTemplate.substring(j + 16);
				group.itemTemplate = group.itemTemplate.substring(0, j);
			}
			j = group.itemTemplate.indexOf("[group ");
			if (j >= 0)
			{
				int k = group.itemTemplate.indexOf(']', j + 7);
				if (k < 0)
				{
					throw new IOException("Template format invalid: No ']' in group begin");
				}
				group.groupName = group.itemTemplate.substring(j + 7, k);
				group.groupTemplate = group.itemTemplate.substring(k + 1);
				group.itemTemplate = group.itemTemplate.substring(0, j);
				j = group.groupTemplate.indexOf("[/group]");
				if (j < 0)
				{
					throw new IOException("Template format invalid: No [/group] found after group begein");
				}
				group.groupPostTemplate = group.groupTemplate.substring(j + 8);
				group.groupTemplate = group.groupTemplate.substring(0, j);
			}

			group.postTemplate = parseItemGroup(group.postTemplate, vars);
			this.parseTemplate(group.postTemplate, group.sbPost, vars);
		}
		return content;
	}

	public void addItem(int itemIndex, Map<String, String> itemVars) throws IllegalArgumentException, TemplateFormatException, TemplateItemException
	{
		ItemGroup group = this.groups.get(itemIndex);
		if (group == null)
		{
			throw new IllegalArgumentException("Item Template Group not found");
		}
		if (group.groupTemplate != null)
		{
			String thisVal = itemVars.get(group.groupName);
			if (thisVal == null)
				thisVal = "";
			if (group.groupLast != null && group.groupLast.equals(thisVal))
			{
			}
			else
			{
				if (group.groupLast != null)
					group.sbItem.append(group.groupPostTemplate);
				group.groupLast = thisVal;
				this.parseTemplate(group.itemTemplate, group.sbItem, itemVars);
			}
			this.parseTemplate(group.groupTemplate, group.sbItem, itemVars);
		}
		else
		{
			this.parseTemplate(group.itemTemplate, group.sbItem, itemVars);
		}
	}

	public void addItem(Map<String, String> itemVars) throws IllegalArgumentException, TemplateFormatException, TemplateItemException
	{
		addItem(0, itemVars);
	}

	public void addItems(List<Map<String, String>> itemVarsList) throws IllegalArgumentException, TemplateFormatException, TemplateItemException
	{
		int i = 0;
		int j = itemVarsList.size();
		while (i < j)
		{
			addItem(0, itemVarsList.get(i));
			i++;
		}
	}
	
	private void parseTemplate(String template, StringBuilder sb, Map<String, String> vars) throws TemplateFormatException, TemplateItemException
	{
		int i = 0;
		int j;
		int k;
		while (true)
		{
			j = template.indexOf('[', i);
			if (j < 0)
			{
				sb.append(template.substring(i));
				return;
			}
			if (j + 1 >= template.length())
			{
				throw new TemplateFormatException("'[' cannot be the end of the template file");
			}
			if (template.charAt(j + 1) == '[')
			{
				sb.append(template.substring(i, j));
				sb.append("[");
				k = j + 1;
			}
			else
			{
				k = template.indexOf(']', j);
				if (k < 0)
				{
					throw new TemplateFormatException("Closing ']' not found in the template file");
				}
				String paramName = template.substring(j + 1, k);
				String param;
				String keyName;
				keyName = paramName;
				while (keyName.startsWith("@") || keyName.startsWith("#") || keyName.startsWith("$") || keyName.startsWith("^"))
				{
					keyName = keyName.substring(1);
				}
				param = vars.get(keyName);
				if (param == null && vars.containsKey(keyName))
				{
					param = "";
				}
				if (param == null)
				{
					throw new TemplateItemException("Parameter value ["+paramName+"] not provided");
				}
				while (true)
				{
					if (paramName.startsWith("@"))
					{
						param = XmlUtil.toAttr(param);
						paramName = paramName.substring(1);
					}
					else if (paramName.startsWith("#"))
					{
						param = XmlUtil.toHTMLText(param);
						paramName = paramName.substring(1);
					}
					else if (paramName.startsWith("^"))
					{
						param = FormEncoding.formEncode(param);
						paramName = paramName.substring(1);
					}
					else if (paramName.startsWith("$")) //Direct output
					{
						paramName = paramName.substring(1);
					}
					else
					{
						break;
					}
				}
				sb.append(template.substring(i, j));
				sb.append(param);
			}
			i = k + 1;
		}
	}

	public String getSubject()
	{
		return this.sbSubj.toString();
	}

	public String getContent()
	{
		if (this.groups.size() == 0)
		{
			return this.sbPre.toString();
		}
		StringBuilder sb = new StringBuilder();
		int i = 0;
		int j = this.groups.size();
		ItemGroup group;
		sb.append(this.sbPre);
		while (i < j)
		{
			group = this.groups.get(i);
			if (group.sbItem.length() == 0)
			{
				if (group.noItem != null)
					sb.append(group.noItem);
			}
			else
			{
				if (group.hasItemPre != null)
					sb.append(group.hasItemPre);
				sb.append(group.sbItem);
				if (group.groupLast != null)
					sb.append(group.groupPostTemplate);
				if (group.hasItemPost != null)
					sb.append(group.hasItemPost);
			}
			sb.append(group.sbPost);
			i++;
		}
		return sb.toString();
	}

	@Override
	public boolean isContentHTML()
	{
		return true;
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

	public boolean addAttachment(String attachmentPath)
	{
		EmailAttachment att = EmailAttachment.createFromFile(attachmentPath, "attach"+(this.attachments.size() + 1));
		if (att != null)
		{
			this.attachments.add(att);
			return true;
		}
		return false;
	}

	@Override
	public int getAttachmentCount()
	{
		return this.attachments.size();
	}

	@Override
	public EmailAttachment getAttachment(int index)
	{
		return this.attachments.get(index);
	}
}
