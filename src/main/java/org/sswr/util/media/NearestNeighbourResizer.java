package org.sswr.util.media;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import jakarta.annotation.Nonnull;

public class NearestNeighbourResizer
{
	@Nonnull
	public StaticImage resize(@Nonnull StaticImage img, @Nonnull Size2D newSize)
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
		StaticImage newImg = new StaticImage(new BufferedImage(cm, raster, isAlphaPremultiplied, null));
		EXIFData exif = img.getExif();
		if (exif != null)
		{
			newImg.setExif(exif.clone());
		}
		return newImg;
	}
}
