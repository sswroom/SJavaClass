package org.sswr.util.net.email;

import java.io.IOException;
import java.util.Map;

import org.sswr.util.io.ResourceLoader;
import org.sswr.util.net.email.EmailTemplate.TemplateFormatException;
import org.sswr.util.net.email.EmailTemplate.TemplateItemException;

public class ResourceEmailTemplateCreator implements EmailTemplateCreator
{
	private Class<?> cls;

	public ResourceEmailTemplateCreator(Class<?> cls)
	{
		this.cls = cls;
	}

	@Override
	public EmailTemplate createTemplate(String tplName, Map<String, String> vars) throws IOException, TemplateFormatException, TemplateItemException
	{
		return new EmailTemplate(ResourceLoader.load(this.cls, "email/"+tplName+".txt", null), vars);
	}
	
}
