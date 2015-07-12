package nocom.pull2refresh.interfaces;


import android.view.View;


/**
 * Interface that allows Pull2RefreshBase to hijack the call to
 * AdapterView.setEmptyView()
 * 
 * @author chris
 */
public interface EmptyViewMethodAccessor {
	/**
	 * Calls upto AdapterView.setEmptyView()
	 * 
	 * @param emptyView
	 *            - to set as Empty View
	 */
	public void setEmptyViewInternal (View emptyView);


	/**
	 * Should call Pull2RefreshBase.setEmptyView() which will then
	 * automatically call through to setEmptyViewInternal()
	 * 
	 * @param emptyView
	 *            - to set as Empty View
	 */
	public void setEmptyView (View emptyView);
}
