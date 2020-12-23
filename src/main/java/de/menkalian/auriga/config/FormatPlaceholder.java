package de.menkalian.auriga.config;

public enum FormatPlaceholder {
    PRINTF("%s"),
    SLF4J("{}"),
    NONE("%s");

    private final String placeholder;

    FormatPlaceholder (String placeholder) {
        this.placeholder = placeholder;
    }

    public String getPlaceholder () {
        return placeholder;
    }
}
