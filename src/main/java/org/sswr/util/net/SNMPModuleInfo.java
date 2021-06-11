package org.sswr.util.net;

import java.util.List;
import java.util.Objects;

import org.sswr.util.data.DataTools;

public class SNMPModuleInfo
{
	private String moduleName;
	private String moduleFileName;
	private List<String> objKeys;
	private List<SNMPObjectInfo> objValues;
	private List<SNMPObjectInfo> oidList;

	public SNMPModuleInfo() {
	}

	public SNMPModuleInfo(String moduleName, String moduleFileName, List<String> objKeys, List<SNMPObjectInfo> objValues, List<SNMPObjectInfo> oidList) {
		this.moduleName = moduleName;
		this.moduleFileName = moduleFileName;
		this.objKeys = objKeys;
		this.objValues = objValues;
		this.oidList = oidList;
	}

	public String getModuleName() {
		return this.moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getModuleFileName() {
		return this.moduleFileName;
	}

	public void setModuleFileName(String moduleFileName) {
		this.moduleFileName = moduleFileName;
	}

	public List<String> getObjKeys() {
		return this.objKeys;
	}

	public void setObjKeys(List<String> objKeys) {
		this.objKeys = objKeys;
	}

	public List<SNMPObjectInfo> getObjValues() {
		return this.objValues;
	}

	public void setObjValues(List<SNMPObjectInfo> objValues) {
		this.objValues = objValues;
	}

	public List<SNMPObjectInfo> getOidList() {
		return this.oidList;
	}

	public void setOidList(List<SNMPObjectInfo> oidList) {
		this.oidList = oidList;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof SNMPModuleInfo)) {
			return false;
		}
		SNMPModuleInfo sNMPModuleInfo = (SNMPModuleInfo) o;
		return Objects.equals(moduleName, sNMPModuleInfo.moduleName) && Objects.equals(moduleFileName, sNMPModuleInfo.moduleFileName) && Objects.equals(objKeys, sNMPModuleInfo.objKeys) && Objects.equals(objValues, sNMPModuleInfo.objValues) && Objects.equals(oidList, sNMPModuleInfo.oidList);
	}

	@Override
	public int hashCode() {
		return Objects.hash(moduleName, moduleFileName, objKeys, objValues, oidList);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}

	public void toString(StringBuilder sb)
	{
		sb.append("Module ");
		sb.append(this.moduleName);
		sb.append("\r\n");
		SNMPObjectInfo obj;
		int i = 0;
		int j = this.objValues.size();
		while (i < j)
		{
			obj = this.objValues.get(i);
			sb.append(obj.getObjectName());
			sb.append(", ");
			SNMPUtil.oidToString(obj.getOid(), obj.getOidLen(), sb);
			sb.append(", ");
			if (obj.getTypeName() != null)
			{
				sb.append(obj.getTypeName());
			}
			sb.append(", ");
			if (obj.getTypeVal() != null)
			{
				sb.append(obj.getTypeVal());
			}
			sb.append("\r\n");
			i++;
		}
	}
}
