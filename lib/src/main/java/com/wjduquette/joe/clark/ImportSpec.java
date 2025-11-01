package com.wjduquette.joe.clark;

/**
 * An import spec, from the `import` statement.
 * @param pkgName The name of the package to import
 * @param symbol The name of the symbol to import, or "*"
 */
public record ImportSpec(String pkgName, String symbol) {
    @Override
    public String toString() {
        return pkgName + "." + symbol;
    }
}
