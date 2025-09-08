package beyond.ordersystem.ordering.repository;

import beyond.ordersystem.ordering.domain.OrderDetail;
import beyond.ordersystem.ordering.domain.Ordering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderingDetailRepository extends JpaRepository<OrderDetail, Long> {

    List<OrderDetail> findByOrdering(Ordering ordering);
}
