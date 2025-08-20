package org.sswr.util.data;

import java.util.Comparator;

public class VariItemComparatorInv implements Comparator<Object> {

	@Override
	public int compare(Object arg0, Object arg1) {
		if ((arg0 instanceof Integer) && (arg1 instanceof Integer))
		{
			return -((Integer)arg0).compareTo((Integer)arg1);
		}
		System.out.println("VariItemComparatorInv: Unsupported type: "+arg0.getClass().toString());
		throw new UnsupportedOperationException("Unimplemented method 'compare'");
	}
	
}
