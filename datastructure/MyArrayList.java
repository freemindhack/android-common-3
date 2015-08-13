
package common.datastructure;


import java.util.ArrayList;


public class MyArrayList <T> {
	public interface CompareMethod <T_RIGHT> {
		int cmp (Object l, T_RIGHT r);
	}


	public MyArrayList () {
		this.data = new ArrayList <T>();
	}


	public MyArrayList (T item) {
		this.data = new ArrayList <T>();

		this.add(item);
	}


	public int size () {
		if (null != this.data) {
			return this.data.size();
		} else {
			return 0;
		}
	}


	public int clear () {
		if (null != this.data) {
			this.data.clear();
			return 0;
		} else {
			return -1;
		}
	}


	public boolean add (T item) {
		if (null != item) {
			if (this.data.add(item)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}


	public int addAll (MyArrayList <T> another) {
		if (null == another) {
			return 0;
		}

		this.data.addAll(another.data);

		return another.size();
	}


	public MyArrayList <Integer> remove (T w) {
		if (null != this.data) {
			int n = this.data.size();
			MyArrayList <Integer> retval = new MyArrayList <Integer>();

			for (int i = 0; i < n; ++i) {
				if (this.data.equals(w)) {
					retval.add(Integer.valueOf(i));
					this.data.remove(i);
					--n;
					--i;
				}
			}

			return retval;
		} else {
			return null;
		}
	}


	public MyArrayList <T> removeByCmp (Object w, CompareMethod <T> cmp) {

		int n = this.data.size();
		MyArrayList <T> retval = new MyArrayList <T>();

		for (int i = 0; i < n; ++i) {
			if (0 == cmp.cmp(w, this.data.get(i))) {
				retval.add(this.data.remove(i));
				--n;
				--i;
			}
		}

		return retval;
	}


	public T removeByIndex (int index) {
		return this.data.remove(index);
	}


	public T get (int index) {
		return this.data.get(index);
	}


	private ArrayList <T> data = null;

}
