package com.cookandroid.emergency2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final long LOCATION_UPDATE_INTERVAL = 1000; // 1초
    private static final long LOCATION_FASTEST_INTERVAL = 500; // 0.5초

    private TextView feedbackText;
    private TextView timerText;
    private Button startButton, stopButton, locationButton;
    private Handler handler = new Handler();
    private long startTime = 0L;
    private long elapsedMillis = 0L; // 경과 시간 저장 변수
    private DecimalFormat decimalFormat = new DecimalFormat("00.00");

    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference databaseReference;
    private boolean isSendingLocation = false;
    private Runnable locationRunnable;

    // 타이머 Runnable 정의
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            elapsedMillis = System.currentTimeMillis() - startTime;
            double seconds = (elapsedMillis % 60000) / 1000.0; // 초를 소수점 포함 2자리까지
            int minutes = (int) (elapsedMillis / 60000); // 분 단위 계산

            // "소요시간:" 문구 추가
            timerText.setText(String.format("소요시간: %02d:%s", minutes, decimalFormat.format(seconds)));
            handler.postDelayed(this, 100); // 0.1초마다 업데이트
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // View 초기화
        feedbackText = findViewById(R.id.feedbackText);
        timerText = findViewById(R.id.timerText);
        startButton = findViewById(R.id.button4);
        stopButton = findViewById(R.id.button5);
        locationButton = findViewById(R.id.locationButton);

        // Firebase 초기화
        databaseReference = FirebaseDatabase.getInstance().getReference("ambulance");

        // Fused Location Provider 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 출동 버튼 클릭 리스너
        startButton.setOnClickListener(v -> {
            feedbackText.setText("현재 출동 버튼 작동 중입니다.");
            startTimer(); // 출동 시 타이머 시작 및 초기화
            startSendingLocation(); // 위치 전송 시작
            // 출동 상태 업데이트
            updateDispatchStatus(true);
        });

        // 종료 버튼 클릭 리스너
        stopButton.setOnClickListener(v -> {
            feedbackText.setText("종료 상태입니다.");
            stopTimer(); // 종료 시 타이머 멈춤, 시간 유지
            stopSendingLocation(); // 위치 전송 중단
            // 출동 상태 해제
            updateDispatchStatus(false);
        });

        // 현위치 버튼 클릭 리스너
        locationButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LocationActivity.class);
            startActivity(intent); // LocationActivity로 전환
        });

        // 위치 권한 확인 및 요청
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // 타이머 시작 메서드
    private void startTimer() {
        startTime = System.currentTimeMillis(); // 타이머 초기화 및 시작
        handler.postDelayed(timerRunnable, 0);
    }

    // 타이머 멈춤 메서드
    private void stopTimer() {
        handler.removeCallbacks(timerRunnable);
        // 타이머 멈춤, 텍스트는 종료 시점에서 멈춘 상태로 유지
    }

    // 위치 전송 시작
    private void startSendingLocation() {
        isSendingLocation = true;
        locationRunnable = new Runnable() {
            @Override
            public void run() {
                if (isSendingLocation) {
                    getLocation();
                    handler.postDelayed(this, LOCATION_UPDATE_INTERVAL); // 1초마다 호출
                }
            }
        };
        handler.post(locationRunnable);
    }

    // 위치 전송 중단
    private void stopSendingLocation() {
        isSendingLocation = false;
        handler.removeCallbacks(locationRunnable);
    }

    // 출동 상태 업데이트
    private void updateDispatchStatus(boolean isDispatching) {
        // ambulance_1 노드에 상태 업데이트
        databaseReference.child("ambulance_1").child("isDispatching").setValue(isDispatching);
    }

    // 현재 위치 가져오기
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // 고정된 ID (ambulance_1) 사용
                            DatabaseReference ambulanceRef = databaseReference.child("ambulance_1");
                            ambulanceRef.child("latitude").setValue(location.getLatitude());
                            ambulanceRef.child("longitude").setValue(location.getLongitude());
                            ambulanceRef.child("timestamp").setValue(System.currentTimeMillis());
                            ambulanceRef.child("isDispatching").setValue(true); // 위치 전송 중에는 항상 true

                            // 위치 전송 로그
                            feedbackText.setText("위치 전송 중: " + location.getLatitude() + ", " + location.getLongitude());
                        }
                    });
        }
    }

    // 위치 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용된 경우, 위치 접근 로직 추가 가능
            } else {
                // 권한이 거부된 경우 처리
                feedbackText.setText("위치 권한이 필요합니다.");
            }
        }
    }
}
