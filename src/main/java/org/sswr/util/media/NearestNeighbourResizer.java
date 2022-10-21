package org.sswr.util.media;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

public class NearestNeighbourResizer
{
	public StaticImage resize(StaticImage img, Size2D newSize)
	{
		BufferedImage bimg = img.getBufferedImage();
		ColorModel cm = bimg.getColorModel();
		WritableRaster raster = cm.createCompatibleWritableRaster((int)newSize.getWidth(), (int)newSize.getHeight());
		WritableRaster srcRaster = bimg.getRaster();
		int srcWidth = bimg.getWidth();
		int srcHeight = bimg.getHeight();
		int destWidth = raster.getWidth();
		int destHeight = raster.getHeight();
		int i;
		int j;
		int srcX;
		int srcY;
		int samples[] = new int[raster.getNumBands()];
		i = 0;
		while (i < destHeight)
		{
			srcY = i * srcHeight / destHeight;
			j = 0;
			while (j < destWidth)
			{
				srcX = j * srcWidth / destWidth;
				srcRaster.getPixel(srcX, srcY, samples);
				raster.setPixel(j, i, samples);
				j++;
			}
			i++;
		}
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		StaticImage newImg = new StaticImage(new BufferedImage(cm, raster, isAlphaPremultiplied, null), img.getMetadata());
		return newImg;
	}
}
