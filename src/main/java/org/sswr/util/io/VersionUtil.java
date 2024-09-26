package org.sswr.util.io;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class VersionUtil
{
	@Nullable
	public static String getFileVersion(@Nonnull Class<?> cls)
	{
		String jarPath = null;
		try
		{
			jarPath = cls.getProtectionDomain().getCodeSource().getLocation().toURI().toString();
		}
		catch (URISyntaxException ex)
		{
			ex.printStackTrace();
			return null;
		}
		if (jarPath != null)
		{
			if (jarPath.startsWith("jar:"))
			{
				jarPath = jarPath.substring(4);
			}
			int i = jarPath.indexOf("!");
			if (i >= 0)
			{
				jarPath = jarPath.substring(0, i);
			}
		}
//		System.out.println("Jar Path: "+jarPath);
		String ver = null;
		if (jarPath != null && jarPath.endsWith(".jar"))
		{
			String filePath = jarPath+"!/META-INF/MANIFEST.MF";
//			System.out.println("File Path: "+filePath);
			try
			{
				Enumeration<URL> resources = cls.getClassLoader().getResources("META-INF/MANIFEST.MF");
				while (ver == null && resources.hasMoreElements())
				{
					try
					{
						URL url = resources.nextElement();
						if (url.getFile().equals(filePath))
						{
//							System.out.println("Manifest: "+url.toString());
							Manifest manifest = new Manifest(url.openStream());
							ver = manifest.getMainAttributes().getValue("Implementation-Version");
							if (ver != null)
								break;
						}
					}
					catch (IOException ex)
					{
						ex.printStackTrace();
					}
			
				}
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
		return ver;
	}
}
