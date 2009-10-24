#!/bin/sh

function error ()
{
	echo $@ && exit 1
}

test -z "$JAVA_HOME" && error need to set JAVA_HOME env
test -z "$COOKCC" && error need to set COOKCC env

for v in *; do
	if [ ! -d $v  ]; then continue; fi
	if [ ! -f $v/test.sh  ]; then continue; fi
	echo running $v/test.sh
	(cd $v; test.sh) || exit 1
done
