public class Test {
	public static void main(String[] args) {
		try {
			while(true) {
				Thread.sleep(1000);
				new Test().x();
			}
		}catch (Throwable t) {
			t.printStackTrace();
		}
	}
	public void x() {
		System.out.println("100");
	}
}
