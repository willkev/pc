package system.start;

public class Start {

    public static void main(String[] args) throws InterruptedException {
        while (true) {
            System.out.println(System.currentTimeMillis());
            Thread.sleep(3000);
        }
    }
}
