package org.sswr.util.media;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.sswr.util.basic.Matrix3;
import org.sswr.util.basic.Vector3;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.StringUtil;
import org.sswr.util.media.LUTInt.DataFormat;
import org.sswr.util.media.cs.TransferParam;
import org.sswr.util.media.cs.TransferType;

public class ICCProfile
{
	private byte iccBuff[];

	public ICCProfile(byte iccBuff[])
	{
		int buffSize = ByteTool.readMInt32(iccBuff, 0);
		if (buffSize != iccBuff.length)
		{
			throw new IllegalArgumentException("Invalid Profile size");
		}
		this.iccBuff = iccBuff;
	}

	public int getCMMType()
	{
		return ByteTool.readMInt32(this.iccBuff, 4);
	}
	
	public int getMajorVer()
	{
		return this.iccBuff[8] & 0xff;
	}

	public int getMinorVer()
	{
		return (this.iccBuff[9] >> 4) & 0xf;
	}

	public int getBugFixVer()
	{
		return this.iccBuff[9] & 0xf;
	}

	public int getProfileClass()
	{
		return ByteTool.readMInt32(this.iccBuff, 12);
	}
	
	public int getDataColorspace()
	{
		return ByteTool.readMInt32(this.iccBuff, 16);
	}
	
	public int getPCS()
	{
		return ByteTool.readMInt32(this.iccBuff, 20);
	}
	
	public LocalDateTime getCreateTime()
	{
		return readDateTimeNumber(this.iccBuff, 24);
	}
	
	public int getPrimaryPlatform()
	{
		return ByteTool.readMInt32(this.iccBuff, 40);
	}
	
	public int getProfileFlag()
	{
		return ByteTool.readMInt32(this.iccBuff, 44);
	}
	
	public int getDeviceManufacturer()
	{
		return ByteTool.readMInt32(this.iccBuff, 48);
	}
	
	public int getDeviceModel()
	{
		return ByteTool.readMInt32(this.iccBuff, 52);
	}
	
	public int getDeviceAttrib()
	{
		return ByteTool.readMInt32(this.iccBuff, 56);
	}
	
	public int getDeviceAttrib2()
	{
		return ByteTool.readMInt32(this.iccBuff, 60);
	}
	
	public int getRenderingIntent()
	{
		return ByteTool.readMInt32(this.iccBuff, 64);
	}
	
	public Vector3 getPCSIlluminant()
	{
		return readXYZNumber(this.iccBuff, 68);
	}
	
	public int getProfileCreator()
	{
		return ByteTool.readMInt32(this.iccBuff, 80);
	}
	
	public int getTagCount()
	{
		return ByteTool.readMInt32(this.iccBuff, 128);
	}

	public LUT createRLUT()
	{
		int cnt = ByteTool.readMInt32(this.iccBuff, 128);
		int i = 0;
		LUTInt lut;
		while (i < cnt)
		{
			int tagSign;
			int tagOfst;
	//		int tagLeng;
			int tagType;
			int valCnt;
	
			tagSign = ByteTool.readMInt32(this.iccBuff, 132 + 12 * i);
			if (tagSign == 0x72545243 || tagSign == 0x6B545243)
			{
				tagOfst = ByteTool.readMInt32(this.iccBuff, 136 + 12 * i);
	//			tagLeng = ByteTool.readMInt32(this.iccBuff, 140 + 12 * i);
				tagType = ByteTool.readMInt32(this.iccBuff, tagOfst);
				if (tagType == 0x63757276)
				{
					int j;
					int dtab[];
					valCnt = ByteTool.readMInt32(this.iccBuff, tagOfst + 8);
					if (valCnt <= 1)
					{
						return null;
					}
					lut = new LUTInt(1, valCnt, 1, DataFormat.UINT16);
					dtab = lut.getTablePtr();
					j = 0;
					while (j < valCnt)
					{
						dtab[j] = ByteTool.readMUInt16(this.iccBuff, tagOfst + 12 + j * 2);
						j++;
					}
					return lut;
				}
				else
				{
					return null;
				}
			}
			i++;
		}
		return null;
	}
	
	public LUT createGLUT()
	{
		int cnt = ByteTool.readMInt32(this.iccBuff, 128);
		int i = 0;
		LUTInt lut;
		while (i < cnt)
		{
			int tagSign;
			int tagOfst;
	//		int tagLeng;
			int tagType;
			int valCnt;
	
			tagSign = ByteTool.readMInt32(this.iccBuff, 132 + 12 * i);
			if (tagSign == 0x67545243 || tagSign == 0x6B545243)
			{
				tagOfst = ByteTool.readMInt32(this.iccBuff, 136 + 12 * i);
	//			tagLeng = ByteTool.readMInt32(this.iccBuff, 140 + 12 * i);
				tagType = ByteTool.readMInt32(this.iccBuff, tagOfst);
				if (tagType == 0x63757276)
				{
					int j;
					int dtab[];
					valCnt = ByteTool.readMInt32(this.iccBuff, tagOfst + 8);
					if (valCnt <= 1)
					{
						return null;
					}
					lut = new LUTInt(1, valCnt, 1, DataFormat.UINT16);
					dtab = lut.getTablePtr();
					j = 0;
					while (j < valCnt)
					{
						dtab[j] = ByteTool.readMUInt16(this.iccBuff, tagOfst + 12 + j * 2);
						j++;
					}
					return lut;
				}
				else
				{
					return null;
				}
			}
			i++;
		}
		return null;
	}
	
	public LUT createBLUT()
	{
		int cnt = ByteTool.readMInt32(this.iccBuff, 128);
		int i = 0;
		LUTInt lut;
		while (i < cnt)
		{
			int tagSign;
			int tagOfst;
	//		int tagLeng;
			int tagType;
			int valCnt;
	
			tagSign = ByteTool.readMInt32(this.iccBuff, 132 + 12 * i);
			if (tagSign == 0x62545243 || tagSign == 0x6B545243)
			{
				tagOfst = ByteTool.readMInt32(this.iccBuff, 136 + 12 * i);
	//			tagLeng = ByteTool.readMInt32(this.iccBuff, 140 + 12 * i);
				tagType = ByteTool.readMInt32(this.iccBuff, tagOfst);
				if (tagType == 0x63757276)
				{
					int j;
					int dtab[];
					valCnt = ByteTool.readMInt32(this.iccBuff, tagOfst + 8);
					if (valCnt <= 1)
					{
						return null;
					}
					lut = new LUTInt(1, valCnt, 1, DataFormat.UINT16);
					dtab = lut.getTablePtr();
					j = 0;
					while (j < valCnt)
					{
						dtab[j] = ByteTool.readMUInt16(this.iccBuff, tagOfst + 12 + j * 2);
						j++;
					}
					return lut;
				}
				else
				{
					return null;
				}
			}
			i++;
		}
		return null;
	}
	
	public TransferParam getRedTransferParam()
	{
		int cnt = ByteTool.readMInt32(this.iccBuff, 128);
		int i = 0;
		TransferParam param = null;
		while (i < cnt)
		{
			int tagSign;
			int tagOfst;
	//		int tagLeng;
			int tagType;
			int valCnt;
	
			tagSign = ByteTool.readMInt32(this.iccBuff, 132 + 12 * i);
			if (tagSign == 0x72545243 || tagSign == 0x6B545243)
			{
				tagOfst = ByteTool.readMInt32(this.iccBuff, 136 + 12 * i);
	//			tagLeng = ByteTool.readMInt32(this.iccBuff, 140 + 12 * i);
				tagType = ByteTool.readMInt32(this.iccBuff, tagOfst);
				if (tagType == 0x63757276)
				{
					valCnt = ByteTool.readMInt32(this.iccBuff, tagOfst + 8);
					if (valCnt == 0)
					{
						param = new TransferParam(TransferType.LINEAR, 1.0);
					}
					else if (valCnt == 1)
					{
						param = new TransferParam(TransferType.GAMMA, readU8Fixed8Number(this.iccBuff, tagOfst + 12));
					}
					else
					{
						LUT lut = this.createRLUT();
						param = new TransferParam(lut);
					}
	
					return param;
				}
				else if (tagType == 0x70617261) //parametricCurveType
				{
					int funcType = ByteTool.readMInt16(this.iccBuff, tagOfst + 8);
					double params[] = new double[7];
					if (funcType == 0)
					{
						param = new TransferParam(TransferType.GAMMA, readS15Fixed16Number(this.iccBuff, tagOfst + 12));
						return param;
					}
					else if (funcType == 3)
					{
						params[0] = readS15Fixed16Number(this.iccBuff, tagOfst + 12);
						params[1] = readS15Fixed16Number(this.iccBuff, tagOfst + 16);
						params[2] = readS15Fixed16Number(this.iccBuff, tagOfst + 20);
						params[3] = readS15Fixed16Number(this.iccBuff, tagOfst + 24);
						params[4] = readS15Fixed16Number(this.iccBuff, tagOfst + 28);
						params[5] = 0;
						params[6] = 0;
						param = new TransferParam();
						param.set(TransferType.PARAM1, params);
						return param;
					}
					else if (funcType == 4)
					{
						params[0] = readS15Fixed16Number(this.iccBuff, tagOfst + 12);
						params[1] = readS15Fixed16Number(this.iccBuff, tagOfst + 16);
						params[2] = readS15Fixed16Number(this.iccBuff, tagOfst + 20);
						params[3] = readS15Fixed16Number(this.iccBuff, tagOfst + 24);
						params[4] = readS15Fixed16Number(this.iccBuff, tagOfst + 28);
						params[5] = readS15Fixed16Number(this.iccBuff, tagOfst + 32);
						params[6] = readS15Fixed16Number(this.iccBuff, tagOfst + 36);
						param = new TransferParam();
						param.set(TransferType.PARAM1, params);
						return param;
					}
				}
				else
				{
					return null;
				}
			}
			i++;
		}
		return null;
	}
	
	public TransferParam getGreenTransferParam()
	{
		int cnt = ByteTool.readMInt32(this.iccBuff, 128);
		int i = 0;
		TransferParam param = null;
		while (i < cnt)
		{
			int tagSign;
			int tagOfst;
	//		int tagLeng;
			int tagType;
			int valCnt;
	
			tagSign = ByteTool.readMInt32(this.iccBuff, 132 + 12 * i);
			if (tagSign == 0x67545243 || tagSign == 0x6B545243)
			{
				tagOfst = ByteTool.readMInt32(this.iccBuff, 136 + 12 * i);
	//			tagLeng = ByteTool.readMInt32(this.iccBuff, 140 + 12 * i);
				tagType = ByteTool.readMInt32(this.iccBuff, tagOfst);
				if (tagType == 0x63757276)
				{
					valCnt = ByteTool.readMInt32(this.iccBuff, tagOfst + 8);
					if (valCnt == 0)
					{
						param = new TransferParam(TransferType.LINEAR, 1.0);
					}
					else if (valCnt == 1)
					{
						param = new TransferParam(TransferType.GAMMA, readU8Fixed8Number(this.iccBuff, tagOfst + 12));
					}
					else
					{
						LUT lut = this.createGLUT();
						param = new TransferParam(lut);
					}
	
	/*				Double gamma;
					Media::CS::TransferType tranType;
					tranType = FindTransferType(valCnt, (UInt16*)&this.iccBuff[tagOfst + 12], &gamma);
					param->Set(tranType, gamma);*/
					return param;
				}
				else if (tagType == 0x70617261) //parametricCurveType
				{
					int funcType = ByteTool.readMInt16(this.iccBuff, tagOfst + 8);
					double params[] = new double[7];
					if (funcType == 0)
					{
						param = new TransferParam(TransferType.GAMMA, readS15Fixed16Number(this.iccBuff, tagOfst + 12));
						return param;
					}
					else if (funcType == 3)
					{
						params[0] = readS15Fixed16Number(this.iccBuff, tagOfst + 12);
						params[1] = readS15Fixed16Number(this.iccBuff, tagOfst + 16);
						params[2] = readS15Fixed16Number(this.iccBuff, tagOfst + 20);
						params[3] = readS15Fixed16Number(this.iccBuff, tagOfst + 24);
						params[4] = readS15Fixed16Number(this.iccBuff, tagOfst + 28);
						params[5] = 0;
						params[6] = 0;
						param = new TransferParam();
						param.set(TransferType.PARAM1, params);
						return param;
					}
					else if (funcType == 4)
					{
						params[0] = readS15Fixed16Number(this.iccBuff, tagOfst + 12);
						params[1] = readS15Fixed16Number(this.iccBuff, tagOfst + 16);
						params[2] = readS15Fixed16Number(this.iccBuff, tagOfst + 20);
						params[3] = readS15Fixed16Number(this.iccBuff, tagOfst + 24);
						params[4] = readS15Fixed16Number(this.iccBuff, tagOfst + 28);
						params[5] = readS15Fixed16Number(this.iccBuff, tagOfst + 32);
						params[6] = readS15Fixed16Number(this.iccBuff, tagOfst + 36);
						param = new TransferParam();
						param.set(TransferType.PARAM1, params);
						return param;
					}
				}
				else
				{
					return null;
				}
			}
			i++;
		}
		return null;
	}
	
	public TransferParam getBlueTransferParam()
	{
		int cnt = ByteTool.readMInt32(this.iccBuff, 128);
		int i = 0;
		TransferParam param = null;
		while (i < cnt)
		{
			int tagSign;
			int tagOfst;
	//		int tagLeng;
			int tagType;
			int valCnt;
	
			tagSign = ByteTool.readMInt32(this.iccBuff, 132 + 12 * i);
			if (tagSign == 0x62545243 || tagSign == 0x6B545243)
			{
				tagOfst = ByteTool.readMInt32(this.iccBuff, 136 + 12 * i);
	//			tagLeng = ByteTool.readMInt32(this.iccBuff, 140 + 12 * i);
				tagType = ByteTool.readMInt32(this.iccBuff, tagOfst);
				if (tagType == 0x63757276)
				{
					valCnt = ByteTool.readMInt32(this.iccBuff, tagOfst + 8);
					if (valCnt == 0)
					{
						param = new TransferParam(TransferType.LINEAR, 1.0);
					}
					else if (valCnt == 1)
					{
						param = new TransferParam(TransferType.GAMMA, readU8Fixed8Number(this.iccBuff, tagOfst + 12));
					}
					else
					{
						LUT lut = this.createBLUT();
						param = new TransferParam(lut);
					}
	
	/*				Double gamma;
					Media::CS::TransferType tranType;
					tranType = FindTransferType(valCnt, (UInt16*)&this.iccBuff[tagOfst + 12], &gamma);
					param->Set(tranType, gamma);*/
					return param;
				}
				else if (tagType == 0x70617261) //parametricCurveType
				{
					int funcType = ByteTool.readMInt16(this.iccBuff, tagOfst + 8);
					double params[] = new double[7];
					if (funcType == 0)
					{
						param = new TransferParam(TransferType.GAMMA, readS15Fixed16Number(this.iccBuff, tagOfst + 12));
						return param;
					}
					else if (funcType == 3)
					{
						params[0] = readS15Fixed16Number(this.iccBuff, tagOfst + 12);
						params[1] = readS15Fixed16Number(this.iccBuff, tagOfst + 16);
						params[2] = readS15Fixed16Number(this.iccBuff, tagOfst + 20);
						params[3] = readS15Fixed16Number(this.iccBuff, tagOfst + 24);
						params[4] = readS15Fixed16Number(this.iccBuff, tagOfst + 28);
						params[5] = 0;
						params[6] = 0;
						param = new TransferParam();
						param.set(TransferType.PARAM1, params);
						return param;
					}
					else if (funcType == 4)
					{
						params[0] = readS15Fixed16Number(this.iccBuff, tagOfst + 12);
						params[1] = readS15Fixed16Number(this.iccBuff, tagOfst + 16);
						params[2] = readS15Fixed16Number(this.iccBuff, tagOfst + 20);
						params[3] = readS15Fixed16Number(this.iccBuff, tagOfst + 24);
						params[4] = readS15Fixed16Number(this.iccBuff, tagOfst + 28);
						params[5] = readS15Fixed16Number(this.iccBuff, tagOfst + 32);
						params[6] = readS15Fixed16Number(this.iccBuff, tagOfst + 36);
						param = new TransferParam();
						param.set(TransferType.PARAM1, params);
						return param;
					}
				}
				else
				{
					return null;
				}
			}
			i++;
		}
		return null;
	}
	
	public ColorPrimaries getColorPrimaries()
	{
		int cnt = ByteTool.readMInt32(this.iccBuff, 128);
		int i = 0;
		Vector3 rxyz = null;
		Vector3 gxyz = null;
		Vector3 bxyz = null;
		Vector3 wxyz = null;
		while (i < cnt)
		{
			int tagSign;
			int tagOfst;
	//		int tagLeng;
			int tagType;
	
			tagSign = ByteTool.readMInt32(this.iccBuff, 132 + 12 * i);
			if (tagSign == 0x7258595A)
			{
				tagOfst = ByteTool.readMInt32(this.iccBuff, 136 + 12 * i);
	//			tagLeng = ByteTool.readMInt32(this.iccBuff, [140 + 12 * i]);
				tagType = ByteTool.readMInt32(this.iccBuff, tagOfst);
				if (tagType == 0x58595A20)
				{
					rxyz = readXYZNumber(this.iccBuff, tagOfst + 8);
				}
				else
				{
					return null;
				}
			}
			else if (tagSign == 0x6758595A)
			{
				tagOfst = ByteTool.readMInt32(this.iccBuff, 136 + 12 * i);
	//			tagLeng = ByteTool.readMInt32(this.iccBuff, 140 + 12 * i);
				tagType = ByteTool.readMInt32(this.iccBuff, tagOfst);
				if (tagType == 0x58595A20)
				{
					gxyz = readXYZNumber(this.iccBuff, tagOfst + 8);
				}
				else
				{
					return null;
				}
			}
			else if (tagSign == 0x6258595A)
			{
				tagOfst = ByteTool.readMInt32(this.iccBuff, 136 + 12 * i);
	//			tagLeng = ByteTool.readMInt32(this.iccBuff, 140 + 12 * i);
				tagType = ByteTool.readMInt32(this.iccBuff, tagOfst);
				if (tagType == 0x58595A20)
				{
					bxyz = readXYZNumber(this.iccBuff, tagOfst + 8);
				}
				else
				{
					return null;
				}
			}
			else if (tagSign == 0x77747074)
			{
				tagOfst = ByteTool.readMInt32(this.iccBuff, 136 + 12 * i);
	//			tagLeng = ByteTool.readMInt32(this.iccBuff, [140 + 12 * i]);
				tagType = ByteTool.readMInt32(this.iccBuff, tagOfst);
				if (tagType == 0x58595A20)
				{
					wxyz = readXYZNumber(this.iccBuff, tagOfst + 8);
				}
				else
				{
					return null;
				}
			}
			i++;
		}
		if (rxyz != null && gxyz != null && gxyz != null && wxyz != null)
		{
			Matrix3 mat;
			Matrix3 mat2;
			Matrix3 mat3;
			Vector3 vec1;
			Vector3 vec2;
			Vector3 vec3;
			ColorPrimaries color = new ColorPrimaries();
	
			mat = ColorPrimaries.getMatrixBradford();
			mat2 = new Matrix3();
			mat2.set(mat);
			
			mat3 = new Matrix3();
			mat3.setIdentity();
	
			vec3 = ColorPrimaries.getWhitePointXYZ(WhitePointType.D50);
			vec1 = mat.multiply(vec3);
			vec2 = mat.multiply(wxyz);
			mat.inverse();
			mat3.vec[0].val[0] = vec2.val[0] / vec1.val[0];
			mat3.vec[1].val[1] = vec2.val[1] / vec1.val[1];
			mat3.vec[2].val[2] = vec2.val[2] / vec1.val[2];
			mat.multiply(mat3);
			mat.multiply(mat2);
	
			color.colorType = ColorType.CUSTOM;
			vec1 = mat.multiply(rxyz);
			vec2 = ColorPrimaries.XYZToxyY(vec1);
			color.r.setX(vec2.val[0]);
			color.r.setY(vec2.val[1]);
			vec1 = mat.multiply(gxyz);
			vec2 = ColorPrimaries.XYZToxyY(vec1);
			color.g.setX(vec2.val[0]);
			color.g.setY(vec2.val[1]);
			vec1 = mat.multiply(bxyz);
			vec2 = ColorPrimaries.XYZToxyY(vec1);
			color.b.setX(vec2.val[0]);
			color.b.setY(vec2.val[1]);
			vec2 = ColorPrimaries.XYZToxyY(wxyz);
			color.w.setX(vec2.val[0]);
			color.w.setY(vec2.val[1]);
			return color;
		}
		else
		{
			return null;
		}
	}
		
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	public void toString(StringBuilder sb)
	{
		LocalDateTime ldt;
		Vector3 xyz;
		int val;
	
		sb.append("Preferred CMM Type = ");
		sb.append(getNameCMMType(this.getCMMType()));
	
		sb.append("\r\nProfile version number = ");
		sb.append(this.getMajorVer());
		sb.append(".");
		sb.append(this.getMinorVer());
		sb.append(".");
		sb.append(this.getBugFixVer());
	
		sb.append("\r\nProfile/Device class = ");
		sb.append(getNameProfileClass(this.getProfileClass()));
	
		sb.append("\r\nData colour space = ");
		sb.append(getNameDataColorspace(this.getDataColorspace()));
	
		sb.append("\r\nPCS (profile connection space) = ");
		sb.append(getNameDataColorspace(this.getPCS()));
	
		ldt = this.getCreateTime();
		sb.append("\r\nCreate Time = ");
		sb.append(ldt);
	
		sb.append("\r\nPrimary Platform = ");
		sb.append(getNamePrimaryPlatform(this.getPrimaryPlatform()));
	
		val = this.getProfileFlag();
		if ((val & 1) != 0)
		{
			sb.append("\r\nEmbedded profile = True");
		}
		else
		{
			sb.append("\r\nEmbedded profile = False");
		}
		if ((val & 2) != 0)
		{
			sb.append("\r\nProfile cannot be used independently of the embedded colour data = True");
		}
		else
		{
			sb.append("\r\nProfile cannot be used independently of the embedded colour data = False");
		}
	
		sb.append("\r\nDevice manufacturer = ");
		sb.append(getNameDeviceManufacturer(this.getDeviceManufacturer()));
	
		sb.append("\r\nDevice model = ");
		sb.append(getNameDeviceModel(this.getDeviceModel()));
	
		xyz = this.getPCSIlluminant();
		sb.append("\r\nPCS illuminant = ");
		getDispCIEXYZ(sb, xyz);
	
		sb.append("\r\nProfile creator = ");
		sb.append(getNameDeviceManufacturer(this.getProfileCreator()));
	
		int cnt = ByteTool.readMInt32(this.iccBuff, 128);
		int i = 0;
		while (i < cnt)
		{
			int tagSign;
			int tagOfst;
			int tagLeng;
			tagSign = ByteTool.readMInt32(this.iccBuff, 132 + 12 * i);
			tagOfst = ByteTool.readMInt32(this.iccBuff, 136 + 12 * i);
			tagLeng = ByteTool.readMInt32(this.iccBuff, 140 + 12 * i);
	
			sb.append("\r\n");
			sb.append(getNameTag(tagSign));
			sb.append(" = ");
			getDispTagType(sb, this.iccBuff, tagOfst, tagLeng);
			i++;
		}
	}

	public static LocalDateTime readDateTimeNumber(byte buff[], int index)
	{
		return LocalDateTime.of(ByteTool.readMUInt16(buff, index),
			ByteTool.readMUInt16(buff, index + 2),
			ByteTool.readMUInt16(buff, index + 4),
			ByteTool.readMUInt16(buff, index + 6),
			ByteTool.readMUInt16(buff, index + 8),
			ByteTool.readMUInt16(buff, index + 10));
	}
	
	public static Vector3 readXYZNumber(byte buff[], int index)
	{
		Vector3 vec = new Vector3();
		vec.val[0] = readS15Fixed16Number(buff, index + 0);
		vec.val[1] = readS15Fixed16Number(buff, index + 4);
		vec.val[2] = readS15Fixed16Number(buff, index + 8);
		return vec;
	}
	
	public static double readS15Fixed16Number(byte buff[], int index)
	{
		return ByteTool.readMInt32(buff, index) / 65536.0;
	}
	
	public static double readU16Fixed16Number(byte buff[], int index)
	{
		return ByteTool.readMUInt16(buff, index) + ByteTool.readMUInt16(buff, index + 2) / 65536.0;
	}
	
	public static double readU8Fixed8Number(byte buff[], int index)
	{
		return ByteTool.readMUInt16(buff, index) / 256.0;
	}
	
	public static String getNameCMMType(int val)
	{
		switch (val)
		{
		case 0:
			return "(not used)";
		case 0x33324254:
			return "the imaging factory CMM";
		case 0x41434D53:
			return "Agfa CMM";
		case 0x41444245:
			return "Adobe CMM";
		case 0x43434D53:
			return "ColorGear CMM";
		case 0x45464920:
			return "EFI CMM";
		case 0x46462020:
			return "Fuji Film CMM";
		case 0x48434d4d:
			return "Harlequin RIP CMM";
		case 0x48444d20:
			return "Heidelberg CMM";
		case 0x4b434d53:
			return "Kodak CMM";
		case 0x4C676f53:
			return "LogoSync CMM";
		case 0x4d434d44:
			return "Konica Minolta CMM";
		case 0x52474d53:
			return "DeviceLink CMM";
		case 0x53494343:
			return "SampleICC CMM";
		case 0x5349474E:
			return "Mutoh CMM";
		case 0x5543434D:
			return "ColorGear CMM Lite";
		case 0x55434D53:
			return "ColorGear CMM C";
		case 0x57544720:
			return "Ware to Go CMM";
		case 0x6170706C:
			return "Apple CMM";
		case 0x6172676C:
			return "Argyll CMS CMM";
		case 0x7a633030:
			return "Zoran CMM";
		case 0x6C636d73:
			return "Little CMS CMM";
		default:
			return "Unknown";
		}
	}
	
	public static String getNameProfileClass(int val)
	{
		switch (val)
		{
		case 0:
			return "(not used)";
		case 0x73636E72:
			return "Input device profile";
		case 0x6D6E7472:
			return "Display device profile";
		case 0x70727472:
			return "Output device profile";
		case 0x6C696E6B:
			return "DeviceLink profile";
		case 0x73706163:
			return "ColorSpace profile";
		case 0x61627374:
			return "Abstract profile";
		case 0x6E6D636C:
			return "NamedColor profile";
		default:
			return "Unknown";
		}
	}
	
	public static String getNameDataColorspace(int val)
	{
		switch (val)
		{
		case 0:
			return "(not used)";
		case 0x58595A20:
			return "nCIEXYZ or PCSXYZ";
		case 0x4C616220:
			return "CIELAB or PCSLAB";
		case 0x4C757620:
			return "CIELUV";
		case 0x59436272:
			return "YCbCr";
		case 0x59787920:
			return "CIEYxy";
		case 0x52474220:
			return "RGB";
		case 0x47524159:
			return "Gray";
		case 0x48535620:
			return "HSV";
		case 0x484C5320:
			return "HLS";
		case 0x434D594B:
			return "CMYK";
		case 0x434D5920:
			return "CMY";
		case 0x32434C52:
			return "2 colour";
		case 0x33434C52:
			return "3 colour";
		case 0x34434C52:
			return "4 colour";
		case 0x35434C52:
			return "5 colour";
		case 0x36434C52:
			return "6 colour";
		case 0x37434C52:
			return "7 colour";
		case 0x38434C52:
			return "8 colour";
		case 0x39434C52:
			return "9 colour";
		case 0x41434C52:
			return "10 colour";
		case 0x42434C52:
			return "11 colour";
		case 0x43434C52:
			return "12 colour";
		case 0x44434C52:
			return "13 colour";
		case 0x45434C52:
			return "14 colour";
		case 0x46434C52:
			return "15 colour";
		default:
			return "Unknown";
		}
	}
	
	public static String getNamePrimaryPlatform(int val)
	{
		switch (val)
		{
		case 0:
			return "(not used)";
		case 0x4150504C:
			return "Apple Computer, Inc.";
		case 0x4D534654:
			return "Microsoft Corporation";
		case 0x53474920:
			return "Silicon Graphics, Inc.";
		case 0x53554E57:
			return "Sun Microsystems, Inc.";
		default:
			return "Unknown";
		}
	}
	
	public static String getNameDeviceManufacturer(int val)
	{
		switch (val)
		{
		case 0:
			return "(not used)";
		default:
			return "Unknown";
		}
	}
	
	public static String getNameDeviceModel(int val)
	{
		switch (val)
		{
		case 0:
			return "(not used)";
		default:
			return "Unknown";
		}
	}
	
	public static String getNameTag(int val)
	{
		switch (val)
		{
		case 0:
			return "(not used)";
		case 0x41324230:
			return "AToB0Tag";
		case 0x41324231:
			return "AToB1Tag";
		case 0x41324232:
			return "AToB2Tag";
		case 0x42324130:
			return "BToA0Tag";
		case 0x42324131:
			return "BToA1Tag";
		case 0x42324132:
			return "BToA2Tag";
		case 0x42324430:
			return "BToD0Tag";
		case 0x42324431:
			return "BToD1Tag";
		case 0x42324432:
			return "BToD2Tag";
		case 0x42324433:
			return "BToD3Tag";
		case 0x44324230:
			return "DToB0Tag";
		case 0x44324231:
			return "DToB1Tag";
		case 0x44324232:
			return "DToB2Tag";
		case 0x44324233:
			return "DToB3Tag";
		case 0x62545243:
			return "blueTRCTag";
		case 0x6258595A:
			return "blueMatrixColumnTag";
		case 0x626B7074:
			return "mediaBlackPointTag";
		case 0x63616C74:
			return "calibrationDateTimeTag";
		case 0x63686164:
			return "chromaticAdaptationTag";
		case 0x63696973:
			return "colorimetricIntentImageStateTag";
		case 0x636C6F74:
			return "colorantTableOutTag";
		case 0x6368726D:
			return "chromaticityTag";
		case 0x636C726F:
			return "colorantOrderTag";
		case 0x636C7274:
			return "colorantTableTag";
		case 0x63707274:
			return "copyrightTag";
		case 0x64657363:
			return "profileDescriptionTag";
		case 0x646D6464:
			return "deviceModelDescTag";
		case 0x646D6E64:
			return "deviceMfgDescTag";
		case 0x67545243:
			return "greenTRCTag";
		case 0x6758595A:
			return "greenMatrixColumnTag";
		case 0x67616D74:
			return "gamutTag";
		case 0x6B545243:
			return "grayTRCTag";
		case 0x6C756D69:
			return "luminanceTag";
		case 0x6D656173:
			return "measurementTag";
		case 0x6E636C32:
			return "namedColor2Tag";
		case 0x70726530:
			return "preview0Tag";
		case 0x70726531:
			return "preview1Tag";
		case 0x70726532:
			return "preview2Tag";
		case 0x70736571:
			return "profileSequenceDescTag";
		case 0x70736964:
			return "profileSequenceIdentifierTag";
		case 0x72545243:
			return "redTRCTag";
		case 0x7258595A:
			return "redMatrixColumnTag";
		case 0x72657370:
			return "outputResponseTag";
		case 0x72696730:
			return "perceptualRenderingIntentGamutTag";
		case 0x72696732:
			return "saturationRenderingIntentGamutTag";
		case 0x74617267:
			return "charTargetTag";
		case 0x74656368:
			return "technologyTag";
		case 0x76696577:
			return "viewingConditionsTag";
		case 0x76756564:
			return "viewingCondDescTag";
		case 0x77747074:
			return "mediaWhitePointTag";
		default:
			return "Unknown";
		}
	}
	
	public static String getNameStandardObserver(int val)
	{
		switch (val)
		{
		case 0:
			return "Unknown";
		case 1:
			return "CIE 1931 standard colorimetric observer";
		case 2:
			return "CIE 1964 standard colorimetric observer";
		default:
			return "Not defined";
		}
	}
	
	public static String getNameStandardIlluminent(int val)
	{
		switch (val)
		{
		case 0:
			return "Unknown";
		case 1:
			return "D50";
		case 2:
			return "D65";
		case 3:
			return "D93";
		case 4:
			return "F2";
		case 5:
			return "D55";
		case 6:
			return "A";
		case 7:
			return "Equi-Power (E)";
		case 8:
			return "F8";
		default:
			return "Not defined";
		}
	}

	public static void getDispCIEXYZ(StringBuilder sb, Vector3 xyz)
	{
		sb.append("X = ");
		sb.append(xyz.val[0]);
		sb.append(", Y = ");
		sb.append(xyz.val[1]);
		sb.append(", Z = ");
		sb.append(xyz.val[2]);
	
		double sum = xyz.val[0] + xyz.val[1] + xyz.val[2];
		if (sum != 0)
		{
			sb.append(", x = ");
			sb.append(xyz.val[0] / sum);
			sb.append(", y = ");
			sb.append(xyz.val[1] / sum);
		}
	}	

	public static void getDispTagType(StringBuilder sb, byte buff[], int index, int leng)
	{
		int typ = ByteTool.readMInt32(buff, index);
		int nCh;
		int val;
		Vector3 xyz;
		//Media::CS::TransferType tt;
		//double gamma;
		switch(typ)
		{
		case 0:
			sb.append("(not used)");
			break;
		case 0x6368726D:
			nCh = ByteTool.readMInt16(buff, index + 8);
			val = ByteTool.readMInt16(buff, index + 10);
			sb.append(val);
			sb.append(" {");
			val = 0;
			while (val < nCh)
			{
				if (val > 0)
				{
					sb.append(", ");
				}
				sb.append("(");
				sb.append(readU16Fixed16Number(buff, index + val * 8 + 12));
				sb.append(", ");
				sb.append(readU16Fixed16Number(buff, index + val * 8 + 16));
				sb.append(")");
				val++;
			}
			sb.append("}");
			break;
		case 0x74657874: //textType
			{
				if (buff[index + leng - 1] != 0)
				{
					sb.append(new String(buff, index + 8, leng - 8));
				}
				else
				{
					sb.append(new String(buff, index + 8, leng - 9));
				}
			}
			break;
		case 0x58595A20: //XYZType
			val = 8;
			nCh = 0;
			while (val <= leng - 12)
			{
				if (nCh != 0)
					sb.append("  ");
				xyz = readXYZNumber(buff, index + val);
				getDispCIEXYZ(sb, xyz);
				val += 12;
				nCh++;
			}
			break;
		case 0x76696577: //viewingConditionsTag
			sb.append("Illuminant: {");
			xyz = readXYZNumber(buff, index + 8);
			getDispCIEXYZ(sb, xyz);
	
			sb.append("}, Surround: {");
			xyz = readXYZNumber(buff, index + 20);
			getDispCIEXYZ(sb, xyz);
			sb.append("}, Illuminant type = ");
			sb.append(ByteTool.readMInt32(buff, index + 32));
			break;
		case 0x6D656173: //measurementType
			sb.append("Standard observer = ");
			sb.append(getNameStandardObserver(ByteTool.readMInt32(buff, index + 8)));
			sb.append(", Measurement backing: {");
			xyz = readXYZNumber(buff, index + 12);
			getDispCIEXYZ(sb, xyz);
			sb.append("}, Measurement geometry = ");
			sb.append(ByteTool.readMInt32(buff, index + 24));
			sb.append(", Measurement flare = ");
			sb.append(ByteTool.readMInt32(buff, index + 28));
			sb.append(", Standard illuminent = ");
			sb.append(getNameStandardIlluminent(ByteTool.readMInt32(buff, index + 32)));
			break;
		case 0x64657363: //desc
			{
				val = ByteTool.readMInt32(buff, index + 8);
				if (buff[index + 12 + val - 1] != 0)
				{
					sb.append(new String(buff, index + 12, val));
				}
				else
				{
					sb.append(new String(buff, index + 12, val - 1));
				}
			}
			break;
		case 0x73696720: //signatureType
			sb.append(StringUtil.toHex(buff, index + 8, 4));
			break;
		case 0x63757276: //curveType
			val = ByteTool.readMInt32(buff, index + 8);
			sb.append("Curve: ");
			if (val > 1)
			{
				sb.append(val);
				sb.append(" entries, ");
				sb.append("Closed to ");
			}
/*			tt = FindTransferType((UInt32)val, (UInt16*)&buff[12], &gamma);
			sb.append(Media::CS::TransferFunc::GetTransferFuncName(tt));
			if (tt == Media::CS::TRANT_GAMMA)
			{
				sb.append(", gamma = ");
				Text::SBAppendF64(sb, gamma);
			}*/
			break;
		case 0x70617261: //parametricCurveType
			sb.append("CurveType: ");
			sb.append(ByteTool.readMInt16(buff, index + 8));
			{
				double g;
				double a;
				double b;
				double c;
				double d;
				double e;
				double f;
	
				switch (ByteTool.readMInt16(buff, index + 8))
				{
				case 0:
					g = readS15Fixed16Number(buff, index + 12);
					sb.append(" Y = X ^ ");
					sb.append(g);
					break;
				case 1:
					g = readS15Fixed16Number(buff, index + 12);
					a = readS15Fixed16Number(buff, index + 16);
					b = readS15Fixed16Number(buff, index + 20);
					break;
				case 2:
					g = readS15Fixed16Number(buff, index + 12);
					a = readS15Fixed16Number(buff, index + 16);
					b = readS15Fixed16Number(buff, index + 20);
					c = readS15Fixed16Number(buff, index + 24);
					break;
				case 3:
					g = readS15Fixed16Number(buff, index + 12);
					a = readS15Fixed16Number(buff, index + 16);
					b = readS15Fixed16Number(buff, index + 20);
					c = readS15Fixed16Number(buff, index + 24);
					d = readS15Fixed16Number(buff, index + 28);
					sb.append(" if (X >= ");
					sb.append(d);
					sb.append(") Y = (");
					sb.append(a);
					sb.append(" * X + ");
					sb.append(b);
					sb.append(") ^ ");
					sb.append(g);
					sb.append(" else Y = ");
					sb.append(c);
					sb.append(" * X");
					break;
				case 4:
					g = readS15Fixed16Number(buff, index + 12);
					a = readS15Fixed16Number(buff, index + 16);
					b = readS15Fixed16Number(buff, index + 20);
					c = readS15Fixed16Number(buff, index + 24);
					d = readS15Fixed16Number(buff, index + 28);
					e = readS15Fixed16Number(buff, index + 32);
					f = readS15Fixed16Number(buff, index + 36);
					sb.append(" if (X >= ");
					sb.append(d);
					sb.append(") Y = (");
					sb.append(a);
					sb.append(" * X + ");
					sb.append(b);
					sb.append(") ^ ");
					sb.append(g);
					sb.append(" + ");
					sb.append(e);
					sb.append(" else Y = ");
					sb.append(c);
					sb.append(" * X + ");
					sb.append(f);
					break;
				default:
					break;
				}
			}
			break;
		case 0x6d667431: //lut8Type
			sb.append("LUT8");
			break;
		case 0x6d667432: //lut16Type
			sb.append("LUT16");
			break;
		case 0x75693332: //uInt32ArrayType
			sb.append("uInt32 Array (");
			sb.append((leng - 8) >> 2);
			sb.append(")");
			break;
		case 0x75693038: //uInt8ArrayType
			sb.append("uInt8 Array (");
			sb.append((leng - 8));
			sb.append(")");
			break;
		case 0x73663332: //s15Fixed16ArrayType
			sb.append("s15Fixed16 Array (");
			sb.append((leng - 8) >> 2);
			sb.append(")");
			break;
		case 0x6D6C7563: //multiLocalizedUnicodeType
			{
				int i;
				int j;
				i = ByteTool.readMInt32(buff, index + 8);
				j = 16;
				while (i-- > 0)
				{
					
					sb.append(new String(buff, index + ByteTool.readMInt32(buff, index + j + 8), ByteTool.readMInt32(buff, index + j + 4), StandardCharsets.UTF_16BE));
					if (i > 0)
					{
						sb.append(", ");
					}
					j += 12;
				}
			}
			break;
		case 0x6d6d6f64:
			sb.append("Unknown (mmod)");
			break;
		case 0x6D414220:
			sb.append("lutAToBType");
			break;
		case 0x6D424120:
			sb.append("lutBToAType");
			break;
		default:
			sb.append("Unknown");
			break;
		}
	}
}
