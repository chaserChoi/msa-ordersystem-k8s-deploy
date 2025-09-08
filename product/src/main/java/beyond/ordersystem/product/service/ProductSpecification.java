package beyond.ordersystem.product.service;

import beyond.ordersystem.product.domain.Product;
import beyond.ordersystem.product.dto.ProductSearchDto;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> search(ProductSearchDto dto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
//            predicateList.add(criteriaBuilder.equal(root.get("delYn"), "N"));
            if (dto.getProductName() != null) {
                predicateList.add(criteriaBuilder.like(root.get("name"), "%" + dto.getProductName() + "%"));
            }
            if (dto.getCategory() != null) {
                predicateList.add(criteriaBuilder.equal(root.get("category"), dto.getCategory()));
            }

            Predicate[] predicateArr = new Predicate[predicateList.size()];
            for (int i = 0; i < predicateList.size(); i++) {
                predicateArr[i] = predicateList.get(i);
            }

            // 위의 검색 조건들을 하나(한 줄)의 Predicate 객체로 만들어서 return
            Predicate predicate = criteriaBuilder.and(predicateArr);

            return predicate;
        };
    }
}
