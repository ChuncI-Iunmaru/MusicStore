package tu.kielce.walczak.MusicStore.recommenders;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import tu.kielce.walczak.MusicStore.entity.Album;

import java.util.Collection;
import java.util.Map;

public class CosineItemSimilarity implements ItemSimilarity {

    private final Map<Long, Album> fastMapAlbums;

    public CosineItemSimilarity(Map<Long, Album> fastMapAlbums) {
        this.fastMapAlbums = fastMapAlbums;
    }
    @Override
    public double itemSimilarity(long l, long l1) throws TasteException {
//            Album first = albumRepository.findById(l).get();
//            Album second = albumRepository.findById(l1).get();
        Album first = this.fastMapAlbums.get(l);
        Album second = this.fastMapAlbums.get(l1);
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