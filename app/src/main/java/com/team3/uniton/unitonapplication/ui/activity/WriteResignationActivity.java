package com.team3.uniton.unitonapplication.ui.activity;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.sdk.newtoneapi.SpeechRecognizeListener;
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient;
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager;
import com.team3.uniton.unitonapplication.App;
import com.team3.uniton.unitonapplication.R;
import com.team3.uniton.unitonapplication.model.Reason;
import com.team3.uniton.unitonapplication.model.Reasons;
import com.team3.uniton.unitonapplication.model.Status;
import com.team3.uniton.unitonapplication.ui.customView.DynamicSineWaveView;
import com.team3.uniton.unitonapplication.ui.customView.ReasonItemView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WriteResignationActivity extends AppCompatActivity {

    static final String TAG = WriteResignationActivity.class.getSimpleName();
    static final int REQUEST_CODE_AUDIO_AND_WRITE_EXTERNAL_STORAGE = 3;

    Handler mHandler = new Handler(Looper.getMainLooper());
    SpeechRecognizerClient mSpeachClient;

    DynamicSineWaveView mWave;

    ImageView mRecordButton;

    boolean isAnimating = false;
    boolean isKeyboardShowing = true;

    LinearLayout mContentsLayout;

    ImageView mBackButton;
    TextView mCompleteButton;

    ProgressBar mProgressBar;

    int mRid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_resignation);

        // statusbar 색 변경
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            int flags = window.getDecorView().getSystemUiVisibility();
            flags = flags ^ View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            window.getDecorView().setSystemUiVisibility(flags);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.white_four));
        }


        mProgressBar = findViewById(R.id.progressBar);
        mContentsLayout = findViewById(R.id.content_layout);
        mBackButton = findViewById(R.id.backButton);
        mCompleteButton = findViewById(R.id.completeButton);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mCompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                postReasons();
            }
        });

        mWave = findViewById(R.id.waveView);
        mWave.addWave(0.5f, 0.5f, 0, 0, 0); // Fist wave is for the shape of other waves.
        mWave.addWave(0.1f, 2f, 0.7f, getResources().getColor(R.color.bright_blue), 3);
        mWave.setBaseWaveAmplitudeScale(0);
        mWave.startAnimation();

        mRecordButton = findViewById(R.id.record_button);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAnimating) {
                    mWave.setBaseWaveAmplitudeScale(0);
                    mWave.startAnimation();
                    mSpeachClient.startRecording(true);
                } else {
                    mWave.setBaseWaveAmplitudeScale(0.5f);
                    mWave.startAnimation();
                    mSpeachClient.stopRecording();
                }
                isAnimating = !isAnimating;
            }
        });

        requestPermission();
        requestDatas();
    }


    void requestDatas() {

        SharedPreferences currentSP = getSharedPreferences("USER_MODEL", MODE_PRIVATE);
        String userID = currentSP.getString("USER_ID","");

        App.serverApi.getReasons(userID).enqueue(new Callback<Reasons>() {
            @Override
            public void onResponse(Call<Reasons> call, Response<Reasons> response) {

                mRid = response.body().getResignation_id();
                addViews(response.body());
            }

            @Override
            public void onFailure(Call<Reasons> call, Throwable t) {

            }
        });
    }

    void addViews(Reasons datas) {
        if (datas.getCurrent_reason_count() == 0) {
            ReasonItemView view = new ReasonItemView(this);
            view.setData(1, datas.getFirst_reason(), true);
            mContentsLayout.addView(view);
        }

        if (datas.getCurrent_reason_count() == 1) {
            ReasonItemView view = new ReasonItemView(this);
            view.setData(1, datas.getFirst_reason(), false);
            mContentsLayout.addView(view);

            ReasonItemView view1 = new ReasonItemView(this);
            view1.setData(2, datas.getSecond_reason(), true);
            mContentsLayout.addView(view1);

        }

        if (datas.getCurrent_reason_count() == 2) {

            ReasonItemView view = new ReasonItemView(this);
            view.setData(1, datas.getFirst_reason(), false);
            mContentsLayout.addView(view);
            ReasonItemView view1 = new ReasonItemView(this);
            view1.setData(2, datas.getSecond_reason(), false);
            mContentsLayout.addView(view1);
            ReasonItemView view2 = new ReasonItemView(this);
            view2.setData(3, datas.getThird_reason(), true);
            mContentsLayout.addView(view2);
        }

        if (datas.getCurrent_reason_count() == 3) {

            ReasonItemView view = new ReasonItemView(this);
            view.setData(1, datas.getFirst_reason(), false);
            mContentsLayout.addView(view);
            ReasonItemView view1 = new ReasonItemView(this);
            view1.setData(2, datas.getSecond_reason(), false);
            mContentsLayout.addView(view1);
            ReasonItemView view2 = new ReasonItemView(this);
            view2.setData(3, datas.getThird_reason(), false);
            mContentsLayout.addView(view2);
        }

    }

    void postReasons() {

        mProgressBar.setVisibility(View.VISIBLE);
        SharedPreferences currentSP = getSharedPreferences("USER_MODEL", MODE_PRIVATE);
        String userID = currentSP.getString("USER_ID","");

        ReasonItemView view = (ReasonItemView)mContentsLayout.getChildAt(mContentsLayout.getChildCount() - 1);
        Reason reason = new Reason();

        if (view.getText() != null) {
            reason.setReason(view.getText());
        }
        App.serverApi.postReasons(userID, reason).enqueue(new Callback<Status>() {
            @Override
            public void onResponse(Call<Status> call, Response<Status> response) {
                String result = response.body().status;
                if ("200".equals(result)) {
                    if (mContentsLayout.getChildCount() == 3) {
                        Intent intent = new Intent(WriteResignationActivity.this, ResignationActivity.class);
                        intent.putExtra("ID", mRid);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(WriteResignationActivity.this, "등록 성공!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<Status> call, Throwable t) {
                mProgressBar.setVisibility(View.INVISIBLE);

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mWave.stopAnimation();
        // API를 더이상 사용하지 않을 때 finalizeLibrary()를 호출한다.
        SpeechRecognizerManager.getInstance().finalizeLibrary();
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null){
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null){
            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_AUDIO_AND_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    startUsingSpeechSDK();
                } else {

                }
                break;
            default:
                break;
        }
    }


    private void requestPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)
            && ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                requestPermission();

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_AUDIO_AND_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            startUsingSpeechSDK();
        }

    }

    private void startUsingSpeechSDK() {
        // library를 초기화 합니다.
        // API를 사용할 시점이 되었을 때 initializeLibrary(Context)를 호출한다.
        // 사용을 마치면 finalizeLibrary()를 호출해야 한다.
        SpeechRecognizerManager.getInstance().initializeLibrary(this);

        // 클라이언트 생성
        SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder()
                .setServiceType(SpeechRecognizerClient.SERVICE_TYPE_DICTATION);

        mSpeachClient = builder.build();

        // 음성 인식 중에 발생하는 다양한 이벤트 처리
        mSpeachClient.setSpeechRecognizeListener(new SpeechRecognizeListener() {
            @Override
            public void onReady(){
            }

            @Override
            public void onBeginningOfSpeech(){
            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(final int errorCode, final String errorMsg) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "onError \n errorCode:"  + errorCode + "\n errorMsg:" + errorMsg);

                    }
                });
            }

            /**
             완전히 음성이 종료되기 이전에 현재까지 인식된 음성데이터 문자열을 알려줍니다.
             이 데이터는 서버에 질의해 데이터를 보정하는 과정을 거치지 않으므로, 다소 부정확할 수 있습니다.
             중간 인식 결과에 대한 결과가 발생할 때마다 호출되므로 여러번 호출될 수 있습니다.
             */
            @Override
            public void onPartialResult(String partialResult) {
            }

            /**
             onResults는 음성 입력이 종료된 것으로 판단하거나 stopRecording()을 호출한 후에 서버에 질의하는 과정까지 마치고 나면 호출됩니다.
             인식된 문자열은 신뢰도가 높은 값부터 순서대로 Bundle의 SpeechRecognizerClient.KEY_RECOGNITION_RESULTS 값을 통해 ArrayList로 얻을 수 있습니다.
             신뢰도는 Bundle의 SpeechRecognizerClient.KEY_CONFIDENCE_VALUES 값을 통해 ArrayList로 얻을 수 있으며 높은 값부터 순서대로 입니다.
             신뢰도값은 항상 0보다 크거나 같은 정수이며, 문자열 목록과 같은 개수입니다.
             */
            @Override
            public void onResults(final Bundle results) {

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<String> texts =  results.getStringArrayList(SpeechRecognizerClient.KEY_RECOGNITION_RESULTS);
                        ArrayList<Integer> confs =   results.getIntegerArrayList(SpeechRecognizerClient.KEY_CONFIDENCE_VALUES);

                        Log.e(TAG, "onResults: " + texts.get(0));
                        Log.e(TAG, "size = " + texts.size());
                        Toast.makeText(WriteResignationActivity.this, texts.get(0), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAudioLevel(float audioLevel) {

            }

            @Override
            public void onFinished() {

            }
        });
    }
}
