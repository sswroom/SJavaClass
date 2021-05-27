package org.sswr.util.media;

import java.awt.image.BufferedImage;
import javax.imageio.metadata.IIOMetadata;

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
}