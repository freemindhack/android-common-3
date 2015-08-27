
package common.datastructure;


import java.util.ArrayList;
import java.util.List;


import common.my_android_log.MyLog;


import android.util.Log;


/*
 * XXX-NOTE: when access obj of this should add lock first if thread > 0
 */
public class MyQueue <T> {
	protected List <T> datas = new ArrayList <T>();


	public MyQueue (MyQueue <T> cp) {
		if (null == cp) {
			return;
		}
		int sz = cp.datas.size();
		for (int i = 0; i < sz; ++i) {
			this.datas.add(cp.datas.get(i));
		}
	}


	public MyQueue () {
	}


	public void enqueue (T data) {
		datas.add(data);
	}


	/**
	 * NAME
	 *   enqueueAll - enqueue All from 0 to (length - 1) to tail
	 */
	public void enqueueAll (T[] datas) {
		if (null == datas) {
			return;
		}

		int n = datas.length;

		MyLog.v(TAG, "n: " + n);
		for (int i = 0; i < n; ++i) {
			this.datas.add(datas[i]);
		}
	}


	/**
	 * NAME
	 *   dequeue - dequeue this first one
	 */
	public T dequeue () {
		if (datas.size() > 0) {
			T ret = datas.remove(0);
			return ret;
		} else {
			return null;
		}
	}


	public int find (T what) {
		if (null == what) {
			return -1;
		} else {
			int sz = this.datas.size();

			for (int i = 0; i < sz; ++i) {
				if (this.datas.get(i).equals(what)) {
					return i;/* found */
				}
			}

			return -1;
		}
	}


	public ArrayList <T> dequeueAll () {
		ArrayList <T> r = new ArrayList <T>();

		int sz = this.datas.size();
		for (int i = 0; i < sz; ++i) {
			r.add(this.datas.remove(0));
		}

		return r;
	}


	public ArrayList <T> dequeueAll (int maxCount) {
		if (maxCount < 0) {
			return null;
		}

		ArrayList <T> r = new ArrayList <T>();

		int sz = this.datas.size();
		for (int i = 0; i < sz && i < maxCount; ++i) {
			r.add(this.datas.remove(0));
		}

		return r;
	}


	/**
	 * NAME
	 *   pop - pop the last add one
	 */
	public T pop () {
		if (datas.size() > 0) {
			T ret = datas.remove(datas.size() - 1);
			return ret;
		} else {
			return null;
		}
	}


	public T get (int i) {
		if ((datas.size() > 0) && (i >= 0 && i < datas.size())) {
			T ret = datas.get(i);
			return ret;
		} else {
			return null;
		}
	}


	public ArrayList <T> getAll () {
		ArrayList <T> r = new ArrayList <T>();

		int sz = this.datas.size();
		for (int i = 0; i < sz; ++i) {
			r.add(this.datas.get(i));
		}

		return r;
	}


	public ArrayList <T> getAll (int maxCount) {
		if (maxCount < 0) {
			return null;
		}

		ArrayList <T> r = new ArrayList <T>();

		int sz = this.datas.size();
		for (int i = 0; i < sz && i < maxCount; ++i) {
			r.add(this.datas.get(i));
		}

		return r;
	}


	public int count () {
		return datas.size();
	}


	public T getFirst () {
		if (datas.size() > 0) {
			T ret = datas.get(0);
			return ret;
		} else {
			return null;
		}
	}


	public T getLast () {
		if (datas.size() > 0) {
			T ret = datas.get(datas.size() - 1);
			return ret;
		} else {
			return null;
		}
	}


	public MyQueue <T> dump () {
		return new MyQueue <T>(this);
	}


	public MyQueue <T> dumpEmpty () {
		MyQueue <T> ret = new MyQueue <T>(this);
		this.empty();
		return ret;
	}


	public void empty () {
		this.datas.clear();
	}


	public T find (byte[] key, MyCompareMethod <byte[], T> cmp) {
		boolean found = false;
		int sz = this.datas.size();
		int i = 0;
		for (; i < sz; ++i) {
			int ret = cmp.compare(key, this.datas.get(i));
			if (0 == ret) {
				found = true;
				break;
			}
		}
		if (found) {
			return this.datas.get(i);
		} else {
			return null;
		}
	}


	public T removeFound (byte[] key, MyCompareMethod <byte[], T> cmp) {
		try {
			Log.v(TAG, "removeFound");

			boolean found = false;
			int sz = this.datas.size();
			int i = 0;
			for (; i < sz; ++i) {
				try {
					int ret = cmp.compare(key, this.datas.get(i));

					if (0 == ret) {
						found = true;
						break;
					}
				} catch (Exception e) {
					Log.e(TAG + ":removeFound",
						"callback compare: E: " + e.getMessage());
					return null;
				}
			}

			if (found) {
				T r = this.datas.remove(i);

				return r;
			} else {
				return null;
			}
		} catch (Exception e) {
			Log.e(TAG + ":removeFound", "E: " + e.getMessage());
			return null;
		}
	}


	public void append (MyQueue <T> q) {
		int cnt = q.count();
		if ((null == q) || (cnt <= 0)) {
			return;
		}
		for (int i = 0; i < cnt; ++i) {
			this.datas.add(q.get(i));
		}
	}


	public void appendEmpty (MyQueue <T> q) {
		int cnt = q.count();
		if ((null == q) || (cnt <= 0)) {
			return;
		}
		for (int i = 0; i < cnt; ++i) {
			this.datas.add(q.datas.remove(0));
		}
	}


	public boolean equs (MyQueue <T> another) {
		if (null == another) {
			return false;
		}
		if (another.count() != this.count()) {
			return false;
		}
		int n = another.count();
		for (int i = 0; i < n; ++i) {
			if (!this.datas.get(i).equals(another.datas.get(i))) {
				return false;
			}
		}
		return true;
	}


	private static final String TAG = MyQueue.class.getSimpleName();
}
