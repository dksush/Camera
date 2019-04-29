package com.example.camera;


import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
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


        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "y", 0, 3);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


    }


    public void dialog(View view){
        Log.v("눌린다","ㅇ");
        DialogTest.dialog(new DialogTest.ClickListener() {
            @Override
            public void hungry(Dialog dialog) {
                Toast.makeText(MainActivity.this, "졸려", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void sleep(Dialog dialog) {
                Toast.makeText(MainActivity.this, "배고파", Toast.LENGTH_SHORT).show();

            }
        }).show(getSupportFragmentManager(),"ff");
    }





    public void gallery(View view){
//        Intent intent = new Intent(MainActivity.this, Main2Activity.class);
  //      startActivity(intent);

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
       // dialog.dismiss();
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
                    Log.v("파일 사이즈 : ", String.valueOf(file.length()));
                    Log.v("파일 경로 : ", mPhotoPath);
                    Log.v("파일 확장자 : ", exName);
                    requestUpdatePhoto(fileSize, exName); // 서버에 올리기.
                }

                break;

        }
        //그런데 JPEG로 변환 해야할 의무사항이 있나요? 그냥 PNG로 압축해도 되지 않나요>?
        //                JEPG 로 받아와서 PNG로 변환해서 보여주거나.  JPEG로 올려서 PNG로 받아와도 되는데. (단 후자는 서버에서 변환 해줘야함)
        //        전자는 클라이언트에서 변환이 필요하고. 메모리관리를 잘해야해요.

        super.onActivityResult(requestCode, resultCode, data);
    }





    public void requestUpdatePhoto(final int fileSize, String exName){
        /*
        Bitmap -> byte :  비트맵 객체(이미지)를 디비에 저장하는 방법 중 하나.
        비트맵 >
        - 압축되지 않은 이미지 전체 데이터를 저장 및 관리하는 클래스
        - Png, jpeg 는 원본이미지(비트맵)을 압축(인코딩) 하여 저장하는 것.
        - Png,jpeg  파일을 압축해제(디코딩).
        */

        Bitmap bitmap = BitmapFactory.decodeFile(mPhotoPath);// 경로를 입력하면, 로컬에 있는 이미지를 비트맵 형태로 읽어온다.
        ByteArrayOutputStream stream = new ByteArrayOutputStream(); // 비트맵을 바이트로 바꿔 내보내기 위한 스트림 생성.
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, stream); // jpeg로 손실압축하고(압축률이 좋고 속도가 빠르다) 그걸 stream 객체에 넣는다. 서버에서 png로 전환해서 클라에 쏴줄 수 있다.
        byte[] bytes = stream.toByteArray(); // stream 을 바이트배열로 전환. 이걸 서버로 전송.




        // 레트로핏 코드 생략.

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
    }//  device file explorer 에서 원하는 기기 선택후, 저장소(storeage) 에서 self - primary - apk 에 넣으면 된다.
    // 베이스 액티비티, 엑소 플레이어, 리사이클러뷰 풋터 해더,



}
