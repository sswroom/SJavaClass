package org.sswr.util.net.email;

import java.io.IOException;
import java.util.Map;

import org.sswr.util.net.email.EmailTemplate.TemplateFormatException;
import org.sswr.util.net.email.EmailTemplate.TemplateItemException;

public interface EmailTemplateCreator
{
	public EmailTemplate createTemplate(String tplName, Map<String, String> vars) throws IOException, TemplateFormatException, TemplateItemException;
}
