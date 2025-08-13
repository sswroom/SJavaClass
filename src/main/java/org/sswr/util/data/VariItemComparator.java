package org.sswr.util.data;

import java.util.Comparator;

public class VariItemComparator implements Comparator<Object> {

	@Override
	public int compare(Object arg0, Object arg1) {
		System.out.println("VariItemComparator: Unsupported type: "+arg0.getClass().toString());
		throw new UnsupportedOperationException("Unimplemented method 'compare'");
	}
	
}
