/**
 * ShortHistogram.java
 * Written by Gil Tene of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Gil Tene
 * @version 1.1.5
 */

package org.HdrHistogram;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * <h3>A High Dynamic Range (HDR) Histogram using a <b><code>short</code></b> count type </h3>
 * <p>
 * See package description for {@link org.HdrHistogram} for details.
 */

public class ShortHistogram extends AbstractHistogram {
    long totalCount;
    final short[] counts;

    @Override
    long getCountAtIndex(final int index) {
        return counts[index];
    }

    @Override
    void incrementCountAtIndex(final int index) {
        counts[index]++;
    }

    @Override
    void addToCountAtIndex(final int index, final long value) {
        counts[index] += value;
    }

    @Override
    void clearCounts() {
        java.util.Arrays.fill(counts, (short) 0);
        totalCount = 0;
    }

    /**
     * @inheritDoc
     */
    @Override    public ShortHistogram copy() {
      ShortHistogram copy = new ShortHistogram(highestTrackableValue, numberOfSignificantValueDigits);
      copy.add(this);
      return copy;
    }

    /**
     * @inheritDoc
     */
    @Override
    public ShortHistogram copyCorrectedForCoordinatedOmission(final long expectedIntervalBetweenValueSamples) {
        ShortHistogram toHistogram = new ShortHistogram(highestTrackableValue, numberOfSignificantValueDigits);
        toHistogram.addWhileCorrectingForCoordinatedOmission(this, expectedIntervalBetweenValueSamples);
        return toHistogram;
    }

    @Override
    long getTotalCount() {
        return totalCount;
    }

    @Override
    void setTotalCount(final long totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    void incrementTotalCount() {
        totalCount++;
    }

    @Override
    void addToTotalCount(long value) {
        totalCount += value;
    }

    /**
     * Provide a (conservatively high) estimate of the Histogram's total footprint in bytes
     *
     * @return a (conservatively high) estimate of the Histogram's total footprint in bytes
     */
    @Override
    public int getEstimatedFootprintInBytes() {
        return (512 + (2 * counts.length));
    }

    /**
     * Construct a Histogram given the Highest value to be tracked and a number of significant decimal digits
     *
     * @param highestTrackableValue The highest value to be tracked by the histogram. Must be a positive
     *                              integer that is >= 2.
     * @param numberOfSignificantValueDigits The number of significant decimal digits to which the histogram will
     *                                       maintain value resolution and separation. Must be a non-negative
     *                                       integer between 0 and 5.
     */
    public ShortHistogram(final long highestTrackableValue, final int numberOfSignificantValueDigits) {
        super(highestTrackableValue, numberOfSignificantValueDigits);
        counts = new short[countsArrayLength];
    }

    private void readObject(final ObjectInputStream o)
            throws IOException, ClassNotFoundException {
        o.defaultReadObject();
    }
}