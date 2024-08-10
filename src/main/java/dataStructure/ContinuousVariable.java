package dataStructure;

import javax.annotation.Nonnull;

public class ContinuousVariable extends Variable implements Comparable<ContinuousVariable> {

    @Nonnull
    private Double lowerBound;
    @Nonnull
    private Double upperBound;

    public ContinuousVariable(@Nonnull String name) {
        this(name, Double.valueOf(0));
    }

    public ContinuousVariable(@Nonnull String name, @Nonnull Double value) {
        this(name, value, value);
    }

    public ContinuousVariable(@Nonnull String name, @Nonnull Double lowerBound, @Nonnull Double upperBound) {
        super(name);
        if (lowerBound.compareTo(upperBound) <= 0) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        } else {
            this.lowerBound = upperBound;
            this.upperBound = lowerBound;
        }
    }

    public ContinuousVariable(@Nonnull ContinuousVariable continuousVariable) {
        super(continuousVariable.name);
        this.lowerBound = continuousVariable.lowerBound;
        this.upperBound = continuousVariable.upperBound;
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
    public int compareTo(@Nonnull ContinuousVariable other) {
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
        if (obj instanceof ContinuousVariable) {
            ContinuousVariable castedObject = ((ContinuousVariable) obj);
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
                "ContinuousVariable{ %s = [%s, %s] }",
                name,
                lowerBound,
                upperBound
        );
    }

    public boolean isSubsetOf(ContinuousVariable other) {
        return this.lowerBound.compareTo(other.lowerBound) >= 0
                && this.upperBound.compareTo(other.upperBound) <= 0;
    }

    public boolean isValid() {
        return (this.lowerBound.compareTo(this.upperBound) <= 0) &&
                (this.lowerBound.compareTo(Double.valueOf(0)) >= 0) &&
                (this.upperBound.compareTo(Double.valueOf(0)) >= 0);
    }
}
