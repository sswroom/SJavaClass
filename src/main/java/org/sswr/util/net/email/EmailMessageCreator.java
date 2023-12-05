package org.sswr.util.net.email;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.sswr.util.net.email.EmailTemplate.TemplateFormatException;
import org.sswr.util.net.email.EmailTemplate.TemplateItemException;

public interface EmailMessageCreator
{
	public EmailTemplate createMessage(String tplName, Map<String, String> vars, List<Map<String, String>> itemVars) throws IOException, TemplateFormatException, TemplateItemException;
}
