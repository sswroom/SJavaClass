package org.sswr.util.io;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;

public class VersionUtil
{
	public static String getFileVersion(Class<?> cls)
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
		String ver = null;
		if (jarPath != null && jarPath.endsWith(".jar"))
		{
			String filePath = jarPath+"!/META-INF/MANIFEST.MF";
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
