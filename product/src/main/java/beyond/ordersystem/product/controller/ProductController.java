package beyond.ordersystem.product.controller;

import beyond.ordersystem.common.dto.CommonDto;
import beyond.ordersystem.product.dto.*;
import beyond.ordersystem.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    // 1. 상품 등록: /product/create
    @PostMapping("/create")
    public ResponseEntity<?> createProduct(@ModelAttribute @Valid ProductCreateDto dto, @RequestHeader("X-User-Email") String email) {
        Long id = productService.createProduct(dto, email);

        return new ResponseEntity<>(
                new CommonDto(id, HttpStatus.CREATED.value(), "상품 등록 완료")
                , HttpStatus.CREATED);
    }

    // 상품 목록 조회
    @GetMapping("/list")
    public ResponseEntity<?> getProductList(@PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable, ProductSearchDto dto) {
        Page<ProductResDto> productList = productService.getProductList(pageable, dto);

        return new ResponseEntity<>(
                new CommonDto(productList, HttpStatus.OK.value(), "상품 목록 조회 성공")
                , HttpStatus.OK);
    }

    // 상품 상세 조회 (-> 캐싱 처리 고려)
    @GetMapping("/detail/{inputId}")
    public ResponseEntity<?> getProductDetail(@PathVariable Long inputId) throws InterruptedException {
        // circuit test를 위한 지연
        // Thread.sleep(3000L);
        ProductResDto productResDto = productService.getProductDetail(inputId);
        return new ResponseEntity<>(
                new CommonDto(productResDto, HttpStatus.OK.value(), "상품 상세 조회 성공"),
                HttpStatus.OK
        );
    }

    // 상품 수정
    @PutMapping("/update/{inputId}")
    public ResponseEntity<?> updateProduct(@PathVariable Long inputId, @ModelAttribute @Valid ProductUpdateDto dto) {
        Long id = productService.updateProduct(inputId, dto);
        return new ResponseEntity<>(
                new CommonDto(id, HttpStatus.OK.value(), "상품 수정 성공"),
                HttpStatus.OK
        );
    }

    // 상품 수량 감소
    @PutMapping("/updatestock")
    public ResponseEntity<?> updateStock(@RequestBody ProductUpdateStockDto dto) {
        Long id = productService.updateStock(dto);
        return new ResponseEntity<>(new CommonDto(id, HttpStatus.OK.value(), "상품 수량 변경 성공"), HttpStatus.OK);
    }
}
