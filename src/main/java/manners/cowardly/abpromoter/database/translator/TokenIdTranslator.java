package manners.cowardly.abpromoter.database.translator;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;


public class TokenIdTranslator {
    private Map<String, Integer> tokenToDeliveryId = new HashMap<String, Integer>();
    private Map<String, UUID> tokenToUser = new HashMap<String, UUID>();
    private Queue<TimeEntry> expiration = new ArrayDeque<TimeEntry>();
    private long lastClearMillis = System.currentTimeMillis();

    public void addToken(String token, int deliveryId, UUID user) {
        clearOld();
        tokenToDeliveryId.put(token, deliveryId);
        tokenToUser.put(token, user);
        expiration.add(new TimeEntry(token, System.currentTimeMillis()));
    }

    /**
     * -1 if none, from sync
     * 
     * @param token
     * @return
     */
    public int idOfToken(String token, UUID user) {
        Integer id = tokenToDeliveryId.get(token);
        if (id != null && tokenToUser.get(token).equals(user))
            return id.intValue();
        else
            return -1;
    }

    private void clearOld() {
        if (System.currentTimeMillis() - lastClearMillis > 300000) {
            lastClearMillis = System.currentTimeMillis();
            while (expiration.peek() != null
                    && System.currentTimeMillis() - expiration.peek().timeMillis > 34000000) {
                String token = expiration.poll().token;
                tokenToDeliveryId.remove(token);
                tokenToUser.remove(token);
            }
        }
    }

    private class TimeEntry {
        public TimeEntry(String token, long timeMillis) {
            this.token = token;
            this.timeMillis = timeMillis;
        }

        private String token;
        private long timeMillis;
    }
}
