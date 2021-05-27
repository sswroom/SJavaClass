package org.sswr.util.media;

public class Resizer
{
	public static Size2D calcOutputSize(double width, double height, double par, double targetWidth, double targetHeight, ResizeAspectRatio rar)
	{
		if (rar == ResizeAspectRatio.IGNORE_AR)
		{
			return new Size2D(targetWidth, targetHeight);
		}
		else if (rar == ResizeAspectRatio.KEEP_AR)
		{
			if (targetWidth * height > targetHeight * width)
			{
				return new Size2D(targetHeight * width / height, targetHeight);
			}
			else
			{
				return new Size2D(targetWidth, targetWidth * height / width);
			}
		}
		else
		{
			if (targetWidth * height * par > targetHeight * width)
			{
				return new Size2D(targetHeight / par * width / height, targetHeight);
			}
			else
			{
				return new Size2D(targetWidth, targetWidth * height * par / width);
			}
		}
	}
}
