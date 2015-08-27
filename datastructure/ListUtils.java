
package common.datastructure;


import java.util.ArrayList;


public class ListUtils {
	public static Byte[] toByteArray (ArrayList <Byte> l) {
		if (null == l) {
			return null;
		}

		int sz = l.size();
		Byte[] r = new Byte[sz];

		for (int i = 0; i < sz; ++i) {
			r[i] = l.get(i);
		}

		return r;
	}


	public static Byte[] toByteArray (byte[] a, int start, int count) {
		if (null == a) {
			return null;
		}

		Byte[] r = new Byte[count];

		for (int i = 0; i < count; ++i) {
			r[i] = a[i + start];
		}

		return r;
	}


	public static byte[] tobyteArray (ArrayList <Byte> l) {
		if (null == l) {
			return null;
		}

		int sz = l.size();
		byte[] r = new byte[sz];

		for (int i = 0; i < sz; ++i) {
			r[i] = l.get(i);
		}

		return r;
	}
}
