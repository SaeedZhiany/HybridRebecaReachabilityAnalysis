package dataStructure;

import javax.annotation.Nonnull;

public class StringVariable extends Variable implements Comparable<StringVariable> {

    @Nonnull
    private String value;

    public StringVariable(@Nonnull String name) {
        this(name, "");
    }

    public StringVariable(@Nonnull String name, @Nonnull String value) {
        super(name);
        this.value = value;
    }

    public StringVariable(@Nonnull StringVariable stringVariable) {
        super(stringVariable.name);
        this.value = stringVariable.value;
    }

    @Nonnull
    public String getValue() {
        return value;
    }

    public void setValue(@Nonnull String value) {
        this.value = value;
    }

    @Override
    public int compareTo(@Nonnull StringVariable other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StringVariable) {
            StringVariable castedObject = ((StringVariable) obj);
            return this.name.equals(castedObject.name) && this.value.equals(castedObject.value);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("StringVariable{ %s = %s }", name, value);
    }
}
