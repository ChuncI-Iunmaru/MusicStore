package tu.kielce.walczak.MusicStore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tu.kielce.walczak.MusicStore.dao.CustomerRepository;
import tu.kielce.walczak.MusicStore.dto.Purchase;
import tu.kielce.walczak.MusicStore.dto.PurchaseResponse;
import tu.kielce.walczak.MusicStore.entity.Customer;
import tu.kielce.walczak.MusicStore.entity.Order;
import tu.kielce.walczak.MusicStore.entity.OrderItem;

import java.util.Set;
import java.util.UUID;

@Service
public class CheckoutServiceImpl implements CheckoutService{

    private CustomerRepository customerRepository;

    @Autowired
    public CheckoutServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public PurchaseResponse placeOrder(Purchase purchase) {
        Order order = purchase.getOrder();

        String orderTrackingNumber = generateTrackingNumber();
        order.setOrderTrackingNumber(orderTrackingNumber);

        Set<OrderItem> orderItems = purchase.getOrderItems();
        orderItems.forEach(item -> order.add(item));

        Customer customer = purchase.getCustomer();
        String email = customer.getEmail();

        Customer customerFromDB = customerRepository.findByEmail(email);
        if (customerFromDB != null) {
            customer = customerFromDB;
        }

        customer.add(order);

        customerRepository.save(customer);

        return new PurchaseResponse(orderTrackingNumber);
    }

    private String generateTrackingNumber() {
        return UUID.randomUUID().toString();
    }
}
