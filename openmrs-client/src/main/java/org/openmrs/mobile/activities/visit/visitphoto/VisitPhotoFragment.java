/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.mobile.activities.visit.visitphoto;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.visit.VisitContract;
import org.openmrs.mobile.activities.visit.VisitFragment;
import org.openmrs.mobile.data.DataService;
import org.openmrs.mobile.models.VisitPhoto;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.StringUtils;
import org.openmrs.mobile.utilities.ViewUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class VisitPhotoFragment extends VisitFragment implements VisitContract.VisitPhotoView {

	private LinearLayoutManager layoutManager;
	private RecyclerView recyclerView;
	private VisitPhotoRecyclerViewAdapter adapter;

	//Upload Visit photo
	private final static int IMAGE_REQUEST = 1;
	private ImageView visitImageView;
	private FloatingActionButton capturePhoto;
	private Bitmap visitPhoto = null;
	private Button uploadVisitPhotoButton;

	private File output;
	private EditText fileCaption;
	private TextView noVisitImage;

	public static VisitPhotoFragment newInstance() {
		return new VisitPhotoFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setPresenter(mPresenter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_visit_photo, container, false);
		layoutManager = new LinearLayoutManager(this.getActivity());
		recyclerView = (RecyclerView)root.findViewById(R.id.downloadPhotoRecyclerView);
		recyclerView.setLayoutManager(layoutManager);

		capturePhoto = (FloatingActionButton)root.findViewById(R.id.capture_photo);
		visitImageView = (ImageView)root.findViewById(R.id.visitPhoto);
		uploadVisitPhotoButton = (Button)root.findViewById(R.id.uploadVisitPhoto);
		fileCaption = (EditText)root.findViewById(R.id.fileCaption);
		noVisitImage = (TextView)root.findViewById(R.id.noVisitImage);

		addListeners();

		return root;
	}

	@Override
	public void updateVisitImageMetadata(List<VisitPhoto> visitPhotos) {
		if (adapter == null) {
			adapter = new VisitPhotoRecyclerViewAdapter(this.getActivity(), visitPhotos, this);
		}

		RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), visitPhotos.size());
		recyclerView.setLayoutManager(layoutManager);

		recyclerView.setAdapter(adapter);
	}

	@Override
	public void downloadImage(String obsUuid, DataService.GetCallback<Bitmap> callback) {
		((VisitPhotoPresenter)mPresenter).downloadImage(obsUuid, callback);
	}

	@Override
	public void refresh() {
		fileCaption.setText(ApplicationConstants.EMPTY_STRING);
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.detach(this).attach(this).commit();
	}

	@Override
	public void showNoVisitPhoto() {
		noVisitImage.setVisibility(View.VISIBLE);
		recyclerView.setVisibility(View.GONE);
	}

	@Override
	public String formatVisitImageDescription(String description, String uploadedOn, String uploadedBy) {
		return getString(R.string.visit_image_description, description, uploadedOn, uploadedBy);
	}

	@NeedsPermission({ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE })
	public void capturePhoto() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
			File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
			output = new File(dir, getUniqueImageFileName());
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
			startActivityForResult(takePictureIntent, IMAGE_REQUEST);
		}
	}

	@OnShowRationale({ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE })
	public void showRationaleForCamera(final PermissionRequest request) {
		new AlertDialog.Builder(getActivity())
				.setMessage(R.string.permission_camera_rationale)
				.setPositiveButton(R.string.button_allow, (dialog, which) -> request.proceed())
				.setNegativeButton(R.string.button_deny, (dialog, button) -> request.cancel())
				.show();
	}

	@OnPermissionDenied({ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE })
	public void showDeniedForCamera() {
		createSnackbarLong(R.string.permission_camera_denied)
				.show();
	}

	@OnNeverAskAgain({ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE })
	public void showNeverAskForCamera() {
		createSnackbarLong(R.string.permission_camera_neverask)
				.show();
	}

	private String getUniqueImageFileName() {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		return timeStamp + "_" + ".jpg";
	}

	private Snackbar createSnackbarLong(int stringId) {
		//Snackbar snackbar = Snackbar.make(linearLayout, stringId, Snackbar.LENGTH_LONG);
		//View sbView = snackbar.getView();
		//TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
		//textView.setTextColor(Color.WHITE);
		//return snackbar;
		return null;
	}

	private Bitmap getPortraitImage(String imagePath) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;
		Bitmap photo = BitmapFactory.decodeFile(output.getPath(), options);
		float rotateAngle;
		try {
			ExifInterface exifInterface = new ExifInterface(imagePath);
			int orientation =
					exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_270:
					rotateAngle = 270;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					rotateAngle = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_90:
					rotateAngle = 90;
					break;
				default:
					rotateAngle = 0;
					break;
			}
			return rotateImage(photo, rotateAngle);
		} catch (IOException e) {
			return photo;
		}
	}

	public static Bitmap rotateImage(Bitmap source, float angle) {
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		VisitPhotoFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
	}

	private void addListeners() {
		capturePhoto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				VisitPhotoFragmentPermissionsDispatcher.capturePhotoWithCheck(VisitPhotoFragment.this);

			}
		});

		visitImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (output != null) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setDataAndType(Uri.fromFile(output), "image/jpeg");
					startActivity(i);
				}
			}
		});

		uploadVisitPhotoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (visitPhoto != null) {
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					visitPhoto.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

					MultipartBody.Part uploadFile = MultipartBody.Part.createFormData("file",
							output.getName(), RequestBody.create(MediaType.parse("image/jpeg"), output));

					((VisitPhotoPresenter)mPresenter).getVisitPhoto().setRequestImage(uploadFile);
					((VisitPhotoPresenter)mPresenter).getVisitPhoto().setFileCaption(
							StringUtils.notEmpty(
									ViewUtils.getInput(fileCaption)) ?
									ViewUtils.getInput(fileCaption) :
									getString(R.string.default_file_caption_message));
					((VisitPhotoPresenter)mPresenter).uploadImage();
				}
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == IMAGE_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {
				visitPhoto = getPortraitImage(output.getPath());
				Bitmap bitmap =
						ThumbnailUtils.extractThumbnail(visitPhoto, visitImageView.getWidth(), visitImageView.getHeight());
				visitImageView.setImageBitmap(bitmap);
				visitImageView.invalidate();
			} else {
				output = null;
			}
		}
	}
}