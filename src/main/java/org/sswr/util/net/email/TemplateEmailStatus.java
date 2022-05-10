package org.sswr.util.net.email;

import java.util.List;
import java.util.Map;

public interface TemplateEmailStatus
{
	public String getTplname();
	public String getEmails();
	public Map<String, String> getParamObj();
	public String getItemParams();
	public List<Map<String, String>> getItemParamsObj();
	public void setEmailsSuccess(String emailList);
	public void setEmailsFail(String emailList);
	public void setStatus(EmailStatus status);
}
