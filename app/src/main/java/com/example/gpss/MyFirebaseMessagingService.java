package com.example.gpss;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String CHANNEL_ID = "fcm_default_channel";

    // 새로운 FCM 토큰이 생성될 때 호출됩니다.
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM Registration Token: " + token);

        // 서버에 새 토큰을 전송하거나 로컬에 저장하는 로직을 추가하세요.
        sendRegistrationToServer(token);
    }

    // 받은 메시지를 처리하는 부분
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        // 알림 제목과 내용 설정 (알림 메시지가 있을 경우 처리)
        String title = "FCM 메시지";  // 기본 제목
        String message = "기본 내용";  // 기본 내용

        // 알림 메시지 처리
        if (remoteMessage.getNotification() != null) {
            message = remoteMessage.getNotification().getBody();  // 알림 본문
        }

        // 데이터 메시지 처리 (알림 외에 다른 데이터가 있을 경우)
        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "Data Message: " + remoteMessage.getData());
        }

        // FirebaseMessagingService에서 권한 요청 알림
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent intent = new Intent("REQUEST_NOTIFICATION_PERMISSION");
            sendBroadcast(intent);  // 권한 요청 메시지 전송
        }

        // 알림 채널 설정 (Android 8.0 이상에서 필요)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Default Channel";
            String description = "Default channel for FCM notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;  // 높은 중요도
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // 알림을 화면에 표시
        sendNotification(message);
    }

    // 알림을 표시하는 메서드
    private void sendNotification(String messageBody) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)  // 알림 아이콘 설정
                .setContentTitle("\uD83D\uDEA8구급차 접근 중\uD83D\uDEA8")  // 알림 제목
                .setContentText(messageBody)  // 알림 내용
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // 높은 우선순위 설정
                .setAutoCancel(true);  // 알림 클릭 시 자동 제거

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // 고유 알림 ID 설정
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    // 서버로 토큰을 보내는 로직 (옵션)
    private void sendRegistrationToServer(String token) {
        // 서버에 토큰을 저장하는 로직을 작성하세요.
        // 예: Retrofit, Firebase Realtime Database 등
    }
}
