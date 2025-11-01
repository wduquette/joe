#!/bin/sh
echo "Building Joe javadoc"
rm -rf ../docs/javadoc
mkdir -p ../docs/javadoc
javadoc -sourcepath ../lib/src/main/java   \
    -subpackages com.wjduquette.joe        \
    -d ../docs/javadoc                     \
    -doctitle "Joe Language Java API"      \
    -windowtitle "Joe Language Java API"   \
    -javafx                                \
    -link https://docs.oracle.com/en/java/javase/21/docs/api/ \
    -link https://openjfx.io/javadoc/21/ \
    -quiet
