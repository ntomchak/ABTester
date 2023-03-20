package manners.cowardly.abpromoter.utilities;

public class Timer {

    private static long start;
    private static long end;

    public static void start() {
        start = System.nanoTime();
    }

    public static void end() {
        end = System.nanoTime();
    }

    public static float time() {
        return (float) (end - start) / 1000000f;
    }

    public static void endAndPrint() {
        end();
        System.out.println(time());
    }
}
