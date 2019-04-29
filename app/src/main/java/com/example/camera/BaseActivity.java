package com.example.camera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;


// BaseActivity 이놈을 상속하는 액티비티들은 등장시 애니메이션이 적용된다.
public class BaseActivity extends AppCompatActivity {


    int activityCount = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // 엑티비티 등장 방향정하기.
        if(savedInstanceState == null){ // 해당 액티비티에 처음 접근했을때.
            // savedInstanceState 는 별도의 값을 저장하지 않더라도, onPuase 나 onStop 에 호출되어 null 값이 아니게된다.
            // 앱 종료나, onDestroy 시에는 null 값이 된다.


            // 등장시 오른쪽에서 왼쪽으로 화면이 나온다.
            this.overridePendingTransition(R.anim.anim_slide_in_left,  // 새롭게 나타날 액티비티.
                    R.anim.anim_fade_out); // 기존의 액티비티
        }else{
            activityCount = 2;
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        // 굳이 int 를 사용하는 이유는  savedInstanceState 가 여기선 선언이 안된다.
        // 해당 액티비티가 onstop 까지 갔다가 다시 돌아온다면 onC 가 아니라 onS 로 오기때문.
        if(activityCount>1){
            // 위에꺼랑 반대로.
        }else if (activityCount == 1){
            activityCount++;
        }

    }
}
