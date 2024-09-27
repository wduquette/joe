rm -rf ../docs/javadoc
javadoc \
    -sourcepath ../lib/src/main/java \
    -doctitle "Joe 0.2.0 API" \
    -d ../docs/javadoc \
    -quiet \
    -subpackages com.wjduquette.joe

