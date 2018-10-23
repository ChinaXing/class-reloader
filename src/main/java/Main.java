import java.io.File;
import java.io.FileInputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;

public class Main {
	public static void main(String[] args) throws Exception {
		System.out.println(">>>>> -------------- Class-ReLoader -------------- <<<<<");
		String javaHome = System.getenv("JAVA_HOME");
		if(javaHome == null || javaHome.equals("")) {
			javaHome = System.getProperty("java.home");
		}
		System.out.println("Java Home is :" + javaHome);
		String toolLib = (javaHome.endsWith("/") ? javaHome : javaHome + "/") + "lib/tools.jar";
		System.out.println("tools.jar : " + toolLib);
		if(!new File(toolLib).exists()) {
			System.err.println("error : tools.jar not exist !");
			System.exit(1);
		}
		URLClassLoader url = new URLClassLoader(new URL[]{new URL("jar", "", "file:"+ toolLib + "!/")}, ClassLoader.getSystemClassLoader());
		Thread.currentThread().setContextClassLoader(url);
		try {
			run(args[0], args[1], args[2]);
			System.out.println("**reload Done.");
		}catch (Throwable t) {
			t.printStackTrace();
			System.err.println("**reload Fail.");
		}
		System.out.println(">>>>> -------------- Class-ReLoader -------------- <<<<<");
	}

	public static void agentmain(String agentArgs, Instrumentation inst) {
		try {
			System.out.println(">>>>> -------------- Class-ReLoader Agent -------------- <<<<<");
			System.out.println("with Arguments :");
			System.out.println(agentArgs);
			String[] clazz = agentArgs.split(",");
			if(clazz.length != 2) {
				System.err.println("Invalid arguments, expect : className,classFilePath");
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
			System.out.println("Succeed.");
		}catch (Throwable t) {
			t.printStackTrace();
		}finally {
			System.out.println(">>>>> -------------- Class-ReLoader Agent -------------- <<<<<");
		}
	}

	private static void run(String pid, String clz, String clzFile) throws Exception{
		Class<?> vmClz = Thread.currentThread().getContextClassLoader().loadClass("com.sun.tools.attach.VirtualMachine");
		Object vm = vmClz.getDeclaredMethod("attach", String.class).invoke(null, pid);
		String agentJar = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		System.out.println("Agent jar : " + agentJar);
		if(!new File(agentJar).exists()) {
			System.err.println("Agent jar file not exist, please make sure it is in current dir");
			return;
		}
		vmClz.getDeclaredMethod("loadAgent", String.class, String.class).invoke(vm, agentJar, clz + "," + clzFile);
	}
}
