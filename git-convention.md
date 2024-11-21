
# Git Convention

## 1. 커밋 메시지 규칙
모든 커밋 메시지는 다음 형식을 따릅니다:
```
<type>: <subject>
```

### Type 종류
- `feat` : 새로운 기능 추가
- `fix` : 버그 수정
- `docs` : 문서 수정
- `style` : 코드 스타일 변경 (세미콜론, 들여쓰기 등)
- `refactor` : 코드 리팩토링
- `test` : 테스트 코드
- `chore` : 기타 변경사항 (빌드 스크립트 등)
- `build` : 빌드 관련 변경사항
- `ci` : CI/CD 관련 변경사항
- `perf` : 성능 개선
- `revert` : 커밋 되돌리기
- `design` : UI/UX 디자인 변경
- `add` : 새로운 파일/라이브러리 추가
- `remove` : 파일/기능 제거
- `rename` : 파일/폴더 이름 변경
- `move` : 파일/폴더 이동
- `init` : 프로젝트 초기화

### 예시
```bash
feat: 로그인 기능 추가
fix: 회원가입 유효성 검사 오류 수정
docs: README 업데이트
style: 코드 포맷팅 적용
```

## 2. Git Hooks 설정 방법

프로젝트를 처음 클론한 후 다음 단계를 따라주세요:

1. Node.js 설치
- [Node.js 웹사이트](https://nodejs.org/)에서 LTS 버전 다운로드 및 설치
- 설치 완료 후 터미널(또는 명령 프롬프트)에서 설치 확인:
```bash
node --version
```

2. 프로젝트 루트 디렉토리에서 의존성 설치
```bash
npm install
```

이게 전부입니다! 이제부터 커밋 시 자동으로 커밋 메시지 규칙이 적용됩니다.

## 3. 문제 해결

### Git Hook이 동작하지 않을 때
```bash
npm run prepare
```

### Git Bash가 아닌 다른 터미널에서 Husky 실행 오류 발생 시
다음 중 한 가지 방법을 선택하세요:
1. Git Bash를 사용하여 커밋 진행 (권장)
2. VSCode나 IntelliJ의 내장 터미널 사용
3. Windows Terminal에서 Git Bash 프로필 사용

## 4. IDE에서 사용하기

### VSCode
- 기본적으로 작동합니다
- Source Control 탭에서 커밋 시에도 규칙이 적용됩니다

### IntelliJ
- 기본적으로 작동합니다
- Commit 탭에서 커밋 시에도 규칙이 적용됩니다

## 5. 참고 사항
- 커밋 메시지는 한글 또는 영어로 작성 가능합니다.
- 모든 IDE와 터미널에서 사용 가능합니다.
- 긴급하게 규칙을 우회해야 할 경우 `git commit --no-verify -m "메시지"` 사용 가능
  (단, 특별한 경우에만 사용하세요!)