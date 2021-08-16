package tu.kielce.walczak.MusicStore.recommenders;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import tu.kielce.walczak.MusicStore.entity.Album;

import java.util.Collection;
import java.util.Map;

public class GenreEuclidItemDistance implements ItemSimilarity {

    private final Map<Long, Album> fastMapAlbums;

    public GenreEuclidItemDistance(Map<Long, Album> fastMapAlbums) {
        this.fastMapAlbums = fastMapAlbums;
    }

    private double normalizeEuclid(double value) {
        return 1.0 / (1.0 + value);
    }

    @Override
    public double itemSimilarity(long l, long l1) throws TasteException {
//            Album first = albumRepository.findById(l).get();
//            Album second = albumRepository.findById(l1).get();
//            // Tutaj zmiana znaku, bo te o najmniejszym dystansie są najbardziej podobne
//            return -first.getEuclidDistGenres(second);
        return -this.fastMapAlbums.get(l).getEuclidDistGenres(this.fastMapAlbums.get(l1));
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
