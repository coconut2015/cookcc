#!/bin/bash

if [ -z "$COOKCC" ]; then
	export COOKCC="../tool/cookcc-latest.jar"
fi

source bin/settings.sh

for v in *; do
	if [ ! -d $v  ]; then continue; fi
	if [ ! -f $v/test.sh  ]; then continue; fi
	echo running $v/test.sh
	(cd $v; ./test.sh) || exit 1
done

echo All tests passed.
