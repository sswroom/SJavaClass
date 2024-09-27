package org.sswr.util.media;

import jakarta.annotation.Nullable;

public interface PrintDocument
{
	public void setDocName(@Nullable String docName);
	public void waitForEnd();
}
