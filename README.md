# 🚑 Ambulance App for Emergency Yielding (Capstone Design)

**"골든타임 확보를 위한 실시간 긴급차량 접근 알림 및 내비게이션 시스템" - 구급차(긴급차량)용 앱**

본 프로젝트의 구급대원용 애플리케이션으로, 출동 시 실시간 위치 정보(GPS)를 서버로 전송하여 전방의 일반 운전자들에게 접근 사실을 알리는 역할을 수행합니다.

---

## 📱 Key Features (핵심 기능)

### 1. 📡 실시간 위치 송신 (Real-time GPS Tracking)
- **출동 모드**: 긴급 출동 시 스위치를 켜면 **1초 단위**로 GPS 위치 추적을 시작합니다.
- **Firebase Sync**: 수집된 위도/경도(Lat/Lng) 데이터를 **Firebase Realtime Database**의 `ambulance/` 노드에 실시간으로 업로드합니다.
- **Low Latency**: 운전자 앱에서 즉각적인 반응이 가능하도록 초저지연 데이터 동기화를 구현했습니다.

### 2. 🚦 출동 상태 관리 (Dispatch Control)
- **상태 제어**: 출동 중(`isDispatching: true`)일 때만 위치를 공유하여 불필요한 데이터 전송 및 사생활 침해를 방지합니다.
- **백그라운드 동작**: 화면이 꺼지거나 다른 앱 사용 중에도 안정적으로 위치를 전송하도록 Service를 최적화했습니다.

---

## 🛠️ Tech Stack (기술 스택)

| Category | Technology |
|:---:|:---|
| **Language** | Java 17, Android SDK (API 34) |
| **Location** | Google Play Services Location (FusedLocationProviderClient) |
| **Backend** | Firebase Realtime Database (Pub/Sub Pattern) |
| **Networking** | Fireabse SDK |
| **Permissions** | ACCESS_FINE_LOCATION, FOREGROUND_SERVICE |

---

## 🚀 How to Build

1. **Clone Repository (Branch: AmbulanceApp)**
   ```bash
   git clone -b AmbulanceApp https://github.com/minsung010/NavigationApp.git
   ```
2. **Open in Android Studio**
   - Open current directory as an Android Project.
3. **Firebase Setup**
   - Ensure `google-services.json` is present in the app directory.
4. **Run**
   - Install on a physical Android device (Recommended for accurate GPS testing).
   - Click "출동 시작" (Start Dispatch) to begin broadcasting location.

---

## 👨‍💻 Team & Role

- **Android Front**: 구급차용 앱 개발, GPS 로직 및 Firebase 송신 모듈 구현
- **System Logic**: 위치 기반 이벤트 트리거(Event Trigger) 설계
