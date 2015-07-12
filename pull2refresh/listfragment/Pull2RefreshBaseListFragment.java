package nocom.pull2refresh.listfragment;


import nocom.pull2refresh.views.Pull2RefreshBase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;


abstract class Pull2RefreshBaseListFragment <T extends Pull2RefreshBase <? extends AbsListView>>
	extends ListFragment {
	private T mPull2RefreshListView;


	@Override
	public final View onCreateView (LayoutInflater inflater,
		ViewGroup container, Bundle savedInstanceState) {
		View layout = super.onCreateView(inflater, container,
			savedInstanceState);
		ListView lv = (ListView) layout.findViewById(android.R.id.list);
		ViewGroup parent = (ViewGroup) lv.getParent();
		// Remove ListView and add Pull2RefreshListView in its place
		int lvIndex = parent.indexOfChild(lv);
		parent.removeViewAt(lvIndex);
		mPull2RefreshListView = onCreatePull2RefreshListView(inflater,
			savedInstanceState);
		parent.addView(mPull2RefreshListView, lvIndex, lv.getLayoutParams());
		return layout;
	}


	/**
	 * @return The {@link Pull2RefreshBase} attached to this ListFragment.
	 */
	public final T getPull2RefreshListView () {
		return mPull2RefreshListView;
	}


	/**
	 * Returns the {@link Pull2RefreshBase} which will replace the ListView
	 * created from ListFragment. You should override this method if you wish
	 * to customise the {@link Pull2RefreshBase} from the default.
	 * 
	 * @param inflater
	 *            - LayoutInflater which can be used to inflate from XML.
	 * @param savedInstanceState
	 *            - Bundle passed through from
	 *            {@link ListFragment#onCreateView(LayoutInflater, ViewGroup, Bundle)
	 *            onCreateView(...)}
	 * @return The {@link Pull2RefreshBase} which will replace the ListView.
	 */
	protected abstract T onCreatePull2RefreshListView (
		LayoutInflater inflater, Bundle savedInstanceState);
}