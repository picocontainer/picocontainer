import java.io.*;

def integrationDirectory = "${project.build.directory}/it";
def version = "${project.version}";

//def scriptDir = new File(".").getAbsolutePath();
//def integrationDirectory = "${scriptDir}/target/it";
//def version = "3.0-SNAPSHOT";
def fail(msg) {
	throw new RuntimeException(msg);
}

println("---------------------------------");
println("Verifying basic-module-test");
println("---------------------------------");

File baseDir = new File(integrationDirectory);

/* Takes a java.io.File object */
def assertDirectoryExists(testFile) {
	if (!testFile.exists()) {
		fail("Could not find directory ${testFile}");
	}
	
	if (testFile.isFile()) {
		fail("File ${testFile} is a normal file.  Expected Directory");
	}
}

/* Takes a java.io.File object */
def assertFileExists(testFile) {
	if (!testFile.exists()) {
		fail("Could not find file ${testFile}");
	}
	
	if (!testFile.isFile()) {
		fail("File ${testFile} is a directory. Expected normal file");
	}
}


assertFileExists(new File( baseDir, "basic-module-package-test/target/basic-module-test-${version}.pico-module" ));
println("--- Pass ---");

println("---------------------------------");
println("Verifying war-package-copy-test");
println("---------------------------------");

assertDirectoryExists( new File( baseDir, "war-package-copy-test/target/war-package-copy-test-${version}/WEB-INF/modules/com.picocontainer.testmodules.moduleOne" ));
assertDirectoryExists( new File( baseDir, "war-package-copy-test/target/war-package-copy-test-${version}/WEB-INF/modules/com.picocontainer.testmodules.moduleTwo" ));
println("--- Pass ---");

//File file = new File( baseDir, "war-package-copy-test/target/war-package-copy-test-${version}/WEB-INF/modules/" );
//if (!file.exists() || !file.isFile() )
//{
    //fail( "Could not find generated Pico Module: " + file );
//}

println("---------------------------------");
println("Verifying inplace-module-copy-test");
println("---------------------------------");
assertDirectoryExists( new File( baseDir, "inplace-module-copy-test/src/main/webapp/WEB-INF/modules/com.picocontainer.testmodules.moduleOne" ));
assertDirectoryExists( new File( baseDir, "inplace-module-copy-test/src/main/webapp/WEB-INF/modules/com.picocontainer.testmodules.moduleTwo" ));
println("-- Pass ---");

println("---------------------------------");
println("Verifying inplace-module-clean-test");
println("---------------------------------");
assertDirectoryExists( new File( baseDir, "inplace-module-clean-test/src/main/webapp/WEB-INF/modules/org.example.somemodule" ));
File moduleOneCleaned = new File( baseDir,"inplace-module-clean-test/src/main/webapp/WEB-INF/modules/com.picocontainer.testmodules.moduleOne" );
if (moduleOneCleaned.exists()) {
	fail("Module One Didn't Get Cleaned");
}

File moduleTwoCleaned = new File( baseDir, "inplace-module-clean-test/src/main/webapp/WEB-INF/modules/com.picocontainer.testmodules.moduleTwo" );
if (moduleTwoCleaned.exists()) {
	fail("Module Two Didn't Get Cleaned");
}
println("-- Pass ---");

