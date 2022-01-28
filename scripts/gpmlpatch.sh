#!/bin/sh

CLASSPATH=\
../modules/org.pathvisio.core.jar:\
../lib/org.apache.servicemix.bundles.jdom-2.0.6_1.jar:\
../lib/org.bridgedb-3.0.13.jar:\
../lib/org.bridgedb.bio-3.0.13.jar

java -ea -classpath $CLASSPATH org.pathvisio.core.gpmldiff.PatchMain "$@" 
