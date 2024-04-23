package dataStructure;

import javax.annotation.Nonnull;

public class DiscreteBoolVariable extends Variable implements Comparable<DiscreteBoolVariable> {
    @Nonnull
    private Boolean value;

    public DiscreteBoolVariable(@Nonnull String name) {
        this(name, false);
    }

    public DiscreteBoolVariable(@Nonnull String name, @Nonnull Boolean value) {
        super(name);
        this.value = value;
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
        return this.value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiscreteBoolVariable) {
            DiscreteBoolVariable castedObject = ((DiscreteBoolVariable) obj);
            return this.name.equals(castedObject.name) && this.value.equals(castedObject.value);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("DiscreteBoolVariable{ %s = %s }", name, value);
    }
}
