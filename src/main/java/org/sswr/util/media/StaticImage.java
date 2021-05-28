package org.sswr.util.media;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;

import javax.imageio.metadata.IIOMetadata;

import org.sswr.util.data.DataTools;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.StringUtil;

public class StaticImage
{
	private BufferedImage img;
	private IIOMetadata metadata;

	public StaticImage(BufferedImage img, IIOMetadata metadata)
	{
		this.img = img;
		this.metadata = metadata;
	}

	public BufferedImage getBufferedImage()
	{
		return this.img;
	}

	public IIOMetadata getMetadata()
	{
		return this.metadata;
	}

	public int getWidth()
	{
		return this.img.getWidth();
	}

	public int getHeight()
	{
		return this.img.getHeight();
	}

	public double getPixelAspectRatio()
	{
		return 1.0;
	}

	public static String getBufferedImageTypeString(int type)
	{
		switch (type)
		{
		case BufferedImage.TYPE_3BYTE_BGR:
			return "TYPE_3BYTE_BGR";
		case BufferedImage.TYPE_4BYTE_ABGR:
			return "TYPE_4BYTE_ABGR";
		case BufferedImage.TYPE_4BYTE_ABGR_PRE:
			return "TYPE_4BYTE_ABGR_PRE";
		case BufferedImage.TYPE_BYTE_BINARY:
			return "TYPE_BYTE_BINARY";
		case BufferedImage.TYPE_BYTE_GRAY:
			return "TYPE_BYTE_GRAY";
		case BufferedImage.TYPE_BYTE_INDEXED:
			return "TYPE_BYTE_INDEXED";
		case BufferedImage.TYPE_CUSTOM:
			return "TYPE_CUSTOM";
		case BufferedImage.TYPE_INT_ARGB:
			return "TYPE_INT_ARGB";
		case BufferedImage.TYPE_INT_ARGB_PRE:
			return "TYPE_INT_ARGB_PRE";
		case BufferedImage.TYPE_INT_BGR:
			return "TYPE_INT_BGR";
		case BufferedImage.TYPE_INT_RGB:
			return "TYPE_INT_RGB";
		case BufferedImage.TYPE_USHORT_555_RGB:
			return "TYPE_USHORT_555_RGB";
		case BufferedImage.TYPE_USHORT_565_RGB:
			return "TYPE_USHORT_565_RGB";
		case BufferedImage.TYPE_USHORT_GRAY:
			return "TYPE_USHORT_GRAY";
		default:
			return "Unknwon Type";
		}
	}

	public static String getTransparencyString(int transparency)
	{
		switch (transparency)
		{
		case Transparency.OPAQUE:
			return "OPAQUE";
		case Transparency.BITMASK:
			return "BITMASK";
		case Transparency.TRANSLUCENT:
			return "TRANSLUCENT";
		default:
			return "Unknown";
		}
	}

	public static String getTransferTypeString(int transferType)
	{
		switch (transferType)
		{
		case DataBuffer.TYPE_USHORT:
			return "TYPE_USHORT";
		case DataBuffer.TYPE_BYTE:
			return "TYPE_BYTE";
		case DataBuffer.TYPE_INT:
			return "TYPE_INT";
		case DataBuffer.TYPE_SHORT:
			return "TYPE_SHORT";
		case DataBuffer.TYPE_FLOAT:
			return "TYPE_FLOAT";
		case DataBuffer.TYPE_DOUBLE:
			return "TYPE_DOUBLE";
		default:
			return "Unknown";
		}
	}

	public static void toICC_ProfileString(ICC_Profile profile, StringBuilder sb)
	{
		try
		{
			ICCProfile prof = new ICCProfile(profile.getData());
			prof.toString(sb);
		}
		catch (IllegalArgumentException ex)
		{
			StringUtil.toHex(profile.getData(), ' ', LineBreakType.CRLF, sb);
		}
	}

	public static void toColorSpaceString(ColorSpace cs, StringBuilder sb)
	{
		if (cs instanceof ICC_ColorSpace)
		{
			ICC_ColorSpace iccCs = (ICC_ColorSpace)cs;
			ICC_Profile iccProfile = iccCs.getProfile();
			sb.append("ColorSpace = ICC");
			sb.append("\r\nICC Profile:\r\n");
			toICC_ProfileString(iccProfile, sb);
		}
		else
		{
			sb.append("ColorSpace = ");
			sb.append(DataTools.toObjectString(cs));
		}
	}

	public static void toColorModelString(ColorModel cm, StringBuilder sb)
	{
		int i;
		int j = cm.getNumComponents();
		sb.append("PixelSize = ");
		sb.append(cm.getPixelSize());
		sb.append("\r\nNumComponents = ");
		sb.append(j);
		i = 0;
		while (i < j)
		{
			sb.append("\r\nComponentSize[");
			sb.append(i);
			sb.append("] = ");
			sb.append(cm.getComponentSize(i));
			i++;
		}
		sb.append("\r\nTransparency = ");
		sb.append(getTransparencyString(cm.getTransparency()));
		sb.append("\r\nTransferType = ");
		sb.append(getTransferTypeString(cm.getTransferType()));
		sb.append("\r\nHasAlpha = ");
		sb.append(cm.hasAlpha());
		sb.append("\r\nIsAlphaPremultiplied = ");
		sb.append(cm.isAlphaPremultiplied());
		ColorSpace cs = cm.getColorSpace();
		if (cs == null)
		{
			sb.append("\r\nColorSpace = null");
		}
		else
		{
			sb.append("\r\n");
			toColorSpaceString(cs, sb);
		}
	}

	public static void toBufferedImageString(BufferedImage bImage, StringBuilder sb)
	{
		sb.append("Type = ");
		sb.append(getBufferedImageTypeString(bImage.getType()));
		sb.append("\r\nWidth = ");
		sb.append(bImage.getWidth());
		sb.append("\r\nHeight = ");
		sb.append(bImage.getHeight());
		sb.append("\r\n");
		toColorModelString(bImage.getColorModel(), sb);
		sb.append("\r\nRaster = ");
		sb.append(bImage.getRaster());
	}

	public void toString(StringBuilder sb)
	{
		toBufferedImageString(this.img, sb);
		if (this.metadata != null)
		{
			sb.append("\r\n");
			sb.append(DataTools.toObjectString(this.metadata));
		}
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}
}