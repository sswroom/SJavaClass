package org.sswr.util.net;

import java.util.List;
import java.util.Objects;

import org.sswr.util.data.DataTools;

import jakarta.annotation.Nonnull;

public class ASN1ModuleInfo
{
	private String moduleName;
	private String moduleFileName;
	private List<String> objKeys;
	private List<ASN1ObjectInfo> objValues;
	private List<ASN1ObjectInfo> oidList;

	public ASN1ModuleInfo() {
	}

	public ASN1ModuleInfo(@Nonnull String moduleName, @Nonnull String moduleFileName, @Nonnull List<String> objKeys, @Nonnull List<ASN1ObjectInfo> objValues, @Nonnull List<ASN1ObjectInfo> oidList) {
		this.moduleName = moduleName;
		this.moduleFileName = moduleFileName;
		this.objKeys = objKeys;
		this.objValues = objValues;
		this.oidList = oidList;
	}

	@Nonnull
	public String getModuleName() {
		return this.moduleName;
	}

	public void setModuleName(@Nonnull String moduleName) {
		this.moduleName = moduleName;
	}

	@Nonnull
	public String getModuleFileName() {
		return this.moduleFileName;
	}

	public void setModuleFileName(@Nonnull String moduleFileName) {
		this.moduleFileName = moduleFileName;
	}

	@Nonnull
	public List<String> getObjKeys() {
		return this.objKeys;
	}

	public void setObjKeys(@Nonnull List<String> objKeys) {
		this.objKeys = objKeys;
	}

	@Nonnull
	public List<ASN1ObjectInfo> getObjValues() {
		return this.objValues;
	}

	public void setObjValues(@Nonnull List<ASN1ObjectInfo> objValues) {
		this.objValues = objValues;
	}

	@Nonnull
	public List<ASN1ObjectInfo> getOidList() {
		return this.oidList;
	}

	public void setOidList(@Nonnull List<ASN1ObjectInfo> oidList) {
		this.oidList = oidList;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof ASN1ModuleInfo)) {
			return false;
		}
		ASN1ModuleInfo sNMPModuleInfo = (ASN1ModuleInfo) o;
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

	public void toString(@Nonnull StringBuilder sb)
	{
		sb.append("Module ");
		sb.append(this.moduleName);
		sb.append("\r\n");
		ASN1ObjectInfo obj;
		int i = 0;
		int j = this.objValues.size();
		while (i < j)
		{
			obj = this.objValues.get(i);
			sb.append(obj.getObjectName());
			sb.append(", ");
			SNMPUtil.oidToString(obj.getOid(), 0, obj.getOidLen(), sb);
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
