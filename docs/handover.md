# 새 노트북 작업 인계 체크리스트

작성일: 2026-05-12 (최종 갱신)
이전 환경: Windows + WSL, 한글 Windows 사용자명, 프로젝트 `C:\dev\smsforward`
다음 환경: 새 노트북 (Android Studio 신규 설치 예정)

---

## 1. 현재 상태 스냅샷

| 항목 | 상태 |
|------|------|
| 소스 코드 | ✅ `main` 최신 (`b22a65c`, 푸시 전이면 push 필요) |
| Gradle Wrapper | ✅ 저장소 포함 (clone 직후 `./gradlew` 사용 가능) |
| Release APK 빌드 | ✅ 성공 (`build-release.bat`, 서명 적용 `assembleRelease`) |
| 폰 설치 | ✅ S25(시리얼 `R3CY90YASYA`)에 **release 서명 APK** 설치 완료 (디버그→릴리스 1회 삭제 후 재설치 완료) |
| 매칭 로직 | ✅ C안 정규식 기반 (삼성/신한 전용, 승인+승인거절) |
| 월 누적 요약 | ✅ 카드별 당월 누적 자체 집계 후 전달 문자에 첨부 (승인만 가산, 추정치) |
| Release 서명 | ✅ keystore 생성·서명 적용 완료. 재설치해도 누적 데이터 보존됨 (⚠️ keystore 백업 필수 — 아래 §7) |
| 권한 설정 | ⏳ A폰에서 사용자가 권한 5종 설정 + 서비스 시작 수동 진행 단계 |
| 운영 검증 | ⏳ 첫 실 카드 결제 발생 대기 — Logcat `SmsReceiver: 문자 전달 성공` 확인 필요 |

**최근 커밋 (작업 재개 시 동기화 기준)**
- `b22a65c` build: release keystore 서명 설정 추가
- `9e21042` feat: 카드별 월 누적 사용액 요약을 전달 문자에 첨부
- `3f9ee68` docs: 2026-05-12 작업 결과 반영해 handover 갱신
- `78e548d` fix: 다크 모드에서 안내 텍스트 가독성 개선
- `db434c0` docs: RCS 자동 변환 이슈 트러블슈팅 + 빌드/설치 헬퍼 스크립트

**저장소에 포함된 헬퍼 (새 노트북에서 그대로 사용 가능)**
- `build-debug.bat` — JBR 21 환경변수 세팅 후 assembleDebug 실행
- `build-release.bat` — JBR 21 세팅 후 서명된 assembleRelease 실행
- `adb-install.bat` — adb devices 확인 후 `-r` 옵션 설치 (디버그용)

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

### Step 7. 앱 설치 (ADB로 해결됨 ✅)

> 갤럭시 S25 카톡 전송 시 "악성앱 의심" 차단됨 — ADB 설치로 우회. 검증 완료.

**ADB 설치 (추천 ⭐, 헬퍼 사용)**
```bat
cd C:\dev\smsforward
adb-install.bat
```
WSL에서 호출 시:
```bash
cmd.exe /c "cd /d C:\dev\smsforward && adb-install.bat"
```
→ `Success` 뜨면 끝. Play Protect / Auto Blocker / 카톡 차단 모두 우회됨.

> ⚠️ **WSL의 adb는 USB 못 봄** — 반드시 Windows의 adb(헬퍼 배치 안에서 호출)를 써야 함.

**차단 우회 (ADB 불가 환경 한정)**
👉 **`install-troubleshooting.md` 4섹션** 참조

### Step 8. 앱 권한 설정 + 동작 테스트
👉 **`setup-guide.md` 6~7단계** 그대로 진행

**⚠️ 주의 — 알림 권한 거부 시 Foreground Service 동작 안 함**
- 이전 실패 사례: 사용자가 알림 권한 거부 → Logcat에 `Suppressing notification by user request` → 서비스 미동작
- 반드시 SMS 3종 + **알림** 모두 허용

**⚠️ 보안 폴더 자동 클론 주의**
- 어느 시점에 보안 폴더가 활성화돼 있으면 같은 앱이 user 0(일반) + user 150(보안 폴더)로 자동 복제됨
- 홈화면에 같은 앱 두 개(하나는 작은 점 표시) 보이면 → 설정 → 보안 및 개인정보 보호 → 보안 폴더 진입 → 안에서 제거
- 진단: `adb shell pm list packages | findstr smsforward` 결과에 `Error: ... user 150` 보이면 확정

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
| **테스트 SMS 보냈는데 SmsForward가 못 받음** (Logcat에 `SmsReceiver` 로그 0건, 메시지 앱엔 도착) | 갤럭시↔갤럭시 RCS 자동 변환 → 발신 폰 삼성 메시지 → 채팅 설정 → "채팅 기능" OFF. 실 카드사 SMS는 단축번호라 운영 시엔 불필요. (상세: `install-troubleshooting.md` 5섹션) |

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

**현재 (2026-05-12 기준) — 폰 설치 완료, 운영 검증 대기 중**

운영 검증 (실 카드 결제 발생 후):
```bat
adb logcat -s SmsReceiver
```
- `문자 전달 성공 → 010-xxxx-xxxx` 한 줄 보이면 운영 OK
- `삼성 누적 +17,000원` 형태 로그로 누적 가산 확인
- 와이프 폰에 `[카드문자 전달]` + 하단 `📊 N월 누적 사용` 요약 도착 확인
- ⚠️ 누적 요약은 앱 자체 집계(추정치) — SMS 내 `누적` 표기는 월 합계가 아니라 미사용. 카드사 공식 청구액과 다를 수 있음

새 노트북 인계 후 추가 작업이 필요해진다면:
1. **카드사 추가 지원** (현대/KB 등) — `SmsReceiver.kt`의 `CARD_PATTERNS` + `APPROVAL_PATTERNS`에 정규식 추가
2. ✅ ~~release keystore로 서명한 APK 빌드~~ — **완료** (아래 §7 백업만 챙길 것)
3. **Google Play 내부 테스트 트랙** — 정석 배포 (등록비·검수 필요)

막히면 새 Claude Code 세션 열어서 이 문서 + 막힌 화면 캡처 공유하면 이어서 봐드릴 수 있습니다.

---

## 7. ⚠️ Release Keystore 백업 (가장 중요)

release 서명으로 전환 완료. **재설치해도 데이터(카드 월 누적 등)가 보존**되지만, 그러려면 **항상 같은 keystore로 서명**해야 한다.

### 백업 대상 (git에 없음 — `.gitignore` 처리됨, 공개 저장소라 커밋 금지)
| 파일 | 위치 | 내용 |
|------|------|------|
| `smsforward-release.keystore` | 프로젝트 루트 | 서명 키 (PKCS12, alias `smsforward`, 10000일 유효) |
| `keystore.properties` | 프로젝트 루트 | 비밀번호·alias 등 서명 설정 |

### 반드시 할 것
- 위 **두 파일을 안전한 곳에 백업** (개인 클라우드/USB/비밀번호 매니저). git 말고.
- **비밀번호 별도 기록** (`keystore.properties`의 `storePassword`/`keyPassword`).

### 분실하면?
- keystore를 잃으면 **같은 서명을 다시 만들 수 없음** → 새 keystore로 서명 시 서명 불일치로 **기존 앱 위에 못 덮어씀** → 한 번 더 삭제 후 재설치(누적 데이터 초기화) 필요.
- 그러니 **앱을 계속 쓸 거면 keystore 백업은 필수**.

### 새 노트북 인계 시
- clone 직후엔 keystore가 없으므로(gitignore) **백업해둔 두 파일을 프로젝트 루트에 복사**해야 `build-release.bat`가 동작한다.
- 복사 없이 빌드하면 서명 없는 release가 만들어지거나 설치 시 기존 데이터와 호환 안 됨.
