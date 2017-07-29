package com.samsung.gallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = MainActivity.class.getSimpleName();

	private RecyclerView mRecyclerView;
	private RecyclerView.Adapter mAdapter;
	private RecyclerView.LayoutManager mLayoutManager;

	private int mCardViewWidth = 0;
	private int mCardViewHeight = 0;

	private boolean mLongClick = false;
	private List<MainInfo> mMainInfos = new ArrayList<>();

	private MainInfo mSelectedInfo = null;

	private ViewPager mViewPager;

	private float mX = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Figure out card view size for cropping
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mCardViewWidth = metrics.widthPixels;
		mCardViewHeight = getResources().getDimensionPixelSize(R.dimen.list_item_main_height);

		// Obtain the assets
		try {
			String[] titles = getAssets().list(Constants.MAIN);
			if (titles != null) {
				for (int i = 0; i < titles.length; i++) {
					String[] images = getAssets().list(Constants.MAIN + "/" + titles[i]);
					if (images != null) {
						mMainInfos.add(new MainInfo(titles[i], images));
					}
				}
			}

		} catch (IOException e) {
			// We have an error, likely not able to continue if no assets.
			e.printStackTrace();
			finish();
			return;
		}

		mViewPager = (ViewPager) findViewById(R.id.main_view_pager);
		ViewGroup.LayoutParams layoutParams = mViewPager.getLayoutParams();
		// Figure out the image size base on device, minus some spacing
		int side = metrics.widthPixels - (2 * getResources().getDimensionPixelSize(R.dimen.fragment_gallery_padding));
		layoutParams.width = side;
		layoutParams.height = side;
		// Set size in layout, which affects the image view since it is the only view inside
		mViewPager.setLayoutParams(layoutParams);

		// The layout will be shown from X
		mViewPager.setX(getResources().getDimensionPixelSize(R.dimen.fragment_gallery_padding));
		// The layout will be shown from Y, which is the device height without action and nav bar, minus image side
		mViewPager.setY(((metrics.heightPixels - side) / 2) - getActionBarHeight() - getNavigationBarHeight());

		mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
		mRecyclerView.setHasFixedSize(true);

		mLayoutManager = new LinearLayoutManager(this);
		mRecyclerView.setLayoutManager(mLayoutManager);

		mAdapter = new MainAdapter();
		mRecyclerView.setAdapter(mAdapter);

		// Setting up the listener for the showing galleries in this activity
		mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
			@Override
			public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
				if (mLongClick) {
					// Only intercept if a long click has been detected
					return true;
				}
				return false;
			}

			@Override
			public void onTouchEvent(RecyclerView rv, MotionEvent e) {
				// We would only get in here if we intercepted the touch event after long press
				int action = e.getActionMasked();
				switch(action) {
					case MotionEvent.ACTION_UP:
						// User lift off finger, long press finished, dismiss gallery
						dismissGallery();
						mLongClick = false;
						mX = -1;
						break;
					case MotionEvent.ACTION_MOVE:
						// Will need to investigate if there is a way change viewpager page with move instead of a fling.
						// For now keep the existing way.
						// Figure out how far we moved since long press
						float diffX = e.getX() - mX;
						if (Math.abs(diffX) > getResources().getDimensionPixelSize(R.dimen.gallery_change_distance)) {
							// Detected a change in horizontal distance of 40dp pixel size
							if (diffX > 0) {
								// To the right
								showPreviousImage();
							} else {
								// To the left, show next image
								showNextImage();
							}
							// Update new x.
							mX = e.getX();
						}
						break;
				}
			}

			@Override
			public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
		});
	}

	private void showGallery(final MainInfo mainInfo) {
		// We show the first image of the gallery selected after long press
		mSelectedInfo = mainInfo;

		// Set up a new adapter, or we can have a member and update its contents
		mViewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager(), mSelectedInfo));
		mViewPager.setVisibility(View.VISIBLE);
	}

	private void showNextImage() {
		if (mSelectedInfo == null) {
			// Error checking, nothing selected.
			return;
		}

		String[] images = mSelectedInfo.getImages();
		if ((images.length - 1) == mViewPager.getCurrentItem()) {
			// No more images
			return;
		}
		// Set next current item
		mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
	}

	private void showPreviousImage() {
		if (mViewPager.getCurrentItem() == 0) {
			return;
		}
		// Go back to previous item
		mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
	}

	private void dismissGallery() {
		if (mSelectedInfo == null) {
			return;
		}

		// Hide view pager again.
		mViewPager.setAdapter(null);
		mViewPager.setVisibility(View.GONE);
		mSelectedInfo = null;
	}

	private int getActionBarHeight() {
		// Helper function to get action bar height
		ActionBar actionBar = getSupportActionBar();
		return (actionBar == null) ? 0 : actionBar.getHeight();
	}

	private int getNavigationBarHeight() {
		int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
		return (resourceId > 0) ? getResources().getDimensionPixelSize(resourceId) : 0;
	}

	private class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {
		// Adapter for RecycleView

		public class ViewHolder extends RecyclerView.ViewHolder {
			public CardView cardView;
			public ImageView imageView;
			public TextView textView;

			public ViewHolder(View itemView) {
				super(itemView);
				cardView = (CardView)itemView.findViewById(R.id.main_card_view);
				textView = (TextView)itemView.findViewById(R.id.main_text_view);
				imageView = (ImageView)itemView.findViewById(R.id.main_image_view);
			}
		}

		@Override
		public MainAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_main, parent, false);
			ViewHolder viewHolder = new ViewHolder(view);
			return viewHolder;
		}

		@Override
		public void onBindViewHolder(final ViewHolder holder, int position) {
			final MainInfo galleryInfo = mMainInfos.get(position);
			holder.textView.setText(galleryInfo.getTitle());

			// We only need to show the first image of each gallery
			String image = galleryInfo.getFirstImage();
			if (image != null) {
				String imagePath = Constants.ASSET_PATH + galleryInfo.getTitle() + "/" + image;
				// Use Glide to load and crop the image.
				Glide.with(MainActivity.this).load(Uri.parse(imagePath)).override(mCardViewWidth, mCardViewHeight)
						.centerCrop().into(holder.imageView);
			}

			holder.cardView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						// In case it is a long press, get the x coordinate
						mX = event.getX();
					}
					return false;
				}
			});

			holder.cardView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Simple click, show the gallery in GalleryActivity
					Bundle bundle = new Bundle();
					bundle.putString(GalleryActivity.KEY_TITLE, galleryInfo.getTitle());
					bundle.putStringArray(GalleryActivity.KEY_IMAGES, galleryInfo.getImages());
					Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
					intent.putExtras(bundle);

					startActivity(intent);
				}
			});

			holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					// It is a long press, show gallery in this activity with the first image.
					showGallery(galleryInfo);
					mLongClick = true;
					return true;
				}
			});
		}

		@Override
		public int getItemCount() {
			return mMainInfos.size();
		}
	}

	private class MainInfo {
		// This keep track of the main gallery info.
		String mTitle;
		String[] mImages;

		MainInfo(final String title, final String[] images) {
			mTitle = title;
			mImages = images;
		}

		String getTitle() {
			return mTitle;
		}

		String[] getImages() {
			return mImages;
		}

		String getFirstImage() {
			if ((mImages != null) && (mImages.length > 0)) {
				return mImages[0];
			}
			return null;
		}
	}

	private class MainPagerAdapter extends FragmentStatePagerAdapter {
		// Pager Adapter to show the gallery image fragments
		private MainInfo mMainInfo;

		public MainPagerAdapter(FragmentManager fragmentManager, MainInfo mainInfo) {
			super(fragmentManager);
			mMainInfo = mainInfo;
		}

		@Override
		public Fragment getItem(int position) {
			String imagePath = Constants.ASSET_PATH + mMainInfo.getTitle() + "/" + mMainInfo.getImages()[position];
			return GalleryFragment.newInstance(imagePath);
		}

		@Override
		public int getCount() {
			return mMainInfo.getImages().length;
		}
	}
}
