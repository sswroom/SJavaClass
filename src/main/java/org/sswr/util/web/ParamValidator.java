package org.sswr.util.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.sswr.util.data.DataTools;
import org.sswr.util.data.JSONMapper;
import org.sswr.util.data.StringUtil;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class ParamValidator {
	private String funcName;
	private LogTool logger;
	private HttpServletRequest req;
	private HttpServletResponse resp;
	private String errMsg;
	private String errVarName;
	private String errVarValue;
	private String errFuncDesc;
	private String exDetail;
	private int forceRespStatus;
	private Map<String, String> mpartReq;
	private ArrayList<Part> mpartFiles;

	public ParamValidator(String funcName, LogTool logger, HttpServletRequest req, HttpServletResponse resp)
	{
		this.funcName = funcName;
		this.logger = logger;
		this.req = req;
		this.resp = resp;
		this.errMsg = null;
		this.errVarName = null;
		this.errVarValue = null;
		this.errFuncDesc = null;
		this.exDetail = null;
		this.mpartReq = null;
		this.mpartFiles = null;
		this.forceRespStatus = 0;
		if (this.req != null)
		{
			this.mpartFiles = new ArrayList<Part>();
			this.mpartReq = DataTools.toStringStringMap(HttpUtil.parseParams(req, this.mpartFiles), HttpUtil.PART_SEPERATOR);
		}
	}

	public void forceRespStatus(int status)
	{
		this.forceRespStatus = status;
	}
	
	public Timestamp str2Timestamp(String varName, String strTime)
	{
		if (this.errMsg != null) return null;
		try
		{
			return StringUtil.toTimestamp(strTime);
		}
		catch (Exception ex)
		{
		}
		this.errMsg = this.funcName + ": "+varName+" is not valid: "+strTime;
		setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
		this.logger.logMessage(this.errMsg, LogLevel.ERROR);
		this.errVarName = varName;
		this.errVarValue = strTime;
		this.errFuncDesc = varName+" is not in format of 'yyyyMMdd' or 'yyyyMMddHHmm'";
		return null;
	}

	public Integer str2Integer(String varName, String varValue)
	{
		try
		{
			return Integer.valueOf(varValue);
		}
		catch (NumberFormatException ex)
		{
			this.errMsg = this.funcName + ": "+varName+" is not an integer: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarName = varName;
			this.errVarValue = varValue;
			this.errFuncDesc = varName+" is not an integer: "+varValue;
			return null;
		}
	}

	public Double str2Double(String varName, String varValue)
	{
		try
		{
			return Double.valueOf(varValue);
		}
		catch (NumberFormatException ex)
		{
			this.errMsg = this.funcName + ": "+varName+" is not a number: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarName = varName;
			this.errVarValue = varValue;
			this.errFuncDesc = varName+" is not a number: "+varValue;
			return null;
		}
	}

	public <T extends Enum<T>> T str2Enum(Class<T> cls, String varName, String varValue)
	{
		if (this.errMsg != null) return null;
		T ret = DataTools.getEnum(cls, varValue);
		if (ret == null)
		{
			this.errMsg = this.funcName + ": "+varName+" is not valid: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarName = varName;
			this.errVarValue = varValue;
			this.errFuncDesc = varName+" cannot map to any value of Enum "+cls.getName();
		}
		return ret;
	}

	public Geometry str2Geometry(String varName, String varValue, int srid)
	{
		try
		{
			Geometry geom = new WKTReader().read(varValue);
			geom.setSRID(srid);
			return geom;
		}
		catch (ParseException ex)
		{
			this.errMsg = this.funcName + ": "+varName+" is not valid: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarName = varName;
			this.errVarValue = varValue;
			this.errFuncDesc = varName+" cannot be parsed to geometry";
			return null;
		}
	}

	public String getReqStringOpt(String varName)
	{
		if (this.errMsg != null) return null;
		if (this.req == null) { this.setReqEmptyErr(); return null; }
		String varValue;
		if (this.mpartReq != null)
		{
			varValue = this.mpartReq.get(varName);
		}
		else
		{
			varValue = this.req.getParameter(varName);
		}
		return varValue;
	}

	public String getReqString(String varName)
	{
		if (this.errMsg != null) return null;
		if (this.req == null) { this.setReqEmptyErr(); return null; }
		String varValue;
		if (this.mpartReq != null)
		{
			varValue = this.mpartReq.get(varName);
		}
		else
		{
			varValue = this.req.getParameter(varName);
		}
		if (varValue == null)
		{
			this.errMsg = this.funcName + ": "+varName+" is not found";
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarName = varName;
			this.errVarValue = null;
			this.errFuncDesc = varName+" is not found";
		}
		return varValue;
	}

	public String []getReqStrings(String varName)
	{
		if (this.errMsg != null) return null;
		if (this.req == null) { this.setReqEmptyErr(); return null; }
		String varValue[];
		if (this.mpartReq != null)
		{
			String s = this.mpartReq.get(varName);
			if (s == null)
			{
				varValue = null;
			}
			else
			{
				varValue = s.split(HttpUtil.PART_SEPERATOR);
			}
		}
		else
		{
			varValue = this.req.getParameterValues(varName);
		}
		if (varValue == null)
		{
			this.errMsg = this.funcName + ": "+varName+" is not found";
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarName = varName;
			this.errVarValue = null;
			this.errFuncDesc = varName+" is not found";
		}
		return varValue;
	}

	public String getReqStringLen(String varName, int minCharCnt, int maxCharCnt)
	{
		String varValue = getReqString(varName);
		if (varValue == null) return null;
		if (this.checkStringCharDbLen(varName, varValue, minCharCnt, maxCharCnt)) return null;
		return varValue;
	}

	public Timestamp getReqTimestamp(String varName)
	{
		String varValue;
		if ((varValue = this.getReqString(varName)) == null) return null;
		return str2Timestamp(varName, varValue);
	}

	public Integer getReqInt(String varName)
	{
		String varValue;
		if ((varValue = this.getReqString(varName)) == null) return null;
		return str2Integer(varName, varValue);
	}

	public Integer getReqIntOpt(String varName)
	{
		String varValue = this.getReqStringOpt(varName);
		if ((varValue = this.getReqStringOpt(varName)) == null) return null;
		return StringUtil.toInteger(varValue);
	}

	public Integer getReqIntRange(String varName, int min, int max, boolean noZero)
	{
		Integer varValue = this.getReqInt(varName);
		if (varValue == null) return null;
		if (this.checkRange(varName, varValue, min, max, noZero)) return null;
		return varValue;
	}

	public Double getReqDouble(String varName)
	{
		String varValue;
		if ((varValue = this.getReqString(varName)) == null) return null;
		return str2Double(varName, varValue);
	}

	public Double getReqDoubleRange(String varName, double min, double max)
	{
		Double varValue = this.getReqDouble(varName);
		if (varValue == null) return null;
		if (this.checkRangeDbl(varName, varValue, min, max)) return null;
		return varValue;
	}

	public <T extends Enum<T>> T getReqEnum(String varName, Class<T> cls)
	{
		String varValue;
		if (this.errMsg != null) return null;
		if ((varValue = this.getReqString(varName)) == null) return null;
		return str2Enum(cls, varName, varValue);
	}

	public Geometry getReqGeometry(String varName, int srid)
	{
		String varValue;
		if (this.errMsg != null) return null;
		if ((varValue = this.getReqString(varName)) == null) return null;
		return str2Geometry(varName, varValue, srid);
	}

	public int getReqFileCount()
	{
		if (this.mpartFiles == null)
			return 0;
		return this.mpartFiles.size();
	}

	public Part getReqFile(int index)
	{
		if (this.mpartFiles == null)
			return null;
		return this.mpartFiles.get(index);
	}


	public <T extends Enum<T>> boolean checkEnum(T val, String varName, T[] validVals)
	{
		int i = validVals.length;
		while (i-- > 0)
		{
			if (val == validVals[i])
				return false;
		}
		this.errMsg = this.funcName + ": "+varName+" is out of range: "+val.toString();
		setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
		this.logger.logMessage(this.errMsg, LogLevel.ERROR);
		this.errVarName = varName;
		this.errVarValue = val.toString();
		this.errFuncDesc = varName+" is out of range: "+val.toString();
		return true;
	}

	public boolean checkVarcharLen(String varName, String varValue, int maxLen)
	{
		if (this.errMsg != null) return true;
		byte b[] = varValue.getBytes(StandardCharsets.UTF_8);
		if (b.length > maxLen)
		{
			this.errMsg = this.funcName + ": "+varName+" is too long: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarName = varName;
			this.errVarValue = varValue;
			this.errFuncDesc = varName+" is longer than "+maxLen+" of varchar";
			return true;
		}
		return false;
	}

	public boolean checkStringDbLen(String varName, String varValue, int maxLen, boolean notEmpty)
	{
		if (this.errMsg != null) return true;
		byte b[] = varValue.getBytes(StandardCharsets.UTF_16LE);
		if ((b.length >> 1) > maxLen)
		{
			this.errMsg = this.funcName + ": "+varName+" is too long: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarName = varName;
			this.errVarValue = varValue;
			this.errFuncDesc = varName+" is longer than "+maxLen+" of nvarchar";
			return true;
		}
		if (notEmpty && b.length == 0)
		{
			this.errMsg = this.funcName + ": "+varName+" is empty";
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarName = varName;
			this.errVarValue = varValue;
			this.errFuncDesc = varName+" is empty";
			return true;
		}
		return false;
	}

	public boolean checkStringCharDbLen(String varName, String varValue, int minCharCnt, int maxCharCnt)
	{
		if (this.errMsg != null) return true;
		int leng = varValue.length();
		if (leng > maxCharCnt)
		{
			this.errMsg = this.funcName + ": "+varName+" is too long: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarName = varName;
			this.errVarValue = varValue;
			this.errFuncDesc = varName+" is longer than "+maxCharCnt+" Characters";
			return true;
		}
		if (leng < minCharCnt)
		{
			this.errMsg = this.funcName + ": "+varName+" is too short: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarName = varName;
			this.errVarValue = varValue;
			this.errFuncDesc = varName+" is shorter than "+minCharCnt+" Characters";
			return true;
		}
		byte b[] = varValue.getBytes(StandardCharsets.UTF_16LE);
		if ((b.length >> 2) > (maxCharCnt << 1))
		{
			this.errMsg = this.funcName + ": "+varName+" is too long for db: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarName = varName;
			this.errVarValue = varValue;
			this.errFuncDesc = varName+" is longer than "+(maxCharCnt << 1)+" of nvarchar";
			return true;
		}
		return false;
	}

	public boolean checkRange(String varName, int varValue, int min, int max, boolean noZero)
	{
		if (this.errMsg != null) return true;
		if ((noZero && varValue == 0) || varValue < min || varValue > max)
		{
			this.errMsg = this.funcName + ": "+varName+" out of range: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarName = varName;
			this.errVarValue = ""+varValue;
			if (noZero)
			{
				this.errFuncDesc = varName+" cannot be zero and range is >= "+ min +" and <= "+max;
			}
			else
			{
				this.errFuncDesc = varName+" must be in range >= "+ min +" and <= "+max;
			}
			return true;
		}
		return false;
	}

	public boolean checkRangeDbl(String varName, double varValue, double min, double max)
	{
		if (this.errMsg != null) return true;
		if (varValue < min || varValue > max)
		{
			this.errMsg = this.funcName + ": "+varName+" out of range: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarName = varName;
			this.errVarValue = ""+varValue;
			this.errFuncDesc = varName+" must be in range >= "+ min +" and <= "+max;
			return true;
		}
		return false;
	}

	public <T> boolean checkInList(String varName, T varValue, List<T> validList)
	{
		if (this.errMsg != null) return true;
		boolean found = false;
		int i = validList.size();
		while (i-- > 0)
		{
			if (varValue.equals(validList.get(i)))
			{
				found = true;
				break;
			}
		}
		if (!found)
		{
			this.errMsg = this.funcName + ": "+varName+" is not valid: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarName = varName;
			this.errVarValue = ""+varValue;
			this.errFuncDesc = varName+" is not in valid list";
			return true;
		}
		return false;
	}

	public boolean isError()
	{
		return errMsg != null;
	}

	private void setRespStatus(int errorStatus)
	{
		if (this.forceRespStatus == 0)
		{
			this.resp.setStatus(errorStatus);
		}
		else
		{
			this.resp.setStatus(this.forceRespStatus);
		}
	}

	private void setReqEmptyErr()
	{
		this.errMsg = funcName+": req is null";
		setRespStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		this.logger.logMessage(this.errMsg, LogLevel.ERROR);
		this.errVarName = "req";
		this.errVarValue = null;
		this.errFuncDesc = "req is null";
	}

	public Map<String, Object> setError(String varName, String varValue, String funcDesc, String errMsg)
	{
		if (errMsg == null)
		{
			errMsg = funcDesc;
		}
		if (varValue != null)
		{
			this.errMsg = funcName+": "+errMsg+": "+varValue;
		}
		else
		{
			this.errMsg = funcName+": "+errMsg;
		}
		setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
		this.logger.logMessage(this.errMsg, LogLevel.ERROR);
		this.errVarName = varName;
		this.errVarValue = varValue;
		this.errFuncDesc = funcDesc;
		return getErrorObj();
	}

	public Map<String, Object> setError(String varName, String varValue, String funcDesc)
	{
		if (varValue != null)
		{
			this.errMsg = funcName+": "+funcDesc+": "+varValue;
		}
		else
		{
			this.errMsg = funcName+": "+funcDesc;
		}
		setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
		this.logger.logMessage(this.errMsg, LogLevel.ERROR);
		this.errVarName = varName;
		this.errVarValue = varValue;
		this.errFuncDesc = funcDesc;
		return getErrorObj();
	}

	public Map<String, Object> getErrorObj()
	{
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("errorMsg", this.errMsg);
		retMap.put("funcName", this.funcName);
		if (this.errVarName != null)
		{
			retMap.put("varName", this.errVarName);
		}
		if (this.errVarValue != null)
		{
			retMap.put("varValue", this.errVarValue);
		}
		if (this.errFuncDesc != null)
		{
			retMap.put("funcDesc", this.errFuncDesc);
		}
		if (this.exDetail != null)
		{
			retMap.put("exDetail", this.exDetail);
		}
		return retMap;
	}

	public void respErrorObj() throws IOException
	{
		String json = JSONMapper.object2Json(getErrorObj());
		this.resp.setContentType("text/json");
		this.resp.getWriter().print(json);
	}

	public Map<String, Object> handleException(Exception ex) throws IOException
	{
		this.logger.logException(ex);
		setRespStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		return null;
	}

	public void exceptionDetail(Exception ex)
	{
		StringWriter writer = new StringWriter();
		ex.printStackTrace(new PrintWriter(writer));
		this.exDetail = writer.toString();
	}
}
