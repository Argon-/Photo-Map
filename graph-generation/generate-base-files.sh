#!/usr/bin/env bash

set -e


[[ $# -eq 0 ]] && INPUT="raw.txt" || INPUT="$1"
TMPDIR="working-dir"
TMPWAYS="$TMPDIR/ways.txt"
TMPNODES="$TMPDIR/nodes.txt"

mkdir $TMPDIR

echo "Generating temporary files in $TMPDIR"
echo "   > extracting ways with heighway key"
egrep "^[ ]{0,1}way .*highway" "$INPUT" > "$TMPWAYS"
echo "   > extracting nodes"
egrep "^[ ]{0,1}node " "$INPUT" > "$TMPNODES"
