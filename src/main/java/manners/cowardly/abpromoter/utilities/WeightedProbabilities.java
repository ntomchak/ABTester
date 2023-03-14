package manners.cowardly.abpromoter.utilities;

import java.util.Collection;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

public class WeightedProbabilities<T> {
    private TreeMap<Double, T> probabilities = new TreeMap<Double, T>();
    private double max = 0;

    /**
     * Negative and 0 probabilities disregarded, O(nlogn)
     * 
     * @param probabilities
     */
    public WeightedProbabilities(Collection<Probability<T>> probabilities) {
        double cumulative = 0;
        for (Probability<T> prob : probabilities) {
            double p = prob.getProbability();
            if (p > 0)
                cumulative += p;
            this.probabilities.put(cumulative, prob.getValue());
        }
        max = cumulative;
    }

    public WeightedProbabilities() {
    }

    public boolean isEmpty() {
        return probabilities.isEmpty();
    }

    /**
     * null if distribution is empty, O(logn)
     * 
     * @return
     */
    public T sample() {
        if (probabilities.isEmpty())
            return null;
        double roll = ThreadLocalRandom.current().nextDouble(0, max);
        return probabilities.ceilingEntry(roll).getValue();
    }

    /**
     * Add to the distribution, O(logn)
     * 
     * @param t
     * @param prob
     * @return return false if the probability was <= 0 thus not able to be added
     */
    public boolean add(T t, double prob) {
        if (prob <= 0)
            return false;
        max += prob;
        probabilities.put(max, t);
        return true;
    }

    public static class Probability<T> {
        private double probability;
        private T value;

        public Probability(double probability, T value) {
            this.probability = probability;
            this.value = value;
        }

        public double getProbability() {
            return probability;
        }

        public T getValue() {
            return value;
        }
    }
}
