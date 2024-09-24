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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import org.sswr.util.data.DataTools;
import org.sswr.util.data.JSONMapper;
import org.sswr.util.data.StringUtil;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;


public class ParamValidator {
	protected String funcName;
	protected LogTool logger;
	protected HttpServletRequest req;
	protected HttpServletResponse resp;
	protected String errMsg;
	protected String errVarDispName;
	protected String errVarValue;
	protected String errFuncDesc;
	protected String exDetail;
	protected int forceRespStatus;
	protected Map<String, String> mpartReq;
	protected ArrayList<Part> mpartFiles;

	public ParamValidator(@Nonnull String funcName, @Nonnull LogTool logger, @Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp)
	{
		this.funcName = funcName;
		this.logger = logger;
		this.req = req;
		this.resp = resp;
		this.errMsg = null;
		this.errVarDispName = null;
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
	
	@Nullable
	public Timestamp str2Timestamp(@Nonnull String varDispName, @Nonnull String strTime)
	{
		if (this.errMsg != null) return null;
		try
		{
			return StringUtil.toTimestamp(strTime);
		}
		catch (Exception ex)
		{
		}
		this.errMsg = this.funcName + ": "+varDispName+" is not valid: "+strTime;
		setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
		this.logger.logMessage(this.errMsg, LogLevel.ERROR);
		this.errVarDispName = varDispName;
		this.errVarValue = strTime;
		this.errFuncDesc = varDispName+" is not in format of 'yyyyMMdd' or 'yyyyMMddHHmm'";
		return null;
	}

	@Nullable
	public Long str2Long(@Nonnull String varDispName, @Nonnull String varValue)
	{
		try
		{
			return Long.valueOf(varValue);
		}
		catch (NumberFormatException ex)
		{
			this.errMsg = this.funcName + ": "+varDispName+" is not a long: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = varDispName;
			this.errVarValue = varValue;
			this.errFuncDesc = varDispName+" is not a long: "+varValue;
			return null;
		}
	}

	@Nullable
	public Byte str2Byte(@Nonnull String varDispName, @Nonnull String varValue)
	{
		try
		{
			return Byte.valueOf(varValue);
		}
		catch (NumberFormatException ex)
		{
			this.errMsg = this.funcName + ": "+varDispName+" is not a byte: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = varDispName;
			this.errVarValue = varValue;
			this.errFuncDesc = varDispName+" is not a byte: "+varValue;
			return null;
		}
	}

	@Nullable
	public Integer str2Integer(@Nonnull String varDispName, @Nonnull String varValue)
	{
		try
		{
			return Integer.valueOf(varValue);
		}
		catch (NumberFormatException ex)
		{
			this.errMsg = this.funcName + ": "+varDispName+" is not an integer: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = varDispName;
			this.errVarValue = varValue;
			this.errFuncDesc = varDispName+" is not an integer: "+varValue;
			return null;
		}
	}

	@Nullable
	public Double str2Double(@Nonnull String varDispName, @Nonnull String varValue)
	{
		try
		{
			return Double.valueOf(varValue);
		}
		catch (NumberFormatException ex)
		{
			this.errMsg = this.funcName + ": "+varDispName+" is not a number: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = varDispName;
			this.errVarValue = varValue;
			this.errFuncDesc = varDispName+" is not a number: "+varValue;
			return null;
		}
	}

	@Nullable
	public <T extends Enum<T>> T str2Enum(@Nonnull Class<T> cls, @Nonnull String varDispName, @Nonnull String varValue)
	{
		if (this.errMsg != null) return null;
		T ret = DataTools.getEnum(cls, varValue);
		if (ret == null)
		{
			this.errMsg = this.funcName + ": "+varDispName+" is not valid: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = varDispName;
			this.errVarValue = varValue;
			this.errFuncDesc = varDispName+" cannot map to any value of Enum "+cls.getName();
		}
		return ret;
	}

	@Nullable
	public Geometry str2Geometry(@Nonnull String varDispName, @Nonnull String varValue, int srid)
	{
		try
		{
			Geometry geom = new WKTReader().read(varValue);
			geom.setSRID(srid);
			return geom;
		}
		catch (ParseException ex)
		{
			this.errMsg = this.funcName + ": "+varDispName+" is not valid: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = varDispName;
			this.errVarValue = varValue;
			this.errFuncDesc = varDispName+" cannot be parsed to geometry";
			return null;
		}
	}

	@Nullable
	public String getReqStringOpt(@Nonnull String varName)
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

	@Nullable
	public String getReqString(@Nonnull String varName, @Nullable String varDispName)
	{
		if (varDispName == null) varDispName = varName;
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
			this.errMsg = this.funcName + ": "+varDispName+" is not found";
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = varDispName;
			this.errVarValue = null;
			this.errFuncDesc = varDispName+" is not found";
		}
		return varValue;
	}

	@Nullable
	public String []getReqStrings(@Nonnull String varName, @Nullable String varDispName)
	{
		if (varDispName == null) varDispName = varName;
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
				varValue = StringUtil.split(s, HttpUtil.PART_SEPERATOR);
			}
		}
		else
		{
			varValue = this.req.getParameterValues(varName);
		}
		if (varValue == null)
		{
			this.errMsg = this.funcName + ": "+varDispName+" is not found";
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = varDispName;
			this.errVarValue = null;
			this.errFuncDesc = varDispName+" is not found";
		}
		return varValue;
	}

	@Nullable
	public String []getReqStringsOpt(@Nonnull String varName, @Nullable String varDispName)
	{
		if (varDispName == null) varDispName = varName;
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
				varValue = StringUtil.split(s, HttpUtil.PART_SEPERATOR);
			}
		}
		else
		{
			varValue = this.req.getParameterValues(varName);
		}
		return varValue;
	}

	@Nullable
	public String getReqStringLen(@Nonnull String varName, @Nullable String varDispName, int minCharCnt, int maxCharCnt)
	{
		if (varDispName == null) varDispName = varName;
		String varValue = getReqString(varName, varDispName);
		if (varValue == null) return null;
		if (this.checkStringCharDbLen(varDispName, varValue, minCharCnt, maxCharCnt)) return null;
		return varValue;
	}

	@Nullable
	public Timestamp getReqTimestamp(@Nonnull String varName, @Nullable String varDispName)
	{
		if (varDispName == null) varDispName = varName;
		String varValue;
		if ((varValue = this.getReqString(varName, varDispName)) == null) return null;
		return str2Timestamp(varDispName, varValue);
	}

	@Nullable
	public Long getReqLong(@Nonnull String varName, @Nullable String varDispName)
	{
		if (varDispName == null) varDispName = varName;
		String varValue;
		if ((varValue = this.getReqString(varName, varDispName)) == null) return null;
		return str2Long(varDispName, varValue);
	}

	@Nullable
	public Byte getReqByte(@Nonnull String varName, @Nullable String varDispName)
	{
		if (varDispName == null) varDispName = varName;
		String varValue;
		if ((varValue = this.getReqString(varName, varDispName)) == null) return null;
		return str2Byte(varDispName, varValue);
	}

	@Nullable
	public Integer getReqInt(@Nonnull String varName, @Nullable String varDispName)
	{
		if (varDispName == null) varDispName = varName;
		String varValue;
		if ((varValue = this.getReqString(varName, varDispName)) == null) return null;
		return str2Integer(varDispName, varValue);
	}

	@Nullable
	public Integer getReqIntOpt(@Nonnull String varName)
	{
		String varValue = this.getReqStringOpt(varName);
		if ((varValue = this.getReqStringOpt(varName)) == null) return null;
		return StringUtil.toInteger(varValue);
	}

	@Nullable
	public Integer getReqIntRange(@Nonnull String varName, @Nullable String varDispName, int min, int max, boolean noZero)
	{
		if (varDispName == null) varDispName = varName;
		Integer varValue = this.getReqInt(varName, varDispName);
		if (varValue == null) return null;
		if (this.checkRange(varDispName, varValue, min, max, noZero)) return null;
		return varValue;
	}

	@Nullable
	public Double getReqDouble(@Nonnull String varName, @Nullable String varDispName)
	{
		if (varDispName == null) varDispName = varName;
		String varValue;
		if ((varValue = this.getReqString(varName, varDispName)) == null) return null;
		return str2Double(varDispName, varValue);
	}

	@Nullable
	public Double getReqDoubleRange(@Nonnull String varName, @Nullable String varDispName, double min, double max)
	{
		if (varDispName == null) varDispName = varName;
		Double varValue = this.getReqDouble(varName, varDispName);
		if (varValue == null) return null;
		if (this.checkRangeDbl(varDispName, varValue, min, max)) return null;
		return varValue;
	}

	@Nullable
	public <T extends Enum<T>> T getReqEnum(@Nonnull String varName, @Nullable String varDispName, @Nonnull Class<T> cls)
	{
		if (varDispName == null) varDispName = varName;
		String varValue;
		if (this.errMsg != null) return null;
		if ((varValue = this.getReqString(varName, varDispName)) == null) return null;
		return str2Enum(cls, varDispName, varValue);
	}

	@Nullable
	public Geometry getReqGeometry(@Nonnull String varName, @Nullable String varDispName, int srid)
	{
		if (varDispName == null) varDispName = varName;
		String varValue;
		if (this.errMsg != null) return null;
		if ((varValue = this.getReqString(varName, varDispName)) == null) return null;
		return str2Geometry(varDispName, varValue, srid);
	}

	public int getReqFileCount()
	{
		if (this.mpartFiles == null)
			return 0;
		return this.mpartFiles.size();
	}

	@Nullable
	public Part getReqFile(int index)
	{
		if (this.mpartFiles == null)
			return null;
		return this.mpartFiles.get(index);
	}


	public <T extends Enum<T>> boolean checkEnum(@Nonnull T val, @Nonnull String varDispName, @Nonnull T[] validVals)
	{
		int i = validVals.length;
		while (i-- > 0)
		{
			if (val == validVals[i])
				return false;
		}
		this.errMsg = this.funcName + ": "+varDispName+" is out of range: "+val.toString();
		setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
		this.logger.logMessage(this.errMsg, LogLevel.ERROR);
		this.errVarDispName = varDispName;
		this.errVarValue = val.toString();
		this.errFuncDesc = varDispName+" is out of range: "+val.toString();
		return true;
	}

	public boolean checkVarcharLen(@Nonnull String varDispName, @Nonnull String varValue, int maxLen)
	{
		if (this.errMsg != null) return true;
		byte b[] = varValue.getBytes(StandardCharsets.UTF_8);
		if (b.length > maxLen)
		{
			this.errMsg = this.funcName + ": "+varDispName+" is too long: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = varDispName;
			this.errVarValue = varValue;
			this.errFuncDesc = varDispName+" is longer than "+maxLen+" of varchar";
			return true;
		}
		return false;
	}

	public boolean checkStringDbLen(@Nonnull String varDispName, @Nonnull String varValue, int maxLen, boolean notEmpty)
	{
		if (this.errMsg != null) return true;
		byte b[] = varValue.getBytes(StandardCharsets.UTF_16LE);
		if ((b.length >> 1) > maxLen)
		{
			this.errMsg = this.funcName + ": "+varDispName+" is too long: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = varDispName;
			this.errVarValue = varValue;
			this.errFuncDesc = varDispName+" is longer than "+maxLen+" of nvarchar";
			return true;
		}
		if (notEmpty && b.length == 0)
		{
			this.errMsg = this.funcName + ": "+varDispName+" is empty";
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = varDispName;
			this.errVarValue = varValue;
			this.errFuncDesc = varDispName+" is empty";
			return true;
		}
		return false;
	}

	public boolean checkStringCharDbLen(@Nonnull String varDispName, @Nonnull String varValue, int minCharCnt, int maxCharCnt)
	{
		if (this.errMsg != null) return true;
		int leng = varValue.length();
		if (leng > maxCharCnt)
		{
			this.errMsg = this.funcName + ": "+varDispName+" is too long: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = varDispName;
			this.errVarValue = varValue;
			this.errFuncDesc = varDispName+" is longer than "+maxCharCnt+" Characters";
			return true;
		}
		if (leng < minCharCnt)
		{
			this.errMsg = this.funcName + ": "+varDispName+" is too short: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = varDispName;
			this.errVarValue = varValue;
			this.errFuncDesc = varDispName+" is shorter than "+minCharCnt+" Characters";
			return true;
		}
		byte b[] = varValue.getBytes(StandardCharsets.UTF_16LE);
		if ((b.length >> 2) > (maxCharCnt << 1))
		{
			this.errMsg = this.funcName + ": "+varDispName+" is too long for db: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = varDispName;
			this.errVarValue = varValue;
			this.errFuncDesc = varDispName+" is longer than "+(maxCharCnt << 1)+" of nvarchar";
			return true;
		}
		return false;
	}

	public boolean checkRange(@Nonnull String varDispName, int varValue, int min, int max, boolean noZero)
	{
		if (this.errMsg != null) return true;
		if ((noZero && varValue == 0) || varValue < min || varValue > max)
		{
			this.errMsg = this.funcName + ": "+varDispName+" out of range: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = varDispName;
			this.errVarValue = ""+varValue;
			if (noZero)
			{
				this.errFuncDesc = varDispName+" cannot be zero and range is >= "+ min +" and <= "+max;
			}
			else
			{
				this.errFuncDesc = varDispName+" must be in range >= "+ min +" and <= "+max;
			}
			return true;
		}
		return false;
	}

	public boolean checkRangeDbl(@Nonnull String varDispName, double varValue, double min, double max)
	{
		if (this.errMsg != null) return true;
		if (varValue < min || varValue > max)
		{
			this.errMsg = this.funcName + ": "+varDispName+" out of range: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = varDispName;
			this.errVarValue = ""+varValue;
			this.errFuncDesc = varDispName+" must be in range >= "+ min +" and <= "+max;
			return true;
		}
		return false;
	}

	public <T> boolean checkInList(@Nonnull String varDispName, @Nonnull T varValue, @Nonnull List<T> validList)
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
			this.errMsg = this.funcName + ": "+varDispName+" is not valid: "+varValue;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = varDispName;
			this.errVarValue = ""+varValue;
			this.errFuncDesc = varDispName+" is not in valid list";
			return true;
		}
		return false;
	}

	public boolean isError()
	{
		return errMsg != null;
	}

	protected void setRespStatus(int errorStatus)
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
		this.errVarDispName = "req";
		this.errVarValue = null;
		this.errFuncDesc = "req is null";
	}

	@Nonnull
	public Map<String, Object> setError(@Nonnull String varDispName, @Nullable String varValue, @Nonnull String funcDesc, @Nullable String errMsg)
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
		this.errVarDispName = varDispName;
		this.errVarValue = varValue;
		this.errFuncDesc = funcDesc;
		return getErrorObj();
	}

	@Nonnull
	public Map<String, Object> setError(@Nonnull String varDispName, @Nullable String varValue, @Nonnull String funcDesc)
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
		this.errVarDispName = varDispName;
		this.errVarValue = varValue;
		this.errFuncDesc = funcDesc;
		return getErrorObj();
	}

	@Nonnull
	public Map<String, Object> getErrorObj()
	{
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("errorMsg", this.errMsg);
		retMap.put("funcName", this.funcName);
		if (this.errVarDispName != null)
		{
			retMap.put("varName", this.errVarDispName);
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

	@Nullable
	public Map<String, Object> handleException(@Nonnull Exception ex) throws IOException
	{
		this.logger.logException(ex);
		setRespStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		return null;
	}

	public void exceptionDetail(@Nonnull Exception ex)
	{
		StringWriter writer = new StringWriter();
		ex.printStackTrace(new PrintWriter(writer));
		this.exDetail = writer.toString();
	}
}
