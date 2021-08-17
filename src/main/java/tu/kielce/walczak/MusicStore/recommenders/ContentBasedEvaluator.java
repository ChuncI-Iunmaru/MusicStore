package tu.kielce.walczak.MusicStore.recommenders;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import tu.kielce.walczak.MusicStore.entity.Album;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContentBasedEvaluator {
    private final ItemBasedRecommender euclidGenresRecommender;
    private final ItemBasedRecommender euclidMixedRecommender;
    private final ItemBasedRecommender cosineAllRecommender;
    private final ItemBasedRecommender cosineMixedRecommender;
    private final DataModel itemModel;
    private final Map<Long, Album> fastMapAlbums;

    public ContentBasedEvaluator(DataModel itemModel, Map<Long, Album> fastMapAlbums) {
        this.itemModel = itemModel;
        this.fastMapAlbums = fastMapAlbums;
        euclidGenresRecommender = new GenericItemBasedRecommender(itemModel, new GenreEuclidItemDistance(fastMapAlbums));
        euclidMixedRecommender = new GenericItemBasedRecommender(itemModel, new BothEuclidItemSimilarity(fastMapAlbums));
        cosineAllRecommender = new GenericItemBasedRecommender(itemModel, new MixedItemSimilarity(fastMapAlbums, 1955, 2011));
        cosineMixedRecommender = new GenericItemBasedRecommender(itemModel, new CosineItemSimilarity(fastMapAlbums));
    }

    private ItemBasedRecommender setRecommenderByMode(ContentBasedMode mode){
        if (mode.equals(ContentBasedMode.EuclidOnGenre)) {
            return euclidGenresRecommender;
        } else if (mode.equals(ContentBasedMode.EuclidOnGenreAndSubgenre)) {
            return euclidMixedRecommender;
        } else if (mode.equals(ContentBasedMode.CosineOnGenreAndSubgenre)) {
            return cosineMixedRecommender;
        } else return cosineAllRecommender;
    }

    public Map<Long, Integer> getAlbumRecsCount(ContentBasedMode mode, int recSize){
        ItemBasedRecommender recommender = setRecommenderByMode(mode);
        Map<Long, Integer> albumCount = new HashMap<>();
        for (Long id : fastMapAlbums.keySet()){
            albumCount.put(id, 0);
        }
        try {
            for (Long id : fastMapAlbums.keySet()){
                List<RecommendedItem> recommendations = recommender.mostSimilarItems(id, recSize);
                for (RecommendedItem item : recommendations) {
                    int tmpValue = albumCount.get(item.getItemID());
                    albumCount.replace(item.getItemID(), ++tmpValue);
                }
            }
        } catch (TasteException e){
            e.printStackTrace();
        }
        return albumCount;
    }

    public Map<Long, Float> getMaxSimilarityValues(ContentBasedMode mode, int recSize){
        ItemBasedRecommender recommender = setRecommenderByMode(mode);
        Map<Long, Float> maxSimilarityValues = new HashMap<>();
        try {
            for (Long id : fastMapAlbums.keySet()){
                List<RecommendedItem> recommendations = recommender.mostSimilarItems(id, recSize);
                List<Float> similarityValues = recommendations.stream().map(RecommendedItem::getValue).collect(Collectors.toList());
                maxSimilarityValues.put(id, Collections.max(similarityValues));
            }
        } catch (TasteException e){
            e.printStackTrace();
        }
        return maxSimilarityValues;
    }

    public Map<Long, Float> getMinSimilarityValues(ContentBasedMode mode, int recSize){
        ItemBasedRecommender recommender = setRecommenderByMode(mode);
        Map<Long, Float> minSimilarityValues = new HashMap<>();
        try {
            for (Long id : fastMapAlbums.keySet()){
                List<RecommendedItem> recommendations = recommender.mostSimilarItems(id, recSize);
                List<Float> similarityValues = recommendations.stream().map(RecommendedItem::getValue).collect(Collectors.toList());
                minSimilarityValues.put(id, Collections.min(similarityValues));
            }
        } catch (TasteException e){
            e.printStackTrace();
        }
        return minSimilarityValues;
    }

    public List<Long> getUnrecommendedAlbums(ContentBasedMode mode, int recSize){
        Map<Long, Integer> albumCount = getAlbumRecsCount(mode, recSize);
        return albumCount.entrySet().stream().
                filter(entry -> entry.getValue() == 0).
                map(Map.Entry::getKey).
                collect(Collectors.toList());
    }
}
