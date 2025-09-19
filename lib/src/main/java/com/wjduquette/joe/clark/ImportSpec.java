package com.wjduquette.joe.clark;

/**
 * An import spec, from the `import` statement.
 * @param pkgName
 * @param symbol
 */
public record ImportSpec(String pkgName, String symbol) {
    @Override
    public String toString() {
        return pkgName + "." + symbol;
    }
}
