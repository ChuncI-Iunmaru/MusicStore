package tu.kielce.walczak.MusicStore.recommenders;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.SpearmanCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import tu.kielce.walczak.MusicStore.recommenders.UserBasedMode;

public class UserBasedEvaluator {
    private final DataModel userModel;
    private final double threshold;
    private final int neighborhoodSize;

    public UserBasedEvaluator(DataModel userModel, double threshold, int neighborhoodSize) {
        this.userModel = userModel;
        this.threshold = threshold;
        this.neighborhoodSize = neighborhoodSize;
    }

    public double getEvaluation(double trainingSplit, double usersSplit, UserBasedMode mode) {
        RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
        RecommenderBuilder builder;
        if (mode.equals(UserBasedMode.PearsonNearest)){
            builder = new PearsonNearestRecommenderBuilder();
        } else if (mode.equals(UserBasedMode.PearsonThreshold)){
            builder = new PearsonThresholdRecommenderBuilder();
        } else if (mode.equals(UserBasedMode.SpearmanNearest)){
            builder = new SpearmanNearestRecommenderBuilder();
        } else {
           builder = new SpearmanThresholdRecommenderBuilder();
        }
        try {
            return evaluator.evaluate(builder, null, userModel, trainingSplit, usersSplit);
        } catch (TasteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private class SpearmanNearestRecommenderBuilder implements RecommenderBuilder {
        @Override
        public Recommender buildRecommender(DataModel dataModel) throws TasteException {
            UserSimilarity similarity = new SpearmanCorrelationSimilarity(dataModel);
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(neighborhoodSize, similarity, dataModel);
            return new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
        }
    }

    private class SpearmanThresholdRecommenderBuilder implements RecommenderBuilder {
        @Override
        public Recommender buildRecommender(DataModel dataModel) throws TasteException {
            UserSimilarity similarity = new SpearmanCorrelationSimilarity(dataModel);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(threshold, similarity, dataModel);
            return new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
        }
    }

    private class PearsonThresholdRecommenderBuilder implements RecommenderBuilder {
        @Override
        public Recommender buildRecommender(DataModel dataModel) throws TasteException {
            UserSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(threshold, similarity, dataModel);
            return new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
        }
    }

    private class PearsonNearestRecommenderBuilder implements RecommenderBuilder {
        @Override
        public Recommender buildRecommender(DataModel dataModel) throws TasteException {
            UserSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(neighborhoodSize, similarity, dataModel);
            return new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
        }
    }
}
