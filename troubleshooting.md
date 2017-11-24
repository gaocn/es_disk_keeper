## 常见错误

### JVMCFRE003 bad major version

``` 
./bin/elasticsearch-plugin install file:///home/sm01/elk5.2/elasticsearch-5.2.0/ingest-5.2.0-SNAPSHOT.jar 
Exception in thread "main" java.lang.UnsupportedClassVersionError: JVMCFRE003 bad major version; class=org/elasticsearch/plugins/PluginCli, offset=6
        at java.lang.ClassLoader.defineClassImpl(Native Method)
        at java.lang.ClassLoader.defineClass(ClassLoader.java:331)
        at java.security.SecureClassLoader.defineClass(SecureClassLoader.java:155)
        at java.net.URLClassLoader.defineClass(URLClassLoader.java:712)
        at java.net.URLClassLoader.access$400(URLClassLoader.java:93)
        at java.net.URLClassLoader$ClassFinder.run(URLClassLoader.java:1160)
        at java.security.AccessController.doPrivileged(AccessController.java:452)
        at java.net.URLClassLoader.findClass(URLClassLoader.java:595)
        at java.lang.ClassLoader.loadClassHelper(ClassLoader.java:786)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:760)
        at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:326)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:741)
        at sun.launcher.LauncherHelper.checkAndLoadMain(LauncherHelper.java:495)
```

主版本号不同，确保使用elasticsearch-plugin安装插件的环境至少是JAVA 8以上，否则就会出现上述从错误。

### Could not resolve placeholder 'CLUSTER_NAME'

``` 
./bin/elasticsearch-plugin install ingest-5.2.0-SNAPSHOT.zip 
Exception in thread "main" java.lang.IllegalArgumentException: Could not resolve placeholder 'CLUSTER_NAME'
        at org.elasticsearch.common.settings.PropertyPlaceholder.parseStringValue(PropertyPlaceholder.java:116)
        at org.elasticsearch.common.settings.PropertyPlaceholder.replacePlaceholders(PropertyPlaceholder.java:69)
        at org.elasticsearch.common.settings.Settings$Builder.replacePropertyPlaceholders(Settings.java:986)
        at org.elasticsearch.common.settings.Settings$Builder.replacePropertyPlaceholders(Settings.java:946)
        at org.elasticsearch.node.internal.InternalSettingsPreparer.initializeSettings(InternalSettingsPreparer.java:137)
        at org.elasticsearch.node.internal.InternalSettingsPreparer.prepareEnvironment(InternalSettingsPreparer.java:117)
        at org.elasticsearch.plugins.InstallPluginCommand.execute(InstallPluginCommand.java:203)
        at org.elasticsearch.plugins.InstallPluginCommand.execute(InstallPluginCommand.java:195)
        at org.elasticsearch.cli.SettingCommand.execute(SettingCommand.java:54)
        at org.elasticsearch.cli.Command.mainWithoutErrorHandling(Command.java:122)
        at org.elasticsearch.cli.MultiCommand.execute(MultiCommand.java:69)
        at org.elasticsearch.cli.Command.mainWithoutErrorHandling(Command.java:122)
        at org.elasticsearch.cli.Command.main(Command.java:88)
        at org.elasticsearch.plugins.PluginCli.main(PluginCli.java:47)
```
原因是因为在config/elasticsearch.yml文件中出现了无法识别的变量，修改为字符串即可。

### 成功安装插件

``` 
 ./bin/elasticsearch-plugin install file:///home/sm01/elk5.2/elasticsearch-5.2.0/ingest-5.2.0-SNAPSHOT.zip     
-> Downloading file:///home/sm01/elk5.2/elasticsearch-5.2.0/ingest-5.2.0-SNAPSHOT.zip
[=================================================] 100%?? 
-> Installed ingest
```

### Maven跳过测试打包
mvn clean install -DskipTests


### ES插件中执行本地命令时错误

``` 
[2017-11-22T16:15:31,129][WARN ][o.e.b.ElasticsearchUncaughtExceptionHandler] [] uncaught exception in thread [Thread-5]
java.security.AccessControlException: access denied ("java.io.FilePermission" "<<ALL FILES>>" "execute")
        at java.security.AccessControlContext.checkPermission(AccessControlContext.java:472) ~[?:1.8.0_121]
        at java.security.AccessController.checkPermission(AccessController.java:884) ~[?:1.8.0_121]
        at java.lang.SecurityManager.checkPermission(SecurityManager.java:549) ~[?:1.8.0_121]
        at java.lang.SecurityManager.checkExec(SecurityManager.java:799) ~[?:1.8.0_121]
        at java.lang.ProcessBuilder.start(ProcessBuilder.java:1018) ~[?:1.8.0_121]
        at java.lang.Runtime.exec(Runtime.java:620) ~[?:1.8.0_121]
        at java.lang.Runtime.exec(Runtime.java:450) ~[?:1.8.0_121]
        at java.lang.Runtime.exec(Runtime.java:347) ~[?:1.8.0_121]
        at org.elasticsearch.disk.DiskKeeperAction.executeCMDLine(DiskKeeperAction.java:73) ~[?:?]
        at org.elasticsearch.disk.DiskKeeperAction$1.run(DiskKeeperAction.java:43) ~[?:?]
        at java.lang.Thread.run(Thread.java:745) [?:1.8.0_121]
```

### AccessControlException 

``` 
java.security.AccessControlException: access denied ("java.io.FilePermission" "plugin-settings.properties" "read")
        at java.security.AccessControlContext.checkPermission(AccessControlContext.java:472)
        at java.security.AccessController.checkPermission(AccessController.java:884)
        at java.lang.SecurityManager.checkPermission(SecurityManager.java:549)
        at java.lang.SecurityManager.checkRead(SecurityManager.java:888)
        at java.io.FileInputStream.<init>(FileInputStream.java:127)
        at java.io.FileInputStream.<init>(FileInputStream.java:93)
        at org.elasticsearch.disk.DiskKeeperThread.<clinit>(DiskKeeperThread.java:43)
        at org.elasticsearch.disk.DiskKeeperAction.<init>(DiskKeeperAction.java:25)
        at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
        at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
        at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
        at java.lang.reflect.Constructor.newInstance(Constructor.java:423)
        at org.elasticsearch.common.inject.DefaultConstructionProxyFactory$1.newInstance(DefaultConstructionProxyFactory.java:49)
        at org.elasticsearch.common.inject.ConstructorInjector.construct(ConstructorInjector.java:86)
        at org.elasticsearch.common.inject.ConstructorBindingImpl$Factory.get(ConstructorBindingImpl.java:116)
        at org.elasticsearch.common.inject.ProviderToInternalFactoryAdapter$1.call(ProviderToInternalFactoryAdapter.java:47)
        at org.elasticsearch.common.inject.InjectorImpl.callInContext(InjectorImpl.java:825)
        at org.elasticsearch.common.inject.ProviderToInternalFactoryAdapter.get(ProviderToInternalFactoryAdapter.java:43)
        at org.elasticsearch.common.inject.Scopes$1$1.get(Scopes.java:59)
        at org.elasticsearch.common.inject.InternalFactoryToProviderAdapter.get(InternalFactoryToProviderAdapter.java:50)
        at org.elasticsearch.common.inject.InjectorBuilder$1.call(InjectorBuilder.java:191)
        at org.elasticsearch.common.inject.InjectorBuilder$1.call(InjectorBuilder.java:183)
        at org.elasticsearch.common.inject.InjectorImpl.callInContext(InjectorImpl.java:818)
        at org.elasticsearch.common.inject.InjectorBuilder.loadEagerSingletons(InjectorBuilder.java:183)
        at org.elasticsearch.common.inject.InjectorBuilder.loadEagerSingletons(InjectorBuilder.java:173)
        at org.elasticsearch.common.inject.InjectorBuilder.injectDynamically(InjectorBuilder.java:161)
        at org.elasticsearch.common.inject.InjectorBuilder.build(InjectorBuilder.java:96)
        at org.elasticsearch.common.inject.Guice.createInjector(Guice.java:96)
        at org.elasticsearch.common.inject.Guice.createInjector(Guice.java:70)
        at org.elasticsearch.common.inject.ModulesBuilder.createInjector(ModulesBuilder.java:43)
        at org.elasticsearch.node.Node.<init>(Node.java:468)
        at org.elasticsearch.node.Node.<init>(Node.java:232)
        at org.elasticsearch.bootstrap.Bootstrap$6.<init>(Bootstrap.java:241)
        at org.elasticsearch.bootstrap.Bootstrap.setup(Bootstrap.java:241)
        at org.elasticsearch.bootstrap.Bootstrap.init(Bootstrap.java:333)
        at org.elasticsearch.bootstrap.Elasticsearch.init(Elasticsearch.java:121)
        at org.elasticsearch.bootstrap.Elasticsearch.execute(Elasticsearch.java:112)
        at org.elasticsearch.cli.SettingCommand.execute(SettingCommand.java:54)
        at org.elasticsearch.cli.Command.mainWithoutErrorHandling(Command.java:122)
        at org.elasticsearch.cli.Command.main(Command.java:88)
        at org.elasticsearch.bootstrap.Elasticsearch.main(Elasticsearch.java:89)
        at org.elasticsearch.bootstrap.Elasticsearch.main(Elasticsearch.java:82)
```

解决方案：
vi ../jdk1.8.0_121/jre/lib/security/java.policy
    permission java.io.FilePermission "/home/sm01/elk5.2/elasticsearch-5.2.0/-", "read";