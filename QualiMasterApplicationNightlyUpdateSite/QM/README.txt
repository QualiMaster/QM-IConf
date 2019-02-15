used and tested up to Java 8

Java 9: 
  requires modification to QualiMasterApplication.ini, here one working with for OpenJDK 10
  
--launcher.appendVmargs
-vmargs
-Xms512M
-Xmx1024M
-Divml.configuration.new=true
-Dorg.osgi.framework.bundle.parent=ext
-Dosgi.requiredJavaVersion=1.8
--add-modules=ALL-SYSTEM
