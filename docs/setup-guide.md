# Android Studio 설치 & 빌드 가이드

카드문자 전달 앱(SmsForward)을 Android Studio로 빌드하여 실기기에 설치하기까지의 전체 가이드.

---

## 1단계: Android Studio 설치

1. **다운로드**: https://developer.android.com/studio
   - "Download Android Studio" 클릭 → 약관 동의 → `.exe` 파일 다운로드 (~1GB)

2. **설치 실행**:
   - 다운로드한 `android-studio-2024.x.x.x-windows.exe` 실행
   - "Next" 계속 클릭 (기본 설정 유지)
   - **Choose components**: `Android Studio` + `Android Virtual Device` 둘 다 체크
   - **Install Location**: 기본 `C:\Program Files\Android\Android Studio` 권장
   - 설치 완료까지 5~10분

3. **첫 실행 시 SDK 다운로드**:
   - "Do not import settings" → Next
   - **Install Type**: `Standard` 선택 → Next
   - **UI Theme**: Dark/Light 취향대로
   - **Verify Settings**: 자동으로 SDK Platform / Build-Tools / Emulator 다운로드 시작 (약 3~5GB)
   - "Finish" 클릭 후 다운로드 완료까지 10~20분

---

## 2단계: 프로젝트 열기

1. Android Studio 시작 화면에서 **"Open"** 클릭
2. 폴더 선택: `C:\Users\시대고시\smsforward` → **OK**
3. "Trust Project?" 창이 뜨면 **"Trust Project"** 클릭

### 자동으로 일어나는 일

프로젝트 열면 **Gradle Sync**가 자동 실행됩니다 (하단 상태바 확인):
- `gradle-wrapper.jar` 자동 다운로드 (스펙에서 빠진 바이너리)
- Gradle 8.4 다운로드 (~150MB, 최초 1회만)
- AGP/Kotlin 플러그인, AndroidX 라이브러리 다운로드
- **전체 5~15분 소요** (네트워크 속도에 따라)

> ⚠️ **한글 사용자명 이슈**: `C:\Users\시대고시\` 경로의 한글 때문에 Gradle이 가끔 에러를 냅니다. 빌드 실패 시 → 프로젝트를 `C:\dev\smsforward` 같은 영문 경로로 옮기세요.

---

## 3단계: SDK 컴포넌트 확인

Sync 완료 후 누락 시:

1. 메뉴: **Tools → SDK Manager**
2. **SDK Platforms** 탭:
   - ✅ `Android 14.0 (UpsideDownCake)` API 34 체크
3. **SDK Tools** 탭:
   - ✅ `Android SDK Build-Tools 34`
   - ✅ `Android SDK Platform-Tools`
   - ✅ `Android SDK Command-line Tools (latest)`
4. **Apply** → 다운로드 → **OK**

---

## 4단계: 실기기 연결 (필수)

SMS 송수신은 **에뮬레이터에서 안 됩니다**. 실제 안드로이드 폰이 필요합니다.

### 폰 설정 (개발자 옵션 활성화)

1. 폰 **설정 → 휴대전화 정보 (또는 디바이스 정보)**
2. **"빌드 번호"** 항목을 **7번 연속 탭**
3. "개발자가 되셨습니다" 메시지
4. 설정으로 돌아가 **개발자 옵션** 진입
5. **"USB 디버깅"** 활성화 ✅

### 케이블 연결

1. USB 케이블로 폰 ↔ PC 연결
2. 폰에 **"USB 디버깅을 허용하시겠습니까?"** 팝업 → **확인**
3. (선택) "이 컴퓨터에서 항상 허용" 체크
4. Android Studio 우측 상단에 폰 모델명이 표시되면 성공 (예: `SM-S921 (USB)`)

---

## 5단계: 앱 빌드 & 설치

1. Android Studio 상단 툴바에서 디바이스 드롭다운에 **연결된 폰** 선택
2. **녹색 ▶ Run 버튼** 클릭 (또는 `Shift+F10`)
3. 빌드 → APK 생성 → 폰에 설치 → 자동 실행 (1~2분)

### 빌드 에러 발생 시

| 에러 | 해결 |
|------|------|
| `Gradle sync failed` | 한글 경로 문제 → 영문 경로로 이동 |
| `SDK location not found` | File → Project Structure → SDK Location 확인 |
| `Could not find ...` | 우측 상단 ☁️ 아이콘 → "Sync Project with Gradle Files" |
| 방화벽 차단 | Gradle 다운로드 허용 |

---

## 6단계: 폰에서 앱 권한 설정

앱이 자동 실행되면:

1. **SMS 권한 3종 팝업**: 모두 **"허용"** 클릭 (받기, 읽기, 보내기)

2. **"📱 전달할 번호"** 입력 → **"💾 설정 저장"**

3. **"🔋 배터리 최적화 제외 설정"** 버튼 클릭 → "허용 안 함" 선택
   - (직역상 헷갈리지만 = 최적화 안 함 = 백그라운드 계속 실행 허용)

4. **"▶ 서비스 시작"** 클릭
   - 알림 권한 팝업 뜨면 허용 (Android 13+)
   - 상단에 "💳 카드문자 전달 중" 알림 상시 표시되면 성공 🟢

---

## 7단계: 동작 테스트

1. **다른 폰에서** 본인 폰으로 SMS 전송:
   - 예: `신한카드 승인 100,000원 결제완료`
2. **즉시 와이프 번호로 전달** 확인:
   - `[카드문자 전달]\n발신: ...\n\n신한카드 승인 ...`
3. **일반 문자**도 보내서 무시되는지 확인

### 로그 확인 (Logcat)

Android Studio 하단 **Logcat** 탭:
- 검색창에 `SmsReceiver` 입력
- 동작 로그 실시간 확인:
  - `카드 문자 아님 → 무시`
  - `문자 전달 성공 → 010-xxxx-xxxx`
  - `문자 전달 실패: ...`

---

## 8단계: 추가 설정 (선택)

### 자동 시작 보장 (제조사별)

| 제조사 | 경로 |
|--------|------|
| **삼성** | 설정 → 디바이스 케어 → 배터리 → 백그라운드 사용 제한 → "카드문자 전달" 제한 안 함 |
| **샤오미** | 설정 → 앱 → 자동 시작 관리 → 활성화 |
| **화웨이** | 설정 → 앱 → 시작 관리 → 수동 관리 → 모두 허용 |
| **LG/구글픽셀** | 별도 설정 불필요 |

### APK 추출 (다른 폰에 설치)

- 메뉴: **Build → Build App Bundle(s) / APK(s) → Build APK(s)**
- 완료 알림에서 "locate" 클릭 → `app-debug.apk` 파일 추출
- USB나 카톡으로 옮겨 설치 가능 (단, 폰에서 "출처 불명 앱 허용" 필요)

---

## ⚠️ 중요 주의사항

1. **요금 발생**: 전달되는 SMS마다 통신사 요금 발생 (보통 건당 22~33원)
2. **카드사 검증**: 키워드 매칭 방식이므로 실제 카드 문자로 사전 테스트 권장
3. **민감 정보**: 카드 결제 내역이 와이프 폰으로 가는 점 유의 (의도된 동작)
4. **배터리**: Foreground Service 상시 실행이라 배터리 약간 소모 (체감 거의 없음)
5. **듀얼 SIM**: 발신 SIM 선택 미구현 → 기본 SIM으로 발송됨

---

## 트러블슈팅 체크리스트

문자가 전달되지 않는 경우 순서대로 확인:

- [ ] 앱 메인 화면에 `🟢 실행 중` 표시되어 있는가?
- [ ] 알림 영역에 "💳 카드문자 전달 중" 알림이 있는가?
- [ ] 전달 번호가 정확히 저장되어 있는가? (010 포함)
- [ ] 받은 문자에 키워드 중 하나라도 포함되어 있는가?
- [ ] 배터리 최적화 제외 설정이 되어 있는가?
- [ ] SMS 권한 3종이 모두 허용되어 있는가? (설정 → 앱 → 카드문자 전달 → 권한)
- [ ] Logcat에서 `SmsReceiver` 태그 로그를 확인했는가?

여전히 문제가 있다면 Logcat 로그를 첨부해 문의하세요.
