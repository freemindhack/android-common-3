package nocom.pull2refresh.listfragment;


import nocom.pull2refresh.views.listview.Pull2RefreshExpandableListView;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;


/**
 * A sample implementation of how to use
 * {@link Pull2RefreshExpandableListView} with {@link ListFragment}. This
 * implementation simply replaces the ListView that {@code ListFragment}
 * creates with a new {@code Pull2RefreshExpandableListView}. This means that
 * ListFragment still works 100% (e.g. <code>setListShown(...)</code> ).
 * <p/>
 * The new Pull2RefreshListView is created in the method
 * {@link #onCreatePull2RefreshListView(LayoutInflater, Bundle)}. If you wish
 * to customise the {@code Pull2RefreshExpandableListView} then override this
 * method and return your customised instance.
 * 
 * @author Chris Banes
 * 
 */
public class Pull2RefreshExpandableListFragment extends
	Pull2RefreshBaseListFragment <Pull2RefreshExpandableListView> {
	protected Pull2RefreshExpandableListView onCreatePull2RefreshListView (
		LayoutInflater inflater, Bundle savedInstanceState) {
		return new Pull2RefreshExpandableListView(getActivity());
	}
}