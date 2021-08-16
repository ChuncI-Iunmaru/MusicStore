package tu.kielce.walczak.MusicStore.service;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastMap;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.SpearmanCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tu.kielce.walczak.MusicStore.dao.AlbumRepository;
import tu.kielce.walczak.MusicStore.dao.CustomerRepository;
import tu.kielce.walczak.MusicStore.dao.OrderRepository;
import tu.kielce.walczak.MusicStore.dto.AlbumWrapper;
import tu.kielce.walczak.MusicStore.entity.Album;
import tu.kielce.walczak.MusicStore.entity.Customer;
import tu.kielce.walczak.MusicStore.entity.Order;
import tu.kielce.walczak.MusicStore.entity.OrderItem;
import tu.kielce.walczak.MusicStore.recommenders.CosineItemSimilarity;
import tu.kielce.walczak.MusicStore.recommenders.GenreEuclidItemDistance;
import tu.kielce.walczak.MusicStore.recommenders.MixedItemSimilarity;
import tu.kielce.walczak.MusicStore.recommenders.BothEuclidItemSimilarity;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private AlbumRepository albumRepository;
    private OrderRepository orderRepository;
    private CustomerRepository customerRepository;

    private final Random random = new Random();

    private ItemBasedRecommender euclidGenresRecommender;
    private ItemBasedRecommender euclidSubgenresRecommender;
    private ItemBasedRecommender mixedRecommender;
    private ItemBasedRecommender cosineRecommender;

    private UserBasedRecommender spearmanNRecommender;
    private UserBasedRecommender spearmanTRecommender;
    private UserBasedRecommender pearsonNRecommender;
    private UserBasedRecommender pearsonTRecommender;

    private DataModel userModel;
    private DataModel itemModel;

    private Map<Long, Album> fastMapAlbums = new FastMap<>();

    @Autowired
    public RecommendationServiceImpl(AlbumRepository albumRepository, OrderRepository orderRepository, CustomerRepository customerRepository) {
        this.albumRepository = albumRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    // To musi być odpalone po @PostConstruct - najlepiej przy requescie, inaczej nie działa prawidłowo lazy init - musi być sesja hibernate
    @Transactional
    public void fillDataFromDB() {
        List<Long> allIds = albumRepository.findAll().stream().map(Album::getAlbumId).collect(Collectors.toList());
        for (Long id : allIds) {
            Album a = albumRepository.findById(id).get();
            // !!!Bez tego wypisywania do wrzuca do kolekcji albumy z niezaincijalizowanym genre/subgenre
            System.out.print("Dodawanie albumu "+a.getAlbumTitle()+": ");
            System.out.print(a.getGenres()+"; ");
            System.out.println(a.getSubgenres());
            fastMapAlbums.put(a.getAlbumId(), a);
        }
        this.userModel = fillPreferencesFromDB();
        this.itemModel = getDummyAllItems();
        // Później przenieść tu tworzenie rekomenerów z tego nowego modelu - one i tak wymagają pełnej fast map do działania
        createRecommenders();
    }

    private void createRecommenders(){
        try {
            System.out.println("Tworzenie rekomenderów");
            euclidGenresRecommender = new GenericItemBasedRecommender(itemModel, new GenreEuclidItemDistance(fastMapAlbums));
            euclidSubgenresRecommender = new GenericItemBasedRecommender(itemModel, new BothEuclidItemSimilarity(fastMapAlbums));
            mixedRecommender = new GenericItemBasedRecommender(itemModel, new MixedItemSimilarity(fastMapAlbums, 1955, 2011));
            cosineRecommender = new GenericItemBasedRecommender(itemModel, new CosineItemSimilarity(fastMapAlbums));

            UserSimilarity spearmanCorrelationSimilarity = new SpearmanCorrelationSimilarity(userModel);
            UserSimilarity pearsonSimilarity = new PearsonCorrelationSimilarity(userModel);

            UserNeighborhood spearmanNNeighborhood = new NearestNUserNeighborhood(2500, spearmanCorrelationSimilarity, userModel);
            spearmanNRecommender = new GenericUserBasedRecommender(userModel, spearmanNNeighborhood, spearmanCorrelationSimilarity);

            UserNeighborhood spearmanTNeighborhood = new ThresholdUserNeighborhood(0.001, spearmanCorrelationSimilarity, userModel);
            spearmanTRecommender = new GenericUserBasedRecommender(userModel, spearmanTNeighborhood, spearmanCorrelationSimilarity);

            UserNeighborhood pearsonNNeighborhood = new NearestNUserNeighborhood(2500, pearsonSimilarity, userModel);
            pearsonNRecommender = new GenericUserBasedRecommender(userModel, pearsonNNeighborhood, spearmanCorrelationSimilarity);

            UserNeighborhood pearsonTNeighborhood = new ThresholdUserNeighborhood(0.001, pearsonSimilarity, userModel);
            pearsonTRecommender = new GenericUserBasedRecommender(userModel, pearsonTNeighborhood, spearmanCorrelationSimilarity);
        } catch (TasteException e) {
            e.printStackTrace();
        }
    }

    private DataModel getDummyPreferences() {
        // Get all album ids
        List<Long> albumIds = albumRepository.findAll().stream().map(Album::getAlbumId).collect(Collectors.toList());
        // Assign them all to one fictional user with pref 1
        FastByIDMap<PreferenceArray> preferences = new FastByIDMap<>();
        //PreferenceArray prefsForUser = new GenericUserPreferenceArray(10);
        PreferenceArray prefsForUser = new GenericUserPreferenceArray(albumIds.size());
        prefsForUser.setUserID(0, 1L);
        int index = 0;
        for (Long id : albumIds) {
            prefsForUser.setItemID(index, id);
            prefsForUser.setValue(index, random.nextFloat() % 4);
            index++;
        }
        preferences.put(1L, prefsForUser);

        PreferenceArray prefsForSecondUser = new GenericUserPreferenceArray(3);
        prefsForSecondUser.setUserID(0, 2L);
        prefsForSecondUser.setItemID(0, 1L);
        prefsForSecondUser.setValue(0, 2.0f);
        prefsForSecondUser.setItemID(1, 2L);
        prefsForSecondUser.setValue(1, 3.0f);
        prefsForSecondUser.setItemID(2, 3L);
        prefsForSecondUser.setValue(2, 4.0f);
        preferences.put(2L, prefsForSecondUser);
        // return model
        return new GenericDataModel(preferences);
    }

    private DataModel getDummyAllItems(){
        List<Long> albumIds = albumRepository.findAll().stream().map(Album::getAlbumId).collect(Collectors.toList());
        FastByIDMap<PreferenceArray> preferences = new FastByIDMap<>();
        PreferenceArray prefsForUser = new GenericUserPreferenceArray(albumIds.size());
        prefsForUser.setUserID(-1, -1L);
        int index = 0;
        for (Long id : albumIds) {
            prefsForUser.setItemID(index, id);
            prefsForUser.setValue(index, 1.0f);
            index++;
        }
        preferences.put(-1L, prefsForUser);
        return new GenericDataModel(preferences);
    }

    private DataModel fillPreferencesFromDB() {
        FastByIDMap<PreferenceArray> preferences = new FastByIDMap<>();
        // Dla każdego klienta
        // Pobierz customer po ID z bazy danych
        List<Customer> allCustomers = customerRepository.findAll();
        // Tymczasowy limit dla szybszego restartu
//        for (Customer c : allCustomers.stream().limit(10).collect(Collectors.toList())) {
        for (Customer c : allCustomers) {
            // Pobierz dla niego listę zamówień
            List<Order> customersOrders = orderRepository.findAllByCustomer(c);
            // Dla każdego zamówienia pobierz order item, połącz je w jedną listę
            // Usuń duplikaty zwiększając odpowiednie quantity
            Map<Long, Integer> productIdToQuantity = new HashMap<>();
            for (Order o : customersOrders) {
                for (OrderItem i : o.getOrderItems()) {
                    if (productIdToQuantity.containsKey(i.getProductId())) {
                        int oldQuantity = productIdToQuantity.get(i.getProductId());
                        productIdToQuantity.replace(i.getProductId(), oldQuantity + i.getQuantity());
//                        System.out.println("Zmiana {"+i.getProductId() +", "+oldQuantity+i.getQuantity()+"}");
                    } else {
                        productIdToQuantity.put(i.getProductId(), i.getQuantity());
//                        System.out.println("Dodawanie {"+i.getProductId() +", "+i.getQuantity()+"}");
                    }
                }
            }
            // Dodaj każdy order item do prefs array z value równą quantity
            PreferenceArray tmpPrefs = new GenericUserPreferenceArray(productIdToQuantity.entrySet().size());
            tmpPrefs.setUserID(0, c.getId());
            int tmpIndex = 0;
            for (Long pid : productIdToQuantity.keySet()) {
                tmpPrefs.setItemID(tmpIndex, pid);
                tmpPrefs.setValue(tmpIndex, productIdToQuantity.get(pid));
                tmpIndex++;
            }
            preferences.put(c.getId(), tmpPrefs);
//            System.out.println("Dodawanie preferencji " + tmpPrefs);
        }

        DataModel model = new GenericDataModel(preferences);
        return model;
    }

    public void getDummyItemBasedRecs() throws TasteException {
        DataModel model = getDummyPreferences();
        //ItemSimilarity itemSimilarity = new PearsonCorrelationSimilarity(model);
        ItemSimilarity itemSimilarity = new GenreEuclidItemDistance(fastMapAlbums);

        ItemBasedRecommender recommender = new GenericItemBasedRecommender(model, itemSimilarity);
        long albumId = 2L;
        System.out.println("Recommendations for item: " + albumRepository.getOne(albumId).getAlbumTitle());

        System.out.println("\nMost similar items:");
        List<RecommendedItem> recommendations = recommender.mostSimilarItems(albumId, 10);
        for (RecommendedItem item : recommendations) {
            Album recommended = albumRepository.getOne(item.getItemID());
            System.out.println(recommended.getAlbumTitle() + " value: " + item.getValue());
        }
    }

    private List<AlbumWrapper> wrapRecommendations(List<RecommendedItem> recommendations) {
        List<AlbumWrapper> result = new ArrayList<>();
        for (RecommendedItem item : recommendations) {
            Album recommended = albumRepository.findById(item.getItemID()).get();
            //System.out.println(recommended.getAlbumTitle() + " value: " + item.getValue());
            if (item.getValue() < 0) {
                result.add(new AlbumWrapper(recommended, item.getValue() * -1.0));
            } else result.add(new AlbumWrapper(recommended, item.getValue()));
        }
        return result;
    }

    @Override
    public List<AlbumWrapper> getTestRecommendations(Long albumId, int size) {
        List<Album> foundAlbums = albumRepository.findAll().stream().limit(size).collect(Collectors.toList());
        foundAlbums.get(0).albumToVector();
        try {
            getDummyItemBasedRecs();
        } catch (TasteException e) {
            e.printStackTrace();
        }
        return foundAlbums.stream().map(album -> new AlbumWrapper(album, 1.5)).collect(Collectors.toList());
    }

    @Override
    public List<AlbumWrapper> getEuclidGenreRecs(Long albumId, int size) {
        try {
            List<RecommendedItem> recommendations = null;
            recommendations = euclidGenresRecommender.mostSimilarItems(albumId, size);
            return wrapRecommendations(recommendations);
        } catch (TasteException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<AlbumWrapper> getEuclidSubgenreRecs(Long albumId, int size) {
        try {
            List<RecommendedItem> recommendations = null;
            recommendations = euclidSubgenresRecommender.mostSimilarItems(albumId, size);
            return wrapRecommendations(recommendations);
        } catch (TasteException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<AlbumWrapper> getMixedRecs(Long albumId, int size) {
        try {
            List<RecommendedItem> recommendations = null;
            recommendations = mixedRecommender.mostSimilarItems(albumId, size);
            return wrapRecommendations(recommendations);
        } catch (TasteException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<AlbumWrapper> getCosineRecs(Long albumId, int size) {
        try {
            List<RecommendedItem> recommendations = null;
            recommendations = cosineRecommender.mostSimilarItems(albumId, size);
            return wrapRecommendations(recommendations);
        } catch (TasteException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<AlbumWrapper> getDummyUserRecs(Long userId, int size) {
        try {
            System.out.println("Przedmioty ocenione przez użytkownika:");
            for (Preference pref : userModel.getPreferencesFromUser(userId)) {
                System.out.println("itemId=" + pref.getItemID() + " , miara=" + pref.getValue());
            }
            List<RecommendedItem> recommendations = null;
            recommendations = spearmanNRecommender.recommend(userId, size);
            System.out.println(Arrays.toString(spearmanNRecommender.mostSimilarUserIDs(userId, 10)));
            System.out.println("Rekomendacje dla użytkownika:");
            if (recommendations.isEmpty()) System.out.println("Nie znaleziono żadnych rekomendacji!");
            for (RecommendedItem r : recommendations){
                System.out.println(r.getItemID() + " - " + r.getValue());
            }
            return wrapRecommendations(recommendations);
        } catch (TasteException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<AlbumWrapper> getUniversalUserRecs(Long userId, int size, Mode mode) {
        UserBasedRecommender recommender;
        if (mode.equals(Mode.PearsonNearest)){
            recommender = pearsonNRecommender;
        } else if (mode.equals(Mode.PearsonThreshold)){
            recommender = pearsonTRecommender;
        } else if (mode.equals(Mode.SpearmanNearest)){
            recommender = spearmanNRecommender;
        } else {
            recommender = spearmanTRecommender;
        }
        try {
            System.out.println("Przedmioty ocenione przez użytkownika:");
            for (Preference pref : userModel.getPreferencesFromUser(userId)) {
                System.out.println("itemId=" + pref.getItemID() + " , miara=" + pref.getValue());
            }
            List<RecommendedItem> recommendations = null;
            recommendations = recommender.recommend(userId, size);
            System.out.println(Arrays.toString(recommender.mostSimilarUserIDs(userId, 10)));
            System.out.println("Rekomendacje dla użytkownika:");
            if (recommendations.isEmpty()) System.out.println("Nie znaleziono żadnych rekomendacji!");
            for (RecommendedItem r : recommendations){
                System.out.println(r.getItemID() + " - " + r.getValue());
            }
            return wrapRecommendations(recommendations);
        } catch (TasteException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<AlbumWrapper> getBestsellers(int size) {
        // Pobierz wszystkie zamówienia złożone w ciągu zeszłego miesiąca
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -365);
        List<Order> recentOrders = orderRepository.findAllByDateCreatedAfter(cal.getTime());
        // Wyznacz dla każdego przedmiotu z nich liczbę kopii do id
        Map<Long, Long> productAmounts = new HashMap<>();
        for (Order o : recentOrders) {
            for (OrderItem i : o.getOrderItems()) {
                if (productAmounts.containsKey(i.getProductId())) {
                    long val = productAmounts.get(i.getProductId());
                    productAmounts.replace(i.getProductId(), val + i.getQuantity());
                } else productAmounts.put(i.getProductId(), (long) i.getQuantity());
            }
        }
        // Lista rozmiaru size posortowana według liczby kopii zamówionych
        List<Long> mostBoughtProducts = productAmounts
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .limit(size)
                .collect(Collectors.toList());
        // Opakuj każdą w album wrapper
        List<AlbumWrapper> result = new ArrayList<>();
        for (Long l : mostBoughtProducts) {
            Album tmp = albumRepository.findById(l).get();
            result.add(new AlbumWrapper(tmp, productAmounts.get(l)));
//            System.out.println("Bestseller " + tmp.getAlbumTitle() + " ilość " + productAmounts.get(l));
        }
        return result.stream().sorted(Comparator.comparingDouble(AlbumWrapper::getSimilarityValue).reversed()).collect(Collectors.toList());
    }

    public Map<Long, Long> getCoverageAndVarietyMetricsForMode(int mode) {
        if (mode == 1) {
            System.out.println("Euclidean on genre");
        } else if (mode == 2) {
            System.out.println("Euclidean on subgenre");
        } else if (mode == 3) {
            System.out.println("Cosine on genre & subgenre");
        } else if (mode == 4) {
            System.out.println("Mixed recs");
        }
        // Pobierz wszystkie dostępne id albumów
        List<Long> albumIds = albumRepository.findAll().stream().map(Album::getAlbumId).collect(Collectors.toList());
        // Dla każdego algorytmu rekomendacji
        Set<Long> cosineIds = new HashSet<>();
        Map<Long, Double> sumValues = new HashMap<>();
        Map<Long, Double> avgValues = new HashMap<>();
        Map<Long, Double> minValues = new HashMap<>();
        Map<Long, Double> maxValues = new HashMap<>();
        Map<Long, Long> occurences = new HashMap<>();
        System.out.println("Początek obliczania");
        for (Long albumId : albumIds) {
            //  Pobierz listę 5 elementów jak na produkcji
            List<AlbumWrapper> result = null;
            if (mode == 1) {
                result = getEuclidGenreRecs(albumId, 5);
            } else if (mode == 2) {
                result = getEuclidSubgenreRecs(albumId, 5);
            } else if (mode == 3) {
                result = getCosineRecs(albumId, 5);
            } else if (mode == 4) {
                result = getMixedRecs(albumId, 5);
            }
            //  Dodaj do odpowiedniego setu id tych albumów
            //  Zsumuj ich miary, pobierz max i min, oblicz średnią miar
            //  dodaj to do map z id tego album
            double min = -100, max = -100;
            double sum = 0;
            assert result != null;
            for (AlbumWrapper wrapper : result) {
                long wrapperId = wrapper.getAlbum().getAlbumId();
                cosineIds.add(wrapperId);
                if (occurences.containsKey(wrapperId)) {
                    long val = occurences.get(wrapperId);
                    occurences.replace(wrapperId, val + 1);
                } else occurences.put(wrapperId, 1L);
                if (wrapper.getSimilarityValue() < min) min = wrapper.getSimilarityValue();
                if (wrapper.getSimilarityValue() > max) max = wrapper.getSimilarityValue();
                sum += wrapper.getSimilarityValue();
            }
            sumValues.put(albumId, sum);
            minValues.put(albumId, min);
            maxValues.put(albumId, max);
            avgValues.put(albumId, sum / 5);
        }
        //  Oblicz pokrycie
        System.out.println("Obliczanie pokrycia");
        double coverage = (double) cosineIds.size() / albumIds.size();
        System.out.println("Pokrycie: " + coverage);
        // Wypisz ile razy się pojawia dany
        for (long albumId : albumIds) {
            if (occurences.containsKey(albumId)) {
                System.out.println(albumId + "," + occurences.get(albumId));
            } else System.out.println(albumId + "," + 0);
        }
        return occurences;
    }

    public double getEvaluation(double trainingSplit, double usersSplit) {
        RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
        RecommenderBuilder builder = new SpearmanRecommenderBuilder();
        try {
            return evaluator.evaluate(builder, null, userModel, trainingSplit, usersSplit);
        } catch (TasteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public class SpearmanRecommenderBuilder implements RecommenderBuilder {
        @Override
        public Recommender buildRecommender(DataModel dataModel) throws TasteException {
            UserSimilarity similarity = new SpearmanCorrelationSimilarity(userModel);
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, similarity, userModel);
            return new GenericUserBasedRecommender(userModel, neighborhood, similarity);
        }
    }
}
