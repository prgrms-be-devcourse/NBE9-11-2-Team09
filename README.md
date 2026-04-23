# 🅿️공영주차장 예약 시스템

공영주차장 예약 시스템 PARKEASY는 사용자가 웹에서 공영주차장 정보를 조회하고, 원하는 주차장을 선택해 예약 및 결제까지 진행할 수 있도록 만든 서비스입니다.

사용자는 회원가입 후 차량 정보를 등록하고, 주차장 목록과 상세 정보를 확인한 뒤 예약을 진행할 수 있습니다.

운전자가 목적지 근처 공영 주차장을 미리 검색하고 원하는 자리를 예약 및 결제까지 한 번에 처리할 수 있는 서비스를 목표로 했습니다. 

예약 가능 여부는 실시간으로 확인할 수 있습니다.

## API 문서
<img width="1823" height="1130" alt="image" src="https://github.com/user-attachments/assets/0743f2ef-d10c-4184-b3ec-e4e8aa9d1a21" />
<img width="1823" height="1457" alt="image" src="https://github.com/user-attachments/assets/2fdcc267-89f3-46fd-812b-11c0584c7f7f" />

## **기술 스택**

### **Backend**

<p> <img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"/> <img src="https://img.shields.io/badge/Spring Data JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white"/> <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white"/> </p>

### **Database**

<p> <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> </p>

### **Infra / Tool**

<p> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"/> <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white"/> <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black"/> <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white"/> </p>

### **ERD**
<img width="2428" height="1314" alt="image" src="https://github.com/user-attachments/assets/519dfc7c-fbd7-403a-9800-d9cd5dd74eb0" />


### **시스템 아키텍쳐**
<img width="1161" height="550" alt="image" src="https://github.com/user-attachments/assets/506621a4-e4a6-4d56-adbb-34e4b4a8557a" />


### **Project Structure**

```
src
└── main
├── java
│   └── com.example.parking
│       ├── domain
│       │   ├── admin
│       │   │   ├── reservation
│       │   │   └── user
│       │   ├── parkingLot
│       │   ├── parkingspot
│       │   ├── payment
│       │   ├── reservation
│       │   └── user
│       ├── global
│       │   ├── config
│       │   ├── exception
│       │   ├── response
│       │   └── security
│       └── ParkingApplication.java
└── resources
├── application.yml
└── application-local.yml
```

### **패키지 설명**

- domain.user: 회원가입, 로그인, 토큰 재발급, 내 정보 조회/수정, 회원탈퇴
- domain.admin.user: 관리자 회원 목록 조회
- domain.reservation: 주차 예약 생성, 조회, 취소
- domain.admin.reservation: 관리자 예약 조회
- domain.payment: 예약 기반 결제 처리
- domain.parkingLot: 공영주차장 조회 및 외부 데이터 연동
- domain.parkingspot: 주차면 조회 및 상태 관리
- global.security: JWT 인증/인가 및 Spring Security 설정
- global.exception: 전역 예외 처리
- global.response: 공통 응답 포맷
- 
### **팀원**

| **이름** | **담당** |
| --- | --- |
| 배재현 | 초기 세팅, Reservation 도메인 |
| 강원석 | Payment 도메인 |
| 이현태 | Parking Spot 도메인 |
| 최민호 | User 도메인 |
| 황지윤 | Parking Spot 도메인 |
