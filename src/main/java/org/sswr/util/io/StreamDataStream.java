package org.sswr.util.io;

import jakarta.annotation.Nonnull;

public class StreamDataStream extends SeekableStream {
		private @Nonnull StreamData data;
		private long currOfst;
		private long stmDataLeng;

		public StreamDataStream(@Nonnull StreamData data)
		{
			super(data.getFullName());
			this.currOfst = 0;
			this.stmDataLeng = data.getDataSize();
			this.data = data.getPartialData(0, this.stmDataLeng);
		}

		public StreamDataStream(@Nonnull StreamData data, long ofst, long length)
		{
			super(data.getFullName());
			this.currOfst = 0;
			this.data = data.getPartialData(ofst, length);
			this.stmDataLeng = this.data.getDataSize();
		}

		public void dispose()
		{
			this.data.close();
		}

		public boolean isDown()
		{
			return false;
		}

		@Override
		public int read(@Nonnull byte[] buff, int ofst, int size) {
			int thisSize = size;
			if (this.currOfst + thisSize > this.stmDataLeng)
			{
				thisSize = (int)(this.stmDataLeng - this.currOfst);
			}
			thisSize = this.data.getRealData(this.currOfst, thisSize, buff, ofst);
			currOfst += thisSize;
			return thisSize;
		}

		@Override
		public int write(@Nonnull byte[] buff, int ofst, int size) {
			return 0;
		}

		@Override
		public int flush() {
			return 0;
		}

		@Override
		public void close() {
		}

		@Override
		public boolean recover() {
			return false;
		}		

		@Override
		public long seekFromBeginning(long position) {
			this.currOfst = position;
			if (this.currOfst < 0)
				this.currOfst = 0;
			else if (this.currOfst > this.stmDataLeng)
			{
				this.currOfst = this.stmDataLeng;
			}
			return this.currOfst;
		}

		@Override
		public long seekFromCurrent(long position) {
			long targetPos = this.currOfst + position;
			if (targetPos < 0)
			{
				return this.seekFromBeginning(0);
			}
			else
			{
				return this.seekFromBeginning(targetPos);
			}
		}

		@Override
		public long seekFromEnd(long position) {
			long targetPos = this.stmDataLeng + position;
			if (targetPos < 0)
			{
				return this.seekFromBeginning(0);
			}
			else
			{
				return this.seekFromBeginning(targetPos);
			}
		}

		@Override
		public long getPosition() {
			return currOfst;
		}

		@Override
		public long getLength() {
			return stmDataLeng;
		}


}
