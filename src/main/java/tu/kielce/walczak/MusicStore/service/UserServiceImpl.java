package tu.kielce.walczak.MusicStore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tu.kielce.walczak.MusicStore.dao.AlbumRepository;
import tu.kielce.walczak.MusicStore.dao.OrderRepository;
import tu.kielce.walczak.MusicStore.dto.UserPreferences;
import tu.kielce.walczak.MusicStore.entity.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService{

    private OrderRepository orderRepository;
    private AlbumRepository albumRepository;

    @Autowired
    public UserServiceImpl(OrderRepository orderRepository, AlbumRepository albumRepository) {
        this.orderRepository = orderRepository;
        this.albumRepository = albumRepository;
    }

    private <T> void increaseOccurrence(T item, Map<T, Double> map){
        if (map.containsKey(item)) {
            double val = map.get(item);
            map.replace(item, val+1);
        } else map.put(item, 1.0);
    }

    @Override
    public UserPreferences getUserPreferencesByEmail(String email) {
        List<Order> orderList = orderRepository.findByCustomerEmail(email);
        long items = 0;
        Map<String, Double> weightedGenres = new HashMap<>();
        Map<String, Double> weightedSubgenres = new HashMap<>();
        Map<String, Double> weightedArtists = new HashMap<>();
        Map<String, Double> years = new HashMap<>();
        Map<String, Double> prices = new HashMap<>();
        for (Order o:orderList){
            for (OrderItem item: o.getOrderItems()){
                Album album = albumRepository.findById(item.getProductId()).get();
                for (Genre g: album.getGenres()) {
                    increaseOccurrence(g.getGenreName(), weightedGenres);
                }
                for (Subgenre s: album.getSubgenres()) {
                    increaseOccurrence(s.getSubgenreName(), weightedSubgenres);
                }
                increaseOccurrence(album.getArtist().getArtistName(), weightedArtists);
                increaseOccurrence(album.getAlbumYear(), years);
                increaseOccurrence(String.valueOf(album.getAlbumPrice()), prices);
                items++;
            }
        }
        return new UserPreferences(weightedGenres, weightedSubgenres, weightedArtists, years, prices, items);
    }
}
