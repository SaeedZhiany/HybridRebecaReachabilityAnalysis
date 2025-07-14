package dataStructure;

import javax.annotation.Nonnull;

public class IntervalRealVariable extends Variable implements Comparable<IntervalRealVariable> {
    @Nonnull
    private Double lowerBound;
    @Nonnull
    private Double upperBound;

    public IntervalRealVariable(@Nonnull String name) {
        this(name, 0.0);
    }

    public IntervalRealVariable(@Nonnull String name, Double value) {
        this(name, value, value);
    }

    public IntervalRealVariable(@Nonnull String name, @Nonnull Double lowerBound, @Nonnull Double upperBound) {
        super(name);
        if (lowerBound.compareTo(upperBound) <= 0) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        } else {
            this.lowerBound = upperBound;
            this.upperBound = lowerBound;
        }
    }

    public IntervalRealVariable(@Nonnull IntervalRealVariable intervalRealVariable) {
        super(intervalRealVariable.name);
        this.lowerBound = intervalRealVariable.lowerBound;
        this.upperBound = intervalRealVariable.upperBound;
    }

    @Nonnull
    public Double getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(@Nonnull Double lowerBound) {
        this.lowerBound = lowerBound;
    }

    @Nonnull
    public Double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(@Nonnull Double upperBound) {
        this.upperBound = upperBound;
    }

    @Override
    public int compareTo(@Nonnull IntervalRealVariable other) {
        // CHECKME: check other bounds inside this bound
        if (this.upperBound.compareTo(other.lowerBound) < 0) {
            return -1;
        } else if (this.lowerBound.compareTo(other.upperBound) > 0) {
            return 1;
        } else if (this.lowerBound.compareTo(other.lowerBound) == 0 && this.upperBound.compareTo(other.upperBound) == 0) {
            return 0;
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntervalRealVariable) {
            IntervalRealVariable castedObject = ((IntervalRealVariable) obj);
            return this.name.equals(castedObject.name)
                    && this.lowerBound.equals(castedObject.lowerBound)
                    && this.upperBound.equals(castedObject.upperBound);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format(
                "IntervalRealVariable{ %s = [%s, %s] }",
                name,
                lowerBound,
                upperBound
        );
    }

    public boolean isSubsetOf(IntervalRealVariable other) {
        return this.lowerBound.compareTo(other.lowerBound) >= 0
                && this.upperBound.compareTo(other.upperBound) <= 0;
    }
}
