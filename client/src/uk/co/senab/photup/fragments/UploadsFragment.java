package uk.co.senab.photup.fragments;

import uk.co.senab.photup.PhotoUploadController;
import uk.co.senab.photup.R;
import uk.co.senab.photup.adapters.UploadsListBaseAdapter;
import uk.co.senab.photup.listeners.OnPhotoSelectionChangedListener;
import uk.co.senab.photup.model.PhotoSelection;
import uk.co.senab.photup.model.PhotoUpload;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.example.android.swipedismiss.SwipeDismissListViewTouchListener;
import com.example.android.swipedismiss.SwipeDismissListViewTouchListener.OnDismissCallback;

public class UploadsFragment extends SherlockListFragment implements OnPhotoSelectionChangedListener, OnDismissCallback {

	private PhotoUploadController mPhotoSelectionController;
	private UploadsListBaseAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new UploadsListBaseAdapter(getActivity());

		mPhotoSelectionController = PhotoUploadController.getFromContext(getActivity());
		mPhotoSelectionController.addPhotoSelectionListener(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		ListView listView = getListView();

		SwipeDismissListViewTouchListener swipeListener = new SwipeDismissListViewTouchListener(listView, this);
		listView.setOnTouchListener(swipeListener);
		listView.setOnScrollListener(swipeListener.makeScrollListener());
		listView.setSelector(R.drawable.selectable_background_photup);

		listView.setAdapter(mAdapter);
		setListShown(true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mPhotoSelectionController.removePhotoSelectionListener(this);
	}

	public void onPhotoSelectionChanged(PhotoSelection upload, boolean added) {
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		PhotoUpload upload = (PhotoUpload) l.getItemAtPosition(position);
		if (null != upload && upload.getState() == PhotoUpload.STATE_UPLOAD_COMPLETED) {

			String postId = upload.getResultPostId();
			if (null != postId) {
				final Intent intent = new Intent(Intent.ACTION_VIEW);

				try {
					intent.setData(Uri.parse("fb://post/" + postId));
					startActivity(intent);
					return;
				} catch (Exception e) {
					// Facebook not installed
				}

				try {
					intent.setData(Uri.parse("fplusfree://post?id=" + postId));
					startActivity(intent);
					return;
				} catch (Exception e) {
					// Friendcaster Free not installed
				}

				try {
					intent.setData(Uri.parse("fplus://post?id=" + postId));
					startActivity(intent);
					return;
				} catch (Exception e) {
					// Friendcaster Pro not installed
				}
			}
		}
	}

	public void onSelectionsAddedToUploads() {
		mAdapter.notifyDataSetChanged();
	}

	public void onDismiss(ListView listView, int[] reverseSortedPositions) {
		for (int i = 0, z = reverseSortedPositions.length; i < z; i++) {
			PhotoSelection upload = (PhotoSelection) listView.getItemAtPosition(reverseSortedPositions[i]);
			mPhotoSelectionController.removePhotoFromUploads(upload);
		}
		mAdapter.notifyDataSetChanged();
	}

	public boolean canDismiss(ListView listView, int position) {
		PhotoUpload upload = (PhotoUpload) listView.getItemAtPosition(position);
		switch (upload.getState()) {
			case PhotoUpload.STATE_UPLOAD_COMPLETED:
			case PhotoUpload.STATE_UPLOAD_ERROR:
				return true;
		}
		return false;
	}

	public void onUploadsCleared() {
		// NO-OP
	}

}