package org.sswr.util.data;

import java.util.ArrayList;

import jakarta.annotation.Nullable;

public abstract class XMLNode {
	public static enum NodeType
	{
		Unknown,
		Element,
		Text,
		Document,
		Comment,
		Attribute,
		CData,
		ElementEnd,
		DocType
	}

	public @Nullable String name;
	public @Nullable String value;
	public @Nullable String valueOri;
	
	protected @Nullable ArrayList<XMLNode> childArr;
	protected @Nullable ArrayList<XMLAttrib> attribArr;

	public XMLNode()
	{
		this.name = null;
		this.value = null;
		this.valueOri = null;
		this.attribArr = null;
		this.childArr = null;
	}

	public abstract NodeType getNodeType();
}
