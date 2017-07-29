package com.samsung.gallery;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

		// Load the image
		ImageView imageView = (ImageView) view.findViewById(R.id.gallery_fragment_view);
		Glide.with(getActivity()).load(Uri.parse(mImagePath)).into(imageView);

		return view;
	}
}
