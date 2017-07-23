package com.samsung.gallery;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class GalleryActivity extends AppCompatActivity {
	private static final String TAG = GalleryActivity.class.getSimpleName();

	public static final String KEY_TITLE = "KEY_TITLE";
	public static final String KEY_IMAGES = "KEY_IMAGES";

	private String mTitle = null;
	private String[] mImages = null;

	private RecyclerView mRecyclerView;
	private RecyclerView.Adapter mAdapter;
	private RecyclerView.LayoutManager mLayoutManager;

	private int mImageSideSize = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			// Get title and images for the gallery
			mTitle = bundle.getString(KEY_TITLE);
			mImages = bundle.getStringArray(KEY_IMAGES);
		}

		if (mImages == null) {
			// Something is wrong, we got nothing, finish.
			finish();
			return;
		}

		if ((mTitle != null) && (getSupportActionBar() != null)) {
			// Set the gallery title
			getSupportActionBar().setTitle(mTitle);
		}

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		// Figure out what side size of each image will be based on device
		mImageSideSize = metrics.widthPixels - (2 * getResources().getDimensionPixelSize(R.dimen.list_item_gallery_image_padding));

		mRecyclerView = (RecyclerView) findViewById(R.id.gallery_recycler_view);
		mRecyclerView.setHasFixedSize(true);

		mLayoutManager = new LinearLayoutManager(this);
		mRecyclerView.setLayoutManager(mLayoutManager);

		mAdapter = new GalleryAdapter();
		mRecyclerView.setAdapter(mAdapter);
	}

	private class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
		public class ViewHolder extends RecyclerView.ViewHolder {
			public ImageView imageView;
			public TextView textView;
			public ViewHolder(View itemView) {
				super(itemView);
				textView = (TextView)itemView.findViewById(R.id.gallery_text_view);
				imageView = (ImageView)itemView.findViewById(R.id.gallery_image_view);
			}
		}

		@Override
		public GalleryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_gallery, parent, false);
			ViewHolder viewHolder = new ViewHolder(view);
			return viewHolder;
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			if (position == 0) {
				// TextView have a padding on the bottom, so for the first item, we will add a padding on top
				holder.itemView.setPadding(0,
						getResources().getDimensionPixelSize(R.dimen.list_item_gallery_horizontal_padding), 0 , 0);
			}
			String image = mImages[position];
			// Set the name of the image
			holder.textView.setText(image);

			// Load image
			String imagePath = Constants.ASSET_PATH + mTitle + "/" + image;
			Glide.with(GalleryActivity.this).load(Uri.parse(imagePath)).override(mImageSideSize, mImageSideSize).into(holder.imageView);
		}

		@Override
		public int getItemCount() {
			return mImages.length;
		}
	}
}
