
package common.utils;


public class MyResult <T> {
	public MyResult (int code, String msg, T cc) {
		this.code = code;
		this.msg = msg;
		this.cc = cc;
	}


	public int code;

	public String msg;

	public T cc;
};
