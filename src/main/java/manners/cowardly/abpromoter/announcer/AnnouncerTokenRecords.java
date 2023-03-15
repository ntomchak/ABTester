package manners.cowardly.abpromoter.announcer;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

import manners.cowardly.abpromoter.announcer.abgroup.components.messages.MessageBuilder.DeliverableMessage.MessageTokenInfo;

/**
 * Stores announcer clickable tokens of user with menu page and msg id.
 *
 */
public class AnnouncerTokenRecords {

    private Map<UUID, UserTokens> users = new HashMap<UUID, UserTokens>();

    public void storeToken(UUID user, MessageTokenInfo token) {
        UserTokens tokens = users.get(user);
        if (tokens == null) {
            tokens = new UserTokens();
            users.put(user, tokens);
        }
        tokens.insertRecord(token);
    }

    public void storeTokens(UUID user, Collection<MessageTokenInfo> tokens) {
        UserTokens tokensData = users.get(user);
        if (tokensData == null) {
            tokensData = new UserTokens();
            users.put(user, tokensData);
        }
        tokensData.insertRecords(tokens);
    }

    /**
     * Returns empty if no match with user and token
     * 
     * @param user
     * @param token
     * @return
     */
    public Optional<String[]> tokenData(UUID user, String token) {
        UserTokens tokens = users.get(user);
        if (tokens == null)
            return Optional.empty();
        else
            return Optional.ofNullable(tokens.tokenData(token));
    }

    /**
     * Stores user's announcer clickable tokens and which message and menu page they
     * correspond to.
     *
     */
    private class UserTokens {

        private Queue<UserTokenRecord> records = new ArrayDeque<UserTokenRecord>();
        private Map<String, String[]> tokenToPageNames = new HashMap<String, String[]>();

        public void insertRecord(MessageTokenInfo token) {
            clearOld();
            records.offer(new UserTokenRecord(token.getToken()));
            tokenToPageNames.put(token.getToken(), token.getMenuPages());
        }

        public void insertRecords(Collection<MessageTokenInfo> tokens) {
            clearOld();
            for (MessageTokenInfo token : tokens) {
                records.offer(new UserTokenRecord(token.getToken()));
                tokenToPageNames.put(token.getToken(), token.getMenuPages());
            }
        }

        /**
         * returns msg id and menu page of clicked
         * 
         * @param token
         * @return null if this token is not stored for this user, probably because it
         *         expired
         */
        public String[] tokenData(String token) {
            String[] info = tokenToPageNames.get(token);
            if (info == null)
                return null;
            else
                return info;
        }

        /**
         * return true if empty afterwards
         * 
         * @return
         */
        public boolean clearOld() {
            while (records.peek() != null && (System.currentTimeMillis() / 1000) - records.peek().timeSecs > 32000)
                tokenToPageNames.remove(records.poll().token);
            return records.isEmpty();
        }

        private class UserTokenRecord {
            private long timeSecs;
            private String token;

            public UserTokenRecord(String token) {
                this.timeSecs = System.currentTimeMillis() / 1000L;
                this.token = token;
            }
        }
    }

}
