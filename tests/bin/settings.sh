function sizeof ()
{
	stat -c '%s' $1
}

function error ()
{
	echo $@ && exit 1
}

function testerror ()
{
	error test for $@ failed
}

test -z "$JAVA_HOME" && error need to set JAVA_HOME env

export COOKCC=`/usr/bin/realpath "$COOKCC"`
javac="${JAVA_HOME}/bin/javac"
java="${JAVA_HOME}/bin/java"

function cookcc ()
{
	v=$1
	CCOUTPUT=${v%.xcc}.ccoutput
	"$java" -jar "${COOKCC}" $@ > ccoutput 2>&1
	if [ -f $CCOUTPUT ]; then
		diff ccoutput $CCOUTPUT > /dev/null || testerror $v
	elif [ `sizeof ccoutput` -ne 0 ]; then
		cat ccoutput
		testerror $v
	fi
	rm -f ccoutput
}

function apt ()
{
	"$javac" -proc:only -processor org.yuanheng.cookcc.input.ap.CookCCProcessor -cp "${COOKCC}:." -s . $@
}
