package tu.kielce.walczak.MusicStore.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import tu.kielce.walczak.MusicStore.entity.Order;

import java.util.Date;
import java.util.List;

@RepositoryRestResource
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomerEmailOrderByDateCreatedDesc(@Param("email") String email, Pageable pageable);
    List<Order> findAllByDateCreatedAfter(Date startDate);
    List<Order> findByCustomerEmail(String email);
}