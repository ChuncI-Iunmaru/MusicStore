package tu.kielce.walczak.MusicStore.recommenders;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import tu.kielce.walczak.MusicStore.entity.Album;

import java.util.Collection;
import java.util.Map;

public class BothEuclidItemSimilarity implements ItemSimilarity {

    private final Map<Long, Album> fastMapAlbums;

    public BothEuclidItemSimilarity(Map<Long, Album> fastMapAlbums) {
        this.fastMapAlbums = fastMapAlbums;
    }

    private double normalizeEuclid(double value) {
        return 1.0 / (1.0 + value);
    }

    @Override
    public double itemSimilarity(long l, long l1) throws TasteException {
        Album first = this.fastMapAlbums.get(l);
        Album second = this.fastMapAlbums.get(l1);
        double genreSimilarity = normalizeEuclid(first.getEuclidDistGenres(second));
        double subgenreSimilarity = normalizeEuclid(first.getEuclidDistSubgenres(second));
//        System.out.println("Euclid Similarity for "
//                + first.getAlbumTitle()
//                + ", "
//                + second.getAlbumTitle()
//                + " = " + genreSimilarity
//                + " + " + subgenreSimilarity);

        return 0.5 * genreSimilarity
                + 0.5 * subgenreSimilarity;
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