package dataStructure;

import javax.annotation.Nonnull;

public abstract class Variable {

    @Nonnull
    protected String name;

    public Variable(@Nonnull String name) {
        this.name = name;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(@Nonnull String name) {
        this.name = name;
    }
}
