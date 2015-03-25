package com.happyplayer.util;

/**
 * 
 * @author 
 */
public class CopyOfPinyinUtil {
	public static String[] name = { "zuo", "zun", "zui", "zuan", "zu", "zou",
			"zong", "zi", "zhuo", "zhun", "zhui", "zhuang", "zhuan", "zhuai",
			"zhua", "zhu", "zhou", "zhong", "zhi", "zheng", "zhen", "zhe",
			"zhao", "zhang", "zhan", "zhai", "zha", "zeng", "zen", "zei", "ze",
			"zao", "zang", "zan", "zai", "za", "yun", "yue", "yuan", "yu",
			"you", "yong", "yo", "ying", "yin", "yi", "ye", "yao", "yang",
			"yan", "ya", "xun", "xue", "xuan", "xu", "xiu", "xiong", "xing",
			"xin", "xie", "xiao", "xiang", "xian", "xia", "xi", "wu", "wo",
			"weng", "wen", "wei", "wang", "wan", "wai", "wa", "tuo", "tun",
			"tui", "tuan", "tu", "tou", "tong", "ting", "tie", "tiao", "tian",
			"ti", "teng", "te", "tao", "tang", "tan", "tai", "ta", "suo",
			"sun", "sui", "suan", "su", "sou", "song", "si", "shuo", "shun",
			"shui", "shuang", "shuan", "shuai", "shua", "shu", "shou", "shi",
			"sheng", "shen", "she", "shao", "shang", "shan", "shai", "sha",
			"seng", "sen", "se", "sao", "sang", "san", "sai", "sa", "ruo",
			"run", "rui", "ruan", "ru", "rou", "rong", "ri", "reng", "ren",
			"re", "rao", "rang", "ran", "qun", "que", "quan", "qu", "qiu",
			"qiong", "qing", "qin", "qie", "qiao", "qiang", "qian", "qia",
			"qi", "pu", "po", "ping", "pin", "pie", "piao", "pian", "pi",
			"peng", "pen", "pei", "pao", "pang", "pan", "pai", "pa", "ou", "o",
			"nuo", "nue", "nuan", "nv", "nu", "nong", "niu", "ning", "nin",
			"nie", "niao", "niang", "nian", "ni", "neng", "nen", "nei", "ne",
			"nao", "nang", "nan", "nai", "na", "mu", "mou", "mo", "miu",
			"ming", "min", "mie", "miao", "mian", "mi", "meng", "men", "mei",
			"me", "mao", "mang", "man", "mai", "ma", "luo", "lun", "lue",
			"luan", "lv", "lu", "lou", "long", "liu", "ling", "lin", "lie",
			"liao", "liang", "lian", "lia", "li", "leng", "lei", "le", "lao",
			"lang", "lan", "lai", "la", "kuo", "kun", "kui", "kuang", "kuan",
			"kuai", "kua", "ku", "kou", "kong", "keng", "ken", "ke", "kao",
			"kang", "kan", "kai", "ka", "jun", "jue", "juan", "ju", "jiu",
			"jiong", "jing", "jin", "jie", "jiao", "jiang", "jian", "jia",
			"ji", "huo", "hun", "hui", "huang", "huan", "huai", "hua", "hu",
			"hou", "hong", "heng", "hen", "hei", "he", "hao", "hang", "han",
			"hai", "ha", "guo", "gun", "gui", "guang", "guan", "guai", "gua",
			"gu", "gou", "gong", "geng", "gen", "gei", "ge", "gao", "gang",
			"gan", "gai", "ga", "fu", "fou", "fo", "feng", "fen", "fei",
			"fang", "fan", "fa", "er", "en", "e", "duo", "dun", "dui", "duan",
			"du", "dou", "****", "diu", "ding", "die", "diao", "dian", "di",
			"deng", "de", "dao", "dang", "dan", "dai", "da", "cuo", "cun",
			"cui", "cuan", "cu", "cou", "cong", "ci", "chuo", "chun", "chui",
			"chuang", "chuan", "chuai", "chu", "chou", "chong", "chi", "cheng",
			"chen", "che", "chao", "chang", "chan", "chai", "cha", "ceng",
			"ce", "cao", "cang", "can", "cai", "ca", "bu", "bo", "bing", "bin",
			"bie", "biao", "bian", "bi", "beng", "ben", "bei", "bao", "bang",
			"ban", "bai", "ba", "ao", "ang", "an", "ai", "a" };
	public static int[] code = { -10254, -10256, -10260, -10262, -10270,
			-10274, -10281, -10296, -10307, -10309, -10315, -10322, -10328,
			-10329, -10331, -10519, -10533, -10544, -10587, -10764, -10780,
			-10790, -10800, -10815, -10832, -10838, -11014, -11018, -11019,
			-11020, -11024, -11038, -11041, -11045, -11052, -11055, -11067,
			-11077, -11097, -11303, -11324, -11339, -11340, -11358, -11536,
			-11589, -11604, -11781, -11798, -11831, -11847, -11861, -11867,
			-12039, -12058, -12067, -12074, -12089, -12099, -12120, -12300,
			-12320, -12346, -12359, -12556, -12585, -12594, -12597, -12607,
			-12802, -12812, -12829, -12831, -12838, -12849, -12852, -12858,
			-12860, -12871, -12875, -12888, -13060, -13063, -13068, -13076,
			-13091, -13095, -13096, -13107, -13120, -13138, -13147, -13318,
			-13326, -13329, -13340, -13343, -13356, -13359, -13367, -13383,
			-13387, -13391, -13395, -13398, -13400, -13404, -13406, -13601,
			-13611, -13658, -13831, -13847, -13859, -13870, -13878, -13894,
			-13896, -13905, -13906, -13907, -13910, -13914, -13917, -14083,
			-14087, -14090, -14092, -14094, -14097, -14099, -14109, -14112,
			-14122, -14123, -14125, -14135, -14137, -14140, -14145, -14149,
			-14151, -14159, -14170, -14345, -14353, -14355, -14368, -14379,
			-14384, -14399, -14407, -14429, -14594, -14630, -14645, -14654,
			-14663, -14668, -14670, -14674, -14678, -14857, -14871, -14873,
			-14882,

			-14889, -14894, -14902, -14908, -14914, -14921, -14922, -14926,
			-14928, -14929, -14930, -14933, -14937, -14941, -15109, -15110,
			-15117, -15119, -15121, -15128, -15139, -15140, -15141, -15143,
			-15144, -15149, -15150, -15153, -15158, -15165, -15180, -15183,
			-15362, -15363, -15369, -15375, -15377, -15385, -15394, -15408,
			-15416, -15419, -15435, -15436, -15448, -15454, -15625, -15631,
			-15640, -15652, -15659, -15661, -15667, -15681, -15701, -15707,
			-15878, -15889, -15903, -15915, -15920, -15933, -15944, -15958,
			-15959, -16155, -16158, -16169, -16171, -16180, -16187, -16202,
			-16205, -16212, -16216, -16220, -16393, -16401, -16403, -16407,
			-16412, -16419, -16423, -16427, -16429, -16433, -16448, -16452,
			-16459, -16465, -16470, -16474, -16647, -16657, -16664, -16689,
			-16706, -16708, -16733, -16915, -16942, -16970, -16983, -17185,
			-17202, -17417, -17427, -17433, -17454, -17468, -17482, -17487,
			-17496, -17676, -17683, -17692, -17697, -17701, -17703, -17721,
			-17730, -17733, -17752, -17759, -17922, -17928, -17931, -17947,
			-17950, -17961, -17964, -17970, -17988, -17997, -18012, -18181,
			-18183, -18184, -18201, -18211, -18220, -18231, -18237, -18239,
			-18446, -18447, -18448, -18463, -18478, -18490, -18501, -18518,
			-18526, -18696, -18697, -18710, -18722, -18731, -18735, -18741,
			-18756, -18763, -18773, -18774, -18783, -18952, -18961, -18977,
			-18996, -19003, -19006, -19018, -19023, -19038, -19212, -19218,
			-19224, -19227, -19235, -19238, -19242, -19243, -19249, -19261,
			-19263, -19270, -19275, -19281, -19288, -19289, -19467, -19479,
			-19484, -19500, -19515, -19525, -19531, -19540, -19715, -19725,
			-19728, -19739, -19741, -19746, -19751, -19756, -19763, -19774,
			-19775, -19784, -19805, -19976, -19982, -19986, -19990, -20002,
			-20026, -20032, -20036, -20051, -20230, -20242, -20257, -20265,
			-20283, -20292, -20295, -20304, -20317, -20319 };

	public static String getPinyin(String gb2312) {
		if (null == gb2312 || "".equals(gb2312.trim())) {
			return gb2312;
		}
		char[] chars = gb2312.toCharArray();
		StringBuffer retuBuf = new StringBuffer();
		for (int i = 0, Len = chars.length; i < Len; i++) {
			retuBuf.append(getPinyin(chars[i]));

		}
		return retuBuf.toString();
	}

	public static String getPinyin(char gb2312) {
		int ascii = getCnAscii(gb2312);
		if (ascii == 0) {
			return String.valueOf(gb2312);
		} else {
			String spell = getSpellByAscii(ascii);
			if (spell == null) {
				return String.valueOf(gb2312);
			} else {
				return spell;
			}

		}
	}

	private static int getCnAscii(char cn) {

		byte[] bytes = null;
		try {
			bytes = (String.valueOf(cn)).getBytes("GB2312");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (bytes == null || bytes.length > 2 || bytes.length <= 0) {
			return 0;
		}
		if (bytes.length == 1) {
			return bytes[0];
		} else {
			int hightByte = 256 + bytes[0];
			int lowByte = 256 + bytes[1];
			int ascii = (256 * hightByte + lowByte) - 256 * 256;
			return ascii;
		}
	}

	private static String getSpellByAscii(int ascii) {
		if (ascii > 0 && ascii < 160) { // 单字符
			return String.valueOf((char) ascii);
		}
		if (ascii < -20319 || ascii > -10247) { // 不知道的字符
			return "?";
		}
		int ind;

		for (ind = 0; ind < code.length; ind++) {
			if (ascii >= code[ind]) {
				break;
			}
		}
		return name[ind];
	}
}