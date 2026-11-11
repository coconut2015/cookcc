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

# Extra JVM options for the JVMs that run CookCC itself, such as the JaCoCo
# agent for coverage ("./make.sh test --coverage" sets this).  Empty by
# default.  The options must not contain spaces.  For javac, where CookCC
# runs as the annotation processor, each option is passed with -J.
COOKCC_JVM_OPTS="${COOKCC_JVM_OPTS:-}"
COOKCC_JAVAC_OPTS=""
for opt in $COOKCC_JVM_OPTS; do
	COOKCC_JAVAC_OPTS="$COOKCC_JAVAC_OPTS -J$opt"
done

function cookcc ()
{
	v=${@: -1}
	if [ -z "$UNICODE" ]; then
		unicode=""
	else
		unicode="-unicode"
	fi
	CCOUTPUT=${v%.xcc}.ccoutput
	"$java" $COOKCC_JVM_OPTS -jar "${COOKCC}" $unicode $generics $@ > ccoutput 2>&1
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
	v=$1
	if [ -z "$UNICODE" ]; then
		unicode=""
	else
		unicode="-Aunicode"
	fi
	CCOUTPUT=${v%.java}.ccoutput
	"$javac" $COOKCC_JAVAC_OPTS -proc:only -processor org.yuanheng.cookcc.input.ap.CookCCProcessor -cp "${COOKCC}:." -s . $unicode $@ > ccoutput 2>&1
	if [ -f $CCOUTPUT ]; then
		diff ccoutput $CCOUTPUT > /dev/null || testerror $v
	elif [ `sizeof ccoutput` -ne 0 ]; then
		cat ccoutput
		testerror $v
	fi
	rm -f ccoutput
}

function compile ()
{
	TEST=$1
	shift
	"$javac" -classpath "$COOKCC:.:$COOKCC_RT" $@ > /dev/null 2> /dev/null || testerror $TEST
}


function run ()
{
	"$java" -cp . $1 $3 > output || testerror $2
	diff output $4 > /dev/null || testerror $2
}

function run2 ()
{
	"$java" -cp . $1 $3 > output 2>&1
	diff output $4 > /dev/null || testerror $2
}

function run3 ()
{
	"$java" -cp ".:$COOKCC_RT" $1 $3 > output 2>&1
	diff output $4 > /dev/null || testerror $2
}

