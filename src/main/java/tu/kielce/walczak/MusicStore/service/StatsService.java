package tu.kielce.walczak.MusicStore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tu.kielce.walczak.MusicStore.dao.AlbumRepository;
import tu.kielce.walczak.MusicStore.dao.OrderRepository;
import tu.kielce.walczak.MusicStore.entity.Order;
import tu.kielce.walczak.MusicStore.entity.OrderItem;

import java.util.*;

@Service
public class StatsService {
    private AlbumRepository albumRepository;
    private OrderRepository orderRepository;
    private final String[] months = {"Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec",
                                    "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień"};

    @Autowired
    public StatsService(AlbumRepository albumRepository, OrderRepository orderRepository) {
        this.albumRepository = albumRepository;
        this.orderRepository = orderRepository;
    }

    private long calculateUnitsSoldFromOrders(List<Order> orders){
        long result = 0L;
        for (Order o : orders) {
            for (OrderItem i : o.getOrderItems()) {
                result += i.getQuantity();
            }
        }
        return result;
    }

    private double calculateProfitsFromOrders(List<Order> orders){
        double result = 0;
        for (Order o : orders) {
            for (OrderItem i : o.getOrderItems()) {
                result += i.getQuantity()*i.getUnitPrice().doubleValue();
            }
        }
        return result;
    }

    public List<Long> getSalesByMonth(int year){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        Date dateStart = cal.getTime();
        cal.add(Calendar.MONTH, 1);
        Date dateEnd = cal.getTime();

        List<Long> results = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            List<Order> tmpOrders = orderRepository.findAllByDate(dateStart, dateEnd);
            results.add(calculateUnitsSoldFromOrders(tmpOrders));
            System.out.println("Sprzedaż dla " + months[i] + " - " + results.get(i));
            dateStart = dateEnd;
            cal.add(Calendar.MONTH, 1);
            dateEnd = cal.getTime();
        }
        return results;
    }

    public List<Double> getProfitsByMonth(int year){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        Date dateStart = cal.getTime();
        cal.add(Calendar.MONTH, 1);
        Date dateEnd = cal.getTime();

        List<Double> profits = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            List<Order> tmpOrders = orderRepository.findAllByDate(dateStart, dateEnd);
            profits.add(calculateProfitsFromOrders(tmpOrders));
            System.out.println("Sprzedaż dla " + months[i] + " - " + profits.get(i));
            dateStart = dateEnd;
            cal.add(Calendar.MONTH, 1);
            dateEnd = cal.getTime();
        }
        return profits;
    }
}
