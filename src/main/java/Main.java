import java.io.File;
import java.io.FileInputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;

public class Main {
	public static void main(String[] args) throws Exception {
		log(">>>>> -------------- Class-ReLoader -------------- <<<<<");
		if(args.length < 3) {
			logErr("usage : java -cp class-reloader.jar Main pid fullClassName classFile");
			logErr(" will reload class `fullClassName` from .class file `classFile` in jvm thread `pid`");
		}
		String javaHome = System.getenv("JAVA_HOME");
		if(javaHome == null || javaHome.equals("")) {
			javaHome = System.getProperty("java.home");
		}
		log("Java Home is :" + javaHome);
		String toolLib = (javaHome.endsWith("/") ? javaHome : javaHome + "/") + "lib/tools.jar";
		log("tools.jar : " + toolLib);
		if(!new File(toolLib).exists()) {
			logErr("error : tools.jar not exist !");
			System.exit(1);
		}
		URLClassLoader url = new URLClassLoader(new URL[]{new URL("jar", "", "file:"+ toolLib + "!/")}, ClassLoader.getSystemClassLoader());
		Thread.currentThread().setContextClassLoader(url);
		try {
			run(args[0], args[1], args[2]);
			log("**reload Done.");
		}catch (Throwable t) {
			t.printStackTrace();
			logErr("**reload Fail.");
		}
		log(">>>>> -------------- Class-ReLoader -------------- <<<<<");
	}

	public static void agentmain(String agentArgs, Instrumentation inst) {
		try {
			log(">>>>> -------------- Class-ReLoader Agent -------------- <<<<<");
			log("with Arguments :");
			System.out.println(agentArgs);
			String[] clazz = agentArgs.split(",");
			if(clazz.length != 2) {
				logErr("Invalid arguments, expect : className,classFilePath");
				return;
			}
			String clzName = clazz[0];
			Class clz = ClassLoader.getSystemClassLoader().loadClass(clzName);
			File clzFile = new File(clazz[1]);
			byte[] clzFileContent = new byte[(int)clzFile.length()];
			try(FileInputStream fi = new FileInputStream(clzFile)) {
				fi.read(clzFileContent);
			}
			inst.redefineClasses(new ClassDefinition(clz, clzFileContent));
			log("Succeed.");
		}catch (Throwable t) {
			t.printStackTrace();
		}finally {
			log(">>>>> -------------- Class-ReLoader Agent -------------- <<<<<");
		}
	}

	private static void run(String pid, String clz, String clzFile) throws Exception{
		Class<?> vmClz = Thread.currentThread().getContextClassLoader().loadClass("com.sun.tools.attach.VirtualMachine");
		Object vm = vmClz.getDeclaredMethod("attach", String.class).invoke(null, pid);
		String agentJar = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		log("Agent jar : " + agentJar);
		if(!new File(agentJar).exists()) {
			logErr("Agent jar file not exist, please make sure it is in current dir");
			return;
		}
		log("Target class File : " + clzFile);
		vmClz.getDeclaredMethod("loadAgent", String.class, String.class).invoke(vm, agentJar, clz + "," + clzFile);
	}

	private static void log(String msg) {
		System.out.println(new Date() + " >> " + msg);
	}

	private static void logErr(String msg) {
		System.err.println(new Date() + " >> " + msg);
	}
}
