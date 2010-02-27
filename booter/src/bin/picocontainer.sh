#!/bin/bash

# PicoContainer Booter script v @VER@
# www.picocontainer.org/booter

EXEC="java -Djava.security.manager -Djava.security.policy=file:booter.policy -jar lib/picocontainer-booter-@VER@.jar $@"
echo $EXEC
$EXEC

