---
name: 이슈 템플릿
about: 기본 이슈 템플릿
title: ''
labels: ''
assignees: sorbet0404

---

name: "✨ 1. FEATURE ISSUE (신규 기능 추가)"
description: "기능 관련 이슈 템플릿"
title: "[feat] "
labels: ["✨ type: feature"]
body:
  - type: textarea
    id: feature_description
    attributes:
      label: "✨ 구현 기능"
      description: "추가하려는 기능에 대해 자세히 적어주세요."
    validations:
      required: true
  - type: textarea
    id: todo_list
    attributes:
      label: "✔️ 작업목록"
      value: |
        - [ ] TODO1
        - [ ] TODO2
        - [ ] TODO3
    validations:
      required: true
  - type: textarea
    id: links
    attributes:
      label: "🔗 관련 링크"
      description: "기능과 관련해 참고할 링크가 있다면 적어주세요."
