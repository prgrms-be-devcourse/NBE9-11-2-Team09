---
name: 버그용 템플릿
about: 버그 수정시 사용
title: ''
labels: ''
assignees: ''

---

name: "🐛 2. BUG ISSUE (버그 수정)"
description: "버그 수정 관련 이슈 템플릿"
title: "[fix] "
labels: ["🐛 type: bug"]
body:
  - type: textarea
    id: bug_description
    attributes:
      label: "🐛 버그 현상"
      description: "어떤 문제가 발생했는지 상세히 적어주세요."
    validations:
      required: true
  - type: textarea
    id: reproduction_steps
    attributes:
      label: "🕹️ 재현 방법"
      value: |
        1. 어떤 페이지/기능으로 이동
        2. 어떤 버튼 클릭 또는 데이터 입력
        3. 발생하는 에러 내용
    validations:
      required: true
  - type: textarea
    id: expected_result
    attributes:
      label: "🎯 기대 결과"
      description: "정상적으로 작동한다면 어떤 결과가 나와야 하나요?"
    validations:
      required: true
  - type: textarea
    id: screenshots
    attributes:
      label: "📸 스크린샷 (선택)"
      description: "에러 로그나 화면 캡처가 있다면 첨부해 주세요."
