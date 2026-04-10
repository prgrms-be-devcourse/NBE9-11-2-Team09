---
name: 설정 및 기타 작업 템플릿
about: 설정 및 기타 작업을 할때 사용
title: ''
labels: ''
assignees: ''

---

name: "🔨 4. CHORE ISSUE (설정 및 기타 작업)"
description: "설정 및 기타 작업 관련 이슈 템플릿"
title: "[chore] "
labels: ["🔨 type: chore"]
body:
  - type: textarea
    id: chore_description
    attributes:
      label: "⚙️ 작업 내용"
      description: "비즈니스 로직 외의 변경 사항(환경 설정, 라이브러리, 문서 등)을 적어주세요."
    validations:
      required: true
  - type: textarea
    id: chore_details
    attributes:
      label: "🛠️ 세부 항목"
      value: |
        - [ ] 라이브러리 추가/삭제
        - [ ] 문서(README 등) 수정
        - [ ] 단순 파일 이동 및 정리
    validations:
      required: true
