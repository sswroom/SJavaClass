package org.sswr.util.media;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;

import org.sswr.util.data.RectangleArea;
public class ImageUtil {
	public static String getImageFmt(byte buff[])
	{
		if ((buff[0] & 0xff) == 0x89 && buff[1] == 0x50 && buff[2] == 0x4e && buff[3] == 0x47 && buff[4] == 0x0d && buff[5] == 0x0a && buff[6] == 0x1a && buff[7] == 0x0a)
		{
			return "png";
		}
		if (buff[0] == 'G' && buff[1] == 'I' && buff[2] == 'F' && buff[3] == '8' && buff[4] == '7' && buff[5] == 'a')
		{
			return "gif";
		}
		if (buff[0] == 'G' && buff[1] == 'I' && buff[2] == 'F' && buff[3] == '8' && buff[4] == '9' && buff[5] == 'a')
		{
			return "gif";
		}
		if (buff[0] == 'I' && buff[1] == 'I' && buff[2] == 42 && buff[3] == 0)
		{
			return "tif";
		}
		if (buff[0] == 'M' && buff[1] == 'M' && buff[2] == 0 && buff[3] == 42)
		{
			return "tif";
		}
		if ((buff[0] & 0xff) == 0xff && (buff[1] & 0xff) == 0xd8)
		{
			return "jpg";
		}
	
		return "dat";
	}

	public static ImageList load(InputStream stm)
	{
		try
		{
			ImageReader reader = ImageIO.getImageReadersBySuffix("jpg").next();
			reader.setInput(ImageIO.createImageInputStream(stm));
			IIOMetadata metadata = reader.getImageMetadata(0);
			ImageList imgList = new ImageList(metadata);
			int i = 0;
			int j = reader.getNumImages(true);
			while (i < j)
			{
				imgList.addImage(reader.read(i, null), 0);
				i++;
			}
			return imgList;
		}
		catch (IOException ex)
		{
			return null;
		}
	}

	public static boolean saveAsJpg(ImageList imgList, File output, float quality) //0-1
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(output);
			ImageOutputStream ios = ImageIO.createImageOutputStream(fos);
			ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
			ImageWriteParam iwParam = writer.getDefaultWriteParam();
			iwParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			iwParam.setCompressionQuality(quality);
			writer.setOutput(ios);
			writer.write(null, new IIOImage(imgList.getImage(0), null, imgList.getMetadata()), iwParam);
			writer.dispose();
			return true;
		}
		catch (IOException ex)
		{
			return false;
		}
	}

	public static BufferedImage cropImageSquare(BufferedImage img) throws IOException
	{
		int height = img.getHeight();
		int width = img.getWidth();

		if (height == width)
		{
			return img;
		}
		else if (width > height)
		{
			return img.getSubimage((width - height) >> 1, 0, height, height);
		}
		else
		{
			return img.getSubimage(0, (height - width) >> 1, width, width);
		}
	}

	public static RectangleArea fitToArea(RectangleArea area, double w, double h)
	{
		double newW;
		double newH;
		if (w * area.getH() > h * area.getW())
		{
			newW = area.getW();
			newH = newW / w * h;
		}
		else
		{
			newH = area.getH();
			newW = newH / h * w;
		}
		return new RectangleArea(area.getX() + (area.getW() - newW) * 0.5, area.getY() + (area.getH() - newH) * 0.5, newW, newH);
	}
}
