pico = new org.picocontainer.defaults.DefaultPicoContainer(parent)

// Set up and configure VFS
manager = new org.apache.commons.vfs.impl.DefaultFileSystemManager()
fileProvider = new org.apache.commons.vfs.provider.local.DefaultLocalFileProvider()
zipProvider = new org.apache.commons.vfs.provider.zip.ZipFileProvider()
manager.setDefaultProvider(fileProvider)
manager.addProvider("file", fileProvider)
manager.addProvider("zip", zipProvider)
manager.init()

// Set the root folder
rootPath = new java.io.File("apps").getAbsolutePath()
root = manager.resolveFile(rootPath)

pico.registerComponentInstance(manager)
pico.registerComponentInstance(root)
pico.registerComponentImplementation(org.nanocontainer.deployer.FolderContentPoller)
pico.registerComponentImplementation(org.nanocontainer.deployer.DifferenceAnalysingFolderContentHandler)
pico.registerComponentImplementation(org.nanocontainer.deployer.DeployingFolderListener)
pico.registerComponentImplementation(org.nanocontainer.deployer.NanoContainerDeployer)
return pico
