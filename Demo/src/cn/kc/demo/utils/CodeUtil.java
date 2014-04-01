package cn.kc.demo.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Vector;

public class CodeUtil {

	/**
	 * 数据读取共用类
	 * 
	 */
	public static int getUnsignedByte (byte data){      //将data字节型数据转换为0~255 (0xFF 即BYTE)。
		return data&0x0FF;
	}
	public static int getUnsignedByte (short data){      //将data字节型数据转换为0~65535 (0xFFFF 即 WORD)。
		return data&0x0FFFF;
	}
	public static long getUnsignedInt (int data){     //将int数据转换为0~4294967295 (0xFFFFFFFF即DWORD)。
		return data&0x0FFFFFFFFl;
	}
	private DataInputStream data;

	public CodeUtil(byte abyte0[]) {
		ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(abyte0);
		data = new DataInputStream(bytearrayinputstream);
	}
	
	public final static byte[] short2bytes(short s, boolean asc)
	{
	    byte[] buf = new byte[2];
	    if (asc){
	        for (int i = buf.length - 1; i >= 0; i--){
	            buf[i] = (byte) (s & 0x00ff);
	            s >>= 8;
	        }
	    }
	    else{
	        for (int i = 0; i < buf.length; i++){
	            buf[i] = (byte) (s & 0x00ff);
	            s >>= 8;
	        }
	    }
	    return buf;
	}
	 
	public final static byte[] int2bytes(int s, boolean asc)
	{
	    byte[] buf = new byte[4];
	    if (asc){
	    	for (int i = buf.length - 1; i >= 0; i--){
	    		buf[i] = (byte) (s & 0x000000ff);
	    		s >>= 8;
	    	}
	    }
	    else{
	    	for (int i = 0; i < buf.length; i++){
			    buf[i] = (byte) (s & 0x000000ff);
			    s >>= 8;
	    	}
	    }
	    return buf;
	}
	 
	public final static byte[] long2bytes(long s, boolean asc)
	{
	    byte[] buf = new byte[8];
	    if (asc){
	    	for (int i = buf.length - 1; i >= 0; i--){
	    		buf[i] = (byte) (s & 0x00000000000000ff);
	    		s >>= 8;
	    	}
	    }
	    else{
	    	for (int i = 0; i < buf.length; i++){
	    		buf[i] = (byte) (s & 0x00000000000000ff);
	    		s >>= 8;
	    	}
	    }
	    return buf;
	}
	/**
	 * 
	 * delphi word 2个字节转换成一个int
	 * 
	 */
	public static int word2int(byte b0, byte b1) {
		return (int) (((b1 & 0xff) << 8) | ((b0 & 0xff) << 0));
	}

	/**
	 * 
	 * 四个字节转换成一个int
	 * 
	 */
	public static int makeInt(byte b3, byte b2, byte b1, byte b0) {
		return (int) ((((b3 & 0xff) << 24) | ((b2 & 0xff) << 16)
				| ((b1 & 0xff) << 8) | ((b0 & 0xff) << 0)));
	}

	/**
	 * 
	 * 四个字节转换成一个int
	 * Big-Bian
	 */
	public static int makeInt(byte[] b, int j) {
		return makeInt(b[j], b[j + 1], b[j + 2], b[j + 3]);
	}
	/**
	 * 
	 * 2个字节转换成一个short
	 * 
	 */
	public static short makeShort( byte b1, byte b0) {
		return (short) ( ((b1 & 0xff) << 8) | ((b0 & 0xff) << 0));
	}

	/**
	 * 
	 * 2个字节转换成一个short
	 * 
	 */
	public static short makeShort(byte[] b, int j) {
		return makeShort(b[j], b[j + 1]);
	}
	/**
	 * 将字节数组转换为字符串
	 * 
	 * @param b
	 *            需要转换的字节数组
	 * @return 转换后的字符串
	 * 
	 */
	public static String byte2UTF8String(byte[] b, int start, int count) {
		try {
			byte[] bytes = new byte[count];
			System.arraycopy(b, start, bytes, 0, count);
			return new String(bytes, "UTF-8");
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 将字节数组转换为字符串
	 * 
	 * @param b
	 *            需要转换的字节数组
	 * @return 转换后的字符串
	 * 
	 */
	public static String byte2String(byte[] b, int start, int count) {
		try {
			byte[] bytes = new byte[count];
			System.arraycopy(b, start, bytes, 0, count);
			return UTF2Uni(bytes, bytes.length);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 字节数组到float的转换
	 * 
	 * @param b
	 * @return
	 * @throws DataTranslateException
	 */
	public static float byteToFloat(byte[] b) {
		int l;
		l = b[0];
		l &= 0xff;
		l |= ((long) b[1] << 8);
		l &= 0xffff;
		l |= ((long) b[2] << 16);
		l &= 0xffffff;
		l |= ((long) b[3] << 24);
		l &= 0xffffffffl;
		return Float.intBitsToFloat(l);
	}

	/**
	 * 将UTF-8字节数据转化为Unicode字符串
	 * 
	 * @param utf_data
	 *            byte[] - UTF-8编码字节数组
	 * @param len
	 *            int - 字节数组长度
	 * @return String - 变换后的Unicode编码字符串
	 * 
	 */
	public static String UTF2Uni(byte[] utf_data, int len) {
		StringBuffer unis = new StringBuffer();
		char unic = 0;
		int ptr = 0;
		int cntBits = 0;
		for (; ptr < len;) {
			cntBits = getCntBits(utf_data[ptr]);
			if (cntBits == -1) {
				++ptr;
				continue;
			} else if (cntBits == 0) {
				unic = UTFC2UniC(utf_data, ptr, cntBits);
				++ptr;
			} else {
				unic = UTFC2UniC(utf_data, ptr, cntBits);
				ptr += cntBits;
			}
			unis.append(unic);
		}
		return unis.toString();
	}

	/**
	 * 将指定的UTF-8字节组合成一个Unicode编码字符
	 * 
	 * @param utf
	 *            byte[] - UTF-8字节数组
	 * @param sptr
	 *            int - 编码字节起始位置
	 * @param cntBits
	 *            int - 编码字节数
	 * 
	 * @return char - 变换后的Unicode字符
	 */
	public static char UTFC2UniC(byte[] utf, int sptr, int cntBits) {
		/*
		 * Unicode <-> UTF-8 U-00000000 - U-0000007F: 0xxxxxxx U-00000080 -
		 * U-000007FF: 110xxxxx 10xxxxxx U-00000800 - U-0000FFFF: 1110xxxx
		 * 10xxxxxx 10xxxxxx U-00010000 - U-001FFFFF: 11110xxx 10xxxxxx 10xxxxxx
		 * 10xxxxxx U-00200000 - U-03FFFFFF: 111110xx 10xxxxxx 10xxxxxx 10xxxxxx
		 * 10xxxxxx U-04000000 - U-7FFFFFFF: 1111110x 10xxxxxx 10xxxxxx 10xxxxxx
		 * 10xxxxxx 10xxxxxx
		 */
		int uniC = 0; // represent the unicode char
		byte firstByte = utf[sptr];
		int ptr = 0; // pointer 0 ~ 15
		// resolve single byte UTF-8 encoding char
		if (cntBits == 0)
			return (char) firstByte;
		// resolve the first byte
		firstByte &= (1 << (7 - cntBits)) - 1;
		// resolve multiple bytes UTF-8 encoding char(except the first byte)
		for (int i = sptr + cntBits - 1; i > sptr; --i) {
			byte utfb = utf[i];
			uniC |= (utfb & 0x3f) << ptr;
			ptr += 6;
		}
		uniC |= firstByte << ptr;
		return (char) uniC;
	}

	// 根据给定字节计算UTF-8编码的一个字符所占字节数
	// UTF-8规则定义，字节标记只能为0或2~6
	private static int getCntBits(byte b) {
		int cnt = 0;
		if (b == 0)
			return -1;
		for (int i = 7; i >= 0; --i) {
			if (((b >> i) & 0x1) == 1)
				++cnt;
			else
				break;
		}
		return (cnt > 6 || cnt == 1) ? -1 : cnt;
	}

	/**
	 * 将字节数组转换为字符串
	 * 
	 * @param b
	 *            需要转换的字节数组
	 * @return 转换后的字符串
	 * 
	 */
	public static String ascii2String(byte[] b, int start, int count) {
		try {
			byte[] bytes = new byte[count];
			System.arraycopy(b, start, bytes, 0, count);
			return new String(bytes);
		} catch (Exception e) {
			return null;
		}
	}

	private int readUnsignedShort() {
		try {
			return data.readUnsignedShort();
		} catch (Exception _ex) {
			throw new RuntimeException();
		}
	}

	public final boolean readBoolean() {
		try {
			return data.readBoolean();
		} catch (Exception _ex) {
			throw new RuntimeException();
		}
	}

	// readBooleans
	public final boolean[] readBooleans() {
		boolean aflag[] = new boolean[readUnsignedShort()];
		for (int i1 = 0; i1 < aflag.length; i1++)
			aflag[i1] = readBoolean();

		return aflag;
	}

	// readByte
	public final int readByte() {
		try {
			return data.readByte();
		} catch (Exception _ex) {
			throw new RuntimeException();
		}
	}

	// readBytess
	private int[][] readBytess() {
		int i1;
		if ((i1 = readUnsignedShort()) == 0)
			return new int[0][];
		int ai[][] = new int[i1][readUnsignedShort()];
		for (int j1 = 0; j1 < ai.length; j1++) {
			for (int k1 = 0; k1 < ai[0].length; k1++)
				ai[j1][k1] = readByte();

		}

		return ai;
	}

	// readShort
	public final int readShort() {
		try {
			return data.readShort();
		} catch (Exception _ex) {
			throw new RuntimeException();
		}
	}

	// readShorts
	public final int[] readShorts() {
		int ai[] = new int[readUnsignedShort()];
		for (int i1 = 0; i1 < ai.length; i1++)
			ai[i1] = readShort();

		return ai;
	}

	// readShortss
	private int[][] readShortss() {
		int i1;
		if ((i1 = readUnsignedShort()) == 0)
			return new int[0][];
		int ai[][] = new int[i1][readUnsignedShort()];
		for (int j1 = 0; j1 < ai.length; j1++) {
			for (int k1 = 0; k1 < ai[0].length; k1++)
				ai[j1][k1] = readShort();

		}

		return ai;
	}

	// readInt
	public final int readInt() {
		try {
			return data.readInt();
		} catch (Exception _ex) {
			throw new RuntimeException();
		}
	}

	// readIntss
	private int[][] readIntss() {
		int i1;
		if ((i1 = readUnsignedShort()) == 0)
			return new int[0][];
		int ai[][] = new int[i1][readUnsignedShort()];
		for (int j1 = 0; j1 < ai.length; j1++) {
			for (int k1 = 0; k1 < ai[0].length; k1++)
				ai[j1][k1] = readInt();

		}

		return ai;
	}

	// auto_getIntss
	public final int[][] auto_getIntss() {
		int i1;
		if ((i1 = readByte()) == 1)
			return readBytess();
		if (i1 == 2)
			return readShortss();
		else
			return readIntss();
	}

	// readUTF
	public final String readUTF() {
		try {
			return data.readUTF();
		} catch (Exception _ex) {
			throw new RuntimeException();
		}
	}

	// readUTFs
	public final String[] readUTFs() {
		String as[] = new String[readUnsignedShort()];
		for (int i1 = 0; i1 < as.length; i1++)
			as[i1] = readUTF();

		return as;
	}

	/**
	 * 
	 * 分割字符串，原理：检测字符串中的分割字符串，然后取子串
	 * 
	 * @param original
	 *            需要分割的字符串
	 * 
	 * @paran regex 分割字符串
	 * 
	 * @return 分割后生成的字符串数组
	 * 
	 */

	public static String[] split(String original, String regex) {
		// 取子串的起始位置
		int startIndex = 0;
		// 将结果数据先放入Vector中
		Vector v = new Vector();
		// 返回的结果字符串数组
		String[] str = null;
		// 存储取子串时起始位置
		int index = 0;
		// 获得匹配子串的位置
		startIndex = original.indexOf(regex);
		// System.out.println("0" + startIndex);
		// 如果起始字符串的位置小于字符串的长度，则证明没有取到字符串末尾。
		// -1代表取到了末尾
		while (startIndex < original.length() && startIndex != -1) {
			String temp = original.substring(index, startIndex);
			System.out.println(" " + startIndex);
			// 取子串
			v.addElement(temp);
			// 设置取子串的起始位置
			index = startIndex + regex.length();
			// 获得匹配子串的位置
			startIndex = original.indexOf(regex, startIndex + regex.length());
		}

		// 取结束的子串
		v.addElement(original.substring(index + 1 - regex.length()));
		// 将Vector对象转换成数组
		str = new String[v.size()];
		for (int i = 0; i < v.size(); i++) {
			str[i] = (String) v.elementAt(i);
		}
		// 返回生成的数组
		return str;
	}
	
	private byte[] getBytes (char[] chars) {
	   Charset cs = Charset.forName ("UTF-8");
	   CharBuffer cb = CharBuffer.allocate (chars.length);
	   cb.put (chars);
	   cb.flip ();
	   ByteBuffer bb = cs.encode (cb);
	   return bb.array();
	}
	// byte转char
	private char[] getChars (byte[] bytes) {
		Charset cs = Charset.forName ("UTF-8");
		ByteBuffer bb = ByteBuffer.allocate (bytes.length);
		bb.put (bytes);
		bb.flip ();
		CharBuffer cb = cs.decode (bb);

	    return cb.array();
	}
}
