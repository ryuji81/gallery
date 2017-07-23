package com.samsung.gallery;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

public class GalleryFragment extends Fragment {
	// Simple fragment to show an image

	private static final String TAG = GalleryFragment.class.getSimpleName();

	private static final String KEY_IMAGE_PATH = "KEY_IMAGE_PATH";

	private String mImagePath;


	public GalleryFragment() {}

	public static GalleryFragment newInstance(String imagePath) {
		GalleryFragment fragment = new GalleryFragment();
		Bundle args = new Bundle();
		args.putString(KEY_IMAGE_PATH, imagePath);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mImagePath = getArguments().getString(KEY_IMAGE_PATH);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.fragment_gallery, container, false);

		LinearLayout layout = (LinearLayout) view.findViewById(R.id.gallery_layout);
		ViewGroup.LayoutParams layoutParams = layout.getLayoutParams();

		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		// Figure out the image size base on device, minus some spacing
		int side = metrics.widthPixels - (2 * getResources().getDimensionPixelSize(R.dimen.fragment_gallery_padding));
		layoutParams.width = side;
		layoutParams.height = side;
		// Set size in layout, which affects the image view since it is the only view inside
		layout.setLayoutParams(layoutParams);

		// The layout will be shown from X
		layout.setX(getResources().getDimensionPixelSize(R.dimen.fragment_gallery_padding));
		// The layout will be shown from Y, which is the device height without action bar, minus image side
		layout.setY(((metrics.heightPixels - side) / 2) - getActionBarHeight());

		// Load the image
		ImageView imageView = (ImageView) view.findViewById(R.id.gallery_fragment_view);
		Glide.with(getActivity()).load(Uri.parse(mImagePath)).into(imageView);

		return view;
	}

	private int getActionBarHeight() {
		// Helper function to get action bar height
		AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
		ActionBar actionBar = appCompatActivity.getSupportActionBar();
		return (actionBar == null) ? 0 : actionBar.getHeight();
	}
}
