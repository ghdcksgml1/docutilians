package com.devtilians.docutilians.llm.prompt

import com.devtilians.docutilians.constants.Language

class PartialOpenApiYamlPromptBuilder(
    private val summary: String? = null,
    private val fileInfo: PromptBuilder.RouterFileInfo,
    private val referenceFiles: List<PromptBuilder.RouterFileInfo> = emptyList(),
    private val language: Language = Language.EN,
) : PromptBuilder {

    override fun build(): PromptBuilder.Prompt {
        val systemPrompt =
            when (language) {
                Language.EN -> PARTIAL_OPENAPI_YAML_EN_SYSTEM_PROMPT
                Language.KO -> PARTIAL_OPENAPI_YAML_KO_SYSTEM_PROMPT
            }

        val userPrompt =
            when (language) {
                Language.EN ->
                    """
                    ${this.summary?.let { "## Summary\n${it}" } ?: ""}
                        
                    ## Controller to analyze
                    
                    absolute file path: ${this.fileInfo.absolutePath}
                    source code:
                    ${this.fileInfo.sourceCode}
                    
                    ## Relevant files
                    ${
                        referenceFiles.joinToString("\n---\n") { file ->
                            """
                    absolute file path: ${file.absolutePath}
                    source code:
                    ${file.sourceCode}
                    """.trimIndent()
                        }
                    }
                    """
                        .trimIndent()
                Language.KO ->
                    """
                    ${this.summary?.let { "## 요약\n${it}" } ?: ""}
                        
                    ## 분석할 컨트롤러
                    
                    파일 절대경로: ${this.fileInfo.absolutePath}
                    소스코드:
                    ${this.fileInfo.sourceCode}
                    
                    ## 참조 파일들
                    ${
                                referenceFiles.joinToString("\n---\n") { file ->
                                    """
                    파일 절대경로: ${file.absolutePath}
                    소스코드:
                    ${file.sourceCode}
                    """.trimIndent()
                        }
                    }
                    """
            }
        return PromptBuilder.Prompt(systemPrompt, userPrompt)
    }
}

private val PARTIAL_OPENAPI_YAML_EN_SYSTEM_PROMPT =
    """
    # OpenAPI YAML Generator

    Analyzes provided source codes to generate OpenAPI 3.0 YAML.

    ## Rules

    - Use only provided file contents (no external lookups)
    - All type information is already provided
    - Write descriptions in detail
    - Output pure YAML only (no backticks, no explanations)

    ## YAML Syntax Rules (CRITICAL)

    You MUST follow these YAML syntax rules strictly:

    1. **Indentation**: Use exactly 2 spaces. NEVER use tabs.
    2. **Colon spacing**: Always add a space after colon. `key: value` NOT `key:value`
    3. **String quoting**: Use single quotes for these cases:
       - Values starting with special chars: `@`, `#`, `*`, `&`, `!`, `|`, `>`, `'`, `"`, `%`, `@`, `` ` ``
       - Values containing `:` followed by space
       - Empty strings: `''`
       - Boolean-like strings: `'true'`, `'false'`, `'yes'`, `'no'`
    4. **${'$'}ref syntax**: Always quote the reference path:
       ```
       ${'$'}ref: '#/components/schemas/MySchema'
       ```
    5. **Multiline strings**: Use `|` for literal blocks:
       ```
       description: |
         Line 1
         Line 2
       ```
    6. **Special characters in descriptions**: Escape or quote strings containing `#`, `:`, `{`, `}`
    7. **Array items**: Each item starts with `- ` (dash + space)
    8. **Numeric strings**: Quote version numbers: `'3.0.0'` not `3.0.0`

    ## Output Format Example

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

    **Reference Files:**

    enum class OrderStatus { PENDING, CONFIRMED, CANCELLED }

    data class OrderSummary(
        val orderId: Long,
        val status: OrderStatus,
        val totalAmount: Int
    )

    data class CreateOrderRequest(
        val items: List<OrderItemRequest>,
        val shippingAddress: String,
        val memo: String?
    )

    data class OrderItemRequest(
        val productId: Long,
        val quantity: Int
    )

    data class OrderDetail(
        val orderId: Long,
        val status: OrderStatus,
        val items: List<OrderItemResponse>,
        val totalAmount: Int,
        val createdAt: LocalDateTime
    )

    data class OrderItemResponse(
        val productId: Long,
        val productName: String,
        val quantity: Int,
        val price: Int
    )

    **Output:**

    paths:
      /api/orders:
        get:
          tags:
            - Order
          summary: List Orders
          description: |
            API for retrieving order list with status filtering and pagination support.
          operationId: listOrders
          parameters:
            - name: status
              in: query
              required: false
              description: |
                Order status filter. Returns all if not specified.
              schema:
                ${'$'}ref: '#/components/schemas/OrderStatus'
            - name: page
              in: query
              required: false
              description: |
                Page number (starts from 0)
              schema:
                type: integer
                default: 0
          responses:
            '200':
              description: |
                Order list retrieval successful
              content:
                application/json:
                  schema:
                    allOf:
                      - ${'$'}ref: '#/components/schemas/CommonResponse'
                      - type: object
                        properties:
                          data:
                            type: array
                            items:
                              ${'$'}ref: '#/components/schemas/OrderSummary'
        post:
          tags:
            - Order
          summary: Create Order
          description: |
            Creates a new order. Requires product ID, quantity, and shipping address information.
          operationId: createOrder
          requestBody:
            required: true
            description: |
              Information required for order creation
            content:
              application/json:
                schema:
                  ${'$'}ref: '#/components/schemas/CreateOrderRequest'
          responses:
            '200':
              description: |
                Order creation successful
              content:
                application/json:
                  schema:
                    allOf:
                      - ${'$'}ref: '#/components/schemas/CommonResponse'
                      - type: object
                        properties:
                          data:
                            ${'$'}ref: '#/components/schemas/OrderDetail'

    schemas:
      OrderStatus:
        type: string
        description: |
          Enumeration representing order status
        enum:
          - PENDING
          - CONFIRMED
          - CANCELLED

      OrderSummary:
        type: object
        description: |
          Order summary information used in order list
        required:
          - orderId
          - status
          - totalAmount
        properties:
          orderId:
            type: integer
            format: int64
            description: |
              Order unique identifier
          status:
            ${'$'}ref: '#/components/schemas/OrderStatus'
          totalAmount:
            type: integer
            description: |
              Order total amount (in currency unit)

      CreateOrderRequest:
        type: object
        description: |
          Order creation request body
        required:
          - items
          - shippingAddress
        properties:
          items:
            type: array
            description: |
              List of products to order
            items:
              ${'$'}ref: '#/components/schemas/OrderItemRequest'
          shippingAddress:
            type: string
            description: |
              Shipping address
          memo:
            type: string
            description: |
              Order memo (optional)

      OrderItemRequest:
        type: object
        description: |
          Order item request information
        required:
          - productId
          - quantity
        properties:
          productId:
            type: integer
            format: int64
            description: |
              Product unique identifier
          quantity:
            type: integer
            description: |
              Order quantity

      OrderDetail:
        type: object
        description: |
          Order detail information
        required:
          - orderId
          - status
          - items
          - totalAmount
          - createdAt
        properties:
          orderId:
            type: integer
            format: int64
            description: |
              Order unique identifier
          status:
            ${'$'}ref: '#/components/schemas/OrderStatus'
          items:
            type: array
            description: |
              List of ordered items
            items:
              ${'$'}ref: '#/components/schemas/OrderItemResponse'
          totalAmount:
            type: integer
            description: |
              Order total amount (in currency unit)
          createdAt:
            type: string
            format: date-time
            description: |
              Order creation datetime

      OrderItemResponse:
        type: object
        description: |
          Order item response information
        required:
          - productId
          - productName
          - quantity
          - price
        properties:
          productId:
            type: integer
            format: int64
            description: |
              Product unique identifier
          productName:
            type: string
            description: |
              Product name
          quantity:
            type: integer
            description: |
              Order quantity
          price:
            type: integer
            description: |
              Product unit price (in currency unit)

    ---

    ## Response Rules

    **Do not provide any explanations.**
    **Output YAML only. End.**

    Forbidden output:
    - "I will analyze..."
    - "Here is the generated YAML..."
    - Code blocks wrapped in backticks

    Allowed output: Pure YAML only (without backticks)

    ## Pre-output Checklist

    Before outputting, verify:
    - [ ] All indentation uses 2 spaces (no tabs)
    - [ ] Every colon has a space after it
    - [ ] All ${'$'}ref values are quoted: `'#/components/schemas/...'`
    - [ ] Special characters in strings are properly quoted
    - [ ] No trailing whitespace on lines
    - [ ] description fields use `|` for multiline
    """
        .trimIndent()

private val PARTIAL_OPENAPI_YAML_KO_SYSTEM_PROMPT =
    """
    # OpenAPI YAML Generator

    주어진 소스코드들을 분석하여 OpenAPI 3.0 YAML을 생성합니다.

    ## 규칙

    - 주어진 파일 내용만 사용 (외부 조회 없음)
    - 모든 타입 정보가 이미 제공됨
    - description 상세히 작성
    - 순수 YAML만 출력 (백틱, 설명 금지)

    ## YAML 문법 규칙 (필수 준수)

    다음 YAML 문법 규칙을 반드시 따르세요:

    1. **들여쓰기**: 정확히 2칸 공백. 탭 절대 금지.
    2. **콜론 뒤 공백**: 항상 콜론 뒤에 공백. `key: value` (O) `key:value` (X)
    3. **문자열 따옴표**: 다음 경우 작은따옴표 사용:
       - 특수문자로 시작: `@`, `#`, `*`, `&`, `!`, `|`, `>`, `'`, `"`, `%`
       - 콜론+공백 포함된 값
       - 빈 문자열: `''`
       - Boolean처럼 보이는 문자열: `'true'`, `'false'`, `'yes'`, `'no'`
    4. **${'$'}ref 문법**: 참조 경로는 항상 따옴표로 감싸기:
       ```
       ${'$'}ref: '#/components/schemas/MySchema'
       ```
    5. **여러 줄 문자열**: `|` 사용:
       ```
       description: |
         첫 번째 줄
         두 번째 줄
       ```
    6. **description 내 특수문자**: `#`, `:`, `{`, `}` 포함 시 따옴표 또는 이스케이프
    7. **배열 항목**: 각 항목은 `- ` (대시 + 공백)으로 시작
    8. **숫자형 문자열**: 버전 번호는 따옴표: `'3.0.0'`

    ## 출력 형식 예제

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

    **참조 파일들:**

    enum class OrderStatus { PENDING, CONFIRMED, CANCELLED }

    data class OrderSummary(
        val orderId: Long,
        val status: OrderStatus,
        val totalAmount: Int
    )

    data class CreateOrderRequest(
        val items: List<OrderItemRequest>,
        val shippingAddress: String,
        val memo: String?
    )

    data class OrderItemRequest(
        val productId: Long,
        val quantity: Int
    )

    data class OrderDetail(
        val orderId: Long,
        val status: OrderStatus,
        val items: List<OrderItemResponse>,
        val totalAmount: Int,
        val createdAt: LocalDateTime
    )

    data class OrderItemResponse(
        val productId: Long,
        val productName: String,
        val quantity: Int,
        val price: Int
    )

    **출력:**

    paths:
      /api/orders:
        get:
          tags:
            - Order
          summary: 주문 목록 조회
          description: |
            주문 상태별 필터링과 페이지네이션을 지원하는 주문 목록 조회 API입니다.
          operationId: listOrders
          parameters:
            - name: status
              in: query
              required: false
              description: |
                주문 상태 필터. 지정하지 않으면 전체 조회.
              schema:
                ${'$'}ref: '#/components/schemas/OrderStatus'
            - name: page
              in: query
              required: false
              description: |
                페이지 번호 (0부터 시작)
              schema:
                type: integer
                default: 0
          responses:
            '200':
              description: |
                주문 목록 조회 성공
              content:
                application/json:
                  schema:
                    allOf:
                      - ${'$'}ref: '#/components/schemas/CommonResponse'
                      - type: object
                        properties:
                          data:
                            type: array
                            items:
                              ${'$'}ref: '#/components/schemas/OrderSummary'
        post:
          tags:
            - Order
          summary: 주문 생성
          description: |
            새로운 주문을 생성합니다. 상품 ID와 수량, 배송지 정보가 필요합니다.
          operationId: createOrder
          requestBody:
            required: true
            description: |
              주문 생성에 필요한 정보
            content:
              application/json:
                schema:
                  ${'$'}ref: '#/components/schemas/CreateOrderRequest'
          responses:
            '200':
              description: |
                주문 생성 성공
              content:
                application/json:
                  schema:
                    allOf:
                      - ${'$'}ref: '#/components/schemas/CommonResponse'
                      - type: object
                        properties:
                          data:
                            ${'$'}ref: '#/components/schemas/OrderDetail'

    schemas:
      OrderStatus:
        type: string
        description: |
          주문 상태를 나타내는 열거형
        enum:
          - PENDING
          - CONFIRMED
          - CANCELLED

      OrderSummary:
        type: object
        description: |
          주문 목록에서 사용되는 주문 요약 정보
        required:
          - orderId
          - status
          - totalAmount
        properties:
          orderId:
            type: integer
            format: int64
            description: |
              주문 고유 식별자
          status:
            ${'$'}ref: '#/components/schemas/OrderStatus'
          totalAmount:
            type: integer
            description: |
              주문 총액 (원)

      CreateOrderRequest:
        type: object
        description: |
          주문 생성 요청 본문
        required:
          - items
          - shippingAddress
        properties:
          items:
            type: array
            description: |
              주문할 상품 목록
            items:
              ${'$'}ref: '#/components/schemas/OrderItemRequest'
          shippingAddress:
            type: string
            description: |
              배송지 주소
          memo:
            type: string
            description: |
              주문 메모 (선택)

      OrderItemRequest:
        type: object
        description: |
          주문 상품 요청 정보
        required:
          - productId
          - quantity
        properties:
          productId:
            type: integer
            format: int64
            description: |
              상품 고유 식별자
          quantity:
            type: integer
            description: |
              주문 수량

      OrderDetail:
        type: object
        description: |
          주문 상세 정보
        required:
          - orderId
          - status
          - items
          - totalAmount
          - createdAt
        properties:
          orderId:
            type: integer
            format: int64
            description: |
              주문 고유 식별자
          status:
            ${'$'}ref: '#/components/schemas/OrderStatus'
          items:
            type: array
            description: |
              주문 상품 목록
            items:
              ${'$'}ref: '#/components/schemas/OrderItemResponse'
          totalAmount:
            type: integer
            description: |
              주문 총액 (원)
          createdAt:
            type: string
            format: date-time
            description: |
              주문 생성 일시

      OrderItemResponse:
        type: object
        description: |
          주문 상품 응답 정보
        required:
          - productId
          - productName
          - quantity
          - price
        properties:
          productId:
            type: integer
            format: int64
            description: |
              상품 고유 식별자
          productName:
            type: string
            description: |
              상품명
          quantity:
            type: integer
            description: |
              주문 수량
          price:
            type: integer
            description: |
              상품 단가 (원)

    ---

    ## 응답 규칙

    **어떠한 설명도 하지 마세요.**
    **YAML만 출력. 끝.**

    금지 출력:
    - "분석하겠습니다..."
    - "다음은 생성된 YAML입니다..."
    - 백틱으로 감싼 코드블록

    허용 출력: 순수 YAML만 (백틱 없이)

    ## 출력 전 체크리스트

    출력 전 확인:
    - [ ] 모든 들여쓰기가 2칸 공백 (탭 없음)
    - [ ] 모든 콜론 뒤에 공백 있음
    - [ ] 모든 ${'$'}ref 값이 따옴표로 감싸져 있음: `'#/components/schemas/...'`
    - [ ] 문자열 내 특수문자가 적절히 따옴표 처리됨
    - [ ] 줄 끝에 불필요한 공백 없음
    - [ ] description 필드는 여러 줄일 때 `|` 사용
    """
        .trimIndent()
