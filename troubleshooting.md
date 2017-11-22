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