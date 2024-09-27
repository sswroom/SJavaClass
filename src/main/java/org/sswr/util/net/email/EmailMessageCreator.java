package org.sswr.util.net.email;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.sswr.util.net.email.EmailTemplate.TemplateFormatException;
import org.sswr.util.net.email.EmailTemplate.TemplateItemException;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface EmailMessageCreator
{
	@Nonnull
	public EmailMessage createMessage(@Nonnull String tplName, @Nonnull Map<String, String> vars, @Nullable List<Map<String, String>> itemVars) throws IOException, TemplateFormatException, TemplateItemException;
}
