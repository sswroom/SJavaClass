package org.sswr.util.net;

import java.net.InetAddress;

public class DNSRequestAnswer {
	public String name;
	public int recType;
	public int recClass;
	public int ttl;
	public String rd;
	public InetAddress addr;
	public int priority;
}
