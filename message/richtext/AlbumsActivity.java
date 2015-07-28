
package common.message.richtext;


import java.io.Serializable;
import java.util.List;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;


import com.za.smartlock.manufacturer.R;


public class AlbumsActivity extends Activity {

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		Log.v(TAG, "onCreate");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_picture);

		this.helper = new AlbumHelper();
		this.helper.prepare(getApplicationContext());

		this.initData();
		this.initView();
	}


	private void initData () {
		Log.v(TAG, "initData");

		this.dataList = this.helper.getImagesBucketList(false);
		// PictureAddActivity.bitmap =
		// BitmapFactory.decodeResource(getResources(),
		// R.drawable.icon_addpic_unfocused);
	}


	private void initView () {
		Log.v(TAG, "initView");

		RelativeLayout relativeLayoutAAPCancel = (RelativeLayout) findViewById(R.id.relativeLayoutAAPCancel);
		relativeLayoutAAPCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick (View v) {
				finish();
			}

		});

		gridView = (GridView) findViewById(R.id.gridViewAAP);
		adapter = new ImageBucketAdapter(AlbumsActivity.this, dataList);
		gridView.setAdapter(adapter);

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick (AdapterView <?> parent, View view,
				int position, long id) {
				/**
				 * 根据position参数，可以获得跟GridView的子View相绑定的实体类，然后根据它的isSelected状态，
				 * 来判断是否显示选中效果。 至于选中效果的规则，下面适配器的代码中会有说明
				 */
				// if(dataList.get(position).isSelected()){
				// dataList.get(position).setSelected(false);
				// }else{
				// dataList.get(position).setSelected(true);
				// }
				/**
				 * 通知适配器，绑定的数据发生了改变，应当刷新视图
				 */
				adapter.notifyDataSetChanged();
				Intent intent = new Intent(AlbumsActivity.this,
					ImageGridActivity.class);
				intent.putExtra(AlbumsActivity.EXTRA_IMAGE_LIST,
					(Serializable) dataList.get(position).imageList);
				startActivity(intent);
				finish();
			}

		});
	}


	public static final String EXTRA_IMAGE_LIST = "imagelist";

	private static final String TAG = AlbumsActivity.class.getSimpleName();

	private List <ImageBucket> dataList;

	private GridView gridView;

	private ImageBucketAdapter adapter;

	private AlbumHelper helper;
}
