package org.sswr.util.net.email;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.sswr.util.io.ResourceLoader;
import org.sswr.util.net.email.EmailTemplate.TemplateFormatException;
import org.sswr.util.net.email.EmailTemplate.TemplateItemException;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ResourceEmailMessageCreator implements EmailMessageCreator
{
	private Class<?> cls;

	public ResourceEmailMessageCreator(Class<?> cls)
	{
		this.cls = cls;
	}

	@Override
	@Nonnull
	public EmailTemplate createMessage(@Nonnull String tplName, @Nonnull Map<String, String> vars, @Nullable List<Map<String, String>> itemVars) throws IOException, TemplateFormatException, TemplateItemException
	{
		InputStream ins = ResourceLoader.load(this.cls, "email/"+tplName+".txt", null);
		if (ins == null)
			throw new IOException("Error in opening resource: email/"+tplName+".txt");
		EmailTemplate template = new EmailTemplate(ins, vars);
		ins.close();
		if (itemVars != null)
		{
			template.addItems(itemVars);
		}
		return template;
	}
	
}
