# langer command
java --module-path "C:\javafx-sdk-21.0.11\lib" --add-modules javafx.controls,javafx.fxml,javafx.web,jdk.jsobject -jar crexplorer.jar



C:\Users\nicla\Downloads>java -jar crexplorer.jar
Mai 12, 2026 11:22:10 PM com.sun.javafx.application.PlatformImpl startup
WARNUNG: Unsupported JavaFX configuration: classes were loaded from 'unnamed module @4548e870'
WARNING: A restricted method in java.lang.System has been called
WARNING: java.lang.System::load has been called by com.sun.glass.utils.NativeLibLoader in an unnamed module (file:/C:/Users/nicla/Downloads/crexplorer.jar)
WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
WARNING: Restricted methods will be blocked in a future release unless native access is enabled

Exception in Application start method
Exception in thread "main" java.lang.RuntimeException: Exception in Application start method
at com.sun.javafx.application.LauncherImpl.launchApplication1(LauncherImpl.java:893)
at com.sun.javafx.application.LauncherImpl.lambda$launchApplication$2(LauncherImpl.java:196)
at java.base/java.lang.Thread.run(Thread.java:1516)
Caused by: javafx.fxml.LoadException:
file:/C:/Users/nicla/Downloads/crexplorer.jar!/Main.fxml

    at javafx.fxml.FXMLLoader.constructLoadException(FXMLLoader.java:2722)
    at javafx.fxml.FXMLLoader.loadImpl(FXMLLoader.java:2692)
    at javafx.fxml.FXMLLoader.loadImpl(FXMLLoader.java:2563)
    at javafx.fxml.FXMLLoader.loadImpl(FXMLLoader.java:3376)
    at javafx.fxml.FXMLLoader.loadImpl(FXMLLoader.java:3332)
    at javafx.fxml.FXMLLoader.loadImpl(FXMLLoader.java:3300)
    at javafx.fxml.FXMLLoader.loadImpl(FXMLLoader.java:3272)
    at javafx.fxml.FXMLLoader.loadImpl(FXMLLoader.java:3248)
    at javafx.fxml.FXMLLoader.load(FXMLLoader.java:3241)
    at cre.CitedReferencesExplorerFX.start(CitedReferencesExplorerFX.java:87)
    at com.sun.javafx.application.LauncherImpl.lambda$launchApplication1$9(LauncherImpl.java:839)
    at com.sun.javafx.application.PlatformImpl.lambda$runAndWait$12(PlatformImpl.java:483)
    at com.sun.javafx.application.PlatformImpl.lambda$runLater$10(PlatformImpl.java:456)
    at java.base/java.security.AccessController.doPrivileged(AccessController.java:138)
    at com.sun.javafx.application.PlatformImpl.lambda$runLater$11(PlatformImpl.java:455)
    at com.sun.glass.ui.InvokeLaterDispatcher$Future.run(InvokeLaterDispatcher.java:95)
    at com.sun.glass.ui.win.WinApplication._runLoop(Native Method)
    at com.sun.glass.ui.win.WinApplication.lambda$runLoop$3(WinApplication.java:185)
    ... 1 more
Caused by: java.lang.reflect.InvocationTargetException
at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:119)
at java.base/java.lang.reflect.Method.invoke(Method.java:565)
at com.sun.javafx.reflect.Trampoline.invoke(MethodUtil.java:72)
at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
at java.base/java.lang.reflect.Method.invoke(Method.java:565)
at com.sun.javafx.reflect.MethodUtil.invoke(MethodUtil.java:270)
at com.sun.javafx.fxml.MethodHelper.invoke(MethodHelper.java:84)
at javafx.fxml.FXMLLoader.loadImpl(FXMLLoader.java:2688)
... 17 more
Caused by: java.lang.NoClassDefFoundError: netscape/javascript/JSException
at cre.ui.MainController.initialize(MainController.java:180)
at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
... 24 more
Caused by: java.lang.ClassNotFoundException: netscape.javascript.JSException
at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:580)
at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:502)
... 26 more