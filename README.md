# 💳 SmsForward — 카드문자 자동 전달

카드 결제 SMS를 수신하면 지정된 번호(예: 와이프)로 자동 전달하는 안드로이드 앱.

> 외부 라이브러리·서버 없이 **Android 기본 API만** 사용. SMS는 디바이스에서 디바이스로 직접 전송됩니다.

---

## ✨ 주요 기능

- 📨 **자동 감지**: SMS 수신 시 BroadcastReceiver가 즉시 처리
- 🔍 **키워드 필터링**: 카드사명·승인·결제 등 키워드 매칭으로 카드 문자만 선별
- 📤 **자동 전달**: 매칭된 문자를 지정 번호로 SMS 재전송
- 🔄 **부팅 후 자동 시작**: 폰 재시작 시 서비스 자동 복구
- 🔔 **Foreground Service**: 상시 알림 표시로 백그라운드 종료 방지
- ⚙️ **간단한 UI**: 전화번호·키워드 설정, 서비스 ON/OFF 토글

---

## 🛠 기술 스택

| 항목 | 내용 |
|------|------|
| Language | Kotlin |
| Min SDK | 26 (Android 8.0 Oreo) |
| Target SDK | 34 (Android 14) |
| AGP / Gradle | 8.2.2 / 8.4 |
| 의존성 | AndroidX, Material Components, CardView |
| 외부 서버 | ❌ 없음 (P2P SMS) |

---

## 🚀 빌드 & 설치

상세 단계는 별도 가이드 참조:

📖 **[Android Studio 설치 ~ 실기기 배포 가이드 →](docs/setup-guide.md)**

### 요약 (이미 환경 구축된 경우)

```bash
git clone https://github.com/deejk/SmsForward.git
cd SmsForward
# Android Studio에서 폴더 열기 → Gradle Sync → ▶ Run
```

> ⚠️ Gradle wrapper jar(`gradle/wrapper/gradle-wrapper.jar`)는 저장소에 포함되어 있지 않습니다. Android Studio가 프로젝트 열 때 자동 생성하거나, `gradle wrapper --gradle-version 8.4` 명령으로 직접 생성하세요.

---

## 📱 사용법

1. 앱 실행 → SMS 권한 3종(받기/읽기/보내기) 허용
2. **전달할 번호** 입력 (예: `010-0000-0000`)
3. **필터 키워드** 확인/수정 (기본값: 주요 카드사·승인·결제·카드)
4. **💾 설정 저장** → **🔋 배터리 최적화 제외 설정** → **▶ 서비스 시작**
5. 상단 알림에 `💳 카드문자 전달 중` 표시되면 정상 작동

이후 카드 결제 SMS 수신 시 즉시 지정 번호로 전달됩니다.

---

## 📂 프로젝트 구조

```
SmsForward/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/smsforward/
│       │   ├── MainActivity.kt           # 설정 UI
│       │   ├── PreferenceManager.kt      # SharedPreferences 래퍼
│       │   ├── SmsReceiver.kt            # SMS 수신 + 필터링 + 전달
│       │   ├── SmsSender.kt              # SmsManager 호출
│       │   ├── SmsForegroundService.kt   # 상시 알림 서비스
│       │   └── BootReceiver.kt           # 부팅 후 서비스 재시작
│       └── res/
│           ├── layout/activity_main.xml
│           ├── values/{themes,colors,strings}.xml
│           ├── drawable/ic_launcher_*.xml
│           └── mipmap-anydpi-v26/ic_launcher*.xml
├── docs/
│   └── setup-guide.md                    # Android Studio 빌드 가이드
├── gradle/wrapper/gradle-wrapper.properties
├── build.gradle                          # 루트 프로젝트
├── settings.gradle
└── gradle.properties
```

---

## ⚠️ 알려진 한계 & 주의사항

- **요금 발생**: 전달되는 SMS마다 통신사 요금 발생 (건당 약 22~33원)
- **민감 정보 전송**: 카드 결제 내역이 수신자에게 노출 — 가족 등 신뢰 관계 전용
- **듀얼 SIM 미지원**: 기본 SIM으로만 발신 (선택 UI 없음)
- **재시도 없음**: 전달 실패 시 로그만 남고 자동 재시도하지 않음
- **에뮬레이터 불가**: SMS 송수신 테스트는 실기기 필요
- **제조사별 백그라운드 정책**: 일부 폰(샤오미·화웨이 등)은 별도 자동시작 허용 필요. 가이드의 8단계 참조

---

## 🔐 권한 명세

| 권한 | 용도 |
|------|------|
| `RECEIVE_SMS` | SMS 수신 감지 |
| `READ_SMS` | 메시지 본문 읽기 |
| `SEND_SMS` | 전달용 SMS 발송 |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` | 상시 서비스 실행 |
| `RECEIVE_BOOT_COMPLETED` | 부팅 후 자동 시작 |
| `POST_NOTIFICATIONS` | Android 13+ 알림 표시 |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | 배터리 최적화 제외 요청 |

모든 처리는 **온디바이스**에서 이루어지며, 외부 서버로 데이터를 전송하지 않습니다.

---

## 🧪 테스트 시나리오

| 입력 | 기대 동작 |
|------|----------|
| `신한카드 승인 100,000원` | 와이프 번호로 전달 ✅ |
| `[배송] 택배가 도착했습니다` | 무시 (Logcat: `카드 문자 아님 → 무시`) |
| `현대카드 결제 취소` | 와이프 번호로 전달 ✅ (`현대카드`, `결제` 매칭) |
| 폰 재부팅 | 서비스 자동 재시작 ✅ |

상세 검증 절차는 [setup-guide.md 7단계](docs/setup-guide.md#7단계-동작-테스트) 참조.

---

## 📜 License

[MIT License](LICENSE) © 2026 deejk

본 프로젝트는 학습·개인 용도로 자유롭게 사용·수정·배포 가능합니다. 단, 카드 결제 정보 전달이라는 민감한 기능 특성상 **실제 운용 전 충분한 테스트와 본인 책임 하에 사용**하세요.
