package dataStructure;

import javax.annotation.Nonnull;

public class DiscreteBoolVariable extends Variable implements Comparable<DiscreteBoolVariable> {
    @Nonnull
    private Boolean value;

    private Boolean isDefinite;

    public Boolean getDefinite() {
        return isDefinite;
    }

    public void setDefinite(Boolean definite) {
        isDefinite = definite;
    }

    public DiscreteBoolVariable(@Nonnull String name) {
        this(name, false);
    }

    public DiscreteBoolVariable(@Nonnull String name, @Nonnull Boolean value) {
        this(name, value, true);
    }

    public DiscreteBoolVariable(@Nonnull String name, @Nonnull Boolean value, @Nonnull Boolean isDefinite) {
        super(name);
        this.value = value;
        this.isDefinite = isDefinite;
    }

    public DiscreteBoolVariable(@Nonnull DiscreteBoolVariable discreteBoolVariable) {
        super(discreteBoolVariable.name);
        this.value = discreteBoolVariable.value;
    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public int compareTo(@Nonnull DiscreteBoolVariable other) {
        if (this.value.equals(other.value) && this.isDefinite.equals(other.isDefinite) && this.isDefinite)
            return 0;
        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiscreteBoolVariable) {
            DiscreteBoolVariable castedObject = ((DiscreteBoolVariable) obj);
            return this.name.equals(castedObject.name) && this.value.equals(castedObject.value) && this.isDefinite.equals(castedObject.isDefinite);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("DiscreteBoolVariable{ %s = %s & %s}", name, value, isDefinite);
    }
}
