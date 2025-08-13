package org.sswr.util.net;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.data.StringUtil;
import org.sswr.util.io.FileStream;
import org.sswr.util.io.UTF8Reader;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.sswr.util.io.FileStream.BufferType;
import org.sswr.util.io.FileStream.FileMode;
import org.sswr.util.io.FileStream.FileShare;

public class ASN1MIB
{
	private static final String DEBUGOBJ = "gdStatusBytes";

	private Map<String, ASN1ModuleInfo> moduleMap;
	private ASN1ModuleInfo globalModule;

	private static int calcLineSpace(@Nonnull String txt)
	{
		char carr[] = txt.toCharArray();
		int i = 0;
		int j = carr.length;
		int ret = 0;
		char c;
		while (i < j)
		{
			c = carr[i];
			if (c == ' ')
			{
				ret++;
			}
			else if (c == '\t')
			{
				ret += 4;
			}
			else
			{
				return ret;
			}
			i++;
		}
		return ret;
	}

	private static void moduleAppendOID(@Nonnull ASN1ModuleInfo module, @Nonnull ASN1ObjectInfo obj)
	{
		int i;
		int j;
		int k;
		int l;
		ASN1ObjectInfo obj2;
		List<ASN1ObjectInfo> oidList = module.getOidList();
		if (obj.getOidLen() == 0)
			return;
		i = 0;
		j = oidList.size() - 1;
		while (i <= j)
		{
			k = (i + j) >> 1;
			obj2 = oidList.get(k);
			l = SNMPUtil.oidCompare(obj2.getOid(), obj2.getOidLen(), obj.getOid(), obj.getOidLen());
			if (l > 0)
			{
				j = k - 1;
			}
			else if (l < 0)
			{
				i = k + 1;
			}
			else
			{
				return;
			}
		}
		oidList.add(i, obj);
	}

	private boolean parseObjectOID(@Nonnull ASN1ModuleInfo module, @Nonnull ASN1ObjectInfo obj, @Nonnull String s, @Nonnull StringBuilder errMessage)
	{
		char sarr[] = s.toCharArray();
		int sofst = 0;
		String oriS = s;
		char c;
		int oidNameOfst;
		int oidNameLen;
		int oidNextLevOfst;
		int oidNextLen;
		boolean isFirst = false;
		StringBuilder sb = new StringBuilder();
		while (true)
		{
			c = sarr[sofst++];
			if (c == ' ')
			{
	
			}
			else if (c == '{')
			{
				break;
			}
			else
			{
				errMessage.append("OID Format error: \"");
				errMessage.append(oriS);
				errMessage.append("\"");
				return false;
			}
		}
		while (true)
		{
			c = sarr[sofst++];
			if (c == ' ')
			{
	
			}
			else if (c == '}' || c == 0)
			{
				errMessage.append("OID Format error: \"");
				errMessage.append(oriS);
				errMessage.append("\"");
				return false;
			}
			else
			{
				oidNameOfst = sofst - 1;
				break;
			}
		}
		while (true)
		{
			c = sarr[sofst++];
			if (c == ' ')
			{
				oidNameLen = (sofst - oidNameOfst - 1);
				break;
			}
			else if (c == '}' || c == 0)
			{
				errMessage.append("OID Format error: \"");
				errMessage.append(oriS);
				errMessage.append("\"");
				return false;
			}
		}
		sb.setLength(0);
		sb.append(new String(sarr, oidNameOfst, oidNameLen));
		if (sb.toString().equals("iso"))
		{
			obj.setOid(0, (byte)40);
			obj.setOidLen(1);
			isFirst = true;
		}
		else if (sb.toString().equals("iso(1)"))
		{
			obj.setOid(0, (byte)40);
			obj.setOidLen(1);
			isFirst = true;
		}
		else if (sb.toString().equals("joint-iso-ccitt(2)") || sb.toString().equals("joint-iso-itu-t(2)") || sb.toString().equals("joint-iso-itu-t") || sb.toString().equals("2"))
		{
			obj.setOid(0, (byte)80);
			obj.setOidLen(1);
			isFirst = true;
		}
		else if (sb.toString().equals("0"))
		{
			obj.setOid(0, (byte)0);
			obj.setOidLen(1);
			isFirst = true;
		}
		else
		{
			int i = StringUtil.sortedIndexOf(module.getObjKeys(), sb.toString());
			ASN1ObjectInfo obj2;
			if (i < 0)
			{
	//			i = this.globalModule.objKeys.SortedIndexOf(sb.ToString());
	//			if (i < 0)
	//			{
					errMessage.append(obj.getObjectName());
					errMessage.append(": OID Name \"");
					errMessage.append(sb.toString());
					errMessage.append("\" not found");
					return false;
	//			}
	//			else
	//			{
	//				obj2 = this.globalModule.objValues.GetItem(i);
	//			}
			}
			else
			{
				obj2 = module.getObjValues().get(i);
			}
			if (obj2.getOidLen() == 0)
			{
				String typeVal;
				if ((typeVal = obj2.getTypeVal()) != null)
				{
					if (!parseObjectOID(module, obj2, typeVal, errMessage))
					{
						return false;
					}
				}
			}
			if (obj2.getOidLen() == 0)
			{
				errMessage.append("OID Name \"");
				errMessage.append(sb.toString());
				errMessage.append("\" is not OID");
				return false;
			}
			obj.copyOidFrom(obj2);
		}
		
		while (true)
		{
			while (true)
			{
				c = sarr[sofst++];
				if (c == ' ')
				{
	
				}
				else if (c == '}')
				{
					while (true)
					{
						if (sofst >= sarr.length)
						{
							return true;
						}
						c = sarr[sofst++];
						if (c == ' ')
						{
						}
						else
						{
							errMessage.append("OID Format error: \"");
							errMessage.append(oriS);
							errMessage.append("\"");
							return false;
						}
					}
				}
				else
				{
					oidNextLevOfst = sofst - 1;
					break;
				}
			}
			while (true)
			{
				c = sarr[sofst++];
				if (c == ' ' || c == '}')
				{
					oidNextLen = (sofst - oidNextLevOfst - 1);
					if (c == '}')
					{
						sofst--;
					}
					break;
				}
			}
	
			Integer v;
			int i;
			int j;
			sb.setLength(0);
			sb.append(new String(sarr, oidNextLevOfst, oidNextLen));
			i = sb.indexOf("(");
			j = sb.indexOf(")");
			if (i >= 0 && j > i)
			{
				v = StringUtil.toInteger(sb.substring(i + 1, j));
				if (v == null)
				{
					errMessage.append("OID Format error: \"");
					errMessage.append(oriS);
					errMessage.append("\"");
					return false;
				}
			}
			else
			{
				v = StringUtil.toInteger(sb.toString());
				if (v == null)
				{
					errMessage.append("OID Format error: \"");
					errMessage.append(oriS);
					errMessage.append("\"");
					return false;
				}
			}
			
			if (v < 128)
			{
				if (isFirst)
				{
					obj.setOid(0, (byte)(obj.getOidByte(0) + v));
					isFirst = false;
				}
				else
				{
					obj.appendOid((byte)v.intValue());
				}
				
			}
			else if (v < 0x4000)
			{
				obj.appendOid((byte)(0x80 | (v >> 7)));
				obj.appendOid((byte)(v & 0x7f));
			}
			else if (v < 0x200000)
			{
				obj.appendOid((byte)(0x80 | (v >> 14)));
				obj.appendOid((byte)(0x80 | ((v >> 7) & 0x7f)));
				obj.appendOid((byte)(v & 0x7f));
			}
			else if (v < 0x10000000)
			{
				obj.appendOid((byte)(0x80 | (v >> 21)));
				obj.appendOid((byte)(0x80 | ((v >> 14) & 0x7f)));
				obj.appendOid((byte)(0x80 | ((v >> 7) & 0x7f)));
				obj.appendOid((byte)(v & 0x7f));
			}
			else
			{
				obj.appendOid((byte)(0x80 | ((v >> 28) & 0xf)));
				obj.appendOid((byte)(0x80 | ((v >> 21) & 0x7f)));
				obj.appendOid((byte)(0x80 | ((v >> 14) & 0x7f)));
				obj.appendOid((byte)(0x80 | ((v >> 7) & 0x7f)));
				obj.appendOid((byte)(v & 0x7f));
			}
		}
	}

	private boolean parseObjectBegin(@Nonnull UTF8Reader reader, @Nullable ASN1ObjectInfo obj, @Nonnull StringBuilder errMessage)
	{
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		int i;
		while (true)
		{
			sb.clearStr();
			if (!reader.readLine(sb, 512))
			{
				errMessage.append("Object end not found");
				return false;
			}
	
			i = sb.indexOf("--");
			if (i >= 0)
			{
				sb.trimToLength(i);
			}
			sb.rTrim();
			if (sb.getLength() > 0)
			{
				if (sb.endsWith("BEGIN"))
				{
					errMessage.append("Nested begin found");
					return false;
				}
				else if (sb.endsWith("END"))
				{
					return true;
				}
			}
		}
	}

	private boolean parseModule(@Nonnull UTF8Reader reader, @Nonnull ASN1ModuleInfo module, @Nonnull StringBuilder errMessage)
	{
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		int i;
		int j;
		int lineSpace;
		ASN1ObjectInfo obj;
		ASN1ObjectInfo currObj = null;
		StringBuilderUTF8 sbObjValName = new StringBuilderUTF8();
		StringBuilderUTF8 sbObjValCont = new StringBuilderUTF8();
		int objLineSpace = 0;
		boolean objIsEqual = false;
		boolean objIsBrk = false;
		boolean succ;
		boolean isQuotedText = false;
		String objectName;
		
		while (true)
		{
			sb.clearStr();
			if (!reader.readLine(sb, 512))
			{
				errMessage.append("Module end not found");
				return false;
			}
	
			i = sb.indexOf("--");
			if (i >= 0)
			{
				sb.trimToLength(i);
			}
			sb.rTrim();;
			if (currObj != null && (objectName = currObj.getObjectName()) != null && objectName.equals("PSSEQStringEntry"))
			{
				i = 0;
			}
	
			if (sb.getLength() > 0)
			{
				if (sb.endsWith("BEGIN"))
				{
					succ = parseObjectBegin(reader, null, errMessage);
					if (!succ)
					{
						return succ;
					}
					currObj = null;
				}
				else if (sb.endsWith("END"))
				{
					List<ASN1ObjectInfo> objList = module.getObjValues();
					ASN1ObjectInfo iobj;
					String typeName;
					String typeVal;
					int ui = 0;
					int uj = objList.size();
					while (ui < uj)
					{
						iobj = objList.get(ui);
						if ((typeName = iobj.getTypeName()) != null && (typeVal = iobj.getTypeVal()) != null && iobj.getOidLen() == 0 && !typeName.equals("TRAP-TYPE") && !typeVal.equals("Imported Value"))
						{
							succ = this.parseObjectOID(module, iobj, typeVal, errMessage);
							if (!succ)
							{
								return false;
							}
							moduleAppendOID(module, iobj);
							moduleAppendOID(this.globalModule, iobj);
						}
						else if (iobj.getOidLen() > 0)
						{
							moduleAppendOID(module, iobj);
							moduleAppendOID(this.globalModule, iobj);
						}
						ui++;
					}
					return true;
				}
				else if (isQuotedText)
				{
					if (currObj != null)
					{
						sbObjValCont.append(sb.toString());
					}
					if (sb.endsWith("\""))
					{
						isQuotedText = false;
						if (currObj != null)
						{
							if (sbObjValName.getLength() > 0 && sbObjValCont.getLength() > 0)
							{
								currObj.getValName().add(sbObjValName.toString());
								currObj.getValCont().add(sbObjValCont.toString());
							}
							sbObjValName.clearStr();
							sbObjValCont.clearStr();
						}
					}
					else
					{
						reader.getLastLineBreak(sbObjValCont);
					}
					
				}
				else
				{
					lineSpace = calcLineSpace(sb.toString());
					sb.trim();
					if (currObj != null && (objIsBrk || objIsEqual || (lineSpace > objLineSpace && sb.charAt(0) >= 'A' && sb.charAt(0) <= 'Z') || sb.startsWith("::=") || sb.startsWith("{") || sb.startsWith("\"")))
					{
						if (objIsBrk)
						{
							if (objIsEqual)
							{
								currObj.setTypeVal(currObj.getTypeVal() + ' ' + sb.toString());
							}
							else
							{
								sbObjValCont.append(sb.toString());
							}
							
							if (sb.endsWith("}"))
							{
								objIsBrk = false;
								objIsEqual = false;
								if (sbObjValName.getLength() > 0 && sbObjValCont.getLength() > 0)
								{
									currObj.getValName().add(sbObjValName.toString());
									currObj.getValCont().add(sbObjValCont.toString());
								}
								sbObjValName.clearStr();
								sbObjValCont.clearStr();
								if (currObj.getTypeName() == null)
								{
									String typeVal = currObj.getTypeVal();
									if (typeVal != null)
									{
										StringBuilder sbTmp = new StringBuilder();
										char carr[] = typeVal.toCharArray();
										char tmpC;
										int tmpInd = 0;
										int tmpCnt = carr.length;
										boolean lastIsWs = false;
										while (tmpInd < tmpCnt)
										{
											tmpC = carr[tmpInd];
											if (tmpC == '\r' || tmpC == '\n' || tmpC == '\t' || tmpC == ' ')
											{
												if (!lastIsWs)
												{
													sbTmp.append(' ');
												}
												lastIsWs = true;
											}
											else
											{
												lastIsWs = false;
												sbTmp.append(tmpC);
											}
											tmpInd++;
										}
										currObj.setTypeVal(sbTmp.toString());
									}
									currObj = null;
								}
							}
						}
						else if (objIsEqual)
						{
							if (sb.startsWith("[") && sb.endsWith("]"))
							{
	
							}
							else
							{
								currObj.setTypeVal(sb.toString());
								i = sb.toString().indexOf('{');
								j = sb.toString().indexOf('}');
								if (i >= 0)
								{
									if (j > i)
									{
										currObj = null;
										objIsEqual = false;
									}
									else
									{
										objIsBrk = true;
									}
								}
								else
								{
									currObj = null;
									objIsEqual = false;
								}
							}
						}
						else if (sb.startsWith("::="))
						{
							if (sbObjValName.getLength() > 0 && sbObjValCont.getLength() > 0)
							{
								currObj.getValName().add(sbObjValName.toString());
								currObj.getValCont().add(sbObjValCont.toString());
							}
							sbObjValName.clearStr();
							sbObjValCont.clearStr();
	
							i = 3;
							while (sb.charAt(i) == ' ' || sb.charAt(i) == '\t')
							{
								i++;
							}
							String typeVal;
							currObj.setTypeVal(typeVal = sb.substring(i));
							if (currObj.getTypeName() != null && typeVal.endsWith("}"))
							{
								currObj = null;
								objIsBrk = false;
								objIsEqual = false;
							}							
							else if (typeVal.endsWith("{"))
							{
								objIsBrk = true;
								objIsEqual = true;
							}
						}
						else if (sb.startsWith("{"))
						{
							sbObjValCont.append(sb.toString());
							if (sb.endsWith("}"))
							{
								objIsBrk = false;
								objIsEqual = false;
								if (sbObjValName.getLength() > 0 && sbObjValCont.getLength() > 0)
								{
									currObj.getValName().add(sbObjValName.toString());
									currObj.getValCont().add(sbObjValCont.toString());
								}
								sbObjValName.clearStr();
								sbObjValCont.clearStr();
							}
							else
							{
								objIsBrk = true;
								objIsEqual = false;
							}
						}
						else if (sb.startsWith("\""))
						{
							sbObjValCont.append(sb.toString());
							if (sb.getLength() > 1 && sb.endsWith("\""))
							{
								if (sbObjValName.getLength() > 0 && sbObjValCont.getLength() > 0)
								{
									currObj.getValName().add(sbObjValName.toString());
									currObj.getValCont().add(sbObjValCont.toString());
								}
								sbObjValName.clearStr();
								sbObjValCont.clearStr();
							}
							else
							{
								isQuotedText = true;
							}
						}
						else
						{
							boolean proc = false;
							if (sbObjValName.getLength() > 0 && sbObjValCont.getLength() > 0)
							{
								if (sb.startsWith("{") || sb.startsWith("\""))
								{
									sbObjValCont.appendUTF8Char((byte)' ');
									sbObjValCont.append(sb.toString());
									proc = true;
								}
								else
								{
									currObj.getValName().add(sbObjValName.toString());
									currObj.getValCont().add(sbObjValCont.toString());
									sbObjValName.clearStr();
									sbObjValCont.clearStr();
								}
							}
							else if (sbObjValName.getLength() > 0)
							{
								sbObjValCont.append(sb.toString());
								proc = true;
							}
	
							if (!proc)
							{
								i = sb.indexOf(" ");
								j = sb.indexOf("\t");
								if (i < 0)
								{
									i = j;
								}
								else if (j >= 0 && j < i)
								{
									i = j;
								}
								
								if (i < 0)
								{
									sbObjValName.append(sb.toString());
								}
								else
								{
									sbObjValName.append(sb.substring(0, i));
									sbObjValCont.append(sb.substring(i + 1));
									sbObjValCont.trim();
								}
								
							}
	
							if ((i = sb.indexOf("\"")) >= 0)
							{
								j = sb.indexOf("\"", i + 1);
								if (j < 0)
								{
									reader.getLastLineBreak(sbObjValCont);
									isQuotedText = true;
								}
							}
							else if ((i = sb.indexOf("{")) >= 0)
							{
								j = sb.indexOf("}");
								if (j > i)
								{
									if (sbObjValName.getLength() > 0 && sbObjValCont.getLength() > 0)
									{
										currObj.getValName().add(sbObjValName.toString());
										currObj.getValCont().add(sbObjValCont.toString());
									}
									sbObjValName.clearStr();
									sbObjValCont.clearStr();
								}
								else
								{
									objIsBrk = true;
									objIsEqual = false;
								}
							}
						}
					}
					else if (sb.startsWith("IMPORTS"))
					{
						boolean isEnd = false;
						StringBuilder impObjNames = new StringBuilder();
						ASN1ModuleInfo impModule;
						ASN1ObjectInfo impObj;
						ASN1ObjectInfo impObj2;
						String []impSarr;
						int impCnt;
						int impInd;
						sb.removeRange(0, 7);
						sb.trim();
	
						while (!isEnd)
						{
							if (sb.endsWith(";"))
							{
								isEnd = true;
								sb.trimToLength(sb.getLength() - 1);
							}
							i = sb.indexOf("FROM ");
							if (i >= 0)
							{
								impObjNames.append(sb.substring(0, i));
								StringUtil.trimRight(impObjNames);
								if ((impModule = this.moduleMap.get(sb.substring(i + 5))) != null)
								{
								
								}
								else
								{
									String moduleFileName = module.getModuleFileName();
									j = moduleFileName.lastIndexOf(File.separator);
									succ = loadFile(moduleFileName.substring(0, j + 1) + sb.substring(i + 5), errMessage);
									if (!succ)
									{
										return false;
									}
									impModule = this.moduleMap.get(sb.substring(i + 5));
								}
								
								if (impModule == null)
								{
									errMessage.append("IMPORTS module ");
									errMessage.append(sb.toString() + i + 5);
									errMessage.append(" not found");
									return false;
								}
								impSarr = StringUtil.split(impObjNames.toString(), ",");
								impCnt = 0;
								while (impCnt < impSarr.length)
								{
									impSarr[impCnt] = impSarr[impCnt].trim();
									impInd = StringUtil.sortedIndexOf(impModule.getObjKeys(), impSarr[impCnt]);
									if (impInd >= 0)
									{
										impObj = impModule.getObjValues().get(impInd);
										impObj2 = new ASN1ObjectInfo();
										impObj2.setObjectName(objectName = impSarr[impCnt]);
										if (impObj.getTypeName() != null)
										{
											impObj2.setTypeName(impObj.getTypeName());
										}
										else
										{
											impObj2.setTypeName(null);
										}
										impObj2.setTypeVal("Imported Value");
										impObj2.copyOidFrom(impObj);
										impObj2.setValName(new ArrayList<String>());
										impObj2.setValCont(new ArrayList<String>());
										int ui = 0;
										int uj = impObj.getValName().size();
										while (ui < uj)
										{
											impObj2.getValName().add(impObj.getValName().get(ui));
											impObj2.getValCont().add(impObj.getValCont().get(ui));
											ui++;
										}
										
										ui = StringUtil.sortedInsert(module.getObjKeys(), objectName);
										module.getObjValues().add(ui, impObj2);
									}
									else
									{
										errMessage.append("IMPORTS object ");
										errMessage.append(impSarr[impCnt]);
										errMessage.append(" in module ");
										errMessage.append(sb.substring(i + 5));
										errMessage.append(" not found");
										return false;
									}
									impCnt++;
								}
								impObjNames.setLength(0);
							}
							else
							{
								impObjNames.append(sb.toString());
							}
							if (isEnd)
							{
								break;
							}
							sb.clearStr();
							if (!reader.readLine(sb, 512))
							{
								errMessage.append("IMPORTS end not found");
								return false;
							}
	
							i = sb.indexOf("--");
							if (i >= 0)
							{
								sb.trimToLength(i);
							}
							sb.trim();
						}
					}
					else if (sb.startsWith("EXPORTS"))
					{
						while (true)
						{
							if (sb.endsWith(";"))
							{
								break;
							}
	
							sb.clearStr();
							if (!reader.readLine(sb, 512))
							{
								errMessage.append("EXPORTS end not found");
								return false;
							}
	
							i = sb.indexOf("--");
							if (i >= 0)
							{
								sb.trimToLength(i);
							}
							sb.trim();
						}
					}
					else
					{
						if (currObj != null)
						{
							if (sbObjValName.getLength() > 0 && sbObjValCont.getLength() > 0)
							{
								currObj.getValName().add(sbObjValName.toString());
								currObj.getValCont().add(sbObjValCont.toString());
							}
							sbObjValName.clearStr();
							sbObjValCont.clearStr();
						}
						currObj = null;
						i = sb.indexOf("::=");
						if (i == 0)
						{
							errMessage.append("::= found at non object location");
							return false;
						}
						if (i < 0)
						{
							i = sb.indexOf(" ");
							j = sb.indexOf("\t");
							if (i < 0)
							{
								i = j;
							}
							else if (j >= 0 && j < i)
							{
								i = j;
							}
							if (i >= 0)
							{
								i = -1;
							}
							else
							{
								i = sb.getLength();
								if (!reader.readLine(sb, 512))
								{
									errMessage.append("Unknown format: ");
									errMessage.append(sb.toString());
									return false;
								}
								j = sb.indexOf("--");
								if (j >= 0)
								{
									sb.trimToLength(j);
								}
								sb.rTrim();
								if (sb.charAt(i) == ' ' || sb.charAt(i) == '\t')
								{
									i = sb.indexOf("::=");
								}
								else
								{
									errMessage.append("Unknown format: ");
									errMessage.append(sb.toString());
									return false;
								}
							}
						}
						if (i >= 0)
						{
							j = i;
							int k;
							while (sb.charAt(j - 1) == ' ' || sb.charAt(j - 1) == '\t')
							{
								j--;
							}
							k = sb.indexOf(" ");
							int l = sb.indexOf("\t");
							if (k < 0)
							{
								k = l;
							}
							else if (l >= 0 && l < k)
							{
								k = l;
							}
							obj = new ASN1ObjectInfo();
							obj.setObjectName(objectName = sb.substring(0, k));
							if (j > k)
							{
								while (sb.charAt(k) == ' ' || sb.charAt(k) == '\t')
								{
									k++;
								}
								obj.setTypeName(sb.substring(k, j));
							}
							else
							{
								obj.setTypeName(null);
							}
							obj.setTypeVal(null);
							obj.setOidLen(0);
							obj.setValName(new ArrayList<String>());
							obj.setValCont(new ArrayList<String>());
							int ui = StringUtil.sortedInsert(module.getObjKeys(), objectName);
							module.getObjValues().add(ui, obj);
							ui = StringUtil.sortedInsert(this.globalModule.getObjKeys(), objectName);
							this.globalModule.getObjValues().add(ui, obj);
							if (objectName.equals(DEBUGOBJ))
							{
								currObj = null;
							}
	
							if (sb.endsWith("::="))
							{
								currObj = obj;
								objLineSpace = lineSpace;
								objIsEqual = true;
								sbObjValName.clearStr();
								sbObjValCont.clearStr();
							}
							else
							{
								String typeVal;
								i += 3;
								while (sb.charAt(i) == ' ' || sb.charAt(i) == '\t')
								{
									i++;
								}
								obj.setTypeVal(typeVal = sb.substring(i));
								currObj = obj;
								sbObjValName.clearStr();
								sbObjValCont.clearStr();
								objLineSpace = lineSpace;
								objIsEqual = false;
								objIsBrk = false;
								if (typeVal.endsWith("{"))
								{
									objIsBrk = true;
									objIsEqual = true;
								}
								else if (typeVal.endsWith("}"))
								{
									currObj = null;
								}
							}
						}
						else
						{
							i = sb.indexOf(" ");
							j = sb.indexOf("\t");
							if (i < 0)
							{
								i = j;
							}
							else if (j >= 0 && j < i)
							{
								i = j;
							}
							if (i < 0)
							{
								i = sb.getLength();
								if (!reader.readLine(sb, 512))
								{
									errMessage.append("Unknown format: ");
									errMessage.append(sb.toString());
									return false;
								}
								j = sb.indexOf("--");
								if (j >= 0)
								{
									sb.trimToLength(j);
								}
								sb.rTrim();
								if (sb.charAt(i) == ' ' || sb.charAt(i) == '\t')
								{
								}
								else
								{
									errMessage.append("Unknown format: ");
									errMessage.append(sb.toString());
									return false;
								}
							}
	
							obj = new ASN1ObjectInfo();
							obj.setObjectName(objectName = sb.substring(0, i));
							while (sb.charAt(i) == ' ' || sb.charAt(i) == '\t')
							{
								i++;
							}
							obj.setTypeName(sb.substring(i));
							obj.setTypeVal(null);
							obj.setOidLen(0);
							obj.setValName(new ArrayList<String>());
							obj.setValCont(new ArrayList<String>());
							int ui = StringUtil.sortedInsert(module.getObjKeys(), objectName);
							module.getObjValues().add(ui, obj);
							ui = StringUtil.sortedInsert(this.globalModule.getObjKeys(), objectName);
							this.globalModule.getObjValues().add(ui, obj);
							if (objectName.equals(DEBUGOBJ))
							{
								currObj = obj;
							}
	
							currObj = obj;
							objLineSpace = lineSpace;
							objIsEqual = false;
							sbObjValName.clearStr();
							sbObjValCont.clearStr();
						}
					}
				}
			}
		}
	}

	public ASN1MIB()
	{
		this.moduleMap = new HashMap<String, ASN1ModuleInfo>();
		this.globalModule = new ASN1ModuleInfo();
		this.globalModule.setObjKeys(new ArrayList<String>());
		this.globalModule.setObjValues(new ArrayList<ASN1ObjectInfo>());
		this.globalModule.setOidList(new ArrayList<ASN1ObjectInfo>());
	}

	@Nonnull
	public ASN1ModuleInfo getGlobalModule()
	{
		return this.globalModule;		
	}

	@Nullable
	public ASN1ModuleInfo getModuleByFileName(@Nonnull String fileName)
	{
		Iterator<ASN1ModuleInfo> itMmodule = this.moduleMap.values().iterator();
		ASN1ModuleInfo module;
		while (itMmodule.hasNext())
		{
			module = itMmodule.next();
			if (module.getModuleFileName() != null && module.getModuleFileName().equals(fileName))
				return module;
		}
		return null;
	}

	public void unloadAll()
	{
		this.moduleMap.clear();
	
		this.globalModule.getOidList().clear();
		this.globalModule.getObjKeys().clear();
		this.globalModule.getObjValues().clear();
	}

	public boolean loadFile(@Nonnull String fileName, @Nonnull StringBuilder errMessage)
	{
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		FileStream fs;
		UTF8Reader reader;
		int i;
		ASN1ModuleInfo module;
		boolean moduleFound = false;
		boolean succ;
		fs = new FileStream(fileName, FileMode.ReadOnly, FileShare.DenyNone, BufferType.Normal);
		if (fs.isError())
		{
			errMessage.append("Error in opening file ");
			errMessage.append(fileName);
			return false;
		}
		succ = false;
		reader = new UTF8Reader(fs.createInputStream());
		while (true)
		{
			sb.clearStr();
			if (!reader.readLine(sb, 512))
			{
				if (!moduleFound)
				{
					errMessage.append("Module definition not found");
				}
				break;
			}
	
			i = sb.indexOf("--");
			if (i >= 0)
			{
				sb.trimToLength(i);
			}
			sb.trim();
			if (sb.getLength() > 0)
			{
				if (moduleFound)
				{
					errMessage.append("Object found after Module definition");
					succ = false;
					break;
				}
				i = sb.indexOf(" DEFINITIONS ::= BEGIN");
				if (i < 0)
				{
					succ = false;
					errMessage.append("Wrong Module definition format");
					break;
				}
				sb.trimToLength(i);
				if (this.moduleMap.get(sb.toString()) != null)
				{
					errMessage.append("Module ");
					errMessage.append(sb.toString());
					errMessage.append(" already loaded");
					break;
				}
				module = new ASN1ModuleInfo();
				module.setModuleName(sb.toString());
				module.setModuleFileName(fileName);
				module.setObjKeys(new ArrayList<String>());
				module.setObjValues(new ArrayList<ASN1ObjectInfo>());
				module.setOidList(new ArrayList<ASN1ObjectInfo>());
				this.moduleMap.put(module.getModuleName(), module);
				succ = this.parseModule(reader, module, errMessage);
				moduleFound = true;
				if (!succ)
				{
					break;
				}
			}
	
		}
		reader.close();
		fs.close();
		return succ;
	}

	public void toString(@Nonnull StringBuilder sb)
	{
		Iterator<ASN1ModuleInfo> itModules = this.moduleMap.values().iterator();
		while (itModules.hasNext())
		{
			itModules.next().toString(sb);
			sb.append("\r\n");
		}
	}

	@Nonnull
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}
}
