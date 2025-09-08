package beyond.ordersystem.product.service;

import beyond.ordersystem.product.domain.Product;
import beyond.ordersystem.product.dto.*;
import beyond.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 1,  상품 등록
    public Long createProduct(ProductCreateDto dto, String email) {
        if (productRepository.findByName(dto.getName()).isPresent()) throw new IllegalArgumentException("중복되는 이름입니다.");

        Product product = productRepository.save(dto.toEntity(email));

        // 이미지 파일 s3에 올리고 url 가져오기
        MultipartFile productImage = dto.getProductImage();
        if (!productImage.isEmpty()) {
            // 이미지명 설정
            String fileName = "product-" + product.getId() + "-productImage";

            // 저장 객체 구성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(productImage.getContentType()) // image/jpeg ...
                    .build();

            // 이미지를 업로드 (byte 형태로)
            try {
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(productImage.getBytes()));
            } catch (IOException e) {
                // checked -> unchecked로 바꿔 전체 rollback 되도록 예외처리
                throw new IllegalArgumentException("이미지 업로드 실패");
            }
            // Image Url 추출
            String imgUrl = s3Client.utilities()
                    .getUrl(a -> a.bucket(bucket).key(fileName)) // key 추가
                    .toExternalForm();// TODO - 예외 처리 필요

            // 이미지 삭제 시
//            s3Client.deleteObject(a -> a.bucket(bucket).key(fileName));

            product.setProductImage(imgUrl);
        }

        return product.getId();
    }

    // 상품 목록 조회
    public Page<ProductResDto> getProductList(Pageable pageable, ProductSearchDto dto) {
        Specification specification = ProductSpecification.search(dto);

        Page<Product> productPages = productRepository.findAll(specification, pageable);

        return productPages.map(a -> ProductResDto.fromEntity(a));
    }

    // 상품 상세 조회
    public ProductResDto getProductDetail(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("없는 상품입니다."));
        ProductResDto productResDto = ProductResDto.fromEntity(product);
        return productResDto;
    }

    // 상품 수정
    public Long updateProduct(Long id, ProductUpdateDto dto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("없는 상품입니다."));
        // dto의 정보로 먼저 update
        product.updateProduct(dto.getName(), dto.getCategory(), dto.getPrice(), dto.getStockQuantity());

        // DB에서 파일명 가져오고 기존 사진 삭제 및 재업로드
        MultipartFile profileImage = dto.getProductImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            // 기존 이미지 파일명 가져오기
            String imageUrl = product.getProductImage();
            String originalFileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

            // 이미지 삭제 시 // TODO - 예외 처리
//            s3Client.deleteObject(a -> a.bucket(bucket).key(originalFileName));

            try {
                s3Client.deleteObject(a -> a.bucket(bucket).key(originalFileName));
            } catch (S3Exception e) {
                log.error("S3Exception while deleting object: {}", e.awsErrorDetails().errorMessage());
                // S3 오류 - 권한 없음, 존재하지 않음 등
            } catch (SdkClientException e) {
                // 네트워크 오류, 설정 문제 등
                log.error("SdkClientException while deleting object: {}", e.getMessage());
            }

            // 이미지명 설정
            String newFileName = "product-" + id + "-productImage";

            // 저장 객체 구성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(newFileName)
                    .contentType(profileImage.getContentType())
                    .build();

            try {
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(profileImage.getBytes()));
            } catch (IOException e) {
                throw new IllegalArgumentException("이미지 업로드 실패");
            }
            // image url 추출
            String imgUrl = s3Client.utilities()
                    .getUrl(a -> a.bucket(bucket).key(newFileName)) // <- key 추가
                    .toExternalForm(); // TODO - 예외 처리 필요

            product.setProductImage(imgUrl);
        } else {
            // 사용자가 이미지 없이 올렸을 때 삭제 로직(이미지 삭제만 하는 코드 넣어야 할 둣)
            product.setProductImage(null);
        }

        return product.getId();
    }

    // 상품 수량 감소
    public Long updateStock(ProductUpdateStockDto dto) {
        Product product = productRepository.findById(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException("없는 상품입니다."));

        if (product.getStockQuantity() < dto.getProductCount()) {
            throw new IllegalArgumentException("재고 부족");
        }
        product.decreaseQuantity(dto.getProductCount());

        return product.getId();
    }
}
