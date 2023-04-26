package manners.cowardly.abpromoter.utilities;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

public class DiscreteProbabilityDistribution<T> {
    private TreeMap<Double, T> probabilities = new TreeMap<Double, T>();
    private double cumulative = 0;

    public void setContents(DiscreteProbabilityDistribution<T> toCopy) {
        this.probabilities = toCopy.probabilities;
        this.cumulative = toCopy.cumulative;
    }

    public Collection<T> values() {
        return Collections.unmodifiableCollection(probabilities.values());
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
        double roll = ThreadLocalRandom.current().nextDouble(0, cumulative);
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
        cumulative += prob;
        probabilities.put(cumulative, t);
        return true;
    }
}
