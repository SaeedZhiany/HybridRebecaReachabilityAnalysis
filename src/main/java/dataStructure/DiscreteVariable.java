package dataStructure;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class DiscreteVariable extends Variable implements Comparable<DiscreteVariable> {

    @Nonnull
    private BigDecimal value;

    public DiscreteVariable(@Nonnull String name) {
        this(name, new BigDecimal(0));
    }

    public DiscreteVariable(@Nonnull String name, @Nonnull BigDecimal value) {
        super(name);
        this.value = value;
    }

    @Nonnull
    public BigDecimal getValue() {
        return value;
    }

    public void setValue(@Nonnull BigDecimal value) {
        this.value = value;
    }

    @Override
    public int compareTo(@Nonnull DiscreteVariable other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiscreteVariable) {
            DiscreteVariable castedObject = ((DiscreteVariable) obj);
            return this.name.equals(castedObject.name) && this.value.equals(castedObject.value);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("DiscreteVariable{ %s = %s }", name, value.toPlainString());
    }
}
