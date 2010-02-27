pico = builder.container(parent:parent) {
    classPathElement(path:"test-comps/api.jar")
    classLoader {
        classPathElement(path:"test-comps/honeyimpl.jar")
        component(classNameKey:"org.picocontainer.booter.Honey", class:"org.picocontainer.booter.BeeHiveHoney")
    }
    classLoader {
        classPathElement(path:"test-comps/bearimpl.jar") {
             grant(new java.net.SocketPermission("yahoo.com:80", "connect"))
        }
        component(class:"org.picocontainer.booter.BrownBear")
    }
}
