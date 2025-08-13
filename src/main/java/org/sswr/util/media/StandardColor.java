package org.sswr.util.media;

import jakarta.annotation.Nonnull;

public enum StandardColor {
	Black,
	DarkBlue,
	ConsoleDarkGreen,
	DarkCyan,
	DarkRed,
	DarkMagenta,
	DarkYellow,
	Gray,
	DarkGray,
	Blue,
	Lime,
	Cyan,
	Red,
	Magenta,
	Yellow,
	White,

	IndianRed,
	LightCoral,
	Salmon,
	DarkSalmon,
	LightSalmon,
	Crimson,
	FireBrick,
	Pink,
	LightPink,
	HotPink,
	DeepPink,
	MediumVioletRed,
	PaleVioletRed,
	Coral,
	Tomato,
	OrangeRed,
	DarkOrange,
	Orange,
	Gold,
	LightYellow,
	LemonChiffon,
	LightGoldenrodYellow,
	PapayaWhip,
	Moccasin,
	PeachPuff,
	PaleGoldenrod,
	Khaki,
	DarkKhaki,
	Lavender,
	Thistle,
	Plum,
	Violet,
	Orchid,
	Fuchsia,
	MediumOrchid,
	MediumPurple,
	RebeccaPurple,
	BlueViolet,
	DarkViolet,
	DarkOrchid,
	Purple,
	Indigo,
	SlateBlue,
	DarkSlateBlue,
	MediumSlateBlue,
	GreenYellow,
	Chartreuse,
	LawnGreen,
	LimeGreen,
	PaleGreen,
	LightGreen,
	MediumSpringGreen,
	SpringGreen,
	MediumSeaGreen,
	SeaGreen,
	ForestGreen,
	Green,
	DarkGreen,
	YellowGreen,
	OliveDrab,
	Olive,
	DarkOliveGreen,
	MediumAquamarine,
	DarkSeaGreen,
	LightSeaGreen,
	Teal,
	Aqua,
	LightCyan,
	PaleTurquoise,
	Aquamarine,
	Turquoise,
	MediumTurquoise,
	DarkTurquoise,
	CadetBlue,
	SteelBlue,
	LightSteelBlue,
	PowderBlue,
	LightBlue,
	SkyBlue,
	LightSkyBlue,
	DeepSkyBlue,
	DodgerBlue,
	CornflowerBlue,
	RoyalBlue,
	MediumBlue,
	Navy,
	MidnightBlue,
	Cornsilk,
	BlanchedAlmond,
	Bisque,
	NavajoWhite,
	Wheat,
	BurlyWood,
	Tan,
	RosyBrown,
	SandyBrown,
	Goldenrod,
	DarkGoldenrod,
	Peru,
	Chocolate,
	SaddleBrown,
	Sienna,
	Brown,
	Maroon,
	Snow,
	HoneyDew,
	MintCream,
	Azure,
	AliceBlue,
	GhostWhite,
	WhiteSmoke,
	SeaShell,
	Beige,
	OldLace,
	FloralWhite,
	Ivory,
	AntiqueWhite,
	Linen,
	LavenderBlush,
	MistyRose,
	Gainsboro,
	LightGray,
	Silver,
	DimGray,
	LightSlateGray,
	SlateGray,
	DarkSlateGray;
	
	public static int standardColorGetARGB32(@Nonnull StandardColor c)
	{
		switch (c)
		{
		case Black:
			return 0xFF000000;
		case DarkBlue:
			return 0xFF00008B;
		case ConsoleDarkGreen:
			return 0xFF008B00;
		case DarkCyan:
			return 0xFF008B8B;
		case DarkRed:
			return 0xFF8B0000;
		case DarkMagenta:
			return 0xFF8B008B;
		case DarkYellow:
			return 0xFF8B8B00;
		case Gray:
			return 0xFF808080;
		case DarkGray:
			return 0xFFA9A9A9;
		case Blue:
			return 0xFF0000FF;
		case Lime:
			return 0xFF00FF00;
		case Cyan:
			return 0xFF00FFFF;
		case Red:
			return 0xFFFF0000;
		case Magenta:
			return 0xFFFF00FF;
		case Yellow:
			return 0xFFFFFF00;
		case White:
			return 0xFFFFFFFF;
		case IndianRed:
			return 0xFFCD5C5C;
		case LightCoral:
			return 0xFFF08080;
		case Salmon:
			return 0xFFFA8072;
		case DarkSalmon:
			return 0xFFE9967A;
		case LightSalmon:
			return 0xFFFFA07A;
		case Crimson:
			return 0xFFDC143C;
		case FireBrick:
			return 0xFFB22222;
		case Pink:
			return 0xFFFFC0CB;
		case LightPink:
			return 0xFFFFB6C1;
		case HotPink:
			return 0xFFFF69B4;
		case DeepPink:
			return 0xFFFF1493;
		case MediumVioletRed:
			return 0xFFC71585;
		case PaleVioletRed:
			return 0xFFDB7093;
		case Coral:
			return 0xFFFF7F50;
		case Tomato:
			return 0xFFFF6347;
		case OrangeRed:
			return 0xFFFF4500;
		case DarkOrange:
			return 0xFFFF8C00;
		case Orange:
			return 0xFFFFA500;
		case Gold:
			return 0xFFFFD700;
		case LightYellow:
			return 0xFFFFFFE0;
		case LemonChiffon:
			return 0xFFFFFACD;
		case LightGoldenrodYellow:
			return 0xFFFAFAD2;
		case PapayaWhip:
			return 0xFFFFEFD5;
		case Moccasin:
			return 0xFFFFE4B5;
		case PeachPuff:
			return 0xFFFFDAB9;
		case PaleGoldenrod:
			return 0xFFEEE8AA;
		case Khaki:
			return 0xFFF0E68C;
		case DarkKhaki:
			return 0xFFBDB76B;
		case Lavender:
			return 0xFFE6E6FA;
		case Thistle:
			return 0xFFD8BFD8;
		case Plum:
			return 0xFFDDA0DD;
		case Violet:
			return 0xFFEE82EE;
		case Orchid:
			return 0xFFDA70D6;
		case Fuchsia:
			return 0xFFFF00FF;
		case MediumOrchid:
			return 0xFFBA55D3;
		case MediumPurple:
			return 0xFF9370DB;
		case RebeccaPurple:
			return 0xFF663399;
		case BlueViolet:
			return 0xFF8A2BE2;
		case DarkViolet:
			return 0xFF9400D3;
		case DarkOrchid:
			return 0xFF9932CC;
		case Purple:
			return 0xFF800080;
		case Indigo:
			return 0xFF4B0082;
		case SlateBlue:
			return 0xFF6A5ACD;
		case DarkSlateBlue:
			return 0xFF483D8B;
		case MediumSlateBlue:
			return 0xFF7B68EE;
		case GreenYellow:
			return 0xFFADFF2F;
		case Chartreuse:
			return 0xFF7FFF00;
		case LawnGreen:
			return 0xFF7CFC00;
		case LimeGreen:
			return 0xFF32CD32;
		case PaleGreen:
			return 0xFF98FB98;
		case LightGreen:
			return 0xFF90EE90;
		case MediumSpringGreen:
			return 0xFF00FA9A;
		case SpringGreen:
			return 0xFF00FF7F;
		case MediumSeaGreen:
			return 0xFF3CB371;
		case SeaGreen:
			return 0xFF2E8B57;
		case ForestGreen:
			return 0xFF228B22;
		case Green:
			return 0xFF008000;
		case DarkGreen:
			return 0xFF006400;
		case YellowGreen:
			return 0xFF9ACD32;
		case OliveDrab:
			return 0xFF6B8E23;
		case Olive:
			return 0xFF808000;
		case DarkOliveGreen:
			return 0xFF556B2F;
		case MediumAquamarine:
			return 0xFF66CDAA;
		case DarkSeaGreen:
			return 0xFF8FBC8B;
		case LightSeaGreen:
			return 0xFF20B2AA;
		case Teal:
			return 0xFF008B8B;
		case Aqua:
			return 0xFF008080;
		case LightCyan:
			return 0xFFE0FFFF;
		case PaleTurquoise:
			return 0xFFAFEEEE;
		case Aquamarine:
			return 0xFF7FFFD4;
		case Turquoise:
			return 0xFF40E0D0;
		case MediumTurquoise:
			return 0xFF48D1CC;
		case DarkTurquoise:
			return 0xFF00CED1;
		case CadetBlue:
			return 0xFF5F9EA0;
		case SteelBlue:
			return 0xFF4682B4;
		case LightSteelBlue:
			return 0xFFB0C4DE;
		case PowderBlue:
			return 0xFFB0E0E6;
		case LightBlue:
			return 0xFFADD8E6;
		case SkyBlue:
			return 0xFF87CEEB;
		case LightSkyBlue:
			return 0xFF87CEFA;
		case DeepSkyBlue:
			return 0xFF00BFFF;
		case DodgerBlue:
			return 0xFF1E90FF;
		case CornflowerBlue:
			return 0xFF6495ED;
		case RoyalBlue:
			return 0xFF4169E1;
		case MediumBlue:
			return 0xFF0000CD;
		case Navy:
			return 0xFF000080;
		case MidnightBlue:
			return 0xFF191970;
		case Cornsilk:
			return 0xFFFFF8DC;
		case BlanchedAlmond:
			return 0xFFFFEBCD;
		case Bisque:
			return 0xFFFFE4C4;
		case NavajoWhite:
			return 0xFFFFDEAD;
		case Wheat:
			return 0xFFF5DEB3;
		case BurlyWood:
			return 0xFFDEB887;
		case Tan:
			return 0xFFD2B48C;
		case RosyBrown:
			return 0xFFBC8F8F;
		case SandyBrown:
			return 0xFFF4A460;
		case Goldenrod:
			return 0xFFDAA520;
		case DarkGoldenrod:
			return 0xFFB8860B;
		case Peru:
			return 0xFFCD853F;
		case Chocolate:
			return 0xFFD2691E;
		case SaddleBrown:
			return 0xFF8B4513;
		case Sienna:
			return 0xFFA0522D;
		case Brown:
			return 0xFFA52A2A;
		case Maroon:
			return 0xFF800000;
		case Snow:
			return 0xFFFFFAFA;
		case HoneyDew:
			return 0xFFF0FFF0;
		case MintCream:
			return 0xFFF5FFFA;
		case Azure:
			return 0xFFF0FFFF;
		case AliceBlue:
			return 0xFFF0F8FF;
		case GhostWhite:
			return 0xFFF8F8FF;
		case WhiteSmoke:
			return 0xFFF5F5F5;
		case SeaShell:
			return 0xFFFFF5EE;
		case Beige:
			return 0xFFF5F5DC;
		case OldLace:
			return 0xFFFDF5E6;
		case FloralWhite:
			return 0xFFFFFAF0;
		case Ivory:
			return 0xFFFFFFF0;
		case AntiqueWhite:
			return 0xFFFAEBD7;
		case Linen:
			return 0xFFFAF0E6;
		case LavenderBlush:
			return 0xFFFFF0F5;
		case MistyRose:
			return 0xFFFFE4E1;
		case Gainsboro:
			return 0xFFDCDCDC;
		case LightGray:
			return 0xFFD3D3D3;
		case Silver:
			return 0xFFC0C0C0;
		case DimGray:
			return 0xFF696969;
		case LightSlateGray:
			return 0xFF778899;
		case SlateGray:
			return 0xFF708090;
		case DarkSlateGray:
			return 0xFF2F4F4F;
		default:
			return 0xFF000000;
		}
	}
}
