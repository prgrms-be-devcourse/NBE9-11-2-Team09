---
name: 리팩토링 템플릿
about: 리팩토링시 사용
title: ''
labels: ''
assignees: ''

---

name: "♻️ 3. REFACTOR ISSUE (코드 구조 개선)"
description: "코드 구조 개선 관련 이슈 템플릿"
title: "[refactor] "
labels: ["♻️ refactor"]
body:
  - type: textarea
    id: refactor_target
    attributes:
      label: "♻️ 개선 대상"
      description: "리팩토링이 필요한 코드나 패키지 위치를 적어주세요."
    validations:
      required: true
  - type: textarea
    id: refactor_purpose
    attributes:
      label: "🎯 개선 목적"
      description: "왜 리팩토링이 필요한가요?"
      value: |
        - [ ] 코드 가독성 향상
        - [ ] 성능 개선 
        - [ ] 중복 코드 제거
        - [ ] 구조 개선
        - [ ] 기타: 
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
    id: current_problem
    attributes:
      label: "📋 현재 문제점"
      description: "현재 코드의 어떤 부분이 문제인가요?"
    validations:
      required: true
  - type: textarea
    id: solution
    attributes:
      label: "💡 개선 방향"
      description: "어떻게 개선할 건가요?"
    validations:
      required: true
  - type: textarea
    id: links
    attributes:
      label: "🔗 관련 링크"
      description: "참고할 문서나 자료가 있다면 적어주세요."
