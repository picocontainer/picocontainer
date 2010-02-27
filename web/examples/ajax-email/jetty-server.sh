#!/bin/sh

# starts server
start()
{
	cd $MODULE
	mvn jetty:run-war
}

# ----- Execute the commands -----------------------------------------

MODULE=$1
if [ "$MODULE" == "" ]; then
MODULE="ajax-email-webapp"
fi

start $MODULE   	

exit 1

