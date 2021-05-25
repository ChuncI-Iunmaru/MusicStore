package tu.kielce.walczak.MusicStore.service;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tu.kielce.walczak.MusicStore.dao.AlbumRepository;
import tu.kielce.walczak.MusicStore.dto.AlbumWrapper;
import tu.kielce.walczak.MusicStore.entity.Album;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private AlbumRepository albumRepository;
    private final Random random = new Random();
    private ItemBasedRecommender euclidGenresRecommender;
    private ItemBasedRecommender euclidSubgenresRecommender;
    private ItemBasedRecommender mixedRecommender;
    private ItemBasedRecommender cosineRecommender;
    private DataModel model;

    @PostConstruct
    private void onInit() {
        this.model = getDummyPreferences();
        euclidGenresRecommender = new GenericItemBasedRecommender(model, new GenreEuclidItemDistance());
        euclidSubgenresRecommender = new GenericItemBasedRecommender(model, new SubgenreEuclidItemDistance());
        mixedRecommender = new GenericItemBasedRecommender(model, new MixedItemSimilarity());
        cosineRecommender = new GenericItemBasedRecommender(model, new CosineItemSimilarity());
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

    @Autowired
    public RecommendationServiceImpl(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }


    @Override
    @Transactional
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

    private List<AlbumWrapper> wrapRecommendations(List<RecommendedItem> recommendations) {
        List<AlbumWrapper> result = new ArrayList<>();
        for (RecommendedItem item : recommendations) {
            Album recommended = albumRepository.findById(item.getItemID()).get();
            System.out.println(recommended.getAlbumTitle() + " value: " + item.getValue());
            result.add(new AlbumWrapper(recommended, item.getValue()));
        }
        return result;
    }

    @Override
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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

    class GenreEuclidItemDistance implements ItemSimilarity {

        @Override
        public double itemSimilarity(long l, long l1) throws TasteException {
            Album first = albumRepository.findById(l).get();
            Album second = albumRepository.findById(l1).get();
            // Tutaj zmiana znaku, bo te o najmniejszym dystansie są najbardziej podobne
            return -first.getEuclidDistGenres(second);
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
            Album first = albumRepository.findById(l).get();
            Album second = albumRepository.findById(l1).get();
            // Tutaj zmiana znaku, bo te o najmniejszym dystansie są najbardziej podobne
            return -first.getEuclidDistSubgenres(second);
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
            Album first = albumRepository.findById(l).get();
            Album second = albumRepository.findById(l1).get();
            double similarity = 0.0;
            if (first.getArtist().getArtistId().compareTo(second.getArtist().getArtistId()) == 0) {
                similarity += 5;
            }
            // Maksymalny dystans dla gatunków i podgatunków to 7 - nic w datasecie nie ma więcej - min to ofc 0
            // Znormalizuj do 0-1
            double genreDistance = 1 - (first.getEuclidDistGenres(second) / 7);
            double subgenreDistance = 1 - (first.getEuclidDistSubgenres(second) / 7);
            similarity-=genreDistance;
            similarity-=subgenreDistance;
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
            Album first = albumRepository.findById(l).get();
            Album second = albumRepository.findById(l1).get();
            // Obie z taką samą wagą
            return 0.5*first.getCosineGenres(second) + 0.5*first.getCosineSubgenres(second);
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
}
