# 🗺️ Navigation App for Emergency Yielding (Capstone Design)

**"골든타임 확보를 위한 실시간 긴급차량 접근 알림 및 내비게이션 시스템" - 일반 운전자용 앱**

본 프로젝트는 긴급 차량(구급차)의 실시간 위치를 수신하여, 운전자에게 접근 알림을 제공하고 길 터주기를 유도하는 스마트 내비게이션 앱입니다. 한국 내 지도 규제를 극복하기 위해 OSRM 엔진을 탑재했습니다.

---

## 📱 Key Features (핵심 기능)

### 1. 🛡️ 실시간 긴급차량 접근 알림 (Emergency Alert)
- **거리 감지**: Firebase Realtime Database를 통해 구급차의 실시간 좌표를 구독합니다.
- **알림 트리거**: 본 차량 반경 **500m 이내**에 구급차가 진입하면 즉시 경고 UI(CardView)와 음성 안내(TTS)를 실행합니다.
- **UI UX**: 운전 중 즉각적인 인지가 가능하도록 시인성 높은 경고 디자인을 적용했습니다.

### 2. 🛣️ 한국형 경로 탐색 (Dynamic Routing via OSRM)
- **OSRM Integration**: Google Maps API의 국내 경로 탐색 제한(`ZERO_RESULTS`)을 해결하고자 **OSRM (Open Source Routing Machine)** 오픈소스 엔진을 도입했습니다.
- **Polyline Drawing**: OSRM API로부터 받은 경로 형상 정보(Geometry)를 디코딩하여 Google Maps 위에 정확한 주행 경로를 시각화합니다.
- **내비게이션 UI**: 남은 거리, 소요 시간, 턴-바이-턴 아이콘 등 실제 내비게이션과 유사한 경험을 제공합니다.

---

## 🛠️ Tech Stack (기술 스택)

| Category | Technology |
|:---:|:---|
| **Language** | Java 17, Android SDK (API 34) |
| **Map Engine** | Google Maps SDK for Android |
| **Routing** | **OSRM (Open Source Routing Machine)** |
| **Backend** | Firebase Realtime Database (NoSQL) |
| **Networking** | Retrofit2, OkHttp, GSON |
| **Async** | Callbacks, Listeners |

---

## 📸 Screenshots (시연 화면)

> *실제 앱 구동 화면 스크린샷 위치예정*

---

## 🚀 How to Build

1. **Clone Repository**
   ```bash
   git clone -b main https://github.com/minsung010/NavigationApp.git
   ```
2. **Open in Android Studio**
   - Open current directory as an Android Project.
3. **API Key Setup**
   - Create `local.properties` or set environment variable for `MAPS_API_KEY`.
   - (Note: This project uses OSRM for routing, so no paid Directions API key is required, but Maps SDK key is needed.)
4. **Run**
   - Build and run on a physical device (GPS required) or Emulator with location simulation.

---

## 👨‍💻 Team & Role

- **Android Front**: 일반 운전자용 내비게이션 앱 개발, Google Maps & OSRM 연동
- **Project Structure**: Multi-repo strategy (Navigation / Ambulance)
