package nocom.common.datastructers;


import java.util.ArrayList;
import java.util.List;


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


	public T dequeue () {
		if (datas.size() > 0) {
			T ret = datas.remove(datas.size() - 1);
			return ret;
		} else {
			return null;
		}
	}


	public T pop () {
		if (datas.size() > 0) {
			T ret = datas.remove(0);
			return ret;
		} else {
			return null;
		}
	}


	public T get (int i) {
		if ((datas.size() > 0) && (i > 0 && i < datas.size())) {
			T ret = datas.get(i);
			return ret;
		} else {
			return null;
		}
	}


	public int count () {
		return datas.size();
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


	public T find (byte[] key, CompareMethod <byte[], T> cmp) {
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


	public T removeFound (byte[] key, CompareMethod <byte[], T> cmp) {
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
			return this.datas.remove(i);
		} else {
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
}
