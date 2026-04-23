---
name: 문서 작성 및 수정
about: 문서 작성 및 수정할때 사용
title: ''
labels: ''
assignees: ''

---

name: "📝 5. DOCS ISSUE (문서 작성 및 수정)"
description: "README, Wiki, API 명세서 등 프로젝트 문서 관련 작업"
title: "[docs] "
labels: ["📝 docs"]
body:
  - type: textarea
    id: docs_description
    attributes:
      label: "📝 수정/추가 내용"
      description: "어떤 문서에 어떤 내용을 작성하거나 수정하는지 적어주세요."
    validations:
      required: true
  - type: textarea
    id: docs_purpose
    attributes:
      label: "🎯 작업 목적"
      value: |
        - [ ] 신규 문서 작성 (README, Wiki 등)
        - [ ] 기존 내용 최신화
        - [ ] API 명세서 업데이트
        - [ ] 오타 수정 및 가독성 개선
    validations:
      required: true
  - type: textarea
    id: docs_details
    attributes:
      label: "✔️ 세부 항목"
      value: |
        - [ ] 작업할 문서 명칭: 
        - [ ] 포함될 주요 내용:
    validations:
      required: true
