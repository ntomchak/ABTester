package manners.cowardly.abpromoter.utilities;

import java.util.concurrent.ThreadLocalRandom;

public class RandomStringGenerator {

    private static char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();

    public static String getString(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            builder.append(chars[ThreadLocalRandom.current().nextInt(chars.length)]);
        return builder.toString();
    }
}
