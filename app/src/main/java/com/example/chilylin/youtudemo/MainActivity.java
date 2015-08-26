package com.example.chilylin.youtudemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qcloud.qcloudfr_android_sdk.QcloudFrSDK;
import com.qcloud.qcloudfr_android_sdk.sign.QcloudFrSign;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity implements View.OnClickListener {

	private Uri mFirstPhotoUri;
	private Uri mSecondPhotoUri;

	private static String APPID = "";
	private static String SECRETID = "";
	private static String SECRETKEY = "";

	private static int EXPIRED_SECONDS = 2592000;

	private Context mContext = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;

		findViewById(R.id.btn_first_photo).setOnClickListener(this);
		findViewById(R.id.btn_second_photo).setOnClickListener(this);
		findViewById(R.id.btn_compare).setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_first_photo) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			mFirstPhotoUri = getOutputMediaFileUri(FIRST_IMAGE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mFirstPhotoUri);

			startActivityForResult(intent, 100);

		}
		else if (v.getId() == R.id.btn_second_photo) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			mSecondPhotoUri = getOutputMediaFileUri(SECOND_IMAGE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mSecondPhotoUri);

			startActivityForResult(intent, 200);
		}
		else if (v.getId() == R.id.btn_compare) {

			checkTwoPhotos();

		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			Log.e("YouTuDemo", "take photo failed!");
			return;
		}

		if (requestCode == 100) {
			((ImageView)findViewById(R.id.first_image)).setImageURI(mFirstPhotoUri);
		}
		else if (requestCode == 200) {
			((ImageView)findViewById(R.id.second_image)).setImageURI(mSecondPhotoUri);
		}
	}


	private void checkTwoPhotos() {
		if (mFirstPhotoUri == null || mSecondPhotoUri == null) {
			mFirstPhotoUri = getOutputMediaFileUri(FIRST_IMAGE);
			mSecondPhotoUri = getOutputMediaFileUri(SECOND_IMAGE);

			((ImageView)findViewById(R.id.first_image)).setImageURI(mFirstPhotoUri);
			((ImageView)findViewById(R.id.second_image)).setImageURI(mSecondPhotoUri);
		}
		new checkTwoPhotosTask().execute();
	}


	class checkTwoPhotosTask extends AsyncTask<Void, Void, Boolean>  	{
		String result = "";
		ProgressDialog mProgressDialog;

		@Override
		protected Boolean doInBackground(Void... params) {
			StringBuffer appSign = new StringBuffer();
			QcloudFrSign.appSign(APPID, SECRETID, SECRETKEY, System.currentTimeMillis() / 1000 + EXPIRED_SECONDS,
					"", appSign);

			QcloudFrSDK sdk = new QcloudFrSDK(APPID, appSign.toString());

			try {
				JSONObject response = sdk.FaceCompare(mFirstPhotoUri.getEncodedPath(), mSecondPhotoUri.getEncodedPath());
				Log.i("YouTuDemo", "compare response:" + response.toString());

				result = response.toString();
			}
			catch (Exception e) {
				e.printStackTrace();
				result = "compare failed";
			}

			return null;
		}

		@Override
		protected void onPostExecute(Boolean aBoolean) {
			super.onPostExecute(aBoolean);
			mProgressDialog.dismiss();
			TextView respTxtView = (TextView)findViewById(R.id.txt_compare_result);
			respTxtView.setText(result);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = ProgressDialog.show(mContext, "", "正在比对，请稍后");
			TextView respTxtView = (TextView)findViewById(R.id.txt_compare_result);
			respTxtView.setText("");
		}
	}


	public static final int FIRST_IMAGE = 1;
	public static final int SECOND_IMAGE = 2;


	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "TouTuDemoApp");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		File mediaFile;
		if (type == FIRST_IMAGE){
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"first_face.jpg");
		} else if(type == SECOND_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"second_face.jpg");
		} else {
			return null;
		}

		return mediaFile;
	}

}
