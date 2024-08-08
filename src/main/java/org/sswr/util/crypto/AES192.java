package org.sswr.util.crypto;

import org.sswr.util.data.ByteTool;

public class AES192 extends BlockCipher
{
	private int encRK[];
	private int decRK[];

	public AES192(byte []key)
	{
		super(16);
		this.encRK = new int[52];
		this.decRK = new int[52];
		this.setKey(key);
	}


	public int encryptBlock(byte []inBlock, int inOfst, byte []outBlock, int outOfst)
	{
		int s0;
		int s1;
		int s2;
		int s3;
		int t0;
		int t1;
		int t2;
		int t3;
		s0 = ByteTool.readMInt32(inBlock, inOfst     ) ^ this.encRK[0];
		s1 = ByteTool.readMInt32(inBlock, inOfst +  4) ^ this.encRK[1];
		s2 = ByteTool.readMInt32(inBlock, inOfst +  8) ^ this.encRK[2];
		s3 = ByteTool.readMInt32(inBlock, inOfst + 12) ^ this.encRK[3];
		/* round 1: */
		t0 = AESBase.te0[(s0 >> 24) & 0xff] ^ AESBase.te1[(s1 >> 16) & 0xff] ^ AESBase.te2[(s2 >>  8) & 0xff] ^ AESBase.te3[s3 & 0xff] ^ this.encRK[ 4];
		t1 = AESBase.te0[(s1 >> 24) & 0xff] ^ AESBase.te1[(s2 >> 16) & 0xff] ^ AESBase.te2[(s3 >>  8) & 0xff] ^ AESBase.te3[s0 & 0xff] ^ this.encRK[ 5];
		t2 = AESBase.te0[(s2 >> 24) & 0xff] ^ AESBase.te1[(s3 >> 16) & 0xff] ^ AESBase.te2[(s0 >>  8) & 0xff] ^ AESBase.te3[s1 & 0xff] ^ this.encRK[ 6];
		t3 = AESBase.te0[(s3 >> 24) & 0xff] ^ AESBase.te1[(s0 >> 16) & 0xff] ^ AESBase.te2[(s1 >>  8) & 0xff] ^ AESBase.te3[s2 & 0xff] ^ this.encRK[ 7];
		/* round 2: */
		s0 = AESBase.te0[(t0 >> 24) & 0xff] ^ AESBase.te1[(t1 >> 16) & 0xff] ^ AESBase.te2[(t2 >>  8) & 0xff] ^ AESBase.te3[t3 & 0xff] ^ this.encRK[ 8];
		s1 = AESBase.te0[(t1 >> 24) & 0xff] ^ AESBase.te1[(t2 >> 16) & 0xff] ^ AESBase.te2[(t3 >>  8) & 0xff] ^ AESBase.te3[t0 & 0xff] ^ this.encRK[ 9];
		s2 = AESBase.te0[(t2 >> 24) & 0xff] ^ AESBase.te1[(t3 >> 16) & 0xff] ^ AESBase.te2[(t0 >>  8) & 0xff] ^ AESBase.te3[t1 & 0xff] ^ this.encRK[10];
		s3 = AESBase.te0[(t3 >> 24) & 0xff] ^ AESBase.te1[(t0 >> 16) & 0xff] ^ AESBase.te2[(t1 >>  8) & 0xff] ^ AESBase.te3[t2 & 0xff] ^ this.encRK[11];
		/* round 3: */
		t0 = AESBase.te0[(s0 >> 24) & 0xff] ^ AESBase.te1[(s1 >> 16) & 0xff] ^ AESBase.te2[(s2 >>  8) & 0xff] ^ AESBase.te3[s3 & 0xff] ^ this.encRK[12];
		t1 = AESBase.te0[(s1 >> 24) & 0xff] ^ AESBase.te1[(s2 >> 16) & 0xff] ^ AESBase.te2[(s3 >>  8) & 0xff] ^ AESBase.te3[s0 & 0xff] ^ this.encRK[13];
		t2 = AESBase.te0[(s2 >> 24) & 0xff] ^ AESBase.te1[(s3 >> 16) & 0xff] ^ AESBase.te2[(s0 >>  8) & 0xff] ^ AESBase.te3[s1 & 0xff] ^ this.encRK[14];
		t3 = AESBase.te0[(s3 >> 24) & 0xff] ^ AESBase.te1[(s0 >> 16) & 0xff] ^ AESBase.te2[(s1 >>  8) & 0xff] ^ AESBase.te3[s2 & 0xff] ^ this.encRK[15];
		/* round 4: */
		s0 = AESBase.te0[(t0 >> 24) & 0xff] ^ AESBase.te1[(t1 >> 16) & 0xff] ^ AESBase.te2[(t2 >>  8) & 0xff] ^ AESBase.te3[t3 & 0xff] ^ this.encRK[16];
		s1 = AESBase.te0[(t1 >> 24) & 0xff] ^ AESBase.te1[(t2 >> 16) & 0xff] ^ AESBase.te2[(t3 >>  8) & 0xff] ^ AESBase.te3[t0 & 0xff] ^ this.encRK[17];
		s2 = AESBase.te0[(t2 >> 24) & 0xff] ^ AESBase.te1[(t3 >> 16) & 0xff] ^ AESBase.te2[(t0 >>  8) & 0xff] ^ AESBase.te3[t1 & 0xff] ^ this.encRK[18];
		s3 = AESBase.te0[(t3 >> 24) & 0xff] ^ AESBase.te1[(t0 >> 16) & 0xff] ^ AESBase.te2[(t1 >>  8) & 0xff] ^ AESBase.te3[t2 & 0xff] ^ this.encRK[19];
		/* round 5: */
		t0 = AESBase.te0[(s0 >> 24) & 0xff] ^ AESBase.te1[(s1 >> 16) & 0xff] ^ AESBase.te2[(s2 >>  8) & 0xff] ^ AESBase.te3[s3 & 0xff] ^ this.encRK[20];
		t1 = AESBase.te0[(s1 >> 24) & 0xff] ^ AESBase.te1[(s2 >> 16) & 0xff] ^ AESBase.te2[(s3 >>  8) & 0xff] ^ AESBase.te3[s0 & 0xff] ^ this.encRK[21];
		t2 = AESBase.te0[(s2 >> 24) & 0xff] ^ AESBase.te1[(s3 >> 16) & 0xff] ^ AESBase.te2[(s0 >>  8) & 0xff] ^ AESBase.te3[s1 & 0xff] ^ this.encRK[22];
		t3 = AESBase.te0[(s3 >> 24) & 0xff] ^ AESBase.te1[(s0 >> 16) & 0xff] ^ AESBase.te2[(s1 >>  8) & 0xff] ^ AESBase.te3[s2 & 0xff] ^ this.encRK[23];
		/* round 6: */
		s0 = AESBase.te0[(t0 >> 24) & 0xff] ^ AESBase.te1[(t1 >> 16) & 0xff] ^ AESBase.te2[(t2 >>  8) & 0xff] ^ AESBase.te3[t3 & 0xff] ^ this.encRK[24];
		s1 = AESBase.te0[(t1 >> 24) & 0xff] ^ AESBase.te1[(t2 >> 16) & 0xff] ^ AESBase.te2[(t3 >>  8) & 0xff] ^ AESBase.te3[t0 & 0xff] ^ this.encRK[25];
		s2 = AESBase.te0[(t2 >> 24) & 0xff] ^ AESBase.te1[(t3 >> 16) & 0xff] ^ AESBase.te2[(t0 >>  8) & 0xff] ^ AESBase.te3[t1 & 0xff] ^ this.encRK[26];
		s3 = AESBase.te0[(t3 >> 24) & 0xff] ^ AESBase.te1[(t0 >> 16) & 0xff] ^ AESBase.te2[(t1 >>  8) & 0xff] ^ AESBase.te3[t2 & 0xff] ^ this.encRK[27];
		/* round 7: */
		t0 = AESBase.te0[(s0 >> 24) & 0xff] ^ AESBase.te1[(s1 >> 16) & 0xff] ^ AESBase.te2[(s2 >>  8) & 0xff] ^ AESBase.te3[s3 & 0xff] ^ this.encRK[28];
		t1 = AESBase.te0[(s1 >> 24) & 0xff] ^ AESBase.te1[(s2 >> 16) & 0xff] ^ AESBase.te2[(s3 >>  8) & 0xff] ^ AESBase.te3[s0 & 0xff] ^ this.encRK[29];
		t2 = AESBase.te0[(s2 >> 24) & 0xff] ^ AESBase.te1[(s3 >> 16) & 0xff] ^ AESBase.te2[(s0 >>  8) & 0xff] ^ AESBase.te3[s1 & 0xff] ^ this.encRK[30];
		t3 = AESBase.te0[(s3 >> 24) & 0xff] ^ AESBase.te1[(s0 >> 16) & 0xff] ^ AESBase.te2[(s1 >>  8) & 0xff] ^ AESBase.te3[s2 & 0xff] ^ this.encRK[31];
		/* round 8: */
		s0 = AESBase.te0[(t0 >> 24) & 0xff] ^ AESBase.te1[(t1 >> 16) & 0xff] ^ AESBase.te2[(t2 >>  8) & 0xff] ^ AESBase.te3[t3 & 0xff] ^ this.encRK[32];
		s1 = AESBase.te0[(t1 >> 24) & 0xff] ^ AESBase.te1[(t2 >> 16) & 0xff] ^ AESBase.te2[(t3 >>  8) & 0xff] ^ AESBase.te3[t0 & 0xff] ^ this.encRK[33];
		s2 = AESBase.te0[(t2 >> 24) & 0xff] ^ AESBase.te1[(t3 >> 16) & 0xff] ^ AESBase.te2[(t0 >>  8) & 0xff] ^ AESBase.te3[t1 & 0xff] ^ this.encRK[34];
		s3 = AESBase.te0[(t3 >> 24) & 0xff] ^ AESBase.te1[(t0 >> 16) & 0xff] ^ AESBase.te2[(t1 >>  8) & 0xff] ^ AESBase.te3[t2 & 0xff] ^ this.encRK[35];
		/* round 9: */
		t0 = AESBase.te0[(s0 >> 24) & 0xff] ^ AESBase.te1[(s1 >> 16) & 0xff] ^ AESBase.te2[(s2 >>  8) & 0xff] ^ AESBase.te3[s3 & 0xff] ^ this.encRK[36];
		t1 = AESBase.te0[(s1 >> 24) & 0xff] ^ AESBase.te1[(s2 >> 16) & 0xff] ^ AESBase.te2[(s3 >>  8) & 0xff] ^ AESBase.te3[s0 & 0xff] ^ this.encRK[37];
		t2 = AESBase.te0[(s2 >> 24) & 0xff] ^ AESBase.te1[(s3 >> 16) & 0xff] ^ AESBase.te2[(s0 >>  8) & 0xff] ^ AESBase.te3[s1 & 0xff] ^ this.encRK[38];
		t3 = AESBase.te0[(s3 >> 24) & 0xff] ^ AESBase.te1[(s0 >> 16) & 0xff] ^ AESBase.te2[(s1 >>  8) & 0xff] ^ AESBase.te3[s2 & 0xff] ^ this.encRK[39];
		/* round 10: */
		s0 = AESBase.te0[(t0 >> 24) & 0xff] ^ AESBase.te1[(t1 >> 16) & 0xff] ^ AESBase.te2[(t2 >>  8) & 0xff] ^ AESBase.te3[t3 & 0xff] ^ this.encRK[40];
		s1 = AESBase.te0[(t1 >> 24) & 0xff] ^ AESBase.te1[(t2 >> 16) & 0xff] ^ AESBase.te2[(t3 >>  8) & 0xff] ^ AESBase.te3[t0 & 0xff] ^ this.encRK[41];
		s2 = AESBase.te0[(t2 >> 24) & 0xff] ^ AESBase.te1[(t3 >> 16) & 0xff] ^ AESBase.te2[(t0 >>  8) & 0xff] ^ AESBase.te3[t1 & 0xff] ^ this.encRK[42];
		s3 = AESBase.te0[(t3 >> 24) & 0xff] ^ AESBase.te1[(t0 >> 16) & 0xff] ^ AESBase.te2[(t1 >>  8) & 0xff] ^ AESBase.te3[t2 & 0xff] ^ this.encRK[43];
		/* round 11: */
		t0 = AESBase.te0[(s0 >> 24) & 0xff] ^ AESBase.te1[(s1 >> 16) & 0xff] ^ AESBase.te2[(s2 >>  8) & 0xff] ^ AESBase.te3[s3 & 0xff] ^ this.encRK[44];
		t1 = AESBase.te0[(s1 >> 24) & 0xff] ^ AESBase.te1[(s2 >> 16) & 0xff] ^ AESBase.te2[(s3 >>  8) & 0xff] ^ AESBase.te3[s0 & 0xff] ^ this.encRK[45];
		t2 = AESBase.te0[(s2 >> 24) & 0xff] ^ AESBase.te1[(s3 >> 16) & 0xff] ^ AESBase.te2[(s0 >>  8) & 0xff] ^ AESBase.te3[s1 & 0xff] ^ this.encRK[46];
		t3 = AESBase.te0[(s3 >> 24) & 0xff] ^ AESBase.te1[(s0 >> 16) & 0xff] ^ AESBase.te2[(s1 >>  8) & 0xff] ^ AESBase.te3[s2 & 0xff] ^ this.encRK[47];
	
		s0 =
			(AESBase.te4[(t0 >> 24) & 0xff] & 0xff000000) ^
			(AESBase.te4[(t1 >> 16) & 0xff] & 0x00ff0000) ^
			(AESBase.te4[(t2 >>  8) & 0xff] & 0x0000ff00) ^
			(AESBase.te4[(t3      ) & 0xff] & 0x000000ff) ^
			this.encRK[48];
		ByteTool.writeMInt32(outBlock, outOfst     , s0);
		s1 =
			(AESBase.te4[(t1 >> 24) & 0xff] & 0xff000000) ^
			(AESBase.te4[(t2 >> 16) & 0xff] & 0x00ff0000) ^
			(AESBase.te4[(t3 >>  8) & 0xff] & 0x0000ff00) ^
			(AESBase.te4[(t0      ) & 0xff] & 0x000000ff) ^
			this.encRK[49];
		ByteTool.writeMInt32(outBlock, outOfst +  4, s1);
		s2 =
			(AESBase.te4[(t2 >> 24) & 0xff] & 0xff000000) ^
			(AESBase.te4[(t3 >> 16) & 0xff] & 0x00ff0000) ^
			(AESBase.te4[(t0 >>  8) & 0xff] & 0x0000ff00) ^
			(AESBase.te4[(t1      ) & 0xff] & 0x000000ff) ^
			this.encRK[50];
		ByteTool.writeMInt32(outBlock, outOfst +  8, s2);
		s3 =
			(AESBase.te4[(t3 >> 24) & 0xff] & 0xff000000) ^
			(AESBase.te4[(t0 >> 16) & 0xff] & 0x00ff0000) ^
			(AESBase.te4[(t1 >>  8) & 0xff] & 0x0000ff00) ^
			(AESBase.te4[(t2      ) & 0xff] & 0x000000ff) ^
			this.encRK[51];
		ByteTool.writeMInt32(outBlock, outOfst + 12, s3);
		return 16;
	}
	
	public int decryptBlock(byte []inBlock, int inOfst, byte []outBlock, int outOfst)
	{
		int s0;
		int s1;
		int s2;
		int s3;
		int t0;
		int t1;
		int t2;
		int t3;
	
		s0 = ByteTool.readMInt32(inBlock, inOfst     ) ^ this.decRK[0];
		s1 = ByteTool.readMInt32(inBlock, inOfst +  4) ^ this.decRK[1];
		s2 = ByteTool.readMInt32(inBlock, inOfst +  8) ^ this.decRK[2];
		s3 = ByteTool.readMInt32(inBlock, inOfst + 12) ^ this.decRK[3];
		/* round 1: */
		t0 = AESBase.td0[(s0 >> 24) & 0xff] ^ AESBase.td1[(s3 >> 16) & 0xff] ^ AESBase.td2[(s2 >>  8) & 0xff] ^ AESBase.td3[s1 & 0xff] ^ this.decRK[ 4];
		t1 = AESBase.td0[(s1 >> 24) & 0xff] ^ AESBase.td1[(s0 >> 16) & 0xff] ^ AESBase.td2[(s3 >>  8) & 0xff] ^ AESBase.td3[s2 & 0xff] ^ this.decRK[ 5];
		t2 = AESBase.td0[(s2 >> 24) & 0xff] ^ AESBase.td1[(s1 >> 16) & 0xff] ^ AESBase.td2[(s0 >>  8) & 0xff] ^ AESBase.td3[s3 & 0xff] ^ this.decRK[ 6];
		t3 = AESBase.td0[(s3 >> 24) & 0xff] ^ AESBase.td1[(s2 >> 16) & 0xff] ^ AESBase.td2[(s1 >>  8) & 0xff] ^ AESBase.td3[s0 & 0xff] ^ this.decRK[ 7];
		/* round 2: */
		s0 = AESBase.td0[(t0 >> 24) & 0xff] ^ AESBase.td1[(t3 >> 16) & 0xff] ^ AESBase.td2[(t2 >>  8) & 0xff] ^ AESBase.td3[t1 & 0xff] ^ this.decRK[ 8];
		s1 = AESBase.td0[(t1 >> 24) & 0xff] ^ AESBase.td1[(t0 >> 16) & 0xff] ^ AESBase.td2[(t3 >>  8) & 0xff] ^ AESBase.td3[t2 & 0xff] ^ this.decRK[ 9];
		s2 = AESBase.td0[(t2 >> 24) & 0xff] ^ AESBase.td1[(t1 >> 16) & 0xff] ^ AESBase.td2[(t0 >>  8) & 0xff] ^ AESBase.td3[t3 & 0xff] ^ this.decRK[10];
		s3 = AESBase.td0[(t3 >> 24) & 0xff] ^ AESBase.td1[(t2 >> 16) & 0xff] ^ AESBase.td2[(t1 >>  8) & 0xff] ^ AESBase.td3[t0 & 0xff] ^ this.decRK[11];
		/* round 3: */
		t0 = AESBase.td0[(s0 >> 24) & 0xff] ^ AESBase.td1[(s3 >> 16) & 0xff] ^ AESBase.td2[(s2 >>  8) & 0xff] ^ AESBase.td3[s1 & 0xff] ^ this.decRK[12];
		t1 = AESBase.td0[(s1 >> 24) & 0xff] ^ AESBase.td1[(s0 >> 16) & 0xff] ^ AESBase.td2[(s3 >>  8) & 0xff] ^ AESBase.td3[s2 & 0xff] ^ this.decRK[13];
		t2 = AESBase.td0[(s2 >> 24) & 0xff] ^ AESBase.td1[(s1 >> 16) & 0xff] ^ AESBase.td2[(s0 >>  8) & 0xff] ^ AESBase.td3[s3 & 0xff] ^ this.decRK[14];
		t3 = AESBase.td0[(s3 >> 24) & 0xff] ^ AESBase.td1[(s2 >> 16) & 0xff] ^ AESBase.td2[(s1 >>  8) & 0xff] ^ AESBase.td3[s0 & 0xff] ^ this.decRK[15];
		/* round 4: */
		s0 = AESBase.td0[(t0 >> 24) & 0xff] ^ AESBase.td1[(t3 >> 16) & 0xff] ^ AESBase.td2[(t2 >>  8) & 0xff] ^ AESBase.td3[t1 & 0xff] ^ this.decRK[16];
		s1 = AESBase.td0[(t1 >> 24) & 0xff] ^ AESBase.td1[(t0 >> 16) & 0xff] ^ AESBase.td2[(t3 >>  8) & 0xff] ^ AESBase.td3[t2 & 0xff] ^ this.decRK[17];
		s2 = AESBase.td0[(t2 >> 24) & 0xff] ^ AESBase.td1[(t1 >> 16) & 0xff] ^ AESBase.td2[(t0 >>  8) & 0xff] ^ AESBase.td3[t3 & 0xff] ^ this.decRK[18];
		s3 = AESBase.td0[(t3 >> 24) & 0xff] ^ AESBase.td1[(t2 >> 16) & 0xff] ^ AESBase.td2[(t1 >>  8) & 0xff] ^ AESBase.td3[t0 & 0xff] ^ this.decRK[19];
		/* round 5: */
		t0 = AESBase.td0[(s0 >> 24) & 0xff] ^ AESBase.td1[(s3 >> 16) & 0xff] ^ AESBase.td2[(s2 >>  8) & 0xff] ^ AESBase.td3[s1 & 0xff] ^ this.decRK[20];
		t1 = AESBase.td0[(s1 >> 24) & 0xff] ^ AESBase.td1[(s0 >> 16) & 0xff] ^ AESBase.td2[(s3 >>  8) & 0xff] ^ AESBase.td3[s2 & 0xff] ^ this.decRK[21];
		t2 = AESBase.td0[(s2 >> 24) & 0xff] ^ AESBase.td1[(s1 >> 16) & 0xff] ^ AESBase.td2[(s0 >>  8) & 0xff] ^ AESBase.td3[s3 & 0xff] ^ this.decRK[22];
		t3 = AESBase.td0[(s3 >> 24) & 0xff] ^ AESBase.td1[(s2 >> 16) & 0xff] ^ AESBase.td2[(s1 >>  8) & 0xff] ^ AESBase.td3[s0 & 0xff] ^ this.decRK[23];
		/* round 6: */
		s0 = AESBase.td0[(t0 >> 24) & 0xff] ^ AESBase.td1[(t3 >> 16) & 0xff] ^ AESBase.td2[(t2 >>  8) & 0xff] ^ AESBase.td3[t1 & 0xff] ^ this.decRK[24];
		s1 = AESBase.td0[(t1 >> 24) & 0xff] ^ AESBase.td1[(t0 >> 16) & 0xff] ^ AESBase.td2[(t3 >>  8) & 0xff] ^ AESBase.td3[t2 & 0xff] ^ this.decRK[25];
		s2 = AESBase.td0[(t2 >> 24) & 0xff] ^ AESBase.td1[(t1 >> 16) & 0xff] ^ AESBase.td2[(t0 >>  8) & 0xff] ^ AESBase.td3[t3 & 0xff] ^ this.decRK[26];
		s3 = AESBase.td0[(t3 >> 24) & 0xff] ^ AESBase.td1[(t2 >> 16) & 0xff] ^ AESBase.td2[(t1 >>  8) & 0xff] ^ AESBase.td3[t0 & 0xff] ^ this.decRK[27];
		/* round 7: */
		t0 = AESBase.td0[(s0 >> 24) & 0xff] ^ AESBase.td1[(s3 >> 16) & 0xff] ^ AESBase.td2[(s2 >>  8) & 0xff] ^ AESBase.td3[s1 & 0xff] ^ this.decRK[28];
		t1 = AESBase.td0[(s1 >> 24) & 0xff] ^ AESBase.td1[(s0 >> 16) & 0xff] ^ AESBase.td2[(s3 >>  8) & 0xff] ^ AESBase.td3[s2 & 0xff] ^ this.decRK[29];
		t2 = AESBase.td0[(s2 >> 24) & 0xff] ^ AESBase.td1[(s1 >> 16) & 0xff] ^ AESBase.td2[(s0 >>  8) & 0xff] ^ AESBase.td3[s3 & 0xff] ^ this.decRK[30];
		t3 = AESBase.td0[(s3 >> 24) & 0xff] ^ AESBase.td1[(s2 >> 16) & 0xff] ^ AESBase.td2[(s1 >>  8) & 0xff] ^ AESBase.td3[s0 & 0xff] ^ this.decRK[31];
		/* round 8: */
		s0 = AESBase.td0[(t0 >> 24) & 0xff] ^ AESBase.td1[(t3 >> 16) & 0xff] ^ AESBase.td2[(t2 >>  8) & 0xff] ^ AESBase.td3[t1 & 0xff] ^ this.decRK[32];
		s1 = AESBase.td0[(t1 >> 24) & 0xff] ^ AESBase.td1[(t0 >> 16) & 0xff] ^ AESBase.td2[(t3 >>  8) & 0xff] ^ AESBase.td3[t2 & 0xff] ^ this.decRK[33];
		s2 = AESBase.td0[(t2 >> 24) & 0xff] ^ AESBase.td1[(t1 >> 16) & 0xff] ^ AESBase.td2[(t0 >>  8) & 0xff] ^ AESBase.td3[t3 & 0xff] ^ this.decRK[34];
		s3 = AESBase.td0[(t3 >> 24) & 0xff] ^ AESBase.td1[(t2 >> 16) & 0xff] ^ AESBase.td2[(t1 >>  8) & 0xff] ^ AESBase.td3[t0 & 0xff] ^ this.decRK[35];
		/* round 9: */
		t0 = AESBase.td0[(s0 >> 24) & 0xff] ^ AESBase.td1[(s3 >> 16) & 0xff] ^ AESBase.td2[(s2 >>  8) & 0xff] ^ AESBase.td3[s1 & 0xff] ^ this.decRK[36];
		t1 = AESBase.td0[(s1 >> 24) & 0xff] ^ AESBase.td1[(s0 >> 16) & 0xff] ^ AESBase.td2[(s3 >>  8) & 0xff] ^ AESBase.td3[s2 & 0xff] ^ this.decRK[37];
		t2 = AESBase.td0[(s2 >> 24) & 0xff] ^ AESBase.td1[(s1 >> 16) & 0xff] ^ AESBase.td2[(s0 >>  8) & 0xff] ^ AESBase.td3[s3 & 0xff] ^ this.decRK[38];
		t3 = AESBase.td0[(s3 >> 24) & 0xff] ^ AESBase.td1[(s2 >> 16) & 0xff] ^ AESBase.td2[(s1 >>  8) & 0xff] ^ AESBase.td3[s0 & 0xff] ^ this.decRK[39];
		/* round 10: */
		s0 = AESBase.td0[(t0 >> 24) & 0xff] ^ AESBase.td1[(t3 >> 16) & 0xff] ^ AESBase.td2[(t2 >>  8) & 0xff] ^ AESBase.td3[t1 & 0xff] ^ this.decRK[40];
		s1 = AESBase.td0[(t1 >> 24) & 0xff] ^ AESBase.td1[(t0 >> 16) & 0xff] ^ AESBase.td2[(t3 >>  8) & 0xff] ^ AESBase.td3[t2 & 0xff] ^ this.decRK[41];
		s2 = AESBase.td0[(t2 >> 24) & 0xff] ^ AESBase.td1[(t1 >> 16) & 0xff] ^ AESBase.td2[(t0 >>  8) & 0xff] ^ AESBase.td3[t3 & 0xff] ^ this.decRK[42];
		s3 = AESBase.td0[(t3 >> 24) & 0xff] ^ AESBase.td1[(t2 >> 16) & 0xff] ^ AESBase.td2[(t1 >>  8) & 0xff] ^ AESBase.td3[t0 & 0xff] ^ this.decRK[43];
		/* round 11: */
		t0 = AESBase.td0[(s0 >> 24) & 0xff] ^ AESBase.td1[(s3 >> 16) & 0xff] ^ AESBase.td2[(s2 >>  8) & 0xff] ^ AESBase.td3[s1 & 0xff] ^ this.decRK[44];
		t1 = AESBase.td0[(s1 >> 24) & 0xff] ^ AESBase.td1[(s0 >> 16) & 0xff] ^ AESBase.td2[(s3 >>  8) & 0xff] ^ AESBase.td3[s2 & 0xff] ^ this.decRK[45];
		t2 = AESBase.td0[(s2 >> 24) & 0xff] ^ AESBase.td1[(s1 >> 16) & 0xff] ^ AESBase.td2[(s0 >>  8) & 0xff] ^ AESBase.td3[s3 & 0xff] ^ this.decRK[46];
		t3 = AESBase.td0[(s3 >> 24) & 0xff] ^ AESBase.td1[(s2 >> 16) & 0xff] ^ AESBase.td2[(s1 >>  8) & 0xff] ^ AESBase.td3[s0 & 0xff] ^ this.decRK[47];
	
		s0 =
			(AESBase.td4[(t0 >> 24) & 0xff] & 0xff000000) ^
			(AESBase.td4[(t3 >> 16) & 0xff] & 0x00ff0000) ^
			(AESBase.td4[(t2 >>  8) & 0xff] & 0x0000ff00) ^
			(AESBase.td4[(t1      ) & 0xff] & 0x000000ff) ^
			this.decRK[48];
		ByteTool.writeMInt32(outBlock, outOfst     , s0);
		s1 =
			(AESBase.td4[(t1 >> 24) & 0xff] & 0xff000000) ^
			(AESBase.td4[(t0 >> 16) & 0xff] & 0x00ff0000) ^
			(AESBase.td4[(t3 >>  8) & 0xff] & 0x0000ff00) ^
			(AESBase.td4[(t2      ) & 0xff] & 0x000000ff) ^
			this.decRK[49];
		ByteTool.writeMInt32(outBlock, outOfst +  4, s1);
		s2 =
			(AESBase.td4[(t2 >> 24) & 0xff] & 0xff000000) ^
			(AESBase.td4[(t1 >> 16) & 0xff] & 0x00ff0000) ^
			(AESBase.td4[(t0 >>  8) & 0xff] & 0x0000ff00) ^
			(AESBase.td4[(t3      ) & 0xff] & 0x000000ff) ^
			this.decRK[50];
		ByteTool.writeMInt32(outBlock, outOfst +  8, s2);
		s3 =
			(AESBase.td4[(t3 >> 24) & 0xff] & 0xff000000) ^
			(AESBase.td4[(t2 >> 16) & 0xff] & 0x00ff0000) ^
			(AESBase.td4[(t1 >>  8) & 0xff] & 0x0000ff00) ^
			(AESBase.td4[(t0      ) & 0xff] & 0x000000ff) ^
			this.decRK[51];
		ByteTool.writeMInt32(outBlock, outOfst + 12, s3);
		return 16;
	}
	
	public void setKey(byte []key)
	{
		int i;
		int j;
		int temp;
	
		this.encRK[0] = ByteTool.readMInt32(key,  0);
		this.encRK[1] = ByteTool.readMInt32(key,  4);
		this.encRK[2] = ByteTool.readMInt32(key,  8);
		this.encRK[3] = ByteTool.readMInt32(key, 12);
		this.encRK[4] = ByteTool.readMInt32(key, 16);
		this.encRK[5] = ByteTool.readMInt32(key, 20);
		i = 0;
		while (true)
		{
			temp  = this.encRK[i * 6 + 5];
			this.encRK[i * 6 +  6] = this.encRK[i * 6 + 0] ^
				(AESBase.te4[(temp >> 16) & 0xff] & 0xff000000) ^
				(AESBase.te4[(temp >>  8) & 0xff] & 0x00ff0000) ^
				(AESBase.te4[(temp      ) & 0xff] & 0x0000ff00) ^
				(AESBase.te4[(temp >> 24) & 0xff] & 0x000000ff) ^
				AESBase.rcon[i];
			this.encRK[i * 6 +  7] = this.encRK[i * 6 + 1] ^ this.encRK[i * 6 +  6];
			this.encRK[i * 6 +  8] = this.encRK[i * 6 + 2] ^ this.encRK[i * 6 +  7];
			this.encRK[i * 6 +  9] = this.encRK[i * 6 + 3] ^ this.encRK[i * 6 +  8];
			if (i >= 7)
			{
				break;
			}
			this.encRK[i * 6 + 10] = this.encRK[i * 6 + 4] ^ this.encRK[i * 6 +  9];
			this.encRK[i * 6 + 11] = this.encRK[i * 6 + 5] ^ this.encRK[i * 6 + 10];
			i++;
		}
	
		i = 0;
		j = 48;
		while (i < j)
		{
			this.decRK[i + 0] = this.encRK[j + 0];
			this.decRK[i + 1] = this.encRK[j + 1];
			this.decRK[i + 2] = this.encRK[j + 2];
			this.decRK[i + 3] = this.encRK[j + 3];
			this.decRK[j + 0] = this.encRK[i + 0];
			this.decRK[j + 1] = this.encRK[i + 1];
			this.decRK[j + 2] = this.encRK[i + 2];
			this.decRK[j + 3] = this.encRK[i + 3];
			i += 4;
			j -= 4;
		}
		this.decRK[i + 0] = this.encRK[i + 0];
		this.decRK[i + 1] = this.encRK[i + 1];
		this.decRK[i + 2] = this.encRK[i + 2];
		this.decRK[i + 3] = this.encRK[i + 3];
	
		i = 1;
		while (i < 12)
		{
			this.decRK[i * 4 + 0] =
				AESBase.td0[AESBase.te4[(this.decRK[i * 4 + 0] >> 24) & 0xff] & 0xff] ^
				AESBase.td1[AESBase.te4[(this.decRK[i * 4 + 0] >> 16) & 0xff] & 0xff] ^
				AESBase.td2[AESBase.te4[(this.decRK[i * 4 + 0] >>  8) & 0xff] & 0xff] ^
				AESBase.td3[AESBase.te4[(this.decRK[i * 4 + 0]      ) & 0xff] & 0xff];
			this.decRK[i * 4 + 1] =
				AESBase.td0[AESBase.te4[(this.decRK[i * 4 + 1] >> 24) & 0xff] & 0xff] ^
				AESBase.td1[AESBase.te4[(this.decRK[i * 4 + 1] >> 16) & 0xff] & 0xff] ^
				AESBase.td2[AESBase.te4[(this.decRK[i * 4 + 1] >>  8) & 0xff] & 0xff] ^
				AESBase.td3[AESBase.te4[(this.decRK[i * 4 + 1]      ) & 0xff] & 0xff];
			this.decRK[i * 4 + 2] =
				AESBase.td0[AESBase.te4[(this.decRK[i * 4 + 2] >> 24) & 0xff] & 0xff] ^
				AESBase.td1[AESBase.te4[(this.decRK[i * 4 + 2] >> 16) & 0xff] & 0xff] ^
				AESBase.td2[AESBase.te4[(this.decRK[i * 4 + 2] >>  8) & 0xff] & 0xff] ^
				AESBase.td3[AESBase.te4[(this.decRK[i * 4 + 2]      ) & 0xff] & 0xff];
			this.decRK[i * 4 + 3] =
				AESBase.td0[AESBase.te4[(this.decRK[i * 4 + 3] >> 24) & 0xff] & 0xff] ^
				AESBase.td1[AESBase.te4[(this.decRK[i * 4 + 3] >> 16) & 0xff] & 0xff] ^
				AESBase.td2[AESBase.te4[(this.decRK[i * 4 + 3] >>  8) & 0xff] & 0xff] ^
				AESBase.td3[AESBase.te4[(this.decRK[i * 4 + 3]      ) & 0xff] & 0xff];
			i++;
		}
	}
}
