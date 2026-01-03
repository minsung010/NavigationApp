# 구급차 추적 및 알림 시스템 구현 계획

## 목표 설명
현재의 프로토타입을 실제 작동하는 구급차 출동 시스템으로 전환합니다.
1.  **운전자 측 (`MainActivity`)**: 위치 기록이 계속 쌓이는 문제를 해결하고, *현재 위치*만 실시간으로 업데이트하도록 수정합니다.
2.  **사용자 측 (`LocationActivity`)**: 실시간 모니터링을 구현합니다. 사용자는 지도 위에서 구급차가 움직이는 것을 실시간으로 볼 수 있습니다.
3.  **알림**: 출동 시작 시 사용자에게 알림을 보내는 시스템을 추가합니다.

## 사용자 검토 필요 사항
> [!IMPORTANT]
> **Firebase 설정**: 알림(FCM)이 작동하려면 Firebase 콘솔에서 **Cloud Messaging**을 활성화해야 합니다. 코드는 제가 추가하겠지만, 이전에 메시징을 설정하지 않았다면 새 `google-services.json`을 다운로드해야 할 수도 있습니다.

> [!WARNING]
> **DB 구조 변경**: 데이터베이스 구조를 `locations/{random_id}`에서 `ambulance/{ambulance_id}`로 변경합니다. 이는 과거 데이터와 호환되지 않는 변경이지만, 실시간 추적을 위해 필수적입니다.

## 변경 제안 사항

### 앱 설정 (모듈 레벨)

#### [수정] [build.gradle.kts](file:///c:/Users/minsung/Desktop/caps/realnavimap-realtest/realnavimap-realtest/app/build.gradle.kts)
- 알림 기능을 위해 `implementation("com.google.firebase:firebase-messaging")` 의존성을 추가합니다.

### 운전자 로직 (위치 전송)

#### [수정] [MainActivity.java](file:///c:/Users/minsung/Desktop/caps/realnavimap-realtest/realnavimap-realtest/app/src/main/java/com/cookandroid/emergency2/MainActivity.java)
- **수정**: `databaseReference.push()`를 `.child("ambulance_1")`(또는 고유 ID)로 변경하여 무작위 ID 생성을 막습니다.
- **추가**: `isDispatching` (출동 중 여부) 불리언 플래그를 함께 전송합니다.
- **추가**: 알림/UI 업데이트를 트리거할 수 있는 "출동 시작" 신호를 보내는 로직을 추가합니다.

### 사용자 로직 (위치 수신)

#### [수정] [LocationActivity.java](file:///c:/Users/minsung/Desktop/caps/realnavimap-realtest/realnavimap-realtest/app/src/main/java/com/cookandroid/emergency2/LocationActivity.java)
- **신규**: Firebase 실시간 데이터베이스 연결을 초기화합니다.
- **신규**: `ambulance/{ambulance_id}` 경로에 `ValueEventListener`를 설치합니다.
- **UI**: 데이터가 변경될 때마다:
    - 이전 마커를 지웁니다.
    - 새로운 위치(위도, 경도)에 마커를 추가합니다.
    - (선택 사항) 카메라를 구급차 위치로 이동시킵니다.
    - "구급차가 출동 중입니다!"라는 텍스트 경보를 표시합니다.

### 알림 서비스

#### [신규] [MyFirebaseMessagingService.java](file:///c:/Users/minsung/Desktop/caps/realnavimap-realtest/realnavimap-realtest/app/src/main/java/com/cookandroid/emergency2/MyFirebaseMessagingService.java)
- 들어오는 FCM 메시지를 처리하고 시스템 알림창(Notification)을 띄우는 역할을 합니다.

#### [수정] [AndroidManifest.xml](file:///c:/Users/minsung/Desktop/caps/realnavimap-realtest/realnavimap-realtest/app/src/main/AndroidManifest.xml)
- `MyFirebaseMessagingService` 서비스를 등록합니다.
- 안드로이드 13 이상을 위해 `POST_NOTIFICATIONS` 권한을 요청합니다.
