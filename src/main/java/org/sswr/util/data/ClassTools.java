package org.sswr.util.data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.TypeVariable;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ClassTools
{

	private static void appendTypeName(@Nonnull Class<?> t, @Nonnull StringBuilder sb)
	{
		sb.append(t.getSimpleName());
		TypeVariable<?>[] tparams = t.getTypeParameters();
		int i = 0;
		int j = tparams.length;
		if (j > 0)
		{
			sb.append("<");
			while (i < j)
			{
				sb.append(tparams[i].getName());
				i++;
			}
			sb.append(">");
		}
	}

	private static void appendModifier(int modifier, @Nonnull StringBuilder sb)
	{
		if ((modifier & Modifier.PUBLIC) != 0)
		{
			sb.append("public ");
		}
		else if ((modifier & Modifier.PRIVATE) != 0)
		{
			sb.append("private ");
		}
		else if ((modifier & Modifier.PROTECTED) != 0)
		{
			sb.append("protected ");
		}
		if ((modifier & Modifier.STATIC) != 0)
		{
			sb.append("static ");
		}
		if ((modifier & Modifier.FINAL) != 0)
		{
			sb.append("final ");
		}
		if ((modifier & Modifier.SYNCHRONIZED) != 0)
		{
			sb.append("synchronized ");
		}
		if ((modifier & Modifier.ABSTRACT) != 0)
		{
			sb.append("abstract ");
		}
	}

	private static void appendAnnotation(@Nonnull Annotation ann, int level, @Nonnull StringBuilder sb)
	{
		appendLevel(level, sb);
		sb.append("@?\r\n");
	}

	private static void appendAnnotations(@Nullable Annotation[] anns, int level, @Nonnull StringBuilder sb)
	{
		if (anns == null)
		{
			return;
		}
		int i = 0;
		int j = anns.length;
		while (i < j)
		{
			appendAnnotation(anns[i], level, sb);
			i++;
		}
	}

	private static void appendParamAnns(@Nullable Annotation[] anns, @Nonnull StringBuilder sb)
	{
		if (anns != null)
		{
			int i = 0;
			int j = anns.length;
			while (i < j)
			{
				sb.append("@? ");
				i++;
			}
		}
	}

	private static void appendLevel(int level, @Nonnull StringBuilder sb)
	{
		while (level-- > 0)
		{
			sb.append('\t');
		}
	}

	private static void appendParameters(@Nullable Parameter[] params, @Nonnull StringBuilder sb)
	{
		sb.append("(");
		if (params != null)
		{
			int i = 0;
			int j = params.length;
			while (i < j)
			{
				if (i > 0)
				{
					sb.append(", ");
				}
				appendParamAnns(params[i].getDeclaredAnnotations(), sb);
				appendTypeName(params[i].getType(), sb);
				sb.append(" ");
				sb.append(params[i].getName());
				i++;
			}
		}
		sb.append(")");
	}

	public static void appendClass(@Nonnull Class<?> cls, int level, @Nonnull StringBuilder sb)
	{
		int i;
		int j;
		appendAnnotations(cls.getDeclaredAnnotations(), level, sb);
		appendModifier(cls.getModifiers(), sb);
		if (cls.isEnum())
		{
			sb.append("enum ");
		}
		else if ((cls.getModifiers() & Modifier.INTERFACE) != 0)
		{
			sb.append("interface ");
		}
		else
		{
			sb.append("class ");
		}
		appendTypeName(cls, sb);
		Class<?> scls = cls.getSuperclass();
		if (scls != null && !scls.equals(Object.class))
		{
			sb.append(" extends ");
			appendTypeName(scls, sb);
		}
		Class<?>[] interf = cls.getInterfaces();
		if (interf != null && interf.length > 0)
		{
			sb.append(" implements ");
			i = 0;
			j = interf.length;
			while (i < j)
			{
				if (i > 0)
				{
					sb.append(", ");
				}
				appendTypeName(interf[i], sb);
				i++;
			}
		}
		sb.append("\r\n");
		appendLevel(level, sb);
		sb.append("{\r\n");
		Class<?>[] innerClasses = cls.getDeclaredClasses();
		if (innerClasses != null)
		{
			i = 0;
			j = innerClasses.length;
			while (i < j)
			{
				appendClass(innerClasses[i], level + 1, sb);
				sb.append("\r\n");
				sb.append("\r\n");
				i++;
			}
		}

		Field[] fields = cls.getDeclaredFields();
		if (fields != null)
		{
			i = 0;
			j = fields.length;
			while (i < j)
			{
				appendAnnotations(fields[i].getDeclaredAnnotations(), level + 1, sb);
				appendLevel(level + 1, sb);
				appendModifier(fields[i].getModifiers(), sb);
				appendTypeName(fields[i].getType(), sb);
				sb.append(' ');
				sb.append(fields[i].getName());
				sb.append(";\r\n");
				i++;
			}
		}

		Constructor<?>[] constrs = cls.getDeclaredConstructors();
		i = 0;
		j = constrs.length;
		while (i < j)
		{
			appendAnnotations(constrs[i].getDeclaredAnnotations(), level + 1, sb);
			appendLevel(level + 1, sb);
			appendModifier(constrs[i].getModifiers(), sb);
			sb.append(cls.getSimpleName());
			appendParameters(constrs[i].getParameters(), sb);
			sb.append(" { }\r\n");
			i++;
		}

		Method[] meths = cls.getDeclaredMethods();
		i = 0;
		j = meths.length;
		while (i < j)
		{
			appendAnnotations(meths[i].getDeclaredAnnotations(), level + 1, sb);
			appendLevel(level + 1, sb);
			appendModifier(meths[i].getModifiers(), sb);
			appendTypeName(meths[i].getReturnType(), sb);
			sb.append(' ');
			sb.append(meths[i].getName());
			appendParameters(meths[i].getParameters(), sb);
			sb.append(" { }\r\n");
			i++;
		}
		sb.append("}");
	}

	public static @Nonnull String toString(@Nonnull Class<?> cls)
	{
		StringBuilder sb = new StringBuilder();
		appendClass(cls, 0, sb);
		return sb.toString();
	}	
}
