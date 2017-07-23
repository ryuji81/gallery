package com.samsung.gallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
	private List<String> mShownImages = new ArrayList<>();
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
						// Figure out how far we moved since long press
						float diffX = e.getX() - mX;
						if (Math.abs(diffX) > getResources().getDimensionPixelSize(R.dimen.gallery_change_distance)) {
							// Detected a change in horizontal distance of 40dp pixel size
							if (diffX > 0) {
								// To the right
								if (mShownImages.size() > 1) {
									// Only show previous if there is one
									showPreviousImage();
								}
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

		String imagePath = Constants.ASSET_PATH + mSelectedInfo.getTitle() + "/" + mSelectedInfo.getFirstImage();
		GalleryFragment galleryFragment = GalleryFragment.newInstance(imagePath);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.add(R.id.main_layout, galleryFragment, mainInfo.getFirstImage());
		// Keep track which ones we showed.
		mShownImages.add(mainInfo.getFirstImage());
		transaction.commit();
	}

	private void showNextImage() {
		if (mSelectedInfo == null) {
			// Error checking, nothing selected.
			return;
		}

		String[] images = mSelectedInfo.getImages();
		if (images.length == mShownImages.size()) {
			// No more images
			return;
		}

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		// If add new, slide from right to left, add to pop stack.  If remove, slide from left to right.
		transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
		String image = mSelectedInfo.getImages()[mShownImages.size()];
		String imagePath = Constants.ASSET_PATH + mSelectedInfo.getTitle() + "/" + image;
		// Keep track which ones we showed.
		mShownImages.add(image);
		// New fragment added with animation
		GalleryFragment galleryFragment = GalleryFragment.newInstance(imagePath);
		transaction.add(R.id.main_layout, galleryFragment, image);
		// Add to backstack to pop later
		transaction.addToBackStack(image);
		transaction.commit();
	}

	private void showPreviousImage() {
		if (mShownImages.size() > 1) {
			// If we have showed more than 1 images, simply pop and remove.
			getSupportFragmentManager().popBackStack();
			mShownImages.remove(mShownImages.size() - 1);
		}
	}

	private void dismissGallery() {
		if (mSelectedInfo == null) {
			return;
		}

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		for (String image : mShownImages) {
			// Remove all fragments we showed.
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(image);
			if (fragment != null) {
				transaction.remove(fragment);
			}
		}
		transaction.commit();
		// Clear
		mSelectedInfo = null;
		mShownImages.clear();
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
}
