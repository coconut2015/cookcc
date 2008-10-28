#!/bin/sh

if [ -z "$JAVA_HOME" ]; then
	echo need to set JAVA_HOME env
	exit 1
fi

if [ -z "$COOKCC" ]; then
	echo need to set COOKCC
	exit 1
fi

for v in *; do
	if [ ! -d $v  ]; then continue; fi
	if [ ! -f $v/test.sh  ]; then continue; fi
	echo running $v/test.sh
	(cd $v; test.sh)
	if [ $? -ne 0 ]; then exit 1; fi
done

echo All tests passed.
