# 새 노트북 작업 인계 체크리스트

작성일: 2026-05-11
이전 환경: Windows + WSL, 한글 Windows 사용자명, 프로젝트 `C:\dev\smsforward`
다음 환경: 새 노트북 (Android Studio 신규 설치 예정)

---

## 1. 현재 상태 스냅샷

| 항목 | 상태 |
|------|------|
| 소스 코드 | ✅ GitHub `main` 최신 (`f3abc0e`) |
| Gradle Wrapper | ✅ 저장소 포함 (clone 직후 `./gradlew` 사용 가능) |
| Debug APK 빌드 | ✅ 성공 (이전 노트북, `BUILD SUCCESSFUL`) |
| 폰 설치 | ❌ 미해결 — "악성앱 의심"으로 카톡 설치 차단됨 |

**최근 커밋 (작업 재개 시 동기화 기준)**
- `f3abc0e` docs: README wrapper 안내 갱신
- `5e98558` docs: 빌드/설치 트러블슈팅 가이드 추가
- `4ccc44f` build: Gradle wrapper 파일 추가
- `5079951` chore: MIT 라이선스 추가
- `71b735b` docs: README.md 작성

---

## 2. 새 노트북에 챙길 것 / 안 챙길 것

### ❌ 가져갈 필요 없음 (모두 신규 설치 또는 자동 재다운로드)
- Android Studio · JDK · Android SDK · Gradle 캐시 · IDE 설정
- `local.properties` (`.gitignore` 처리 — 첫 sync 때 자동 생성)
- `app/build/`, `.gradle/` (빌드 산출물)

### ✅ 미리 준비
- 폰 USB 케이블 (설치 차단 이슈 우회용 ADB 설치에 필요)
- GitHub 인증 (HTTPS clone 시 PAT 또는 `gh auth login`)
- 인터넷 (총 ~6GB: SDK 3GB + Gradle 150MB + 의존성 + Android Studio 1GB)
- 디스크 여유 **15GB 이상**

---

## 3. 진행 순서

### Step 1. Windows 사전 세팅 (5분)
- Windows 사용자명이 한글이어도 OK — **프로젝트만 `C:\dev\` 영문 경로에 두면 됨**
- Git for Windows 설치: https://git-scm.com/

### Step 2. Android Studio 설치 (20~30분)
👉 **`setup-guide.md` 1단계 참조**
- `Standard` install type → SDK + Build-Tools 34 자동 포함됨
- 설치 후 첫 실행에서 SDK 다운로드까지 끝낼 것

### Step 3. 저장소 클론 (1분)
```bat
git clone https://github.com/deejk/SmsForward.git C:\dev\smsforward
```
> ⚠️ 반드시 **영문 경로**. `C:\Users\한글이름\` 같은 한글 경로에 두면 Gradle sync에서 인코딩 에러 가능.

### Step 4. Android Studio에서 열기 (5~15분)
👉 **`setup-guide.md` 2~3단계 참조**
- 폴더 Open → "Trust Project"
- Gradle Sync 자동 실행 — 완료 시 우측 하단 `Gradle: Build completed` 확인
- 누락된 SDK 컴포넌트 있으면 Tools → SDK Manager 에서 추가

### Step 5. 빌드 검증 (1~2분)
**IDE 방법**: Build → Build Bundle(s) / APK(s) → Build APK(s)
**CLI 방법**:
```bat
cd C:\dev\smsforward
.\gradlew.bat assembleDebug
```
→ 산출물: `app\build\outputs\apk\debug\app-debug.apk`

### Step 6. 폰 USB 디버깅 (5분)
👉 **`setup-guide.md` 4단계 참조**
1. 폰: 설정 → 휴대전화 정보 → 빌드 번호 7번 탭
2. 폰: 개발자 옵션 → USB 디버깅 ON
3. USB 연결 → "허용" 팝업 → 허용
4. PC 확인:
   ```bat
   adb devices
   ```
   폰 모델명이 보이면 OK.

### Step 7. 앱 설치 ⚠️ (이전 노트북에서 막힘)

> 갤럭시 S25에서 카톡 전송 → "악성앱 의심"으로 차단됨. SMS 권한 때문에 Play Protect / Auto Blocker가 자동 차단하는 정상 동작.

**A안: ADB 설치 (가장 확실, 추천 ⭐)**
```bat
adb install C:\dev\smsforward\app\build\outputs\apk\debug\app-debug.apk
```
→ `Success` 뜨면 끝. Play Protect / Auto Blocker 모두 우회됨.

**B안: 차단 우회 (ADB 안 될 때)**
👉 **`install-troubleshooting.md` 4섹션** 참조 — 설정 검색 / 팝업 숨김 옵션 / Auto Blocker 끄기

### Step 8. 앱 권한 설정 + 동작 테스트
👉 **`setup-guide.md` 6~7단계** 그대로 진행

---

## 4. 새 노트북에서 발생 가능한 이슈

| 증상 | 1차 대응 |
|------|----------|
| Gradle sync 실패 (인코딩) | 프로젝트 경로 한글 확인 → 영문 경로로 이동 |
| `JAVA_HOME` 못 찾음 | 보통 Android Studio가 자동 처리. 안 되면 `install-troubleshooting.md` 환경변수 설정 |
| SDK 컴포넌트 누락 | Tools → SDK Manager → API 34 + Build-Tools 34 체크 |
| `adb` 명령 못 찾음 | `%LOCALAPPDATA%\Android\Sdk\platform-tools`를 PATH에 추가 |
| 폰이 `adb devices`에 안 나옴 | USB 케이블 교체 (충전 전용 케이블 주의) / 폰 USB 디버깅 재허용 |
| 설치 시 `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | 폰에 같은 패키지 기존재 → 먼저 삭제 후 재설치 |

---

## 5. 참고 문서 인덱스

| 문서 | 언제 보는지 |
|------|------------|
| `setup-guide.md` | Android Studio 설치 ~ 첫 빌드/실행 전체 가이드 (단계별 상세) |
| `install-troubleshooting.md` | CLI 빌드 우회 절차 + 설치 차단 이슈 해결 |
| `README.md` | 프로젝트 개요, 권한 명세, 기능, 알려진 한계 |
| `handover.md` (이 문서) | 새 환경에서 작업 재개용 체크리스트 |

---

## 6. 작업 재개 후 다음 할 일

설치 성공 시:
1. setup-guide.md 6~7단계로 권한 + 동작 테스트
2. 실제 카드 결제 SMS로 운영 검증 (Logcat에 `SmsReceiver` 태그 확인)

설치 계속 차단되면:
- **release keystore로 서명한 APK 빌드** 고려 (디버그보다 차단 확률 낮음)
- 또는 Google Play 내부 테스트 트랙 등록 (정석이지만 등록비·검수)

막히면 새 Claude Code 세션 열어서 이 문서 + 막힌 화면 캡처 공유하면 이어서 봐드릴 수 있습니다.
