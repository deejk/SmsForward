# 빌드 & 설치 작업 로그

setup-guide.md 진행 중 발생한 환경 이슈와 해결 과정, 그리고 카톡 설치 차단 이슈에 대한 진행 기록.

작성일: 2026-05-11
프로젝트 경로: `C:\dev\smsforward` (WSL: `/mnt/c/dev/smsforward`)

---

## 1. 환경 현황

| 항목 | 상태 |
|------|------|
| 프로젝트 위치 | `C:\dev\smsforward` (영문 경로로 이전 완료) |
| Android Studio | 설치됨 (`C:\Program Files\Android\Android Studio`) |
| JDK | Android Studio 번들 JBR 21.0.10 사용 |
| Android SDK | `C:\Users\시대고시\AppData\Local\Android\Sdk` (API 34, 36.1 / Build-Tools 34, 36.1, 37) |
| Gradle | 8.4 (캐시: `%USERPROFILE%\.gradle\wrapper\dists\gradle-8.4-bin\...`) |
| 시스템 PATH의 java | OpenJDK 1.8 (Temurin) — **빌드용으로는 부적합**, JBR로 우회 필요 |

---

## 2. Gradle Sync (완료 ✅)

### 발견된 이슈
- `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` 세 파일이 저장소에 없음
  - README 경고대로 Android Studio가 자동 생성 예정인데 GUI 거치지 않고 CLI로 처리함
- 시스템 기본 `java`가 JDK 8 → AGP 8.2.2 요구사항(JDK 17+) 미달
  - Android Studio 번들 JBR 21 사용으로 우회

### 해결 절차
캐시된 Gradle 8.4 + JBR을 사용한 배치 스크립트로 일괄 처리:

```bat
@echo off
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "GRADLE_BIN=%USERPROFILE%\.gradle\wrapper\dists\gradle-8.4-bin\1w5dpkrfk8irigvoxmyhowfim\gradle-8.4\bin\gradle.bat"
cd /d C:\dev\smsforward

call "%GRADLE_BIN%" wrapper --gradle-version 8.4 --no-daemon
call "%GRADLE_BIN%" help --no-daemon --warning-mode=summary
call "%GRADLE_BIN%" :app:dependencies --configuration debugRuntimeClasspath --no-daemon
```

### 결과
- 세 단계 모두 `BUILD SUCCESSFUL`, 에러·deprecation 경고 0건
- 누락 파일 자동 생성됨 (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`)
- 이후로는 IDE 없이도 `gradlew` 사용 가능

### WSL ↔ Windows 인코딩 주의
- 배치 파일은 UTF-8로 저장되지만 cmd.exe 기본 인코딩이 CP949라 한글 경로가 깨짐
- → 배치 안에서는 한글 직접 쓰지 말고 **`%USERPROFILE%`** 같은 환경변수로 우회

---

## 3. Debug APK 빌드 (완료 ✅)

### 명령
```bat
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d C:\dev\smsforward
call gradlew.bat assembleDebug --warning-mode=summary
```

### 결과물
| 항목 | 값 |
|------|----|
| 파일 | `app\build\outputs\apk\debug\app-debug.apk` |
| 크기 | 5,734,777 bytes (≈ 5.5 MB) |
| applicationId | `com.smsforward` |
| versionCode / versionName | 1 / 1.0 |
| 빌드 시간 | 약 1분 (warm cache 상태) |

### 빌드 출력 stdout이 보이지 않은 이슈
- WSL에서 `cmd.exe /c batch.bat 2>&1 | tail` 했을 때 stdout이 안 흘러옴
- 실제로는 빌드 정상 종료, 산출물만 확인하면 됨
- 향후 stdout 필요하면 → 배치 안에서 파일로 리다이렉트(`> build.log 2>&1`) 후 WSL에서 tail

---

## 4. 카톡으로 폰 전송 후 설치 (⏳ 진행 중 — 차단됨)

### 증상
- APK를 카톡으로 폰에 전송 후 설치 시도
- **"악성앱으로 의심"** 알림으로 설치 차단됨
- 원인: SMS 권한(`RECEIVE_SMS`, `READ_SMS`, `SEND_SMS`)이 보이스피싱·요금 사기 가능성으로 가장 민감하게 감시되는 권한이라 Play Protect / 삼성 Auto Blocker가 자동 차단

### 폰 환경
- 갤럭시 S25 (One UI 7 / Android 15 추정)

### 시도한 해결책
- **Auto Blocker 메뉴 진입**: 위치를 못 찾음 (S25 One UI 7 기준 정확한 경로: 설정 → 보안 및 개인정보 보호 → 아래로 스크롤 → Auto Blocker)
- **설정 검색**: 미시도

### 다음에 시도할 방법 (우선순위)

#### A. 설정 검색으로 "출처를 알 수 없는 앱 설치" 찾기
1. 설정 앱 → 상단 검색창(🔍) → `설치` 입력
2. 결과 중 **"출처를 알 수 없는 앱 설치"** 탭
3. **카카오톡** 찾아 **허용**으로 변경
4. 카톡에서 APK 다시 탭 → 설치

#### B. 차단 팝업의 숨겨진 "그래도 설치" 버튼
- "악성앱 의심" 팝업에서 **"자세히"** 또는 **"추가 옵션"** 탭
- 일부 기종은 팝업을 **위로 스와이프**하면 숨은 옵션 노출
- 보통 "**무시하고 설치**" / "**그래도 설치**" 버튼 존재

#### C. ADB로 PC에서 직접 설치 (가장 확실 ⭐)
Play Protect / Auto Blocker 모두 우회됨.

준비:
1. 폰: 설정 → 휴대전화 정보 → 소프트웨어 정보 → **빌드 번호 7번 탭**
2. 폰: 설정 → 개발자 옵션 → **USB 디버깅 ON**
3. USB 케이블로 PC 연결 → 폰에 뜨는 디버깅 허용 팝업 → **허용**

설치 명령:
```bat
set "PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools;%PATH%"
cd /d C:\dev\smsforward
adb devices
adb install app\build\outputs\apk\debug\app-debug.apk
```

`adb devices` 결과에 폰 모델명이 나오면 연결 성공 → `install` 명령에 `Success` 뜨면 완료.

#### D. Auto Blocker 직접 끄기 (S25 One UI 7 정확한 경로)
- 설정 → **보안 및 개인정보 보호** → **아래로 스크롤** → **Auto Blocker** (방패 아이콘)
- 메인 토글 OFF, 또는 내부 **"앱 보안 위협 검사"**만 OFF
- 설치 완료 후 다시 켜기

---

## 5. 갤럭시↔갤럭시 RCS 자동 변환 이슈 (해결 ✅)

### 증상
- B폰(SmsForward 설치)에 A폰에서 카드 형식 테스트 SMS 보냄
- B폰 메시지 앱에는 문자 정상 도착
- 그런데 SmsForward의 `SmsReceiver.onReceive()`가 호출조차 안 됨 (Logcat에 `SmsReceiver` 태그 로그 0건)
- 결과: 와이프 폰으로 전달 실패

### 원인
- A·B 둘 다 갤럭시 → 삼성 메시지 앱이 자동으로 **RCS(채팅+)** 로 변환 전송
- RCS 메시지는 `android.provider.Telephony.SMS_RECEIVED` 액션을 발생시키지 않음
- → BroadcastReceiver 트리거 자체 안 됨

### 해결
**A폰**(또는 B폰) 삼성 메시지 앱 → 설정 → **채팅 설정 → "채팅 기능" OFF**
- OFF 후 보내면 일반 SMS로 전송되어 SmsForward가 정상 수신
- 메시지 앱에서 보낸 문자 색깔로 구분 가능 (RCS=파란색/채팅 표시, SMS=녹색/회색)

### ⭐ 운영 시 중요 안내
- **실제 카드사 SMS(1588-8900 삼성, 1544-7200 신한 등)는 단축번호 발신 일반 SMS** → RCS와 무관
- → **실 운영에서는 채팅 기능 OFF 불필요**
- 채팅 기능 OFF는 **테스트 시에만** 필요
- 즉, 와이프 폰의 채팅 기능은 그대로 두어도 카드 알림 전달은 정상 동작

### 진단 키 (다음에 비슷한 증상 만나면)
- Logcat에 `SmsReceiver` 태그 로그가 0건 + 메시지 앱엔 도착함 → RCS 의심
- 즉시 채팅 기능 OFF 후 재테스트

---

## 6. 향후 개선 (선택)

- **릴리즈 키스토어로 서명한 APK 빌드** → 디버그 APK보다 차단 확률 낮음 (단 keystore 별도 생성 필요, 가이드 8단계의 APK 추출 흐름 확장)
- **앱 아이콘/이름을 일반 앱처럼** → "카드문자 전달" 같은 직관적 이름이 오히려 의심도를 높임. 권한 자체 때문이라 큰 차이 없을 가능성 있음
- **Google Play 내부 테스트 트랙 배포** → 가장 정석이지만 등록비·검수 필요. 와이프 폰 1대용으로는 과한 옵션

---

## 7. 참고

- **setup-guide.md**: 전체 빌드/설치 절차 (Android Studio GUI 기준)
- **README.md**: 프로젝트 개요, 권한 명세, 알려진 한계
- **이 문서**: setup-guide의 CLI 우회 절차 + 설치 차단 트러블슈팅 추가본
