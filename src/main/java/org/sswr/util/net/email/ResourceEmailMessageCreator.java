package org.sswr.util.net.email;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.sswr.util.io.ResourceLoader;
import org.sswr.util.net.email.EmailTemplate.TemplateFormatException;
import org.sswr.util.net.email.EmailTemplate.TemplateItemException;

public class ResourceEmailMessageCreator implements EmailMessageCreator
{
	private Class<?> cls;

	public ResourceEmailMessageCreator(Class<?> cls)
	{
		this.cls = cls;
	}

	@Override
	public EmailMessage createMessage(String tplName, Map<String, String> vars, List<Map<String, String>> itemVars) throws IOException, TemplateFormatException, TemplateItemException
	{
		EmailTemplate template = new EmailTemplate(ResourceLoader.load(this.cls, "email/"+tplName+".txt", null), vars);
		if (itemVars != null)
		{
			template.addItems(itemVars);
		}
		return template;
	}
	
}
