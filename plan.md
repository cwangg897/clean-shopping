# Clean Shopping — DDD 학습용 이커머스 프로젝트 기획서

> **목적**: 이커머스 도메인을 소재로 DDD(Domain-Driven Design)의 핵심 개념(애그리거트, VO, 도메인 서비스, 바운디드 컨텍스트, ID 참조 등)을 직접 코드로 적응하는 사이드 프로젝트.
>
> **방침**: 완성도보다 **DDD 감각 익히기**가 우선. 화려한 기능보다 도메인 모델링과 경계 설정에 집중한다.

---

## 1. 기술 스택

| 항목 | 선택 |
|---|---|
| 언어 | Kotlin (JVM 17) |
| 프레임워크 | Spring Boot 4 (Web MVC) |
| 영속성 | Spring Data JPA + MySQL |
| 빌드 | Gradle |
| 테스트 | JUnit5 + Kotlin Test |

---

## 2. 아키텍처 원칙

### 2.1 레이어 구조 (헥사고날 + DDD)

```
presentation (Controller)
        ↓
application (UseCase / ApplicationService)
        ↓
domain (Aggregate, Entity, VO, DomainService, Repository 인터페이스)
        ↑
infrastructure (Repository 구현, JPA Entity, 외부 API 어댑터)
```

- **domain**: 순수 비즈니스 로직. Spring/JPA 의존성 없음(가능한 한)
- **application**: 유스케이스 조립. 트랜잭션 경계
- **infrastructure**: 기술적 세부 구현
- **presentation**: HTTP 진입점

### 2.2 DDD 규칙 (꼭 지킬 것)

1. **애그리거트 간 참조는 ID로만** (`Order`는 `Product`가 아니라 `ProductId`를 가진다)
2. **트랜잭션은 하나의 애그리거트만 수정** (여러 애그리거트 변경은 도메인 이벤트로)
3. **불변 객체는 VO로** (`Money`, `Address`, `Email` 등)
4. **비즈니스 규칙은 도메인 안에서** (Service에서 if 떡칠 ❌)
5. **Repository는 애그리거트 루트 단위로만**

---

## 3. 도메인 정의 (8개 애그리거트)

| 애그리거트 | 루트 | 내부 Entity | VO |
|---|---|---|---|
| **User** | User | (없음 — Phase 1 기준) | Email, Password, Address, Profile |
| **Product** | Product | ProductOption, ProductImage | Price, ProductId |
| **Inventory** | Stock | StockMovement | ProductId, Quantity |
| **Cart** | Cart | CartItem | ProductId, Money (priceSnapshot) |
| **Order** | Order | OrderLine | ShippingInfo, Money, OrderStatus |
| **Payment** | Payment | (없음) | PaymentMethod, PaymentStatus, Money |
| **Delivery** | Delivery | (없음) | TrackingInfo, DeliveryStatus, Address |
| **Coupon** | Coupon | (없음) | DiscountPolicy, CouponCode |

### VO vs Entity 판별 기준

> **"식별자(ID)가 의미가 있는가?"** — 이 질문 하나가 전부.

| | VO | Entity |
|---|---|---|
| 같은 값이면 같은 것? | ✅ 값 동등성 | ❌ ID로 구분 |
| 변경 방식 | 통째로 교체 (불변) | 상태 변경 (가변) |
| 단독 존재 의미 | ❌ 소속됨 | ✅ 독립적 |

**예시**
- `Email("a@b.com")` → 값이 같으면 같은 이메일 → **VO**
- `Address("서울시 …")` → 값이 같으면 같은 주소 → **VO**
- `OrderLine` → 같은 상품 같은 수량이어도 "어느 주문의 어느 라인"이 중요 → **Entity**
- `DeliveryAddress`(배송지 목록) → 각각 별명/기본배송지 토글 필요 → **Entity로 승격**

### 도메인 간 관계 (전부 ID 참조)

```
User ──┬─→ Cart ──→ Product(ID) ──→ Inventory(ID)
       │     │
       │     ↓
       └─→ Order ──→ Payment
                ├─→ Delivery
                ├─→ Coupon(ID)
                └─→ OrderLine(Product ID + 가격 스냅샷)
```

---

## 4. 핵심 비즈니스 흐름

```
[1] 회원가입/로그인 (User)
     ↓
[2] 상품 조회 (Product + Inventory)
     ↓
[3] 장바구니 담기 (Cart) ─── 가격 스냅샷 저장
     ↓
[4] 쿠폰 적용 (Coupon)
     ↓
[5] 주문 생성 (Order) ─── 재고 차감 (Inventory)
     ↓
[6] 결제 (Payment) ─── 모의 PG 연동
     ↓
[7] 배송 (Delivery) ─── 상태 머신
```

---

## 5. 개발 로드맵 (단계별)

> 각 단계는 **도메인 모델 → 애플리케이션 → 인프라 → API → 테스트** 순서로 진행.

### 📌 Phase 1: User 도메인 (시작점)

**목표**: DDD 레이어 구조와 애그리거트 루트 감각 잡기

- [X] 패키지 구조 셋업 (`domain`, `application`, `infrastructure`, `presentation`)
- [X] `User` 애그리거트 루트 정의
- [X] `Email`, `Password`, `Address` VO 정의
- [ ] `UserRepository` 인터페이스 (domain) + JPA 구현체 (infrastructure)
- [ ] 회원가입 UseCase
- [ ] 회원 조회 UseCase
- [ ] REST API: `POST /api/users`, `GET /api/users/{id}`
- [ ] 도메인 단위 테스트 (Email 검증, Password 정책 등)

**학습 포인트**
- VO와 Entity 구분 감각
- Repository 인터페이스를 왜 domain에 두는지 (의존성 역전)
- 도메인 로직을 Service에 두지 않고 Entity에 두는 연습

---

### 📌 Phase 2: Product + Inventory 도메인 (분리 연습)

**목표**: **하나의 개념을 두 애그리거트로 쪼개는** DDD 핵심 감각

- [ ] `Product` 애그리거트 (상품 정보, 가격, 옵션)
- [ ] `Money` VO (금액 계산 캡슐화)
- [ ] `Stock` 애그리거트 (재고 수량, 입출고 이력) — **Product와 별도**
- [ ] `Product`와 `Stock`은 `ProductId`로만 연결
- [ ] 상품 등록 / 조회 API
- [ ] 재고 입고 / 조회 API
- [ ] 재고 차감 시 동시성 처리 (낙관적 락 or 비관적 락)

**학습 포인트**
- "왜 Product에 stock 필드 하나 두면 안 되는가" 직접 체감
- 트랜잭션 경계 = 애그리거트 경계
- 동시성 제어와 락 전략

---

### 📌 Phase 3: Cart 도메인

**목표**: **애그리거트 간 ID 참조 + 스냅샷 저장** 패턴 익히기

- [ ] `Cart` 애그리거트 (UserId 기준 1:1)
- [ ] `CartItem` 엔티티 (`ProductId`, `priceSnapshot`, `quantity`)
- [ ] 장바구니 담기 / 수량 변경 / 삭제
- [ ] 장바구니 조회 시 현재 Product 가격과 스냅샷 가격 비교 (가격 변동 알림)

**학습 포인트**
- 다른 애그리거트 객체를 직접 들고 있지 않기
- 스냅샷 패턴 (불변성 유지)
- Cart는 왜 User와 다른 애그리거트인가

---

### 📌 Phase 4: Coupon 도메인

**목표**: **정책(Policy) 패턴**과 도메인 서비스 연습

- [ ] `Coupon` 애그리거트 (발급, 사용 상태)
- [ ] `DiscountPolicy` 인터페이스 + 구현체들
    - `FixedAmountDiscount` (정액 할인)
    - `PercentageDiscount` (정률 할인)
    - `MinOrderAmountPolicy` (최소 주문 금액 조건)
- [ ] 쿠폰 발급 / 조회 / 적용 가능 여부 확인

**학습 포인트**
- Specification 패턴
- 전략 패턴을 도메인에서 자연스럽게 쓰기
- 조건이 복잡할 때 if-else 대신 Policy 객체로 표현

---

### 📌 Phase 5: Order 도메인 (가장 복잡)

**목표**: 여러 애그리거트를 **도메인 이벤트로 협력**시키기

- [ ] `Order` 애그리거트 (`OrderLine` 컬렉션, 총액, 상태)
- [ ] `OrderStatus` 상태 머신 (PENDING → PAID → SHIPPED → DELIVERED → COMPLETED, CANCELLED)
- [ ] `ShippingInfo` VO
- [ ] 주문 생성 시 흐름:
    1. Cart → Order로 변환
    2. Coupon 적용 (있으면)
    3. Inventory 재고 차감
    4. Cart 비우기
- [ ] 도메인 이벤트: `OrderCreatedEvent`, `OrderCancelledEvent`

**학습 포인트**
- 한 트랜잭션 = 한 애그리거트 원칙을 어떻게 지키는가
- 도메인 이벤트로 후속 처리 분리
- Application Service의 역할 (조율자, not 비즈니스 로직)

---

### 📌 Phase 6: Payment 도메인

**목표**: 외부 시스템과의 경계 (Anti-Corruption Layer)

- [ ] `Payment` 애그리거트 (`OrderId`, 금액, 결제 수단, 상태)
- [ ] `PaymentStatus` 상태 머신 (READY → IN_PROGRESS → PAID → FAILED, CANCELLED, REFUNDED)
- [ ] PG 어댑터 인터페이스 (`PaymentGateway`) — domain
- [ ] Mock PG 구현체 — infrastructure
- [ ] `OrderCreatedEvent` 수신 → 결제 생성
- [ ] 결제 완료 → `PaymentCompletedEvent` 발행 → Order 상태 변경

**학습 포인트**
- 외부 의존성을 domain에서 인터페이스로만 알기
- 이벤트 기반 비동기 흐름

---

### 📌 Phase 7: Delivery 도메인

**목표**: 상태 머신을 풍부한 도메인 모델로 표현

- [ ] `Delivery` 애그리거트
- [ ] `DeliveryStatus` (READY → IN_TRANSIT → DELIVERED)
- [ ] `TrackingInfo` VO
- [ ] `PaymentCompletedEvent` 수신 → 배송 생성
- [ ] 배송 상태 변경 API
- [ ] `DeliveryCompletedEvent` → Order 상태 COMPLETED

**학습 포인트**
- 상태 변경의 비즈니스 규칙을 Entity 메서드로 (`delivery.startShipping()`, `delivery.complete()`)
- "상태 = enum 필드 하나"에서 벗어나기

---

## 6. 패키지 구조 (제안)

```
src/main/kotlin/com/cleanshopping/
├── CleanShoppingApplication.kt
│
├── user/
│   ├── domain/
│   │   ├── User.kt
│   │   ├── Email.kt
│   │   ├── Password.kt
│   │   ├── Address.kt
│   │   └── UserRepository.kt          ← 인터페이스
│   ├── application/
│   │   ├── RegisterUserUseCase.kt
│   │   └── GetUserUseCase.kt
│   ├── infrastructure/
│   │   ├── UserJpaEntity.kt
│   │   ├── UserJpaRepository.kt       ← Spring Data
│   │   └── UserRepositoryImpl.kt       ← UserRepository 구현
│   └── presentation/
│       ├── UserController.kt
│       └── dto/
│
├── product/
├── inventory/
├── cart/
├── coupon/
├── order/
├── payment/
├── delivery/
│
└── common/
    ├── domain/        (BaseEntity, DomainEvent 등)
    └── exception/
```

각 도메인 패키지는 **자기 완결적**으로 (다른 도메인 패키지 import 최소화).

---

## 7. 데이터베이스 전략

- 각 애그리거트당 1개 테이블 (또는 루트 + 자식 엔티티 테이블)
- **FK는 걸지 않는다** (애그리거트 간 ID 참조 원칙을 DB에서도 유지)
- 인덱스는 조회 패턴에 맞춰 추가
- 초기엔 `ddl-auto: update`, 안정화되면 Flyway 도입 고려

---

## 8. 학습 체크리스트

프로젝트 끝났을 때 다음 질문에 답할 수 있어야 함:

- [ ] Entity와 VO를 언제 어떻게 구분하는가?
- [ ] 왜 Repository를 domain 패키지에 두는가?
- [ ] 한 트랜잭션에서 두 애그리거트를 수정하면 안 되는 이유?
- [ ] 도메인 이벤트는 언제 발행하고 어떻게 처리하는가?
- [ ] Application Service와 Domain Service의 차이?
- [ ] "빈약한 도메인 모델(Anemic Domain Model)"이란 무엇이고 왜 안티패턴인가?
- [ ] 바운디드 컨텍스트가 패키지 분리와 어떻게 다른가?

---

## 9. 마일스톤

| 단계 | 예상 작업량 | 핵심 산출물 |
|---|---|---|
| Phase 1: User | 1주 | 회원가입/조회 API + 레이어 구조 정착 |
| Phase 2: Product + Inventory | 1.5주 | 상품/재고 분리 + 동시성 처리 |
| Phase 3: Cart | 0.5주 | 스냅샷 패턴 적용된 장바구니 |
| Phase 4: Coupon | 1주 | 정책 객체 기반 할인 로직 |
| Phase 5: Order | 2주 | 도메인 이벤트 도입, 흐름 완성 |
| Phase 6: Payment | 1주 | Mock PG + 이벤트 기반 결제 |
| Phase 7: Delivery | 0.5주 | 상태 머신 도메인 모델 |

> 총 ~7.5주 예상 (사이드 프로젝트 페이스 기준).
> 학습이 목적이니 일정에 쫓기지 말고 **각 단계에서 "왜 이렇게 하는지"를 글로 정리**하는 것을 권장.

---

## 10. 다음 액션

**Phase 1: User 도메인부터 시작.**
1. 패키지 구조 만들기
2. `User` 애그리거트 + `Email`, `Password`, `Address` VO 작성
3. `UserRepository` 인터페이스 정의
4. JPA 구현체 작성
5. 회원가입 UseCase + Controller
6. 테스트 작성
