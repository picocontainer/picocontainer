$JAVA_HOME/bin/javac -classpath "$HOME/.maven/repository/picocontainer/jars/picocontainer-1.1.jar" TestComp.java TestComp2.java NotStartable.java
$JAVA_HOME/bin/jar -cf TestComp.jar TestComp.class
$JAVA_HOME/bin/jar -cf TestComp2.jar TestComp2.class
$JAVA_HOME/bin/jar -cf NotStartable.jar NotStartable.class
