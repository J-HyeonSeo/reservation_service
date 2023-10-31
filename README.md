# 매장 예약 서비스

---

## 프로젝트 소개

- 본 프로젝트는, 매장 예약에 관한 서비스를 스프링부트로 구현한 백엔드 프로젝트입니다.
- ```회원가입 -> 로그인 -> 매장예약 -> 방문 -> 리뷰작성```의 프로세스를 따릅니다.
- 회원가입은 파트너와 일반회원의 회원가입을 구분하였습니다.
- 파트너는 매장을 추가하고, 이에 대한 예약을 승인 및 거절 할 권한이 있습니다.
- 유저는 예약을 수행하고, 방문한 상태에 대해 리뷰를 작성할 권한이 있습니다.
- 유저의 평균 리뷰 별점으로 매장의 별점이 정해집니다.
- 유저는 가나다순, 거리순, 별점순의 검색이 가능합니다.

## ERD 모델링

![image](https://github.com/J-HyeonSeo/reservation_service/assets/47245112/300946aa-6079-437d-8127-e20cbbbc3703)

## 인증

- 인증은 Json Web Token을 사용하였습니다.
- 로그인 시에, AccessToken과 RefreshToken이 발급되며,
- AccessToken의 수명은 30분, RefreshToken의 수명은 2주입니다.
- ```/auth/refresh```경로에 RefreshToken을 담아 보내면, 새로운 AccessToken를 발급받을 수 있습니다.
- RefreshToken은 Redis에 저장되며, Redis에 RefreshToken이 없을 경우, 기한이 남아있어도 AccessToken의 재발급이 불가능합니다.
- AccessToken의 수명이 끝났을 경우 ```401```코드를 반환하고, RefreshToken 또한 마찬가지입니다.
- ```403```코드는 해당 계정에 접근권한이 없는 경우에 응답합니다.

## 예약 데이터

- 예약은, 날짜별, 시간대별, 예약 수용 인원과 같은 형식으로 신청이 가능합니다.
- 파트너가 매장을 추가할 때, 다음과 같은 데이터를 넣을 수 있습니다.

|변수명|의미|예시|
|:---:|:---:|:---:|
|resOpenWeek|몇 주 뒤까지 예약을 오픈할 것인가?|1|
|resOpenCount|시간대 별로 오픈 할 수용인원|5|
|resOpenDays|무슨 요일에 오픈 할 것인가?|['MON', 'TUE', 'WED', ..]|
|resOpenTimes|무슨 시간대에 오픈 할 것인가?|['09:00', '10:00', '11:00', ..]|

- 유저는 이 정보를 바탕으로 ```/shop/user/detail/{shopId}```에서 날짜별, 시간대별 예약가능한 인원 정보를 얻을 수 있습니다.
- 이 정보를 통해, 다음과 같은 데이터를 포함하여 요청을 보낼 수 있습니다.

|변수명|의미|    예시     |
|:---:|:---:|:---------:|
|shopId|매장 번호|     1     |
|resDay|예약 할 날짜|'2023-07-15'|
|resTime|예약 시간대|'09:00'|
|count|예약 인원|2|
|note|비고|"예약수행"|

- 이를 통해, 예약이 수행가능하고, 다시 예약가능인원을 조회하게 되면, 인원이 줄어드는 것을 확인할 수 있습니다.
- 유저는 이에대한, 방문처리가 가능하게 됩니다.
- 예약은 다음과 같은 상태를 갖습니다.

|예약 상태|의미|
|:---:|:---:|
|READY|승인 대기 상태|
|ASSIGN|승인 및 방문 대기 상태|
|VISITED|방문 완료 상태|
|REJECT|방문 거절 상태|
|EXPIRED|예약 파기 및 노쇼 상태|

- 특히, REJECT와 EXPIRED는 매장과 회원의 행태를 파악하기에 중요한 지표가 됩니다.
- REJECT가 많을 수록 해당 매장은 거절을 많이 하는 매장으로 알 수 있고,
- EXPIRED가 많을 수록 해당 유저는 예약 파기 및 노쇼를 자주하는 유저로 알 수 있습니다.

## 예약 스케줄러

- 예약 스케줄러는, 매일 새벽 0시 0분 5초에 예약 관련 데이터를 수정합니다.
- 수정되는 항목은 다음과 같습니다.
- ```READY상태인 예약일이 오늘인 경우 => REJECT로 수정하여 강제로 거절 처리합니다.```
- ```ASSIGN상태인 예약일이 예약일 + 1인 경우 => EXPIRED로 수정하여 노쇼 상태로 처리합니다.```

## 키오스크 및 방문

- 키오스크는 기본적으로 파트너의 계정으로 로그인되어 있습니다.
- 키오스크는 ```/reservation/kiosk/{shopId}?phone=010-????-????```를 통해 방문을 위한 데이터를 가져올 수 있습니다.
- 방문 데이터가 조회되는 조건은 ```예약시간 10분전 ~ 예약시간```동안에만 조회가 가능하고,
- 해당 시간대 내에 방문을 수행해야, 예약 상태를 ```VISTIED``` 상태로 만들 수 있습니다.

## 리뷰 작성 및 별점 계산

- 방문 상태인 예약에 한해서, 리뷰를 작성할 수 있습니다.
- 리뷰 작성 기한은 신뢰성을 위해 방문일로부터 일주일 뒤까지 작성이 가능합니다.
- 리뷰를 작성할 때는, 별점과, 리뷰 내용을 작성할 수 있습니다.
- 리뷰를 작성하게 되면, 서버 내부에서는 다음과 같은 로직을 수행합니다.
- ```리뷰작성 -> 리뷰데이터 생성 -> 예약에 리뷰데이터 연결 -> 매장 별점 추가 -> 매장 별점 평균 계산```
- 리뷰를 작성, 수정, 삭제 할 때마다, 상점의 별점데이터가 변동됩니다.
- 리뷰 작성 및 수정은 일주일 내로 가능하지만, 리뷰 삭제는 언제든지 가능합니다.

## 동시성 이슈(Locking)

- 예약은 동시성 이슈가 발생할 우려가 높으므로, 매장 번호를 기준으로 Locking를 수행합니다.
- 분산 환경을 사용할 수 있기에, Redis Locking을 통해 다음과 같은 곳에 사용합니다.

|        Locking 사용 API         |Lock Key|       해제시점       |
|:-----------------------------:|:---:|:----------------:|
|      /reservation(POST)       |reservation-{shopId}|   컨트롤러 로직 종료시점   |
| /review/{reservationId}(POST) |review-shop-{shopId}| 별점 추가 서비스로직 종료시점 |       
|    /review/{reviewId}(PUT)    |review-shop-{shopId}| 별점 수정 서비스로직 종료시점 |       
|  /review/{reviewId}(DELETE)   |review-shop-{shopId}| 별점 차감 서비스로직 종료시점 |       

- 매장 번호를 기준으로 Locking을 수행하여, 동시성 이슈를 방지하고,
- 비정상적인 예약데이터 및 별점데이터가 저장되지 않도록 합니다.

## API

- 본 프로젝트의 API은 Swagger를 통해 기록되었습니다.
- 프로젝트 설정 후에, ```/swagger-ui/index.html```경로에서 확인 바랍니다.
- AuthController를 통해서, ```회원가입 및 로그인```을 수행합니다.
- ShopController를 통해서, ```매장 추가, 수정, 삭제, 조회```를 수행합니다.
- ReservationController를 통해서, ```예약 조회, 추가, 삭제, 승인, 거절```을 수행합니다.
- ReviewController를 통해서, ```리뷰 조회, 작성, 수정, 삭제```를 수행합니다.

## 프로젝트 세팅

- application.yml 에서, MySQL 및 Redis에 관한 정보를 해당 환경에 맞게 조정합니다.
- MySQL에는 기본적으로 reservation 스키마가 생성되어있어야 합니다.
- MySQL 및 Redis서버가 활성화되어 있어야 합니다.
- ReservationApplication.java를 실행합니다.

## 수행 순서

- AuthController를 통해, 회원가입 및 로그인을 수행합니다.(응답으로 AccessToken 및 RefreshToken)
- 모든 요청의 헤더에, ```AccessToken: Bearer 토큰~```와 같이 수행합니다.
- 파트너로 로그인하여, 매장을 추가합니다.
- 유저로 로그인하여, 매장을 검색하고(가나다, 거리순, 별점순), 해당 매장에 대한 예약을 조회하고, 예약을 수행합니다.
- 파트너로 로그인하여, 자신의 매장에 있는 예약목록을 확인하고, 승인 및 거절을 취합니다.
- 키오스크에서 예약 10분전부터 핸드폰번호를 사용하여, 예약을 조회합니다.
- 키오스크에서 방문 처리를 진행합니다.
- 방문 처리 완료 후에, 일주일 뒤까지 리뷰를 작성합니다.
- 파트너 및 유저는 매장을 조회하게 되면, 작성된 리뷰의 별점이 반영되는 것을 확인 할 수 있습니다.

## 테스트
- 테스트 도구 : ```JUnit5```, ```Mockito```
- 테스트 DB : ```H2-Database```, ```Redis(Locking)```
- 통합테스트 : ```23 Cases``` 작성됨
- 단위테스트 : ```137 Cases``` 작성됨
- 결과 : ```allPassed.```

## 개발 환경

|      환경       |              버전              |                     용도                      |
|:-------------:|:----------------------------:|:-------------------------------------------:|
| IntelliJ IDEA | 2022.3.3 (Community Edition) |                  통합 개발 환경                   |
|      JDK      |    Eclipse Temurin 17.0.7    |                  자바 개발 도구                   |
|     MySQL     |            8.0.33            |                    RDBMS                    |
|     Redis     |            7.0.11            | In-Memory, NoSQL, Locking, Token validation | 

## 프레임워크 및 라이브러리

|         이름         |버전|                용도                |
|:------------------:|:---:|:--------------------------------:|
|     SpringBoot     |2.7.13|              웹프레임워크              |
|      data-jpa      |2.7.13|               ORM                |
|     QueryDsl       |5.0.0 |     복잡한 쿼리문을 자바코드로 작성         |
|     data-redis     |2.7.13|      Redis-connector-driver      |
|      security      |2.7.13|                인증                |
|     validation     |2.7.13|            입력 데이터 검증             |
|        web         |2.7.13|               API                |
| springfox(Swagger) |3.0.0|              API문서화              |
|        jjwt        |0.9.1|            JWT발행 및 검증            |
|lombok|1.18.28| Getter, Setter, Constructor 자동생성 
|h2-database|2.1.214|          개발 및 통합 테스트 DB          |
|mysql-connector|8.0.33|      MySQL-connector-driver      |
