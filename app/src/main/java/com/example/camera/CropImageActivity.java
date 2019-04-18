package com.example.camera;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.camera.databinding.ActivityCropImageBinding;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;

public class CropImageActivity extends AppCompatActivity {


    private String imgUri = "";
    private File mLoaclPath = null;
    private String mTmpfname = "";
    private String mPhotoPath = null;
    private ActivityCropImageBinding mBinding;
    public static final String JOIN_TYPE = "JOIN_TYPE";
    public  static final String IMAGE_URL_KEY = "IMAGE_URL";
    public static final String TYPE_PAGE_ACCOUNT = "ACCOUNT";
    public static final String TYPE_PAGE_ACCOUNT_CAMERA = "ACCOUNT_CAMERA";
    public static final String TYPE_PAGE_EDIT_AUDIO = "EDIT_AUDIO";
    public static final String TYPE_PAGE_EDIT_VIDEO = "EDIT_VIDEO";
    private String joinType = "";


    public static String getExternalCacheDirectoryPath(Context context) {
        try {
            if( context.getExternalCacheDir() == null ){
                //return Util.getExternalDSCacheDirPath();
            }

            else{
                return context.getExternalCacheDir().getAbsolutePath();
            }

        } catch( Exception e ) {
            e.printStackTrace();
        }

        return "";
    }



//    public static String getExternalDSCacheDirPath() {

//
//    static public final String PACKAGE_NAME = "l3aderyel3aderye";
//    static public final String EXT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/KTDingastar/";
//    static public final String SINGTOGETHER_EXT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/KTDingastar/SingTogether/";
//    static public final String EXT_CACHE_PATH = EXT_PATH + "/cache/";

//        File path = new File(EXT_PATH);
//        if (!path.isDirectory()) {
//            path.mkdir();
//        }
//
//        File cachePath = new File(EXT_CACHE_PATH);
//        if (!cachePath.isDirectory()) {
//            cachePath.mkdir();
//        }
//
//        return cachePath.getAbsolutePath();
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(0);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_crop_image);
        mLoaclPath = new File(getExternalCacheDirectoryPath(this)); // getExternalCacheDir();
        if (null != getIntent()){

            joinType = getIntent().getStringExtra(JOIN_TYPE);
            imgUri = getIntent().getStringExtra(IMAGE_URL_KEY);
        }

        if (joinType.equals(TYPE_PAGE_ACCOUNT) || joinType.equals(TYPE_PAGE_ACCOUNT_CAMERA)){

            mBinding.cropImageViewOval.setGuidelines(CropImageView.Guidelines.OFF);
            mBinding.cropImageViewOval.setFixedAspectRatio(true);
            mBinding.cropImageViewOval.setScaleType(CropImageView.ScaleType.FIT_CENTER);
            mBinding.cropImageViewOval.setAutoZoomEnabled(true);
            mBinding.cropImageViewOval.setImageUriAsync(Uri.parse(imgUri));
            mBinding.cropImageViewOval.setAspectRatio(1,1);
            mBinding.cropImageViewOval.setShowProgressBar(false);
            mBinding.cropImageViewOval.setVisibility(View.VISIBLE);

            if (joinType.equals(TYPE_PAGE_ACCOUNT_CAMERA)) {
                // mBinding.btnBack.setVisibility(View.GONE);
                mBinding.btnCancel.setVisibility(View.VISIBLE);
            }

        } else {
            mBinding.cropImageViewRectangle.setGuidelines(CropImageView.Guidelines.OFF);
            mBinding.cropImageViewRectangle.setFixedAspectRatio(true);
            mBinding.cropImageViewRectangle.setScaleType(CropImageView.ScaleType.FIT_CENTER);
            mBinding.cropImageViewRectangle.setAutoZoomEnabled(true);
            mBinding.cropImageViewRectangle.setImageUriAsync(Uri.parse(imgUri));

            if(joinType.equals(TYPE_PAGE_EDIT_VIDEO)) {
                mBinding.cropImageViewRectangle.setAspectRatio(9,16);
            } else {
                mBinding.cropImageViewRectangle.setAspectRatio(1,1);
            }

            mBinding.cropImageViewOval.setShowProgressBar(false);
            mBinding.cropImageViewRectangle.setVisibility(View.VISIBLE);
        }



        mBinding.btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Bitmap cropped = mBinding.cropImageViewOval.getCroppedImage();
                mBinding.cropImageViewOval.setOnGetCroppedImageCompleteListener(new CropImageView.OnGetCroppedImageCompleteListener() {
                    @Override
                    public void onGetCroppedImageComplete(CropImageView view, Bitmap bitmap, Exception error) {
                        makeCropImgFile(bitmap);
                    }
                });
                mBinding.cropImageViewOval.getCroppedImageAsync();



            }
        });


        mBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    private String getFilePath(String fTmpname) {

        String path = mLoaclPath.getAbsolutePath() + "/" + fTmpname;

        return path;
    }

    private void makeCropImgFile (Bitmap cropImgBitmap){

        try {
            // 파일 복사 (파일명 변경)
            mTmpfname = "temp_" + String.valueOf(System.currentTimeMillis()) + ".png"; // 임시 파일 만들기

            File pictureFile = new File(getFilePath(mTmpfname));


            pictureFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(
                    pictureFile);

            cropImgBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();

            mPhotoPath = getFilePath(mTmpfname);
            //stopLoadingPopup();
            Intent intent = new Intent();
            intent.putExtra("CROP_URL", mPhotoPath);
            this.setResult(RESULT_OK,intent);
            finish();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
