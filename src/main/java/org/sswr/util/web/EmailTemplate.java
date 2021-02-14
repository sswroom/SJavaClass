package org.sswr.util.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.sswr.util.data.XmlUtil;

public class EmailTemplate {
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

	private String subjTemplate;
	private String contTemplate;
	private String itemTemplate;
	private int itemOfst;
	private StringBuilder sbSubj;
	private StringBuilder sbPre;
	private StringBuilder sbItem;
	private StringBuilder sbPost;

	public EmailTemplate(InputStream templateStm, Map<String, String> vars) throws IOException, TemplateFormatException, TemplateItemException
	{
		this.itemTemplate = null;
		this.itemOfst = 0;
		this.sbSubj = new StringBuilder();

		BufferedReader reader = new BufferedReader(new InputStreamReader(templateStm));
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
		this.contTemplate = sb.toString();
		this.itemOfst = this.contTemplate.indexOf("[item]");
		if (this.itemOfst >= 0)
		{
			int j = this.contTemplate.indexOf("[/item]");
			if (j < this.itemOfst)
			{
				throw new IOException("Template format invalid");
			}
			this.itemTemplate = this.contTemplate.substring(this.itemOfst + 6, j);
			this.sbPre = new StringBuilder();
			this.sbPost = new StringBuilder();
			this.contTemplate = this.contTemplate.substring(0, this.itemOfst) + this.contTemplate.substring(j + 7);
			this.sbItem = new StringBuilder();
			this.parseTemplate(this.contTemplate.substring(0, this.itemOfst), this.sbPre, vars);
			this.parseTemplate(this.contTemplate.substring(this.itemOfst), this.sbPost, vars);
		}
		else
		{
			this.sbPre = new StringBuilder();
			this.parseTemplate(this.contTemplate, this.sbPre, vars);
		}
	}

	public void addItem(Map<String, String> itemVars) throws IllegalArgumentException, TemplateFormatException, TemplateItemException
	{
		if (this.itemTemplate == null)
		{
			throw new IllegalArgumentException("Template has no items");
		}
		this.parseTemplate(this.itemTemplate, this.sbItem, itemVars);
	}

	public void addItems(List<Map<String, String>> itemVarsList) throws IllegalArgumentException, TemplateFormatException, TemplateItemException
	{
		int i = 0;
		int j = itemVarsList.size();
		while (i < j)
		{
			addItem(itemVarsList.get(i));
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
			k = template.indexOf(']', j);
			if (k < 0)
			{
				throw new TemplateFormatException("Closing ']' not found in the template file");
			}
			String paramName = template.substring(j + 1, k);
			String param;
			if (paramName.startsWith("@") || paramName.startsWith("#"))
			{
				param = vars.get(paramName.substring(1));
			}
			else
			{
				param = vars.get(paramName);
			}
			if (param == null)
			{
				throw new TemplateItemException("Parameter value ["+paramName+"] not provided");
			}
			if (paramName.startsWith("@"))
			{
				param = XmlUtil.toAttr(param);
			}
			else if (paramName.startsWith("#"))
			{
				param = XmlUtil.toHTMLText(param);
			}
			sb.append(template.substring(i, j));
			sb.append(param);
			i = k + 1;
		}
	}

	public String getSubject()
	{
		return this.sbSubj.toString();
	}

	public String getContent()
	{
		if (this.itemTemplate == null)
		{
			return this.sbPre.toString();
		}
		else
		{
			return this.sbPre.toString()+this.sbItem.toString()+this.sbPost.toString();
		}
	}
}
