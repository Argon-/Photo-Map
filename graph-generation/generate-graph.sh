#!/usr/bin/env bash

set -e

if [[ $# -eq 0 ]]; then
    echo "No argument given (pbf file or raw file expected)"
    exit 0
fi

FNAME="$1"
bn="$(basename $FNAME)"
SUFFIX="${bn##*.}"

if [ "$SUFFIX" == "pbf" ]; then
    echo "Input is pbf file: generating raw file"
    env python2.7 PARSEOSM/myparsepbf.py "$FNAME" -x raw.txt
    FNAME="raw.txt"
fi

./generate-base-files.sh "$FNAME"
java -Xmx16G -classpath "../lib/trove-3.0.3/3.0.3/lib/trove-3.0.3.jar:../bin" "data_structures.graph.generator.GraphGeneratorMain" "working-dir/nodes.txt" "working-dir/ways.txt" "out.txt"