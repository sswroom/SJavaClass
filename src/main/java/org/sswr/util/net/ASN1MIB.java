package org.sswr.util.net;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sswr.util.data.StringUtil;
import org.sswr.util.io.FileStream;
import org.sswr.util.io.UTF8Reader;
import org.sswr.util.io.FileStream.BufferType;
import org.sswr.util.io.FileStream.FileMode;
import org.sswr.util.io.FileStream.FileShare;

public class ASN1MIB
{
	private static final String DEBUGOBJ = "gdStatusBytes";

	private Map<String, ASN1ModuleInfo> moduleMap;
	private ASN1ModuleInfo globalModule;

	private static int calcLineSpace(String txt)
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

	private static void moduleAppendOID(ASN1ModuleInfo module, ASN1ObjectInfo obj)
	{
		int i;
		int j;
		int k;
		int l;
		ASN1ObjectInfo obj2;
		List<ASN1ObjectInfo> oidList = module.getOidList();
		if (obj.getOid() == null)
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

	private boolean parseObjectOID(ASN1ModuleInfo module, ASN1ObjectInfo obj, String s, StringBuilder errMessage)
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
					errMessage.append("OID Name \"");
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
				if (obj2.getTypeVal() != null)
				{
					if (!parseObjectOID(module, obj2, obj2.getTypeVal(), errMessage))
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

	private boolean parseObjectBegin(UTF8Reader reader, ASN1ObjectInfo obj, StringBuilder errMessage)
	{
		StringBuilder sb = new StringBuilder();
		int i;
		while (true)
		{
			sb.setLength(0);
			if (!reader.readLine(sb, 512))
			{
				errMessage.append("Object end not found");
				return false;
			}
	
			i = sb.indexOf("--");
			if (i >= 0)
			{
				sb.setLength(i);
			}
			StringUtil.trimRight(sb);
			if (sb.length() > 0)
			{
				if (StringUtil.endsWith(sb, "BEGIN"))
				{
					errMessage.append("Nested begin found");
					return false;
				}
				else if (StringUtil.endsWith(sb, "END"))
				{
					return true;
				}
			}
		}
	}

	private boolean parseModule(UTF8Reader reader, ASN1ModuleInfo module, StringBuilder errMessage)
	{
		StringBuilder sb = new StringBuilder();
		int i;
		int j;
		int lineSpace;
		ASN1ObjectInfo obj;
		ASN1ObjectInfo currObj = null;
		StringBuilder sbObjValName = new StringBuilder();
		StringBuilder sbObjValCont = new StringBuilder();
		int objLineSpace = 0;
		boolean objIsEqual = false;
		boolean objIsBrk = false;
		boolean succ;
		boolean isQuotedText = false;
		
		while (true)
		{
			sb.setLength(0);
			if (!reader.readLine(sb, 512))
			{
				errMessage.append("Module end not found");
				return false;
			}
	
			i = sb.indexOf("--");
			if (i >= 0)
			{
				sb.setLength(i);
			}
			StringUtil.trimRight(sb);
			if (currObj != null && currObj.getObjectName() != null && currObj.getObjectName().equals("PSSEQStringEntry"))
			{
				i = 0;
			}
	
			if (sb.length() > 0)
			{
				if (StringUtil.endsWith(sb, "BEGIN"))
				{
					succ = parseObjectBegin(reader, null, errMessage);
					if (!succ)
					{
						return succ;
					}
					currObj = null;
				}
				else if (StringUtil.endsWith(sb, "END"))
				{
					List<ASN1ObjectInfo> objList = module.getObjValues();
					ASN1ObjectInfo iobj;
					int ui = 0;
					int uj = objList.size();
					while (ui < uj)
					{
						iobj = objList.get(ui);
						if (iobj.getTypeName() != null && iobj.getTypeVal() != null && iobj.getOidLen() == 0 && !iobj.getTypeName().equals("TRAP-TYPE") && !iobj.getTypeVal().equals("Imported Value"))
						{
							succ = this.parseObjectOID(module, iobj, iobj.getTypeVal(), errMessage);
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
					if (StringUtil.endsWith(sb, "\""))
					{
						isQuotedText = false;
						if (currObj != null)
						{
							if (sbObjValName.length() > 0 && sbObjValCont.length() > 0)
							{
								currObj.getValName().add(sbObjValName.toString());
								currObj.getValCont().add(sbObjValCont.toString());
							}
							sbObjValName.setLength(0);
							sbObjValCont.setLength(0);
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
					StringUtil.trim(sb);
					if (currObj != null && (objIsBrk || objIsEqual || (lineSpace > objLineSpace && sb.charAt(0) >= 'A' && sb.charAt(0) <= 'Z') || StringUtil.startsWith(sb, "::=") || StringUtil.startsWith(sb, "{") || StringUtil.startsWith(sb, "\"")))
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
							
							if (StringUtil.endsWith(sb, "}"))
							{
								objIsBrk = false;
								objIsEqual = false;
								if (sbObjValName.length() > 0 && sbObjValCont.length() > 0)
								{
									currObj.getValName().add(sbObjValName.toString());
									currObj.getValCont().add(sbObjValCont.toString());
								}
								sbObjValName.setLength(0);
								sbObjValCont.setLength(0);
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
							if (StringUtil.startsWith(sb, "[") && StringUtil.endsWith(sb, "]"))
							{
	
							}
							else
							{
								currObj.setTypeVal(sb.toString());
								i = currObj.getTypeVal().indexOf('{');
								j = currObj.getTypeVal().indexOf('}');
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
						else if (StringUtil.startsWith(sb, "::="))
						{
							if (sbObjValName.length() > 0 && sbObjValCont.length() > 0)
							{
								currObj.getValName().add(sbObjValName.toString());
								currObj.getValCont().add(sbObjValCont.toString());
							}
							sbObjValName.setLength(0);
							sbObjValCont.setLength(0);
	
							i = 3;
							while (sb.charAt(i) == ' ' || sb.charAt(i) == '\t')
							{
								i++;
							}
							currObj.setTypeVal(sb.substring(i));
							if (currObj.getTypeName() != null && currObj.getTypeVal().endsWith("}"))
							{
								currObj = null;
								objIsBrk = false;
								objIsEqual = false;
							}							
							else if (currObj.getTypeVal().endsWith("{"))
							{
								objIsBrk = true;
								objIsEqual = true;
							}
						}
						else if (StringUtil.startsWith(sb, "{"))
						{
							sbObjValCont.append(sb.toString());
							if (StringUtil.endsWith(sb, "}"))
							{
								objIsBrk = false;
								objIsEqual = false;
								if (sbObjValName.length() > 0 && sbObjValCont.length() > 0)
								{
									currObj.getValName().add(sbObjValName.toString());
									currObj.getValCont().add(sbObjValCont.toString());
								}
								sbObjValName.setLength(0);
								sbObjValCont.setLength(0);
							}
							else
							{
								objIsBrk = true;
								objIsEqual = false;
							}
						}
						else if (StringUtil.startsWith(sb, "\""))
						{
							sbObjValCont.append(sb.toString());
							if (sb.length() > 1 && StringUtil.endsWith(sb, "\""))
							{
								if (sbObjValName.length() > 0 && sbObjValCont.length() > 0)
								{
									currObj.getValName().add(sbObjValName.toString());
									currObj.getValCont().add(sbObjValCont.toString());
								}
								sbObjValName.setLength(0);
								sbObjValCont.setLength(0);
							}
							else
							{
								isQuotedText = true;
							}
						}
						else
						{
							boolean proc = false;
							if (sbObjValName.length() > 0 && sbObjValCont.length() > 0)
							{
								if (StringUtil.startsWith(sb, "{") || StringUtil.startsWith(sb, "\""))
								{
									sbObjValCont.append(' ');
									sbObjValCont.append(sb.toString());
									proc = true;
								}
								else
								{
									currObj.getValName().add(sbObjValName.toString());
									currObj.getValCont().add(sbObjValCont.toString());
									sbObjValName.setLength(0);
									sbObjValCont.setLength(0);
								}
							}
							else if (sbObjValName.length() > 0)
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
									StringUtil.trim(sbObjValCont);
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
									if (sbObjValName.length() > 0 && sbObjValCont.length() > 0)
									{
										currObj.getValName().add(sbObjValName.toString());
										currObj.getValCont().add(sbObjValCont.toString());
									}
									sbObjValName.setLength(0);
									sbObjValCont.setLength(0);
								}
								else
								{
									objIsBrk = true;
									objIsEqual = false;
								}
							}
						}
					}
					else if (StringUtil.startsWith(sb, "IMPORTS"))
					{
						boolean isEnd = false;
						StringBuilder impObjNames = new StringBuilder();
						ASN1ModuleInfo impModule;
						ASN1ObjectInfo impObj;
						ASN1ObjectInfo impObj2;
						String []impSarr;
						int impCnt;
						int impInd;
						sb.delete(0, 7);
						StringUtil.trim(sb);
	
						while (!isEnd)
						{
							if (StringUtil.endsWith(sb, ';'))
							{
								isEnd = true;
								sb.setLength(sb.length() - 1);
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
										impObj2.setObjectName(impSarr[impCnt]);
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
										
										ui = StringUtil.sortedInsert(module.getObjKeys(), impObj2.getObjectName());
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
							sb.setLength(0);
							if (!reader.readLine(sb, 512))
							{
								errMessage.append("IMPORTS end not found");
								return false;
							}
	
							i = sb.indexOf("--");
							if (i >= 0)
							{
								sb.setLength(i);
							}
							StringUtil.trim(sb);
						}
					}
					else if (StringUtil.startsWith(sb, "EXPORTS"))
					{
						while (true)
						{
							if (StringUtil.endsWith(sb, ';'))
							{
								break;
							}
	
							sb.setLength(0);
							if (!reader.readLine(sb, 512))
							{
								errMessage.append("EXPORTS end not found");
								return false;
							}
	
							i = sb.indexOf("--");
							if (i >= 0)
							{
								sb.setLength(i);
							}
							StringUtil.trim(sb);
						}
					}
					else
					{
						if (currObj != null)
						{
							if (sbObjValName.length() > 0 && sbObjValCont.length() > 0)
							{
								currObj.getValName().add(sbObjValName.toString());
								currObj.getValCont().add(sbObjValCont.toString());
							}
							sbObjValName.setLength(0);
							sbObjValCont.setLength(0);
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
								i = sb.length();
								if (!reader.readLine(sb, 512))
								{
									errMessage.append("Unknown format: ");
									errMessage.append(sb.toString());
									return false;
								}
								j = sb.indexOf("--");
								if (j >= 0)
								{
									sb.setLength(j);
								}
								StringUtil.trimRight(sb);
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
							obj.setObjectName(sb.substring(0, k));
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
							int ui = StringUtil.sortedInsert(module.getObjKeys(), obj.getObjectName());
							module.getObjValues().add(ui, obj);
							ui = StringUtil.sortedInsert(this.globalModule.getObjKeys(), obj.getObjectName());
							this.globalModule.getObjValues().add(ui, obj);
							if (obj.getObjectName().equals(DEBUGOBJ))
							{
								currObj = null;
							}
	
							if (StringUtil.endsWith(sb, "::="))
							{
								currObj = obj;
								objLineSpace = lineSpace;
								objIsEqual = true;
								sbObjValName.setLength(0);
								sbObjValCont.setLength(0);
							}
							else
							{
								i += 3;
								while (sb.charAt(i) == ' ' || sb.charAt(i) == '\t')
								{
									i++;
								}
								obj.setTypeVal(sb.substring(i));
								currObj = obj;
								sbObjValName.setLength(0);
								sbObjValCont.setLength(0);
								objLineSpace = lineSpace;
								objIsEqual = false;
								objIsBrk = false;
								if (obj.getTypeVal().endsWith("{"))
								{
									objIsBrk = true;
									objIsEqual = true;
								}
								else if (obj.getTypeVal().endsWith("}"))
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
								i = sb.length();
								if (!reader.readLine(sb, 512))
								{
									errMessage.append("Unknown format: ");
									errMessage.append(sb.toString());
									return false;
								}
								j = sb.indexOf("--");
								if (j >= 0)
								{
									sb.setLength(j);
								}
								StringUtil.trimRight(sb);
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
							obj.setObjectName(sb.substring(0, i));
							while (sb.charAt(i) == ' ' || sb.charAt(i) == '\t')
							{
								i++;
							}
							obj.setTypeName(sb.substring(i));
							obj.setTypeVal(null);
							obj.setOidLen(0);
							obj.setValName(new ArrayList<String>());
							obj.setValCont(new ArrayList<String>());
							int ui = StringUtil.sortedInsert(module.getObjKeys(), obj.getObjectName());
							module.getObjValues().add(ui, obj);
							ui = StringUtil.sortedInsert(this.globalModule.getObjKeys(), obj.getObjectName());
							this.globalModule.getObjValues().add(ui, obj);
							if (obj.getObjectName().equals(DEBUGOBJ))
							{
								currObj = obj;
							}
	
							currObj = obj;
							objLineSpace = lineSpace;
							objIsEqual = false;
							sbObjValName.setLength(0);
							sbObjValCont.setLength(0);
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

	public ASN1ModuleInfo getGlobalModule()
	{
		return this.globalModule;		
	}

	public ASN1ModuleInfo getModuleByFileName(String fileName)
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

	public boolean loadFile(String fileName, StringBuilder errMessage)
	{
		StringBuilder sb = new StringBuilder();
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
			sb.setLength(0);
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
				sb.setLength(i);
			}
			StringUtil.trim(sb);
			if (sb.length() > 0)
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
				sb.setLength(i);
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
		try
		{
			reader.close();
		}
		catch (IOException ex)
		{

		}
		fs.close();
		return succ;
	}

	public void toString(StringBuilder sb)
	{
		Iterator<ASN1ModuleInfo> itModules = this.moduleMap.values().iterator();
		while (itModules.hasNext())
		{
			itModules.next().toString(sb);
			sb.append("\r\n");
		}
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}
}
