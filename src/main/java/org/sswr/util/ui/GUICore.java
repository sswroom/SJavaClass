package org.sswr.util.ui;

import jakarta.annotation.Nullable;

public abstract class GUICore
{
	@Nullable
	public static GUICore create()
	{
		try
		{
			Class<?> cls = Class.forName("org.sswr.swing.SwingGUICore");
			if (GUICore.class.isAssignableFrom(cls))
			{
				return (GUICore) cls.getDeclaredConstructor().newInstance();
			}
		}
		catch (ClassNotFoundException e)
		{
		}
		catch (ReflectiveOperationException e)
		{
		}
		return null;
	}
}
