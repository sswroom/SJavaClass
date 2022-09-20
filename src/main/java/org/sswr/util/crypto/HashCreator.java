package org.sswr.util.crypto;

public class HashCreator
{
	public static Hash createHash(HashType hashType)
	{
		Hash hash = null;
		switch (hashType)
		{
		case Adler32:
//			hash = new Adler32();
			break;
		case CRC16:
//			hash = new CRC16();
			break;
		case CRC16R:
//			hash = new CRC16R();
			break;
		case CRC32:
//			hash = new CRC32();
			break;
		case CRC32R_IEEE:
			hash = new CRC32R(CRC32.getPolynormialIEEE());
			break;
		case CRC32C:
			hash = new CRC32R(CRC32.getPolynormialCastagnoli());
			break;
		case DJB2:
//			hash = new DJB2();
			break;
		case DJB2A:
//			hash = new DJB2a();
			break;
		case EXCEL:
//			hash = new ExcelHash();
			break;
		case FNV1:
//			hash = new FNV1();
			break;
		case FNV1A:
//			hash = new FNV1a();
			break;
		case MD5:
//			hash = new MD5();
			break;
		case RIPEMD128:
//			hash = new RIPEMD128();
			break;
		case RIPEMD160:
//			hash = new RIPEMD160();
			break;
		case SDBM:
//			hash = new SDBM();
			break;
		case SHA1:
			hash = new SHA1();
			break;
		case SHA224:
			hash = new SHA224();
			break;
		case SHA256:
			hash = new SHA256();
			break;
		case SHA384:
			hash = new SHA384();
			break;
		case SHA512:
			hash = new SHA512();
			break;
	//	case SUPERFASTHASH:
	//		hash = new SuperFastHash();
	//		break;
		case SHA1_SHA1:
//			hash = new SHA1_SHA1();
			break;
		case Unknown:
		default:
			hash = null;
			break;
		}
		return hash;
	}
}
