package org.sswr.util.data;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class XMLAttrib extends XMLNode {
	public XMLAttrib(@Nonnull String name, @Nullable String value)
	{
		this.name = null;
		this.value = null;
		this.valueOri = null;
		if (name.length() > 0)
		{
			this.name = XmlUtil.parseStr(name);
		}
		if (value != null && value.length() > 0)
		{
			this.value = XmlUtil.parseStr(value);
			this.valueOri = value;
		}
	}

	public NodeType getNodeType()
	{
		return NodeType.Attribute;
	}

	public String toString()
	{
		String s;
		if ((s = this.value) != null)
		{
			if (this.valueOri != null)
			{
				return this.name + "=" + this.valueOri;
			}
			else
			{
				return this.name + "=" + XmlUtil.toAttrText(s);
			}
		}
		else
		{
			return this.name;
		}
	}
}
