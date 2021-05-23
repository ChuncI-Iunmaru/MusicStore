package tu.kielce.walczak.MusicStore.dto;

import lombok.Data;
import tu.kielce.walczak.MusicStore.entity.Customer;
import tu.kielce.walczak.MusicStore.entity.Order;
import tu.kielce.walczak.MusicStore.entity.OrderItem;

import java.util.Set;

@Data
public class Purchase {
    private Customer customer;
    private Order order;
    private Set<OrderItem> orderItems;
}
