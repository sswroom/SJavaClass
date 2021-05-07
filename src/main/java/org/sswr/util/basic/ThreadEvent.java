package org.sswr.util.basic;

public class ThreadEvent {
	private boolean set;
	private boolean auto;

	public ThreadEvent()
	{
		this(true);
	}
	public ThreadEvent(boolean auto)
	{
		this.auto = auto;
		this.set = false;
	}

	public void waitEvent()
	{
		if (this.auto)
		{
			synchronized(this)
			{
				if (this.set)
				{
					this.set = false;
					return;
				}
			}
			while (true)
			{
				try
				{
					this.wait();
				}
				catch (InterruptedException ex)
				{
				}
				synchronized(this)
				{
					if (this.set)
					{
						this.set = false;
						return;
					}
				}
			}
		}
		else
		{
			while (true)
			{
				try
				{
					this.wait();
				}
				catch (InterruptedException ex)
				{
				}
				synchronized(this)
				{
					if (this.set)
					{
						return;
					}
				}
			}
		}
	}

	//true if timed out
	public boolean waitEvent(int timeoutMs)
	{
		long startT = System.currentTimeMillis();
		long t;
		if (this.auto)
		{
			synchronized(this)
			{
				if (this.set)
				{
					this.set = false;
					return false;
				}
			}
			while (true)
			{
				try
				{
					t = System.currentTimeMillis();
					if (t - startT >= timeoutMs)
						return true;
					synchronized(this)
					{
						this.wait(timeoutMs - (t - startT));
					}
				}
				catch (InterruptedException ex)
				{
				}
				synchronized(this)
				{
					if (this.set)
					{
						this.set = false;
						return false;
					}
				}
			}
		}
		else
		{
			while (true)
			{
				try
				{
					t = System.currentTimeMillis();
					if (t - startT >= timeoutMs)
						return true;
					this.wait(timeoutMs - (t - startT));
				}
				catch (InterruptedException ex)
				{
				}
				synchronized(this)
				{
					if (this.set)
					{
						return false;
					}
				}
			}
		}
	}

	public void set()
	{
		this.set = true;
		synchronized(this)
		{
			this.notifyAll();
		}
	}
	
	public void clear()
	{
		this.set = false;
	}
	
	public boolean isSet()
	{
		return this.set;
	}
}
