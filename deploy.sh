#!/bin/bash
#
# This script deploys jars
#

mvn clean package javadoc:aggregate-jar

mv target/cookcc-parent-0.4.3-javadoc.jar dist/cookcc-0.4.2-javadoc.jar

# deploying cookcc-rt
(cd cookcc-rt; mvn deploy)

# deploying cookcc
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh -DpomFile=cookcc.pom -Dfile=dist/cookcc-0.4.3.jar
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh -DpomFile=cookcc.pom -Dfile=dist/cookcc-0.4.3-sources.jar -Dclassifier=sources
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh -DpomFile=cookcc.pom -Dfile=dist/cookcc-0.4.3-javadoc.jar -Dclassifier=javadoc
