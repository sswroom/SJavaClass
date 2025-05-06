package org.sswr.util.crypto.hash;

public enum HashType
{
	Unknown,
	// Primary Algorithm
	Adler32,
	CRC16,
	CRC16R,
	CRC32,
	CRC32R_IEEE,
	CRC32C,
	DJB2,
	DJB2A,
	FNV1,
	FNV1A,
	MD5,
	RIPEMD128,
	RIPEMD160,
	SDBM,
	SHA1,
	EXCEL,
	SHA224,
	SHA256,
	SHA384,
	SHA512,

	// Compound Algorithm
	SHA1_SHA1
}
