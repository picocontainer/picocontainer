pico = builder.container(parent:parent) {
    classPathElement(path:"test-comps/api.jar")
    classLoader {
        classPathElement(path:"test-comps/honeyimpl.jar")
        component(classNameKey:"com.picocontainer.booter.Honey", class:"com.picocontainer.booter.BeeHiveHoney")
    }
    classLoader {
        classPathElement(path:"test-comps/bearimpl.jar") {
             grant(new java.net.SocketPermission("yahoo.com:80", "connect"))
        }
        component(class:"com.picocontainer.booter.BrownBear")
    }
}
