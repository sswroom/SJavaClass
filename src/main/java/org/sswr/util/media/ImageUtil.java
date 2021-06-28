package org.sswr.util.media;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;

import org.sswr.util.data.RectangleArea;

public class ImageUtil
{
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
		String fmt = "jpg";
		try
		{
			ImageReader reader = ImageIO.getImageReadersBySuffix(fmt).next();
			reader.setInput(ImageIO.createImageInputStream(stm));
			ImageList imgList = new ImageList();
			int i = 0;
			int j = reader.getNumImages(true);
			while (i < j)
			{
				try
				{
					BufferedImage bimg = reader.read(i, null);
					IIOMetadata metadata = reader.getImageMetadata(i);
					imgList.add(new StaticImage(bimg, metadata), 0);
					if (fmt.equals("jpg"))
					{
						break;
					}
				}
				catch (IIOException ex2)
				{
					//ex2.printStackTrace();
				}
				i++;
			}
			if (imgList.size() == 0)
			{
				return null;
			}
			imgList.sortImage();
			return imgList;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
//			if (StreamUtil.seekFromBeginning(stm, 0))
//			{
//					byte[] buff = stm.readAllBytes();
//					System.out.println(buff.length +" bytes left");
//			}

			return null;
		}
	}

	public static boolean saveAsFormat(StaticImage img, OutputStream output, String format)
	{
		try
		{
			ImageOutputStream ios = ImageIO.createImageOutputStream(output);
			ImageWriter writer = ImageIO.getImageWritersByFormatName(format).next();
			ImageWriteParam iwParam = writer.getDefaultWriteParam();
			writer.setOutput(ios);
			writer.write(null, new IIOImage(img.getBufferedImage(), null, img.getMetadata()), iwParam);
			writer.dispose();
			return true;
		}
		catch (IOException ex)
		{
			return false;
		}
	}

	public static boolean saveAsTiff(StaticImage img, OutputStream output)
	{
		return saveAsFormat(img, output, "tiff");
	}

	public static boolean saveAsGif(StaticImage img, OutputStream output)
	{
		return saveAsFormat(img, output, "gif");
	}

	public static boolean saveAsPng(StaticImage img, OutputStream output)
	{
		return saveAsFormat(img, output, "png");
	}

	public static boolean saveAsBmp(StaticImage img, OutputStream output)
	{
		return saveAsFormat(img, output, "bmp");
	}

	public static boolean saveAsWbmp(StaticImage img, OutputStream output)
	{
		return saveAsFormat(img, output, "wbmp");
	}

	public static boolean saveAsJpg(StaticImage img, OutputStream output, float quality) //0-1
	{
		try
		{
			ImageOutputStream ios = ImageIO.createImageOutputStream(output);
			ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
			ImageWriteParam iwParam = writer.getDefaultWriteParam();
			iwParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			iwParam.setCompressionQuality(quality);
			writer.setOutput(ios);
			writer.write(null, new IIOImage(img.getBufferedImage(), null, img.getMetadata()), iwParam);
			writer.dispose();
			return true;
		}
		catch (IOException ex)
		{
			return false;
		}
	}

	public static boolean saveAsJpgBySize(StaticImage img, OutputStream output, int minSize, int maxSize)
	{
		try
		{
			ImageOutputStream ios = ImageIO.createImageOutputStream(output);
			ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
			ImageWriteParam iwParam = writer.getDefaultWriteParam();
			IIOImage iioImg = new IIOImage(img.getBufferedImage(), null, img.getMetadata());
			iwParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			ByteArrayOutputStream baos;

			float minQuality = 0.0f;
			float maxQuality = 1.0f;
			int minQSize;
			int maxQSize;
			int thisQSize;

			baos = new ByteArrayOutputStream();
			iwParam.setCompressionQuality(minQuality);
			writer.setOutput(ImageIO.createImageOutputStream(baos));
			writer.write(null, iioImg, iwParam);
			minQSize = baos.size();
			if (minQSize > maxSize)
			{
				writer.setOutput(ios);
				writer.write(null, iioImg, iwParam);
				writer.dispose();
				return true;
			}
			baos = new ByteArrayOutputStream();
			iwParam.setCompressionQuality(maxQuality);
			writer.setOutput(ImageIO.createImageOutputStream(baos));
			writer.write(null, iioImg, iwParam);
			maxQSize = baos.size();
			if (maxQSize < minSize)
			{
				writer.setOutput(ios);
				writer.write(null, iioImg, iwParam);
				writer.dispose();
				return true;
			}
			int i = 10;
			while (i-- > 0)
			{
				baos = new ByteArrayOutputStream();
				iwParam.setCompressionQuality((minQuality + maxQuality) * 0.5f);
				writer.setOutput(ImageIO.createImageOutputStream(baos));
				writer.write(null, iioImg, iwParam);
				thisQSize = baos.size();
				if (thisQSize < minSize)
				{
					minQSize = thisQSize;
					minQuality = (minQuality + maxQuality) * 0.5f;
				}
				else if (thisQSize > maxSize)
				{
					maxQSize = thisQSize;
					maxQuality = (minQuality + maxQuality) * 0.5f;
				}
				else
				{
					writer.setOutput(ios);
					writer.write(null, iioImg, iwParam);
					writer.dispose();
					return true;
				}
			}

			iwParam.setCompressionQuality((minQuality + maxQuality) * 0.5f);
			writer.setOutput(ios);
			writer.write(null, iioImg, iwParam);
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
