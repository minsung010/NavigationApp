# 구급차 출동 및 추적 시스템 업데이트 완료

요청하신 구급차 출동 시 위치 추적 기능과 알림 시스템 구현을 완료했습니다.

## 주요 변경 사항

### 1. 실시간 위치 전송 최적화 (운전자용)
- **파일**: [MainActivity.java](file:///c:/Users/minsung/Desktop/caps/realnavimap-realtest/realnavimap-realtest/app/src/main/java/com/cookandroid/emergency2/MainActivity.java)
- **변경점**: 기존에 위치 데이터가 계속 쌓이기만 하던 문제를 해결했습니다. 이제 `ambulance/ambulance_1`이라는 고정된 경로에 **최신 위치만 업데이트**됩니다.
- **기능**: '출동 시작' 버튼을 누르면 실시간 위치 전송이 시작되고, `isDispatching` 상태가 `true`로 설정됩니다.

### 2. 실시간 구급차 추적 (사용자용)
- **파일**: [LocationActivity.java](file:///c:/Users/minsung/Desktop/caps/realnavimap-realtest/realnavimap-realtest/app/src/main/java/com/cookandroid/emergency2/LocationActivity.java)
- **변경점**: 지도를 열었을 때 구급차가 출동 중이라면 **지도에 구급차 아이콘(마커)이 실시간으로 표시**되고 움직입니다.

### 3. 알림 시스템 (Notification)
- **파일**: [MyFirebaseMessagingService.java](file:///c:/Users/minsung/Desktop/caps/realnavimap-realtest/realnavimap-realtest/app/src/main/java/com/cookandroid/emergency2/MyFirebaseMessagingService.java)
- **추가**: Firebase Cloud Messaging(FCM)을 통해 알림을 받을 수 있는 서비스를 추가했습니다.
- **주의**: 실제로 기기에 푸시 알림을 보내려면 Firebase Console에서 메시지를 보내거나, 서버 측 로직(Cloud Functions 등)을 추가해야 합니다. 현재 코드는 **알림을 받을 준비(수신)** 가 된 상태입니다.

---

## 테스트 방법

### 1단계: 운전자 (데이터 전송)
1. 앱을 실행합니다 (`MainActivity`).
2. **"출동 시작"** 버튼을 누릅니다.
3. Firebase Console의 Realtime Database에 접속하여 `ambulance/ambulance_1` 경로에 데이터(`latitude`, `longitude`, `isDispatching: true`)가 갱신되는지 확인합니다.

### 2단계: 사용자 (위치 확인)
1. 앱에서 **"위치 보기"** 버튼을 누르거나 `LocationActivity`를 실행합니다.
2. 지도가 로드되면, 운전자가 전송 중인 위치에 **"구급차 출동 중!"** 마커가 나타나는지 확인합니다.
3. 운전자가 위치를 바꾸면 마커가 따라 움직여야 합니다.

---

## ⚠️ 추가 설정 필요 사항 (필수)
알림 기능이 정상 작동하려면 Firebase Console 설정이 필요합니다:
1. **Firebase Console** -> **Project Settings** -> **Cloud Messaging** 탭으로 이동.
2. 'Cloud Messaging API (Legacy)'가 활성화되어 있는지 확인하거나, 새 V1 API를 사용하도록 서버(백엔드)를 설정해야 합니다.
3. 현재 앱 코드는 **알림을 받을 준비**만 되어 있으며, "출동 버튼을 눌렀을 때 자동으로 알림을 전송하는 로직"은 **Firebase Functions(백엔드)** 영역입니다. (현재 앱 내에서 자체적으로 자신에게 알림을 띄우는 것은 가능하지만, 다른 사용자에게 보내려면 서버가 필요합니다.)
