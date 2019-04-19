package com.example.camera;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private int currentVersionCode = 0;
    private static final int PERMISSIONS_LIBRARY_READ_STORAGE=52;
    private static final int READ_REQUEST_LIBRARY = 42;
    private static final int READ_REQUEST_CAMERA = 43;
    private static final int PERMISSIONS_CAMERA = 50;
    private static final int CROP_IMG_REQUEST_CODE = 62;
    private static final int PERMISSIONS_CAMERA_READ_STORAGE = 51;
    private static final int REQUEST_CAMERA_PERMISSIONS = 1;
    private static final int REQUEST_EDIT_PROFILE_CODE = 53;
    // 전체 퍼미션
    public static final String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_PHONE_STATE,
    };


    // 카메라 확인용 퍼미션 : 카메라를 선택했을때, 카메라 퍼미션이 허용되있는지 묻는다.
    // 원래 한곳에 전체와 카메라 퍼미션이 있을일은 없지.
    // 그냥 한곳에 때려박다 보니 허허.
    private static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };



    private String mPhotoPath = null;
    private boolean isModifyProfile = false;
    private boolean isModifyImg = false;
    private int fileSize = 0;
    private String exName = "";
    ImageView imageView;


    String tempCameraFileName;  // 카메라로 찍은 사진을 임시로 담아둘 경로 객체.
    private static File mLoaclPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES); // 외부 저장소 경로 : 카메라로 찍은 사진의 경로값을 받기위해(카메라로 찍은 사진은 일단 외부저장소에 저장된다)
    //File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM); // 사진.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image);
        startAfterAuthorityCheck(); // 권한체크.
    }



    public void gallery(View view){
        int permissionCheck;
        permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            // 라이브러리 접근 권한 없음
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_LIBRARY_READ_STORAGE);

        } else {

            try {
                Intent intent = new Intent(Intent.ACTION_PICK); // 갤러리로 이동.(선택한 데이터를 반환해주는 intent)
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE); // 이미지 타입을 원함.
                startActivityForResult(intent, READ_REQUEST_LIBRARY);
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }
        //dialog.dismiss();
    }



    public void camera(View view){
        // 카메라 관련 퍼미션이 없으면, 퍼미션 체크하고 리턴을 토해, if 밑에를 수행한다.
        if(!hasPermissionsGranted(CAMERA_PERMISSIONS)){
           requestEachPermissions(CAMERA_PERMISSIONS,REQUEST_CAMERA_PERMISSIONS);
           return;
        }



        tempCameraFileName = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".png"; // 찍은 사진을 담아둘 객체 파일명을 설정한다.
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // 카메라로 이동
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, MakeFile(tempCameraFileName)); // MakeFile 을 통해 해당 사진을 외부에서 접근할 수 있게 허용.EXTRA_OUTPUT 을 이용해 위에서 설정한 파일객체를 실제 사진파일의 url 지정.
        startActivityForResult(intent, READ_REQUEST_CAMERA);

        //dialog.dismiss(); // 다이얼로그 같은게 있으면 꺼준다.
    }

    // 안드 7(누가넷/ api24) 에서 (intent로) 파일 전송시 "file://" 노출되어 있으면 FileUriExposedException 오류가 발생하게 되고 앱이 종료됨.
    // 앱간 파일을 공유하려면 "file://" 대신 "content://"로 URI를 보내야 한다.( 앱간이라지만 그냥 내 앱에서 다른곳(서버포함) 을 전부 칭하는 듯)
    // URI로 데이터를 보내기 위해선 임시 액세스 권한을 부여해야 하고  FileProvider를 이용해야 함.
    // FileProvider 는 내부 저장소의 특정 파일을 외부(어플)에서 접근 가능하게끔 특정파일 지정.
    // 사용하게 위해
    // 1. 매니페스트 어플리케이션 안쪽에 프로바이더 선언
    // 2. 프로바이더(xml) 경로 설정 ( 카메라로 막 찍은 사진은 '외부저장소' 에 저장된다. )
    // 정리하자면 찍은 파일(사진)을 앱 외부(서버) 에 올리기 위해 provider 를 이용해 권한을 부여하는 것.
    private Uri MakeFile(String filename) {
      //  Uri tUri = FileProvider.getUriForFile(this,"com.dingastar.singstealer.provider", new File(mLoaclPath, filename));
        Uri uri = FileProvider.getUriForFile(this, this.getPackageName()+".provider", new File(mLoaclPath, filename));
        return uri;
    }


    private String getFilePath(String fTmpname) {

        String path = mLoaclPath.getPath() + "/" + fTmpname;

        return path;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        switch (requestCode){

            // 갤러리 사진 받아서 크롭하기
            case READ_REQUEST_LIBRARY:

                if(resultCode == RESULT_OK){
                    // 사진 받아서 크롭으로.
                    Intent intent = new Intent(this, CropImageActivity.class);
                    intent.putExtra(CropImageActivity.IMAGE_URL_KEY, data.getData().toString()); // 갤러리서 받은 이미지데이터 보내기.
                    intent.putExtra(CropImageActivity.JOIN_TYPE, CropImageActivity.TYPE_PAGE_ACCOUNT); // 없어도됨.
                    startActivityForResult(intent, CROP_IMG_REQUEST_CODE);


                }else{
                    //Util.d("result :", "Libray image picked fail...");
                }
                break;


            // 카메라로 찍고 크롭하기.
            case READ_REQUEST_CAMERA:
                if (requestCode == RESULT_OK){
                    Intent intent = new Intent(this, CropImageActivity.class);
                    intent.putExtra(CropImageActivity.IMAGE_URL_KEY, "file://" + getFilePath(tempCameraFileName)); // 사진 경로
                    intent.putExtra(CropImageActivity.JOIN_TYPE, CropImageActivity.TYPE_PAGE_ACCOUNT_CAMERA); // 없어도됨
                    startActivityForResult(intent, CROP_IMG_REQUEST_CODE);
                }
                break;



            // 크롭한 이미지 받기.
            case CROP_IMG_REQUEST_CODE:

                if (resultCode == RESULT_OK) {

                    mPhotoPath = data.getStringExtra("CROP_URL"); // 이미지 경로

                    imageView.setImageURI(Uri.parse(mPhotoPath)); // 이미지 화면에 넣기.
                    File file = new File(mPhotoPath);

                    fileSize = (int) file.length();
                    String[] extension = mPhotoPath.split("[.]");
                    exName = extension[extension.length-1];
                    //Util.d("파일 사이즈 : ", String.valueOf(file.length()));
                    //Util.d("파일 경로 : ", mPhotoPath);
                    //Util.d("파일 확장자 : ", exName);
                   // requestUpdatePhoto(fileSize, exName); // 서버에 올리기.
                }

                break;

        }

        super.onActivityResult(requestCode, resultCode, data);
    }









    // 권한체크
    public void startAfterAuthorityCheck() {

        try {
            // 버전코드 확인
            PackageInfo i = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            currentVersionCode = i.versionCode;
            Log.v("currentVersionCode : ", String.valueOf(currentVersionCode));

        } catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // 퍼미션이 없으면 요청
        if (!hasPermissionsGranted(PERMISSIONS)) {
            requestEachPermissions(PERMISSIONS, 10002); // 퍼미션 요청
            return;
        }

    }



    // 퍼미션 요청
    @TargetApi(23) // 23 이상 버전에선 해당 메소드를 사용함을 명시(런타임 퍼미션은 23 이후 생김)
    private void requestEachPermissions(String[] permissions, int requestCode) {
        requestPermissions(permissions, requestCode);

    }



    //requestEachPermissions 의 콜백.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 10002){
            if(permissions!= null && grantResults != null){
                // 전체 권한
                for(int i=0; i<permissions.length; i++){
                    Log.v("퍼미션", "permission : " + permissions[i] + " result : " + grantResults[i]);
                    if (PackageManager.PERMISSION_DENIED == grantResults[i]) {  // 권한을 하나라도 거부한다면

                        // 하고픈거 하렴


                        break;
                    }
                }


            }
        }


    }




    // 앱 실행 시 퍼미션 체크 여부.
    private boolean hasPermissionsGranted(String[] permissions) {
        // 타켓 sdk.
        int targetSdkVersion = 0;
        try {
            final PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }



        for (String permission : permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { /// 마시멜로우(23, 안드로이드 6.0 : 런타임과 context).

                if (targetSdkVersion >= Build.VERSION_CODES.M) {
                    // targetSdkVersion >= Android M, we can
                    Log.v("Permissions", permission + "  " + ContextCompat.checkSelfPermission(this, permission)); // 허용 0, 비혀옹 -1
                    if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                        return false; // 요청한 퍼미션중 거절된게 있다면 false 값을 토한다.
                    }
                } else {
                    // targetSdkVersion < Android M
                    Log.v("Permissions", permission + "  " + PermissionChecker.checkSelfPermission(this, permission));
                    if (PermissionChecker.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
