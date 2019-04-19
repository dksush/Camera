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
import android.util.Log;
import android.view.View;

import com.example.camera.databinding.ActivityCropImageBinding;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;

public class CropImageActivity extends AppCompatActivity {


     // 그냥 미리 선언해 놓은 인자들.
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
                // 캐시가 널이라면.
            }

            else{
                return context.getExternalCacheDir().getAbsolutePath(); // 임시 데이터를 저장할 외부저장소에(캐시 디렉토리) 저장하기 위해 저장소의 경로값을 받아온다.
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
        mLoaclPath = new File(getExternalCacheDirectoryPath(this)); // 임시로 넘어온 사진 파일을 저장할 캐시 저장소 객체 선언.
        if (null != getIntent()){

            joinType = getIntent().getStringExtra(JOIN_TYPE);
            imgUri = getIntent().getStringExtra(IMAGE_URL_KEY); // 이미지 경로.
        }

        // 사진 크롭 화면.
        // 세부 옵션은 정확히 모르겟다. 설명도 없고 귀찮다.
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

            // else 부분은 그냥 넘어가도 될듯. if 문이 아닌 경로로 들어오는 케이스가 뭔지 모르겟다.
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




        // 완료 버튼 : 크롭한 이미지를 반환한다.
        mBinding.btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final Bitmap cropped = mBinding.cropImageViewOval.getCroppedImage(); // 크롭원 내에 있는 이미지를 비트맵으로 가져온다.
                Log.v("비트맵 : ", String.valueOf(cropped));
                mBinding.cropImageViewOval.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
                    @Override
                    public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
                        makeCropImgFile(cropped); // 그 비트맵을 png 로 바꿔서 main 으로 보낸다.
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

///// 온크레이트 끝 ////



    private String getFilePath(String fTmpname) {

        String path = mLoaclPath.getAbsolutePath() + "/" + fTmpname;
        return path;
    }


    // 이미지 저장순서
    // 1. 파일 저장경로 설정(string 객체)
    // 2. FileOutputStream 객체를 이용해 1에 설정한 파일경로와 파일 이름으로 새로운 파일 객체 만듬.
    // 3. bitmap compress 를 이용해 원하는 확장자로 변환해 2번 객체 넣는다.
    // 4. 2번을 닫는다.

    private void makeCropImgFile (Bitmap cropImgBitmap){

        try {
            // 파일 복사 (파일명 변경) : 괜한 충돌을 막기위해 파일명을 살짝 변경함.
            // main 있던 파일 명 = tmp
            // 1.
            mTmpfname = "temp_" + String.valueOf(System.currentTimeMillis()) + ".png"; // 임시 파일명 만들기
            File pictureFile = new File(getFilePath(mTmpfname)); // 앞서만든 캐시디렉토리에 해당 파일을 선언한다.
            pictureFile.createNewFile(); // 아직 존재하지 않는 파일이기에 createNewFile 로 만들어준다.

            // 2.
            FileOutputStream fileOutputStream = new FileOutputStream( // 파일 입출력용 스트리밍. 파일에서 바이트 데이터를 읽거나, 파일에 바이트 데이터를 저장할 수 있다.
                    pictureFile);

            // 3.
            cropImgBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            // 4.
            fileOutputStream.close();

            mPhotoPath = getFilePath(mTmpfname); // 4번까지 마치면  mTmpfname 이놈이 png 가 되어있다. 절대경로를 받아 넘긴다.
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
