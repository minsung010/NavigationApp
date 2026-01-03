# 🚑 긴급구조 길라잡이 (구급차 전용 앱) - Capstone Design

**"골든타임 확보를 위한 실시간 긴급차량 접근 알림 및 내비게이션 시스템" - 구급대원용**

본 프로젝트의 구급대원용 애플리케이션으로, **긴급 출동 시 실시간 위치 정보(GPS)**를 서버로 전송하여 전방의 일반 운전자들에게 접근 사실을 알리는 핵심 역할을 수행합니다.

---

## 📱 주요 기능 (Key Features)

### 1. 📡 실시간 위치 송신 (GPS Sender)
- **출동 모드**: 긴급 출동 시 앱 중앙의 '출동 시작' 스위치를 켜면 **1초 단위**로 정밀 GPS 위치 추적을 시작합니다.
- **클라우드 동기화**: 수집된 위도/경도(Lat/Lng) 데이터를 **Firebase Realtime Database**의 `ambulance/` 노드에 즉시 소켓 통신으로 전송합니다.
- **초저지연 전송**: 운전자 앱에서 구급차 위치를 딜레이 없이 파악할 수 있도록 최적화된 Pub/Sub 패턴을 사용했습니다.

### 2. 🚦 출동 상태 관리
- **상태 제어**: 실제 출동 상황(`isDispatching: true`)일 때만 위치를 공유하여, 평상시의 불필요한 데이터 전송과 사생활 침해를 방지했습니다.
- **백그라운드 지원**: 화면이 꺼지거나 다른 앱(지도 등)을 사용하는 중에도 끊김 없이 위치를 전송합니다.

---

## 🛠️ 기술 스택 (Tech Stack)

| 카테고리 | 사용 기술 |
|:---:|:---|
| **개발 언어** | Java 17, Android SDK (API 34) |
| **위치 서비스** | Google Play Services Location (FusedProvider) |
| **클라우드** | Firebase Realtime Database |
| **통신** | Firebase SDK (Socket 기반 실시간 통신) |
| **권한** | 정밀 위치 권한(FINE_LOCATION), 포그라운드 서비스 |

---

## 📸 시연 화면 (Screenshots)

> *이곳에 구급차 앱 구동 화면 스크린샷을 첨부해주세요.*

---

## 🚀 설치 및 실행 방법 (How to Build)

1. **저장소 클론 (Clone Repository)**
   ```bash
   git clone -b AmbulanceApp https://github.com/minsung010/NavigationApp.git
   ```
2. **안드로이드 스튜디오 열기**
   - 다운로드한 `car` 폴더를 Android Project로 엽니다.
3. **파이어베이스 설정**
   - `google-services.json` 파일이 프로젝트 내에 있는지 확인합니다.
4. **실행 (Run)**
   - GPS 테스트를 위해 실제 안드로이드 기기에서 실행하는 것을 권장합니다.
   - '출동 시작' 버튼을 누르면 위치 전송이 시작됩니다.

---

## 👨‍💻 팀원 및 역할

- **Android Front (구급차)**: GPS 실시간 트래킹 로직 구현, Firebase 데이터 송신 모가 개발
