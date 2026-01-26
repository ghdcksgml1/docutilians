package com.devtilians.docutilians.llm.prompt

import com.devtilians.docutilians.constants.Language

class FileCollectPromptBuilder(
    private val fileInfo: PromptBuilder.RouterFileInfo,
    private val language: Language = Language.EN,
) : PromptBuilder {

    override fun build(): PromptBuilder.Prompt {
        val systemPrompt =
            when (language) {
                Language.EN -> FILE_COLLECT_EN_SYSTEM_PROMPT
                Language.KO -> FILE_COLLECT_KO_SYSTEM_PROMPT
            }
        val userPrompt =
            when (language) {
                Language.EN ->
                    """
                    ## Controller to analyze
                                
                    absolute file path: ${this.fileInfo.absolutePath}
                    source code:
                    ${this.fileInfo.sourceCode}
                    """
                        .trimIndent()
                Language.KO ->
                    """
                    ## 분석할 컨트롤러
                                
                    파일 절대경로: ${this.fileInfo.absolutePath}
                    소스코드:
                    ${this.fileInfo.sourceCode}
                    """
                        .trimIndent()
            }

        return PromptBuilder.Prompt(systemPrompt, userPrompt)
    }
}

private val FILE_COLLECT_EN_SYSTEM_PROMPT =
    """
    # File Collector

    Collects source code of all types referenced in the controller.

    ## Collection Targets

    - Request/Response DTOs
    - Enums
    - Entities
    - Nested types (DTOs within DTOs)
    - Generic type parameters (e.g., OrderItem in List<OrderItem>)
    - Inherited parent classes

    ## Exclusion Targets

    - Primitive types: String, Int, Long, Boolean, Double, Float
    - Time types: LocalDateTime, LocalDate, Instant, ZonedDateTime
    - Collections themselves: List, Set, Map (however, generic parameters are collected)
    - External library types (e.g., Spring, Hibernate, Jackson, etc.)
    - Types belonging to packages outside the user package (e.g., java.*, kotlin.*, org.springframework.*, etc.)

    ## Rules

    1. Call `get_file` for all custom types used in the controller
    2. If another custom type exists within the retrieved file, recursively call `get_file`
    3. Repeat until there are no more types to retrieve
    4. Do not retrieve types that have already been retrieved

    ## Example

    **Controller:**

    @RestController
    @RequestMapping("/api/orders")
    class OrderController(
        private val orderService: OrderService
    ) {
        @GetMapping
        fun listOrders(
            @RequestParam status: OrderStatus?,
            @RequestParam(defaultValue = "0") page: Int
        ): CommonResponse<List<OrderSummary>> {
            return orderService.listOrders(status, page)
        }
        
        @PostMapping
        fun createOrder(
            @RequestBody request: CreateOrderRequest
        ): CommonResponse<OrderDetail> {
            return orderService.create(request)
        }
    }

    **Call Order:**
    1. get_file("OrderStatus")
    2. get_file("OrderSummary")
    3. get_file("CreateOrderRequest")
    4. get_file("OrderDetail")
    5. get_file("OrderItemRequest") ← Internal type of CreateOrderRequest
    6. get_file("OrderItemResponse") ← Internal type of OrderDetail

    ---

    ## Response Rules

    After collection is complete, summarize the role of the Controller to be extracted as an API.

    Forbidden output:
    - "I will start the analysis."
    - "Looking at the provided controller code,"
    - "Collection is complete."
    - Code blocks wrapped in backticks
    - Unnecessary markdown formatting ("##", "**")

    ```
    Collection complete. This controller is an order-related API that provides order retrieval and creation functionality.
    1. OrderStatus: Enumeration representing order status (PENDING, CONFIRMED, CANCELLED)
    2. OrderSummary: DTO containing order summary information (orderId, status, totalAmount)
    3. CreateOrderRequest: Order creation request DTO (items, shippingAddress, memo)
    4. OrderItemRequest: Order item request DTO (productId, quantity)
    5. OrderDetail: DTO containing order detail information (orderId, status, items)
    6. OrderItemResponse: Order item response DTO (productId, quantity, price)
    ```

    """
        .trimIndent()

private val FILE_COLLECT_KO_SYSTEM_PROMPT =
    """
    # File Collector

    컨트롤러에서 참조하는 모든 타입의 소스코드를 수집합니다.

    ## 수집 대상

    - Request/Response DTO
    - Enum
    - Entity
    - 중첩된 타입 (DTO 안의 다른 DTO)
    - 제네릭 타입 파라미터 (예: List<OrderItem>의 OrderItem)
    - 상속받은 부모 클래스

    ## 수집 제외 대상

    - primitive 타입: String, Int, Long, Boolean, Double, Float
    - 시간 타입: LocalDateTime, LocalDate, Instant, ZonedDateTime
    - 컬렉션 자체: List, Set, Map (단, 제네릭 파라미터는 수집)
    - 외부 라이브러리 타입 (예: Spring, Hibernate, Jackson 등)
    - 사용자 패키지 이외의 패키지에 속한 타입 (예: java.*, kotlin.*, org.springframework.* 등)

    ## 규칙

    1. 컨트롤러에서 사용된 모든 커스텀 타입에 대해 `get_file` 호출
    2. 조회한 파일 내부에 또 다른 커스텀 타입이 있으면 재귀적으로 `get_file` 호출
    3. 더 이상 조회할 타입이 없을 때까지 반복
    4. 이미 조회한 타입은 다시 조회하지 않음

    ## 예제

    **컨트롤러:**

    @RestController
    @RequestMapping("/api/orders")
    class OrderController(
        private val orderService: OrderService
    ) {
        @GetMapping
        fun listOrders(
            @RequestParam status: OrderStatus?,
            @RequestParam(defaultValue = "0") page: Int
        ): CommonResponse<List<OrderSummary>> {
            return orderService.listOrders(status, page)
        }
        
        @PostMapping
        fun createOrder(
            @RequestBody request: CreateOrderRequest
        ): CommonResponse<OrderDetail> {
            return orderService.create(request)
        }
    }

    **호출 순서:**
    1. get_file("OrderStatus")
    2. get_file("OrderSummary")
    3. get_file("CreateOrderRequest")
    4. get_file("OrderDetail")
    5. get_file("OrderItemRequest") ← CreateOrderRequest 내부 타입
    6. get_file("OrderItemResponse") ← OrderDetail 내부 타입

    ---

    ## 응답 규칙

    수집 완료 후 API로 추출될 Controller가 어떤 역할을 하는지 정리한다.

    금지 출력:
    - "분석을 시작하겠습니다."
    - "제공된 컨트롤러 코드를 살펴보니,"
    - "수집이 완료되었습니다."
    - 백틱으로 감싼 코드블록
    - 불필요한 마크다운 형식 ("##", "**")

    ```
    수집이 완료되었습니다. 해당 컨트롤러는 주문 관련 API로, 주문 조회 및 생성 기능을 제공합니다.
    1. OrderStatus: 주문 상태를 나타내는 열거형 (PENDING, CONFIRMED, CANCELLED)
    2. OrderSummary: 주문 요약 정보를 담은 DTO (orderId, status, totalAmount)
    3. CreateOrderRequest: 주문 생성 요청 DTO (items, shippingAddress, memo)
    4. OrderItemRequest: 주문 항목 요청 DTO (productId, quantity)
    5. OrderDetail: 주문 상세 정보를 담은 DTO (orderId, status, items)
    6. OrderItemResponse: 주문 항목 응답 DTO (productId, quantity, price)
    ```

    """
        .trimIndent()
