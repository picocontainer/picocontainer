#!/bin/sh

# starts server
start()
{
	java -jar "$M2_REPOSITORY/org/seleniumhq/selenium/server/selenium-server/$VERSION/selenium-server-$VERSION-standalone.jar" 
}

# ----- Execute the commands -----------------------------------------

M2_REPOSITORY=$1
VERSION=$2
if [ "$M2_REPOSITORY" == "" ]; then
M2_REPOSITORY="$HOME/.m2/repository"
fi
if [ "$VERSION" == "" ]; then
VERSION="1.0.1"
fi

start $M2_REPOSITORY $VERSION

exit 1

