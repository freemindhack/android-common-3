
package common.datastructure;


import java.util.ArrayList;


public class ThreeValuesSet <T_V1, T_V2, T_V3> {
	public ThreeValuesSet (T_V1 v1, T_V2 v2, T_V3 v3) {
		this.v1s.add(v1);
		this.v2s.add(v2);
		this.v3s.add(v3);
	}


	public ThreeValuesSet () {
	}


	public void add (T_V1 v1, T_V2 v2, T_V3 v3) {
		this.v1s.add(v1);
		this.v2s.add(v2);
		this.v3s.add(v3);
	}


	public ThreeValuesSet <T_V1, T_V2, T_V3> remove (int index) {
		return new ThreeValuesSet <T_V1, T_V2, T_V3>(this.v1s.remove(index),
			this.v2s.remove(index), this.v3s.remove(index));
	}


	public T_V2 v2 (int index) {
		return this.v2s.get(index);
	}


	public ArrayList <T_V1> v1s () {
		return this.v1s;
	}


	private ArrayList <T_V1> v1s = new ArrayList <T_V1>();

	private ArrayList <T_V2> v2s = new ArrayList <T_V2>();

	private ArrayList <T_V3> v3s = new ArrayList <T_V3>();
}
