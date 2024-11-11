package org.sswr.util.media;

import org.sswr.util.basic.Matrix3;
import org.sswr.util.basic.Point;
import org.sswr.util.basic.Vector3;

import jakarta.annotation.Nonnull;

public final class ColorPrimaries
{
	public ColorType colorType;
	public Point r;
	public Point g;
	public Point b;
	public Point w;

	public ColorPrimaries()
	{
		this.setByColorType(ColorType.PUNKNOWN);
	}

	public void setWhiteType(@Nonnull WhitePointType wpType)
	{
		this.w = getWhitePointXY(wpType);
	}

	void setWhiteTemp(double colorTemp)
	{
		this.w = getWhitePointXY(colorTemp);
	}

	void set(@Nonnull ColorPrimaries primaries)
	{
		this.colorType = primaries.colorType;
		this.r = primaries.r.clone();
		this.g = primaries.g.clone();
		this.b = primaries.b.clone();
		this.w = primaries.w.clone();
	}

	public void setByColorType(@Nonnull ColorType colorType)
	{
		switch (colorType)
		{
		case ADOBE:
			this.colorType = ColorType.ADOBE;
			this.r = new Point(0.6400, 0.3300);
			this.g = new Point(0.2100, 0.7100);
			this.b = new Point(0.1500, 0.0600);
			this.setWhiteType(WhitePointType.D65);
			break;
	
		case APPLE:
			this.colorType = ColorType.APPLE;
			this.r = new Point(0.6250, 0.3400);
			this.g = new Point(0.2800, 0.5950);
			this.b = new Point(0.1550, 0.0700);
			this.setWhiteType(WhitePointType.D65);
			break;
	
		case CIERGB:
			this.colorType = ColorType.CIERGB;
			this.r = new Point(0.7350, 0.2650);
			this.g = new Point(0.2740, 0.7170);
			this.b = new Point(0.1670, 0.0090);
			this.setWhiteType(WhitePointType.E);
			break;
	
		case COLORMATCH:
			this.colorType = ColorType.COLORMATCH;
			this.r = new Point(0.6300, 0.3400);
			this.g = new Point(0.2950, 0.6050);
			this.b = new Point(0.1500, 0.0750);
			this.setWhiteType(WhitePointType.D65);
			break;
			
		case SRGB:
		case BT709:
			this.colorType = ColorType.BT709;
			this.r = new Point(0.6400, 0.3300);
			this.g = new Point(0.3000, 0.6000);
			this.b = new Point(0.1500, 0.0600);
			this.setWhiteType(WhitePointType.D65);
			break;
	
		case BT470M: //NTSC
			this.colorType = ColorType.BT470M;
			this.r = new Point(0.6700, 0.3300);
			this.g = new Point(0.2100, 0.7100);
			this.b = new Point(0.1400, 0.0800);
			this.setWhiteType(WhitePointType.C);
			break;
	
		case BT470BG: //PAL
			this.colorType = ColorType.BT470BG;
			this.r = new Point(0.6400, 0.3300);
			this.g = new Point(0.2900, 0.6000);
			this.b = new Point(0.1500, 0.0600);
			this.setWhiteType(WhitePointType.D65);
			break;
	
		case SMPTE170M:
			this.colorType = ColorType.SMPTE170M;
			this.r = new Point(0.640, 0.340);
			this.g = new Point(0.310, 0.595);
			this.b = new Point(0.155, 0.070);
			this.setWhiteType(WhitePointType.D65);
			break;
	
		case SMPTE240M:
			this.colorType = ColorType.SMPTE240M;
			this.r = new Point(0.6300, 0.3400);
			this.g = new Point(0.3100, 0.5950);
			this.b = new Point(0.1550, 0.0700);
			this.setWhiteType(WhitePointType.D65);
			break;
	
		case GENERIC_FILM:
			this.colorType = ColorType.GENERIC_FILM;
			this.r = new Point(0.681, 0.319);
			this.g = new Point(0.243, 0.692);
			this.b = new Point(0.145, 0.049);
			this.setWhiteType(WhitePointType.C);
			break;
	
		case BT2020:
			this.colorType = ColorType.BT2020;
			this.r = new Point(0.708, 0.292);
			this.g = new Point(0.170, 0.797);
			this.b = new Point(0.131, 0.046);
			this.setWhiteType(WhitePointType.D65);
			break;
	
		case WIDE: //Wide Gamut
			this.colorType = ColorType.WIDE;
			this.r = new Point(0.7347, 0.2653);
			this.g = new Point(0.1152, 0.8264);
			this.b = new Point(0.1566, 0.0177);
			this.setWhiteType(WhitePointType.D50);
			break;
	
		case SGAMUT:
			this.colorType = ColorType.SGAMUT;
			this.r = new Point(0.73, 0.28);
			this.g = new Point(0.14, 0.855);
			this.b = new Point(0.10, -0.05);
			this.setWhiteType(WhitePointType.D65);
			break;
	
		case SGAMUTCINE:
			this.colorType = ColorType.SGAMUTCINE;
			this.r = new Point(0.76600, 0.27500);
			this.g = new Point(0.22500, 0.80000);
			this.b = new Point(0.08900, -0.08700);
			this.setWhiteType(WhitePointType.D65);
			break;
	
		case DCI_P3:
			this.colorType = ColorType.DCI_P3;
			this.r = new Point(0.68000, 0.32000);
			this.g = new Point(0.26500, 0.69000);
			this.b = new Point(0.15000, 0.06000);
			this.setWhiteType(WhitePointType.DCI);
			break;
	
		case ACESGAMUT:
			this.colorType = ColorType.ACESGAMUT;
			this.r = new Point(0.73470, 0.26530);
			this.g = new Point(0.00000, 1.00000);
			this.b = new Point(0.00010, -0.07700);
			this.w = new Point(0.32168, 0.33767);
			break;
	
		case ALEXAWIDE:
			this.colorType = ColorType.ALEXAWIDE;
			this.r = new Point(0.6840, 0.3130);
			this.g = new Point(0.2210, 0.8480);
			this.b = new Point(0.0861, -0.1020);
			this.setWhiteType(WhitePointType.D65);
			break;
	
		case VGAMUT:
			this.colorType = ColorType.VGAMUT;
			this.r = new Point(0.730, 0.280);
			this.g = new Point(0.165, 0.840);
			this.b = new Point(0.100, -0.030);
			this.setWhiteType(WhitePointType.D65);
			break;
	
		case CUSTOM:
			this.colorType = ColorType.CUSTOM;
			this.r = new Point(0.6400, 0.3300);
			this.g = new Point(0.3000, 0.6000);
			this.b = new Point(0.1500, 0.0600);
			this.setWhiteType(WhitePointType.D65);
			break;
	
		case DISPLAY:
			this.colorType = ColorType.DISPLAY;
			this.r = new Point(0.6400, 0.3300);
			this.g = new Point(0.3000, 0.6000);
			this.b = new Point(0.1500, 0.0600);
			this.setWhiteType(WhitePointType.D65);
			break;
	
		case VUNKNOWN:
			this.colorType = ColorType.VUNKNOWN;
			this.r = new Point(0.6400, 0.3300);
			this.g = new Point(0.3000, 0.6000);
			this.b = new Point(0.1500, 0.0600);
			this.setWhiteType(WhitePointType.D65);
			break;
	
		case PUNKNOWN:
			this.colorType = ColorType.PUNKNOWN;
			this.r = new Point(0.6400, 0.3300);
			this.g = new Point(0.3000, 0.6000);
			this.b = new Point(0.1500, 0.0600);
			this.setWhiteType(WhitePointType.D65);
			break;
		}
	}

	@Nonnull
	public Point getWhitexy()
	{
		return this.w;
	}

	@Nonnull
	public Matrix3 getConvMatrix()
	{
		double Xr;
		double Yr;
		double Zr;
		double Xg;
		double Yg;
		double Zg;
		double Xb;
		double Yb;
		double Zb;
		double Xw;
		double Yw;
		double Zw;
	
		Xr = this.r.getX() / this.r.getY();
		Yr = 1.0;
		Zr = (1 - this.r.getX() - this.r.getY()) / this.r.getY();
		Xg = this.g.getX() / this.g.getY();
		Yg = 1.0;
		Zg = (1 - this.g.getX() - this.g.getY()) / this.g.getY();
		Xb = this.b.getX() / this.b.getY();
		Yb = 1.0;
		Zb = (1 - this.b.getX() - this.b.getY()) / this.b.getY();
		Xw = this.w.getX() / this.w.getY();
		Yw = 1.0;
		Zw = (1 - this.w.getX() - this.w.getY()) / this.w.getY();
		Matrix3 matrixTmp = new Matrix3();
		matrixTmp.vec[0].val[0] = Xr;
		matrixTmp.vec[0].val[1] = Xg;
		matrixTmp.vec[0].val[2] = Xb;
		matrixTmp.vec[1].val[0] = Yr;
		matrixTmp.vec[1].val[1] = Yg;
		matrixTmp.vec[1].val[2] = Yb;
		matrixTmp.vec[2].val[0] = Zr;
		matrixTmp.vec[2].val[1] = Zg;
		matrixTmp.vec[2].val[2] = Zb;
		matrixTmp.inverse();
	
		Matrix3 matrix = new Matrix3();
		Vector3 vecS = matrixTmp.multiply(Xw, Yw, Zw);
		matrix.vec[0].val[0] = vecS.val[0] * Xr;
		matrix.vec[0].val[1] = vecS.val[1] * Xg;
		matrix.vec[0].val[2] = vecS.val[2] * Xb;
		matrix.vec[1].val[0] = vecS.val[0] * Yr;
		matrix.vec[1].val[1] = vecS.val[1] * Yg;
		matrix.vec[1].val[2] = vecS.val[2] * Yb;
		matrix.vec[2].val[0] = vecS.val[0] * Zr;
		matrix.vec[2].val[1] = vecS.val[1] * Zg;
		matrix.vec[2].val[2] = vecS.val[2] * Zb;
		return matrix;
	}

	public void setConvMatrix(@Nonnull Matrix3 matrix)
	{
		double Sr = matrix.vec[1].val[0];
		double Sg = matrix.vec[1].val[1];
		double Sb = matrix.vec[1].val[2];
		double X;
		double Z;
		X = matrix.vec[0].val[0] / Sr;
		Z = matrix.vec[2].val[0] / Sr;
		this.r.setY(1 / (Z + X + 1));
		this.r.setX(X * this.r.getY());
		X = matrix.vec[0].val[1] / Sg;
		Z = matrix.vec[2].val[1] / Sg;
		this.g.setY(1 / (Z + X + 1));
		this.g.setX(X * this.g.getY());
		X = matrix.vec[0].val[2] / Sb;
		Z = matrix.vec[2].val[2] / Sb;
		this.b.setY(1 / (Z + X + 1));
		this.b.setX(X * this.b.getY());
		X = this.r.getX() / this.r.getY() * Sr + this.g.getX() / this.g.getY() * Sg + this.b.getX() / this.b.getY() * Sb;
		Z = (1 - this.r.getX() - this.r.getY()) / this.r.getY() * Sr + (1 - this.g.getX() - this.g.getY()) / this.g.getY() * Sg + (1 - this.b.getX() - this.b.getY()) / this.b.getY() * Sb;
		this.w.setY(1 / (Z + X + 1));
		this.w.setX(X * this.w.getY());
	}

	public boolean equals(@Nonnull ColorPrimaries primaries)
	{
		return this.r.equals(primaries.r) && this.g.equals(primaries.g) && this.b.equals(primaries.b) && this.w.equals(primaries.w);
	}

	@Nonnull
	public static Point getWhitePointXY(@Nonnull WhitePointType wpType)
	{
		switch (wpType)
		{
		case A:
			return new Point(
				0.44757,
				0.40745);
		case B:
			return new Point(
				0.34842,
				0.35161);
		case C:
			return new Point(
				0.310063,
				0.316158);
		case D50:
			// XYZ: 0.96422 	1.00000 	0.82521
			return new Point(
				0.34566918689481363576071096962462,
				0.35849618022319972180696414679702);
		case D55:
			return new Point(
				0.33242,
				0.34743);
//		default:
		case CUSTOM:
		case D65:
			// XYZ: 0.95047 	1.00000 	1.08883
			return new Point(
				0.31272661468101207514888296647254,
				0.32902313032606192215312736485375);
		case D75:
			return new Point(
				0.29902,
				0.31485);
		case E:
			return new Point(
				1 / 3.0,
				1 / 3.0);
		case F1:
			return new Point(
				0.31310,
				0.33727);
		case F2:
			return new Point(
				0.37208,
				0.37529);
		case F3:
			return new Point(
				0.40910,
				0.39430);
		case F4:
			return new Point(
				0.44018,
				0.40329);
		case F5:
			return new Point(
				0.31379,
				0.34531);
		case F6:
			return new Point(
				0.37790,
				0.38835);
		case F7:
			return new Point(
				0.31292,
				0.32933);
		case F8:
			return new Point(
				0.34588,
				0.35875);
		case F9:
			return new Point(
				0.37417,
				0.37281);
		case F10:
			return new Point(
				0.34609,
				0.35986);
		case F11:
			return new Point(
				0.38052,
				0.37713);
		case F12:
			return new Point(
				0.43695,
				0.40441);
		case DCI:
			return new Point(
				0.31400,
				0.35100);
		}
		//D65
		return new Point(
			0.31272661468101207514888296647254,
			0.32902313032606192215312736485375);
	}

	@Nonnull
	public static Point getWhitePointXY(double colorTemp)
	{
		double currX;
		double currY;
		double t = 1000.0 / colorTemp;
		if (colorTemp <= 7000)
		{
			currX = 0.244063 + 0.09911 * t + 2.9678 * t * t - 4.6070 * t * t * t;
		}
		else
		{
			currX = 0.237040 + 0.24748 * t + 1.9018 * t * t - 2.0064 * t * t * t;
		}
		currY = 2.870 * currX - 3.000 * currX * currX - 0.275;
		return new Point(currX, currY);
	}

	@Nonnull
	public static Vector3 getWhitePointXYZ(@Nonnull WhitePointType wpType)
	{
		Point pt = getWhitePointXY(wpType);
		Vector3 xyY = new Vector3();
		xyY.set(pt.getX(), pt.getY(), 1.0);
		return xyYToXYZ(xyY);
	}

	@Nonnull
	public static Matrix3 getMatrixBradford()
	{
		Matrix3 mat = new Matrix3();
		mat.vec[0].set( 0.8951,  0.2664, -0.1614);
		mat.vec[1].set(-0.7502,  1.7135,  0.0367);
		mat.vec[2].set( 0.0389, -0.0685,  1.0296);
		return mat;
	}

	@Nonnull
	public static Matrix3 getMatrixVonKries()
	{
		Matrix3 mat = new Matrix3();
		mat.vec[0].set( 0.40024,  0.70760, -0.08081);
		mat.vec[1].set(-0.22630,  1.16532,  0.04570);
		mat.vec[2].set( 0.00000,  0.00000,  0.91822);
		return mat;
	}

	@Nonnull
	public static Vector3 xyYToXYZ(@Nonnull Vector3 xyY)
	{
		Vector3 XYZ = new Vector3();
		Double v = xyY.val[2] / xyY.val[1];
		XYZ.val[1] = xyY.val[2];
		XYZ.val[0] = xyY.val[0] * v;
		XYZ.val[2] = (1 - xyY.val[0] - xyY.val[1]) * v;
		return XYZ;
	}

	@Nonnull
	public static Vector3 XYZToxyY(@Nonnull Vector3 XYZ)
	{
		Vector3 xyY = new Vector3();
		Double sum = XYZ.val[0] + XYZ.val[1] + XYZ.val[2];
		xyY.val[2] = XYZ.val[1];
		xyY.val[0] = XYZ.val[0] / sum;
		xyY.val[1] = XYZ.val[1] / sum;
		return xyY;
	}

	@Nonnull
	public static Matrix3 getAdaptationMatrix(@Nonnull WhitePointType srcWP, @Nonnull WhitePointType destWP)
	{
		Matrix3 mat;
		Matrix3 mat2 = new Matrix3();
		Matrix3 mat3 = new Matrix3();
		Vector3 vec1;
		Vector3 vec2;
		Vector3 vec3;
	
		//color.primaries.GetConvMatrix(&mat);
		mat = getMatrixBradford();
		mat2.set(mat);
		mat3.setIdentity();
	
		vec3 = getWhitePointXYZ(srcWP);
		vec1 = mat.multiply(vec3);
		vec3 = getWhitePointXYZ(destWP);
		vec2 = mat.multiply(vec3);
		mat.inverse();
		mat3.vec[0].val[0] = vec2.val[0] / vec1.val[0];
		mat3.vec[1].val[1] = vec2.val[1] / vec1.val[1];
		mat3.vec[2].val[2] = vec2.val[2] / vec1.val[2];
		mat.multiply(mat3);
		mat.multiply(mat2);
		return mat;
	}
}
