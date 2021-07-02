package org.sswr.util.media;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import org.sswr.util.data.ByteIO;
import org.sswr.util.data.ByteIOLSB;
import org.sswr.util.data.ByteIOMSB;
import org.sswr.util.data.ClassTools;
import org.sswr.util.data.DataTools;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.StringUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StaticImage
{
	private BufferedImage img;
	private IIOMetadata metadata;
	private boolean metadataParsed;
	private EXIFData exif;

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
			StringUtil.appendHex(sb, profile.getData(), ' ', LineBreakType.CRLF);
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

	private void parseMetadata()
	{
		if (this.metadataParsed)
		{
			return;
		}
		this.metadataParsed = true;
		String clsName = this.metadata.getClass().getName();
		if (clsName.equals("com.sun.imageio.plugins.jpeg.JPEGMetadata"))
		{
			Node treeNode = this.metadata.getAsTree("javax_imageio_jpeg_image_1.0");
			Node node;
			if (treeNode != null)
			{
				NodeList nodeList = treeNode.getChildNodes();
				int i = 0;
				int j = nodeList.getLength();
				while (i < j)
				{
					node = nodeList.item(i);
					if (node != null && node.getNodeName().equals("markerSequence"))
					{
						NodeList markerSequenceList = node.getChildNodes();
						int k = 0;
						int l = markerSequenceList.getLength();
						while (k < l)
						{
							IIOMetadataNode mnode = (IIOMetadataNode)markerSequenceList.item(k);
							byte[] barr = (byte[])mnode.getUserObject();
							if (barr != null && barr.length > 18)
							{
								if (barr[0] == 'E' && barr[1] == 'x' && barr[2] == 'i' && barr[3] == 'f')
								{
									ByteIO byteIO = null;
									if (barr[6] == 'I' && barr[7] == 'I')
									{
										byteIO = new ByteIOLSB();
									}
									else if (barr[6] == 'M' && barr[7] == 'M')
									{
										byteIO = new ByteIOMSB();
									}
									if (byteIO != null && byteIO.readInt16(barr, 8) == 42 && byteIO.readInt32(barr, 10) == 8)
									{
										SharedInt nextOfst = new SharedInt();
										this.exif = EXIFData.parseIFD(barr, 14, barr.length - 14, byteIO, nextOfst, EXIFMaker.STANDARD, 6);
									}
								}
							}
	
							k++;
						}
						//////////////////////////////
					}
					i++;
				}
			}
		}
		else
		{
			System.out.println("Unknown metadata class: "+clsName);
		}
	}

	public void toString(StringBuilder sb)
	{
		toBufferedImageString(this.img, sb);
		if (this.metadata != null)
		{
			this.parseMetadata();
			if (this.exif != null)
			{
				sb.append("\r\n");
				this.exif.toString(sb, null);
			}
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