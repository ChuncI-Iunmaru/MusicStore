package tu.kielce.walczak.MusicStore.service;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastMap;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
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
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tu.kielce.walczak.MusicStore.dao.AlbumRepository;
import tu.kielce.walczak.MusicStore.dao.OrderRepository;
import tu.kielce.walczak.MusicStore.dto.AlbumWrapper;
import tu.kielce.walczak.MusicStore.entity.Album;
import tu.kielce.walczak.MusicStore.entity.Order;
import tu.kielce.walczak.MusicStore.entity.OrderItem;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private AlbumRepository albumRepository;
    private OrderRepository orderRepository;
    private final Random random = new Random();
    private ItemBasedRecommender euclidGenresRecommender;
    private ItemBasedRecommender euclidSubgenresRecommender;
    private ItemBasedRecommender mixedRecommender;
    private ItemBasedRecommender cosineRecommender;
    private UserBasedRecommender userSpearmanRecommender;
    private DataModel model;

    private Map<Long, Album> fastMapAlbums = new FastMap<>();

    @Autowired
    public RecommendationServiceImpl(AlbumRepository albumRepository, OrderRepository orderRepository) {
        this.albumRepository = albumRepository;
        this.orderRepository = orderRepository;
    }

    @PostConstruct
    private void onInit() throws TasteException {
        this.model = getRandomPreferences();
        euclidGenresRecommender = new GenericItemBasedRecommender(model, new GenreEuclidItemDistance());
        euclidSubgenresRecommender = new GenericItemBasedRecommender(model, new SubgenreEuclidItemDistance());
        mixedRecommender = new GenericItemBasedRecommender(model, new MixedItemSimilarity());
        cosineRecommender = new GenericItemBasedRecommender(model, new CosineItemSimilarity());

        //UserSimilarity similarity = new PearsonCorrelationSimilarity(model); - pearson naprawdę nie chce działać z tak rzadkimi danymi
        UserSimilarity similarity = new SpearmanCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, similarity, model);
//        for (int i = 1; i < 10000; i++) {
//            if (neighborhood.getUserNeighborhood(i).length > 0) {
//                System.out.println("userId=" + i + Arrays.toString(neighborhood.getUserNeighborhood(i)));
//            }
//
//        }
        userSpearmanRecommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
    }

    // To musi być odpalone po @PostConstruct - najlepiej przy requescie, inaczej nie działa prawidłowo lazy init - musi być sesja hibernate
    @Transactional
    public void fillFastMapFromDB() {
        List<Long> allIds = albumRepository.findAll().stream().map(Album::getAlbumId).collect(Collectors.toList());
        for (Long id : allIds){
            Album a = albumRepository.findById(id).get();
//            System.out.print("Dodawanie albumu "+a.getAlbumTitle()+": ");
//            System.out.print(a.getGenres()+"; ");
//            System.out.println(a.getSubgenres());
            fastMapAlbums.put(a.getAlbumId(), a);
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

    private DataModel getRandomPreferences() {
        // Same as above
        List<Long> albumIds = albumRepository.findAll().stream().map(Album::getAlbumId).collect(Collectors.toList());
        FastByIDMap<PreferenceArray> preferences = new FastByIDMap<>();
        PreferenceArray prefsForUser = new GenericUserPreferenceArray(albumIds.size());
        prefsForUser.setUserID(0, 1L);
        int index = 0;
        for (Long id : albumIds) {
            prefsForUser.setItemID(index, id);
            prefsForUser.setValue(index, 1.0f);
            index++;
        }
        // Wyłącz do testów
        // preferences.put(1L, prefsForUser);
        // Add n of users with 1-10 random prefs with values 1-3
        long userId = 2;
        for (int i = 0; i < 10000; i++) {
            int prefSize = 1 + random.nextInt(10);
            PreferenceArray tmpPrefs = new GenericUserPreferenceArray(prefSize);
            tmpPrefs.setUserID(0, userId);
            for (int j = 0; j < prefSize; j++) {
                tmpPrefs.setItemID(j, albumIds.get(random.nextInt(albumIds.size())));
                tmpPrefs.setValue(j, 1 + random.nextInt(3));
            }
            preferences.put(userId, tmpPrefs);
            userId++;
        }
        DataModel model = new GenericDataModel(preferences);
        return model;
    }

    public void getDummyItemBasedRecs() throws TasteException {
        DataModel model = getDummyPreferences();
        //ItemSimilarity itemSimilarity = new PearsonCorrelationSimilarity(model);
        ItemSimilarity itemSimilarity = new GenreEuclidItemDistance();

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
            result.add(new AlbumWrapper(recommended, item.getValue()));
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
            for (Preference pref : model.getPreferencesFromUser(userId)) {
                System.out.println("itemId=" + pref.getItemID() + " , miara=" + pref.getValue());
            }
            List<RecommendedItem> recommendations = null;
            recommendations = userSpearmanRecommender.recommend(userId, size);
            System.out.println("Rekomendacje dla użytkownika:");
            return wrapRecommendations(recommendations);
        } catch (TasteException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<AlbumWrapper> getBestsellers(int size) {
        // Pobierz wszystkie zamówienia złożone w ciągu zeszłego tygodnia
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
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
        }
        return result;
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
        for (long albumId: albumIds) {
            if (occurences.containsKey(albumId)){
                System.out.println(albumId+","+occurences.get(albumId));
            } else System.out.println(albumId+","+0);
        }
        return occurences;
    }

    public double getEvaluation(double trainingSplit, double usersSplit) {
        RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
        RecommenderBuilder builder = new SpearmanRecommenderBuilder();
        try {
            return evaluator.evaluate(builder, null, model, trainingSplit, usersSplit);
        } catch (TasteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    class GenreEuclidItemDistance implements ItemSimilarity {

        @Override
        public double itemSimilarity(long l, long l1) throws TasteException {
//            Album first = albumRepository.findById(l).get();
//            Album second = albumRepository.findById(l1).get();
//            // Tutaj zmiana znaku, bo te o najmniejszym dystansie są najbardziej podobne
//            return -first.getEuclidDistGenres(second);
            return fastMapAlbums.get(l).getEuclidDistGenres(fastMapAlbums.get(l1));
        }

        @Override
        public double[] itemSimilarities(long l, long[] longs) throws TasteException {
            return new double[0];
        }

        @Override
        public long[] allSimilarItemIDs(long l) throws TasteException {
            return new long[0];
        }

        @Override
        public void refresh(Collection<Refreshable> collection) {

        }
    }

    class SubgenreEuclidItemDistance implements ItemSimilarity {
        @Override
        public double itemSimilarity(long l, long l1) throws TasteException {
//            Album first = albumRepository.findById(l).get();
//            Album second = albumRepository.findById(l1).get();
//            // Tutaj zmiana znaku, bo te o najmniejszym dystansie są najbardziej podobne
//            return -first.getEuclidDistSubgenres(second);
            return fastMapAlbums.get(l).getEuclidDistSubgenres(fastMapAlbums.get(l1));
        }

        @Override
        public double[] itemSimilarities(long l, long[] longs) throws TasteException {
            return new double[0];
        }

        @Override
        public long[] allSimilarItemIDs(long l) throws TasteException {
            return new long[0];
        }

        @Override
        public void refresh(Collection<Refreshable> collection) {

        }
    }

    class MixedItemSimilarity implements ItemSimilarity {
        @Override
        public double itemSimilarity(long l, long l1) throws TasteException {
//            Album first = albumRepository.findById(l).get();
//            Album second = albumRepository.findById(l1).get();
            Album first = fastMapAlbums.get(l);
            Album second = fastMapAlbums.get(l1);
            double similarity = 0.0;
            if (first.getArtist().getArtistId().compareTo(second.getArtist().getArtistId()) == 0) {
                similarity += 5;
            }
            // Maksymalny dystans dla gatunków i podgatunków to 7 - nic w datasecie nie ma więcej - min to ofc 0
            // Znormalizuj do 0-1
            double genreDistance = 1 - (first.getEuclidDistGenres(second) / 7);
            double subgenreDistance = 1 - (first.getEuclidDistSubgenres(second) / 7);
            similarity -= genreDistance;
            similarity -= subgenreDistance;
            // System.out.println(first.getAlbumTitle() + "-" + second.getAlbumTitle() + ": wynikowe podobieństwo = " + similarity);
            // Im większy wynik tym lepiej
            return similarity;
        }

        @Override
        public double[] itemSimilarities(long l, long[] longs) throws TasteException {
            return new double[0];
        }

        @Override
        public long[] allSimilarItemIDs(long l) throws TasteException {
            return new long[0];
        }

        @Override
        public void refresh(Collection<Refreshable> collection) {

        }
    }

    class CosineItemSimilarity implements ItemSimilarity {
        @Override
        public double itemSimilarity(long l, long l1) throws TasteException {
//            Album first = albumRepository.findById(l).get();
//            Album second = albumRepository.findById(l1).get();
            Album first = fastMapAlbums.get(l);
            Album second = fastMapAlbums.get(l1);
            // Obie z taką samą wagą
            return 0.5 * first.getCosineGenres(second) + 0.5 * first.getCosineSubgenres(second);
        }

        @Override
        public double[] itemSimilarities(long l, long[] longs) throws TasteException {
            return new double[0];
        }

        @Override
        public long[] allSimilarItemIDs(long l) throws TasteException {
            return new long[0];
        }

        @Override
        public void refresh(Collection<Refreshable> collection) {

        }
    }

    public class SpearmanRecommenderBuilder implements RecommenderBuilder {
        @Override
        public Recommender buildRecommender(DataModel dataModel) throws TasteException {
            UserSimilarity similarity = new SpearmanCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, similarity, model);
            return new GenericUserBasedRecommender(model, neighborhood, similarity);
        }
    }
}
