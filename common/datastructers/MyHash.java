package nocom.common.datastructers;


import java.util.ArrayList;
import java.util.List;


public class MyHash <T_KEY, T_VALUE> {
	public MyHash () {
		try {
			this.keys = new ArrayList<T_KEY>();
			this.values = new ArrayList<T_VALUE>();
			this.keys.clear();
			this.values.clear();
		} catch (Exception e) {
			;
		}
	}


	public void finalize () {
		try {
			this.clear();
		} catch (Exception e) {
			;
		}
	}


	public void clear () {
		try {
			this.keys.clear();
			this.values.clear();
		} catch (Exception e) {
			;
		}
	}


	public boolean insert (T_KEY key, T_VALUE value) {
		try {
			if ((null == key) || (null == value)) {
				return false;
			}

			boolean found = false;
			int n = this.keys.size();

			for (int i = 0; i < n; ++i) {
				if (this.keys.get(i).equals(key)) {
					found = true;
					break;
				}
			}

			if (found) {
				return false;
			}

			n = this.values.size();

			for (int i = 0; i < n; ++i) {
				if (this.values.get(i).equals(value)) {
					found = true;
					break;
				}
			}

			if (found) {
				return false;
			}

			this.keys.add(key);
			this.values.add(value);

			return true;
		} catch (Exception e) {
			return false;
		}
	}


	private boolean remove (int which) {
		try {
			if ((which >= 0) && (which < this.keys.size())) {
				this.keys.remove(which);
				this.values.remove(which);

				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}


	public boolean removeWithKey (T_KEY key) {
		try {
			if (null == key) {
				return false;
			}

			int which = this.whichOfKeys(key);

			if ((which >= 0) && (which < this.keys.size())) {
				this.keys.remove(which);
				this.values.remove(which);

				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}


	public boolean removeWithValue (T_VALUE value) {
		try {
			if (null == value) {
				return false;
			}

			int which = this.whichOfValues(value);

			if ((which >= 0)
				&& (which < this.values.size())) {
				this.keys.remove(which);
				this.values.remove(which);

				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}


	public int whichOfKeys (T_KEY key) {
		try {
			boolean found = false;
			int i;
			int n = this.keys.size();

			for (i = 0; i < n; ++i) {
				if (this.keys.get(i).equals(key)) {
					found = true;
					break;
				}
			}

			if (!found) {
				return -1;
			} else {
				return i;
			}
		} catch (Exception e) {
			return -1;
		}
	}


	public int whichOfValues (T_VALUE value) {
		try {
			boolean found = false;
			int i;
			int n = this.values.size();

			for (i = 0; i < n; ++i) {
				if (this.values.get(i).equals(value)) {
					found = true;
					break;
				}
			}

			if (!found) {
				return -1;
			} else {
				return i;
			}
		} catch (Exception e) {
			return -1;
		}
	}


	public int count () {
		return this.keys.size();
	}


	public boolean isEmpty () {
		try {
			return (0 == this.keys.size());
		} catch (Exception e) {
			return true;
		}
	}


	public T_KEY key (int which) {
		try {
			if ((which >= 0) && (which < this.keys.size())) {
				return this.keys.get(which);
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}


	public T_KEY key (T_VALUE value) {
		try {
			boolean found = false;
			int i;
			int n = this.values.size();
			for (i = 0; i < n; ++i) {
				if (this.values.get(i).equals(value)) {
					found = true;
					break;
				}
			}

			if (!found) {
				return null;
			} else {
				return this.keys.get(i);
			}
		} catch (Exception e) {
			return null;
		}
	}


	public T_VALUE value (T_KEY key) {
		try {
			boolean found = false;
			int i;
			int n = this.keys.size();
			for (i = 0; i < n; ++i) {
				if (this.keys.get(i).equals(key)) {
					found = true;
					break;
				}
			}

			if (!found) {
				return null;
			} else {
				return this.values.get(i);
			}
		} catch (Exception e) {
			return null;
		}
	}


	public T_VALUE value (int which) {
		try {
			if ((which >= 0)
				&& (which < this.values.size())) {
				return this.values.get(which);
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}


	public boolean withKey (T_KEY key) {
		try {
			boolean found = false;
			int n = this.keys.size();
			for (int i = 0; i < n; ++i) {
				if (this.keys.get(i).equals(key)) {
					found = true;
					break;
				}
			}

			if (found) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}


	public boolean
		set (int which, T_KEY key, T_VALUE value) {
		try {
			if (which < 0 || which >= this.values.size()) {
				return false;
			}

			boolean ret = this.remove(which);
			if (ret) {
				return this.insert(key, value);
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}


	public boolean
		setNewValue (T_KEY key, T_VALUE newValue) {
		try {
			int which = this.whichOfKeys(key);

			if (which < 0) {
				return false;
			}

			int whichValue = this.whichOfValues(newValue);
			if (whichValue >= 0) {
				return false;
			}

			this.values.set(which, newValue);

			return true;
		} catch (Exception e) {
			return false;
		}
	}


	public boolean setNewKey (T_KEY key, T_KEY newKey) {
		try {
			int which = this.whichOfKeys(key);

			if (which < 0) {
				return false;
			}

			int whichValue = this.whichOfKeys(newKey);
			if (whichValue >= 0) {
				return false;
			}

			this.keys.set(which, newKey);

			return true;
		} catch (Exception e) {
			return false;
		}
	}


	private List<T_KEY> keys;
	private List<T_VALUE> values;
}
