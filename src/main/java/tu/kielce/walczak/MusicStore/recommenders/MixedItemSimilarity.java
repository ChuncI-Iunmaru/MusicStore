package tu.kielce.walczak.MusicStore.recommenders;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import tu.kielce.walczak.MusicStore.entity.Album;

import java.util.Collection;
import java.util.Map;

public class MixedItemSimilarity implements ItemSimilarity {

    private final Map<Long, Album> fastMapAlbums;

    public MixedItemSimilarity(Map<Long, Album> fastMapAlbums) {
        this.fastMapAlbums = fastMapAlbums;
    }

    @Override
    public double itemSimilarity(long l, long l1) throws TasteException {
//            Album first = albumRepository.findById(l).get();
//            Album second = albumRepository.findById(l1).get();
        Album first = this.fastMapAlbums.get(l);
        Album second = this.fastMapAlbums.get(l1);
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