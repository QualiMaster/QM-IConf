SET QM_APP_HOME=c:\users\eichelbe\Desktop\qmtmp-nightly
SET ECLIPSE_HOME=w:\eclipse4\eautarkie-nightly
SET DIRECTOR_ID=org.eclipse.equinox.p2.director
SET IU=de.uni-hildesheim.sse.qualiMasterApplication
SET OS=win32
SET WS=win32
SET ARCH=x86
SET TARGET=%QM_APP_HOME%\qmIConf.%OS%.%WS%.%ARCH%
%ECLIPSE_HOME%\eclipsec.exe -nosplash --launcher.suppressErrors -application %DIRECTOR_ID% -metadataRepository file:%QM_APP_HOME%/repository -artifactRepository file:%QM_APP_HOME%/repository -installIU %IU% -destination %TARGET% -profile QMIConf -bundlepool %TARGET% -profileProperties org.eclipse.update.install.features=true -p2.os %OS% -p2.ws %WS% -p2.arch %ARCH% -roaming -consoleLog -purgeHistory -vmArgs -Declipse.p2.data.area=%TARGET%/p2
SET OS=linux
SET WS=gtk
SET ARCH=x86
SET TARGET=%QM_APP_HOME%\qmIConf.%OS%.%WS%.%ARCH%
%ECLIPSE_HOME%\eclipsec.exe -nosplash --launcher.suppressErrors -application %DIRECTOR_ID% -metadataRepository file:%QM_APP_HOME%/repository -artifactRepository file:%QM_APP_HOME%/repository -installIU %IU% -destination %TARGET% -profile QMIConf -bundlepool %TARGET% -profileProperties org.eclipse.update.install.features=true -p2.os %OS% -p2.ws %WS% -p2.arch %ARCH% -roaming -consoleLog -purgeHistory -vmArgs -Declipse.p2.data.area=%TARGET%/p2


REM %ECLIPSE_HOME%\eclipsec.exe -nosplash --launcher.suppressErrors -application %DIRECTOR_ID% -metadataRepository file:%QM_APP_HOME%/repository -artifactRepository file:%QM_APP_HOME%/repository -list 
