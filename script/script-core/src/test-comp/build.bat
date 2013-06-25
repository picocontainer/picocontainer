"%JAVA_HOME%\bin\javac" -classpath "%HOMEPATH%\.m2\repository\com\picocontainer\picocontainer\3.0-SNAPSHOT\picocontainer-3.0-SNAPSHOT.jar" TestComp.java TestComp2.java NotStartable.java
"%JAVA_HOME%\bin\jar" -cf TestComp.jar TestComp.class
"%JAVA_HOME%\bin\jar" -cf TestComp2.jar TestComp2.class
"%JAVA_HOME%\bin\jar" -cf NotStartable.jar NotStartable.class
