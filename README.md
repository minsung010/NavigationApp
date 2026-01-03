# 🗺️ 긴급구조 길라잡이 (일반 운전자용 내비게이션) - Capstone Design

**"골든타임 확보를 위한 실시간 긴급차량 접근 알림 및 내비게이션 시스템"**

본 프로젝트는 긴급 차량(구급차)의 실시간 위치를 수신하여, 운전자에게 접근 알림을 제공하고 길 터주기를 유도하는 스마트 내비게이션 앱입니다. 한국 내 지도 규제를 극복하기 위해 OSRM 엔진을 독자적으로 탑재했습니다.

---

## 📱 주요 기능 (Key Features)

### 1. 🛡️ 실시간 긴급차량 접근 알림
- **거리 감지**: Firebase 리얼타임 데이터베이스를 통해 출동 중인 구급차의 좌표를 실시간으로 구독합니다.
- **알림 트리거**: 내 차 반경 **500m 이내**에 구급차가 진입하면 즉시 경고창(CardView)과 음성 안내(TTS)가 실행됩니다.
- **UI UX**: "좌우로 피하세요"와 같은 직관적인 안내 메시지를 표시하여 당황하지 않고 대처할 수 있도록 설계했습니다.

### 2. 🛣️ 한국형 경로 탐색 (OSRM 도입)
- **OSRM 엔진**: 구글 지도 API의 국내 경로 탐색 제한(`ZERO_RESULTS`) 문제를 해결하기 위해, 오픈소스 경로 엔진인 **OSRM (Open Source Routing Machine)**을 도입했습니다.
- **경로 그리기**: 서버에서 받은 경로 데이터(Geometry)를 디코딩하여 지도 위에 정확한 파란색 주행 유도선을 그립니다.
- **실시간 안내**: 남은 거리, 소요 시간, 턴-바이-턴(Turn-by-Turn) 정보를 실시간으로 갱신합니다.

---

## 🛠️ 기술 스택 (Tech Stack)

| 카테고리 | 사용 기술 |
|:---:|:---|
| **개발 언어** | Java 17, Android SDK (API 34) |
| **지도 엔진** | Google Maps SDK for Android |
| **경로 탐색** | **OSRM (Open Source Routing Machine)** |
| **백엔드 (DB)** | Firebase Realtime Database (NoSQL) |
| **네트워크** | Retrofit2, OkHttp, GSON |
| **위치 정보** | Google Fused Location Provider |

---

## 📸 시연 화면 (Screenshots)

> *이곳에 실제 앱 구동 화면 스크린샷을 첨부해주세요.*

---

## 🚀 설치 및 실행 방법 (How to Build)

1. **저장소 클론 (Clone Repository)**
   ```bash
   git clone -b main https://github.com/minsung010/NavigationApp.git
   ```
2. **안드로이드 스튜디오 열기**
   - 다운로드한 `navi` 폴더를 Android Project로 엽니다.
3. **API 키 설정**
   - `AndroidManifest.xml`에 본인의 Google Maps API 키가 설정되어 있는지 확인합니다.
4. **실행 (Run)**
   - GPS가 작동하는 실물 안드로이드 기기 또는 위치 시뮬레이터가 있는 에뮬레이터에서 실행하세요.

---

## 👨‍💻 팀원 및 역할

- **Android Front (일반 내비게이션)**: OSRM 기반 경로 탐색 구현, 알림 UI 로직 개발, 구글 지도 연동
