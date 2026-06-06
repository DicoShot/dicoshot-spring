# Disender 배포 가이드

이 문서는 Disender SDK를 처음부터 Maven Central에 배포하는 전 과정을 다룹니다. 신규 컨트리뷰터가 동일한 환경을 재현하거나, 다음 버전을 릴리스할 때 참고할 수 있도록 작성되었습니다.

목차:
1. [전체 흐름 개요](#1-전체-흐름-개요)
2. [사전 준비](#2-사전-준비)
3. [Sonatype Central Portal 계정 설정](#3-sonatype-central-portal-계정-설정)
4. [GPG 키 생성과 등록](#4-gpg-키-생성과-등록)
5. [Gradle 자격 증명 설정](#5-gradle-자격-증명-설정)
6. [Gradle publishing 설정](#6-gradle-publishing-설정)
7. [로컬 검증](#7-로컬-검증)
8. [Maven Central에 업로드](#8-maven-central에-업로드)
9. [GitHub Release 생성](#9-github-release-생성)
10. [다음 버전 릴리스 절차](#10-다음-버전-릴리스-절차)
11. [트러블슈팅](#11-트러블슈팅)
12. [용어 정리](#12-용어-정리)

---

## 1. 전체 흐름 개요

Maven Central 배포는 **인증과 신뢰**의 문제를 해결하기 위해 다음 단계를 거칩니다.

```
[로컬 빌드]
   │
   ├─→ JAR + sources + javadoc 생성
   ├─→ POM 메타데이터 생성
   ├─→ GPG로 모든 파일에 서명 (.asc)
   │
[업로드]
   │
   ├─→ Sonatype Central Portal에 zip bundle 전송
   ├─→ Portal이 자동 검증 (서명, 필수 필드, group 소유 확인)
   │
[공개]
   │
   ├─→ Portal에서 'Publish' 클릭
   ├─→ Maven Central 본 저장소로 동기화 (15~30분)
   │
[사용자가 받기]
   │
   └─→ implementation 'io.github.dicoshot:disender-...' 로 의존성 추가
```

핵심 개념:
- **group ID**: `io.github.dicoshot` — 도메인 형식의 namespace. GitHub username 기반
- **artifact ID**: 모듈 이름 (예: `disender-spring-boot-3-starter`)
- **version**: 릴리스 버전 (예: `0.1.0`). SNAPSHOT은 Maven Central 거부
- **GPG 서명**: 누가 만든 artifact인지 위조 불가능하게 증명
- **Sonatype Central Portal** (`central.sonatype.com`): Maven Central의 공식 업로드 창구

---

## 2. 사전 준비

### 2.1 필수 도구

| 도구 | 용도 | 설치 명령 |
| --- | --- | --- |
| **Homebrew** | macOS 패키지 관리자 | `/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"` |
| **GnuPG** (gpg) | artifact 서명 | `brew install gnupg` |
| **JDK 17** | 빌드 (Gradle toolchain이 자동 다운로드하지만 시스템 java도 17 권장) | `brew install openjdk@17` |
| **Git** | 버전 관리 | macOS는 기본 포함, `xcode-select --install`로 활성화 |

확인:
```bash
brew --version
gpg --version       # gpg (GnuPG) 2.4.x 이상
java -version       # 17 이상
git --version
```

### 2.2 GitHub 조직

- 이 가이드는 `DicoShot` **조직**을 기준으로 작성됨 (레포 `github.com/DicoShot/dicoshot-spring`, group ID `io.github.dicoshot`)
- GitHub 조직명은 `DicoShot`이지만 **Maven group ID는 소문자만 허용**되므로 `io.github.dicoshot`을 사용. namespace 검증 시 Sonatype은 대소문자를 구분하지 않고 GitHub 조직 소유 여부만 확인
- 다른 조직/계정으로 진행하려면 본 문서의 `DicoShot` / `dicoshot`을 본인 값으로 치환
- GPG 키 생성 단계의 `ZaMan0806`은 실제 키 소유자(개인 작성자) 이름이므로 그대로 둬도 됨

### 2.3 프로젝트 클론

```bash
git clone https://github.com/DicoShot/dicoshot-spring.git
cd dicoshot-spring
```

---

## 3. Sonatype Central Portal 계정 설정

> Sonatype은 2024년에 기존 OSSRH(`s01.oss.sonatype.org`)를 deprecated 처리하고 **Central Portal**(`central.sonatype.com`)로 통합했습니다. 신규 가입자는 Central Portal만 사용합니다.

### 3.1 계정 생성

1. https://central.sonatype.com 접속
2. **Sign In** → **GitHub로 로그인** (또는 이메일로 가입)
3. 약관 동의 후 가입 완료

### 3.2 Namespace 등록

namespace는 Maven group ID에 해당합니다. 도메인이 없으면 `io.github.<org-or-username>` 형식을 자동으로 받을 수 있습니다. GitHub **조직** namespace도 동일하게 `io.github.<org>` 형식입니다.

1. 로그인된 상태에서 우측 상단 프로필 → **View Namespaces**
2. **Add Namespace** 클릭
3. Namespace 입력란에 `io.github.dicoshot` 입력
4. **Submit** 클릭
5. Sonatype이 자동으로 verification 코드를 발급. 예: `OSSRH-12345`

### 3.3 GitHub repo로 namespace 검증

Sonatype은 "이 GitHub 조직(또는 계정)의 소유자가 맞는지" 자동으로 확인합니다. **조직 namespace는 검증용 repo도 반드시 그 조직 아래에 만들어야 합니다.**

1. `DicoShot` **조직 안에** **public repository**를 생성. 이름은 Sonatype이 준 검증 코드 그대로 (예: `github.com/DicoShot/OSSRH-12345`)
2. 빈 repo면 충분 (README, 코드 모두 불필요)
3. Central Portal namespace 페이지로 돌아가서 해당 namespace 옆 **Verify** 버튼 클릭
4. 검증 성공 메시지 확인
5. 검증용 repo는 삭제해도 됨

> 검증은 Sonatype 계정에 묶입니다. 즉, **레포를 조직으로 transfer하더라도 배포는 기존 개인 Sonatype 계정의 토큰으로 그대로 합니다.** 조직 namespace를 그 개인 계정으로 검증해두면 됩니다. 조직 구성원 누구나 배포하게 하려면 각자 본인 Sonatype 계정으로 동일 namespace를 검증하거나, 공용 토큰을 공유해야 합니다.

이걸 통과하면 `io.github.dicoshot` namespace를 영구적으로 소유하게 됩니다.

### 3.4 User Token 발급

배포 시 사용할 자격 증명을 만듭니다.

1. 우측 상단 프로필 → **View Account**
2. 하단의 **Generate User Token** 클릭
3. 화면에 **Username**과 **Password**(실제 비밀번호 아닌 토큰값)가 표시됨. 이 창은 다시 열 수 없으므로 **즉시 안전한 곳에 저장**
4. 저장한 값은 5단계에서 사용

> 토큰을 잃어버리면 같은 페이지에서 새로 발급 가능합니다. 단, 새 토큰을 발급하면 기존 토큰은 즉시 무효화됩니다.

---

## 4. GPG 키 생성과 등록

Maven Central은 모든 artifact가 GPG로 서명되어야 받습니다. 사용자가 "이 jar가 정말 ZaMan0806이 만든 것인가?"를 검증할 수 있도록 하기 위함입니다.

### 4.1 키 생성

```bash
gpg --gen-key
```

대화형 프롬프트:
1. **Real name**: `ZaMan0806` (혹은 본명)
2. **Email address**: GitHub에 등록된 이메일 사용 권장 (예: `roblery128@gmail.com`)
3. **Confirm**: O
4. **Passphrase**: 임의 문자열 (반드시 기억). 이 passphrase는 키를 사용할 때마다 필요

생성에는 약간의 엔트로피가 필요해서 시간이 걸릴 수 있습니다 (마우스 움직이기, 키 두드리기로 가속).

### 4.2 키 ID 확인

```bash
gpg --list-secret-keys --keyid-format=long
```

출력 예시:
```
sec   rsa3072/C2B0DF08C39CC898 2026-05-12 [SC] [expires: 2028-05-11]
      ABCDEF0123456789ABCDEF0123456789C39CC898
uid                 [ultimate] ZaMan0806 <roblery128@gmail.com>
ssb   rsa3072/...
```

- **전체 키 ID**: `C2B0DF08C39CC898` (16자리) — keyserver 업로드 시 사용
- **짧은 키 ID**: `C39CC898` (뒤 8자리) — 일부 도구에서 사용

### 4.3 공개키를 keyserver에 업로드

Sonatype은 서명을 검증할 때 공개키를 keyserver에서 찾습니다. 공개 keyserver 여러 곳에 올려두는 게 안정적입니다.

#### 시도 1: 일반 hkp 프로토콜
```bash
gpg --keyserver keyserver.ubuntu.com --send-keys C2B0DF08C39CC898
```

#### 시도 2: HKPS (HTTPS 443 포트) — 학교/회사 방화벽이 hkp 11371을 막는 경우
```bash
gpg --keyserver hkps://keys.openpgp.org --send-keys C2B0DF08C39CC898
gpg --keyserver hkps://keyserver.ubuntu.com --send-keys C2B0DF08C39CC898
```

#### 시도 3: 웹 인터페이스로 직접 업로드 (가장 확실)

위 모든 방법이 실패하면:

1. 공개키를 ASCII 형태로 출력:
   ```bash
   gpg --armor --export C2B0DF08C39CC898
   ```
2. `-----BEGIN PGP PUBLIC KEY BLOCK-----`부터 `-----END PGP PUBLIC KEY BLOCK-----`까지 전체 복사
3. https://keys.openpgp.org/upload 접속
4. 텍스트 영역에 붙여넣고 **Upload** 클릭
5. 등록한 이메일로 검증 메일이 오면 링크 클릭

### 4.4 업로드 확인

```bash
gpg --keyserver hkps://keys.openpgp.org --recv-keys C2B0DF08C39CC898
```

`1 imported` 또는 `unchanged: 1`이 나오면 등록 완료.

또는 브라우저로 `https://keys.openpgp.org/search?q=C2B0DF08C39CC898` 접속해서 검색 결과에 본인 키가 나오는지 확인.

### 4.5 비공개키 export (Gradle 서명용)

Gradle이 서명할 때 비공개키가 필요합니다. ASCII 형태로 export해 in-memory key 방식으로 주입합니다.

```bash
gpg --armor --export-secret-keys C2B0DF08C39CC898 | awk '{printf "%s\\n", $0}' | pbcopy
```

- `--armor`: 바이너리가 아닌 ASCII 형태 (텍스트 파일에 넣기 가능)
- `--export-secret-keys`: 비공개키 추출 (passphrase 입력 요구)
- `awk '{printf "%s\\n", $0}'`: 줄바꿈을 리터럴 `\n`으로 변환 (한 줄에 담기 위함)
- `pbcopy`: macOS 클립보드에 복사

이 시점에 클립보드에 `-----BEGIN PGP PRIVATE KEY BLOCK-----\n...\n-----END PGP PRIVATE KEY BLOCK-----\n` 형태의 한 줄짜리 문자열이 들어있습니다. 다음 단계에서 붙여넣습니다.

> **보안 주의**: 비공개키는 절대 git에 커밋하지 말 것. 절대 채팅이나 이메일에 붙여넣지 말 것. passphrase가 있어 즉시 도용은 어렵지만, passphrase까지 새면 본인 명의 artifact 위조 가능.

---

## 5. Gradle 자격 증명 설정

자격 증명을 build script에 직접 쓰지 않고 홈 디렉토리의 `gradle.properties`에 둡니다. 이 파일은 git에 절대 들어가지 않습니다.

### 5.1 파일 생성

```bash
mkdir -p ~/.gradle
touch ~/.gradle/gradle.properties
open -e ~/.gradle/gradle.properties
```

`open -e`는 macOS에서 TextEdit으로 파일을 엽니다.

### 5.2 내용 작성

```properties
# Sonatype Central Portal user token (3.4에서 받은 값)
mavenCentralUsername=YOUR_TOKEN_USERNAME
mavenCentralPassword=YOUR_TOKEN_PASSWORD

# GPG signing (in-memory key 방식)
signingInMemoryKey=-----BEGIN PGP PRIVATE KEY BLOCK-----\n...\n-----END PGP PRIVATE KEY BLOCK-----\n
signingInMemoryKeyPassword=YOUR_GPG_PASSPHRASE
```

치환:
- `YOUR_TOKEN_USERNAME`, `YOUR_TOKEN_PASSWORD`: 3.4에서 발급받은 토큰 값
- `signingInMemoryKey`: 4.5에서 클립보드에 복사한 값을 그대로 붙여넣기 (한 줄)
- `YOUR_GPG_PASSPHRASE`: 4.1에서 설정한 GPG passphrase

### 5.3 권한 제한

```bash
chmod 600 ~/.gradle/gradle.properties
```

본인만 읽을 수 있게 설정. 다른 사용자가 시스템에 있어도 자격 증명 노출 방지.

### 5.4 키 이름 주의

`com.vanniktech.maven.publish` 플러그인은 다음 이름을 인식합니다:
- ✅ `mavenCentralUsername`, `mavenCentralPassword`
- ✅ `signingInMemoryKey`, `signingInMemoryKeyPassword`

구식 `signing` 플러그인의 `signing.keyId`, `signing.password`, `signing.secretKeyRingFile` 키와는 **다릅니다**. 혼동하지 말 것.

---

## 6. Gradle publishing 설정

`build.gradle` (루트)에 publishing 설정이 이미 들어있습니다. 핵심만 짚어봅니다.

### 6.1 플러그인 선언

```gradle
plugins {
    id 'com.vanniktech.maven.publish' version '0.30.0' apply false
}
```

- `apply false`: 루트에서는 적용하지 않고, 필요한 subproject에만 명시적으로 적용

### 6.2 publishable 모듈 지정

```gradle
def publishableProjects = [
        'disender-core',
        'disender-spring-boot-3-starter',
        'disender-spring-boot-4-starter'
]

configure(subprojects.findAll { it.name in publishableProjects }) { subproject ->
    apply plugin: 'com.vanniktech.maven.publish'
    // ...
}
```

세 모듈에만 publishing이 적용되도록 필터링.

### 6.3 핵심 설정

```gradle
mavenPublishing {
    configure(new com.vanniktech.maven.publish.JavaLibrary(
            new com.vanniktech.maven.publish.JavadocJar.Javadoc(),
            true
    ))
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    // coordinates, pom 메타데이터 ...
}
```

- `JavaLibrary(Javadoc(), true)`: sources jar + javadoc jar 자동 생성
- `publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)`: **반드시 명시적으로 `CENTRAL_PORTAL` 지정**. 기본값은 구 OSSRH라 신규 계정은 402 에러
- `signAllPublications()`: 모든 jar/pom에 GPG 서명 첨부

### 6.4 POM 메타데이터

Maven Central은 다음 필드를 필수로 요구합니다:
- `name`, `description`, `url`
- `licenses`
- `developers`
- `scm`

이 정보가 빠지면 validation 단계에서 거부됩니다. 루트 `build.gradle`의 `pom { ... }` 블록에 모두 정의되어 있습니다.

---

## 7. 로컬 검증

실제 Maven Central에 올리기 전에 로컬 Maven 저장소(`~/.m2/repository/`)에 publish해서 정상 동작하는지 확인합니다.

### 7.1 publishToMavenLocal 실행

```bash
./gradlew clean publishToMavenLocal
```

`clean`을 붙여서 이전 빌드 잔여물을 정리. 결과:
- 모든 모듈 컴파일
- sources/javadoc jar 생성
- GPG 서명
- `~/.m2/repository/io/github/dicoshot/` 아래에 artifact 저장

### 7.2 결과 확인

```bash
ls -la ~/.m2/repository/io/github/dicoshot/
```

다음 디렉토리들이 있어야 합니다:
- `disender-core/0.1.0/`
- `disender-spring-boot-3-starter/0.1.0/`
- `disender-spring-boot-4-starter/0.1.0/`

각 디렉토리 안의 파일:
```
disender-core-0.1.0.jar              # 본 artifact
disender-core-0.1.0.jar.asc          # GPG 서명
disender-core-0.1.0-sources.jar      # 소스 jar
disender-core-0.1.0-sources.jar.asc
disender-core-0.1.0-javadoc.jar      # javadoc jar
disender-core-0.1.0-javadoc.jar.asc
disender-core-0.1.0.pom              # 메타데이터
disender-core-0.1.0.pom.asc
disender-core-0.1.0.module           # Gradle module metadata
```

`.asc` 파일이 모든 artifact에 짝지어 있어야 함. 빠지면 Central에서 거부됨.

---

## 8. Maven Central에 업로드

### 8.1 버전 확인

`build.gradle`의 `version`이 SNAPSHOT이 아닌 release 버전이어야 합니다.

```gradle
allprojects {
    group = 'io.github.dicoshot'
    version = '0.1.0'      // ← NOT '0.1.0-SNAPSHOT'
}
```

> Maven Central은 SNAPSHOT 버전을 절대 받지 않습니다.

### 8.2 업로드 실행

```bash
./gradlew clean publishToMavenCentral
```

수동 release 방식(추천). 다음이 발생:
1. 모든 모듈 빌드 + 서명
2. zip bundle로 묶어서 Central Portal API에 업로드
3. Portal에서 자동 validation 시작
4. 사용자가 직접 'Publish' 버튼을 눌러야 공개

자동 release 명령도 있지만 첫 배포에서는 비추천:
```bash
./gradlew publishAndReleaseToMavenCentral    # validation 통과 시 자동 공개. 첫 배포에선 비추천
```

### 8.3 Portal에서 상태 확인

1. https://central.sonatype.com 로그인
2. 우측 상단 프로필 → **View Deployments**
3. 방금 업로드한 deployment 표시됨. 상태:
   - `VALIDATING`: 검증 진행 중 (~1~2분)
   - `VALIDATED`: 검증 통과. Publish 가능
   - `PUBLISHED`: 공개됨. Maven Central 동기화 진행 중
   - `FAILED`: 에러. 메시지 확인 후 수정

### 8.4 Publish

`VALIDATED` 상태가 되면:
1. Deployment 상세 페이지 진입
2. **Publish** 버튼 클릭
3. 확인 다이얼로그에서 다시 클릭
4. 상태가 `PUBLISHING` → `PUBLISHED`로 변경

### 8.5 Maven Central 동기화 대기

Publish 클릭 후 실제 Maven Central 본 저장소(`repo.maven.apache.org`)에 동기화되기까지 **15~30분** (길면 1~2시간).

확인 방법:
```bash
curl -I https://repo.maven.apache.org/maven2/io/github/dicoshot/disender-core/0.2.0/disender-core-0.2.0.jar
```

- `HTTP/2 200`: 등록 완료
- `HTTP/2 404`: 아직 동기화 중. 더 기다리기

`search.maven.org`나 `mvnrepository.com` 검색에는 추가로 2~4시간 더 걸릴 수 있습니다.

---

## 9. GitHub Release 생성

Maven Central 등록과는 별개로 GitHub에서도 릴리스를 표시합니다.

### 9.1 태그 vs 브랜치 이해

| 종류 | 역할 | 예시 |
| --- | --- | --- |
| **브랜치** | 작업 흐름. 커밋이 계속 추가됨 | `main` |
| **태그** | 특정 시점의 스냅샷. 한번 만들면 그 커밋을 영구히 가리킴 | `v0.1.0` |

`git push origin main`만 해서는 태그가 원격에 가지 않습니다. 태그는 별도로 push해야 함.

### 9.2 release용 태그 생성

annotated tag(-a 플래그)로 만듭니다. lightweight tag보다 정보가 풍부하고 GitHub Release에 적합합니다.

```bash
git tag -a v0.1.0 -m "Disender v0.1.0 — initial Maven Central release"
git push origin v0.1.0
```

### 9.3 main 브랜치 push

배포 직전 커밋이 원격에 안 올라가 있으면 같이 push:
```bash
git push origin main
```

### 9.4 GitHub Release 작성

1. https://github.com/DicoShot/dicoshot-spring/releases/new 접속
2. **Choose a tag** 드롭다운 → `v0.1.0` 선택 (방금 push한 태그가 나타남)
3. **Release title**: `Disender v0.1.0`
4. **Description**: `RELEASE_NOTES_v0.1.0.md` 내용을 복사해 붙여넣기
5. **Publish release** 클릭

### 9.5 Maven Central badge 추가 (선택)

동기화 완료 후 README에 다음을 추가하면 최신 버전이 자동 표시됩니다.

```markdown
[![Maven Central](https://img.shields.io/maven-central/v/io.github.dicoshot/disender-spring-boot-3-starter)](https://central.sonatype.com/artifact/io.github.dicoshot/disender-spring-boot-3-starter)
```

---

## 10. 다음 버전 릴리스 절차

`v0.2.0` 등 후속 버전을 낼 때 반복할 흐름입니다.

### 10.1 코드 변경

- 평소처럼 main에 커밋 쌓기
- 새 기능, 버그 픽스, 리팩터링 등

### 10.2 버전 번호 올리기

`build.gradle`:
```gradle
allprojects {
    group = 'io.github.dicoshot'
    version = '0.2.0'    // 변경
}
```

`README.md`에서 의존성 예시의 버전도 같이 갱신.

### 10.3 변경 사항 커밋

```bash
git add build.gradle README.md
git commit -m "Bump version to 0.2.0"
git push origin main
```

### 10.4 로컬 검증

```bash
./gradlew clean publishToMavenLocal
ls ~/.m2/repository/io/github/dicoshot/disender-core/0.2.0/
```

### 10.5 Central에 업로드

```bash
./gradlew clean publishToMavenCentral
```

Portal에서 validation 확인 → Publish.

### 10.6 GitHub 태그 + Release

```bash
git tag -a v0.2.0 -m "Disender v0.2.0"
git push origin v0.2.0
```

Release notes 작성 → GitHub Releases에서 publish.

### 10.7 다음 사이클 시작

```gradle
version = '0.2.1-SNAPSHOT'    // 다음 개발 시작 표시 (선택)
```

이 변경은 main에만 두고, 다음 릴리스 직전에 다시 release 버전으로 변경.

---

## 11. 트러블슈팅

### 11.1 402 Cannot get stagingProfiles

**원인**: `publishToMavenCentral()`이 기본값(구 OSSRH)을 사용. 신규 Central Portal 가입자는 staging profile이 없음.

**해결**: build.gradle에 `SonatypeHost.CENTRAL_PORTAL` 명시.
```gradle
publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
```

### 11.2 Invalid publication: multiple artifacts with identical extension and classifier

**원인**: 루트 build.gradle의 `withJavadocJar()`/`withSourcesJar()`와 vanniktech 플러그인의 자동 생성이 충돌.

**해결**: 루트에서 `withJavadocJar()`, `withSourcesJar()` 제거. vanniktech 플러그인이 `JavaLibrary(JavadocJar.Javadoc(), true)` 설정으로 알아서 처리.

### 11.3 keyserver send failed: No route to host

**원인**: 네트워크 환경(학교/회사 방화벽)이 hkp 11371 포트를 차단.

**해결**: 4.3절 시도 2(HKPS) 또는 시도 3(웹 업로드)으로 진행.

### 11.4 gpg: command not found

**원인**: macOS는 GPG가 기본 설치 아님.

**해결**: `brew install gnupg`.

### 11.5 Validation failed: missing signature / missing javadoc

**원인**:
- `signAllPublications()` 호출 누락
- `JavadocJar.Javadoc()` 설정 누락
- `signingInMemoryKey` 누락 또는 형식 오류

**해결**: build.gradle의 `mavenPublishing` 블록과 `~/.gradle/gradle.properties`를 다시 점검.

### 11.6 publish 후 curl이 404 반환

**원인**: Maven Central 동기화 진행 중.

**해결**: 15~30분(길면 몇 시간) 대기. Portal의 deployment 상태가 `PUBLISHED`인지 먼저 확인.

### 11.7 토큰 분실

**해결**: Central Portal **View Account** → **Generate User Token** 재발급. 새 토큰이 생기면 기존은 즉시 무효화되므로 `~/.gradle/gradle.properties`도 갱신.

### 11.8 GPG passphrase 분실

**해결**: 키 자체를 다시 만들어야 함. 4단계를 처음부터 다시 진행하고, keyserver에 새 키 업로드 + `~/.gradle/gradle.properties`에 새 in-memory key 반영.

---

## 12. 용어 정리

| 용어 | 설명 |
| --- | --- |
| **Maven Central** | Java 진영의 공식 공개 저장소 (`repo.maven.apache.org`). 누구나 의존성으로 받음 |
| **Sonatype Central Portal** | Maven Central에 업로드하는 공식 창구 (`central.sonatype.com`) |
| **OSSRH** | 구 Sonatype 시스템 (`s01.oss.sonatype.org`). 2024년 deprecated. 신규 가입자는 사용 불가 |
| **group ID** | Maven coordinate의 namespace. 도메인 또는 `io.github.<username>` |
| **artifact ID** | 모듈/라이브러리 이름 |
| **POM** | Project Object Model. 메타데이터 XML 파일 (`*.pom`) |
| **GPG 서명** | 누가 만들었는지 위조 불가능하게 증명하는 암호 서명 (`.asc` 파일) |
| **keyserver** | 공개키를 공유하는 공개 서버. Sonatype이 여기서 키를 가져와 서명 검증 |
| **SNAPSHOT** | 개발 중 버전. Maven Central은 받지 않음. 사용자도 일반적으론 안 씀 |
| **staging** | 검증 단계. Portal에서 'Publish' 누르기 전 상태 |
| **release** | 최종 공개된 안정 버전 |
| **태그 (tag)** | Git에서 특정 커밋을 영구히 가리키는 reference. 릴리스 버전 표시용 |
| **브랜치 (branch)** | Git의 작업 흐름. 커밋이 추가될 때마다 앞으로 이동 |

---

## 부록: 환경 정보

이 가이드 작성 시점:
- **macOS**: Darwin 25.4.0
- **Java**: 17 (Gradle toolchain)
- **Gradle**: 9.0.0 (wrapper)
- **Spring Boot**: 3.3.5 (boot3 starter), 4.0.6 (boot4 starter)
- **vanniktech maven publish**: 0.30.0
- **GPG**: GnuPG 2.4.x
- **Sonatype Central Portal**: 2026-05 기준 UI/API
