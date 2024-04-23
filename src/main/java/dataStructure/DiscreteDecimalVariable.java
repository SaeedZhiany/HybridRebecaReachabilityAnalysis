package dataStructure;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class DiscreteDecimalVariable extends Variable implements Comparable<DiscreteDecimalVariable> {

    @Nonnull
    private BigDecimal value;

    public DiscreteDecimalVariable(@Nonnull String name) {
        this(name, new BigDecimal(0));
    }

    public DiscreteDecimalVariable(@Nonnull String name, @Nonnull BigDecimal value) {
        super(name);
        this.value = value;
    }

    public DiscreteDecimalVariable(@Nonnull DiscreteDecimalVariable discreteDecimalVariable) {
        super(discreteDecimalVariable.name);
        this.value = discreteDecimalVariable.value;
    }

    @Nonnull
    public BigDecimal getValue() {
        return value;
    }

    public void setValue(@Nonnull BigDecimal value) {
        this.value = value;
    }

    @Override
    public int compareTo(@Nonnull DiscreteDecimalVariable other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiscreteDecimalVariable) {
            DiscreteDecimalVariable castedObject = ((DiscreteDecimalVariable) obj);
            return this.name.equals(castedObject.name) && this.value.equals(castedObject.value);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("DiscreteDecimalVariable{ %s = %s }", name, value.toPlainString());
    }
}
