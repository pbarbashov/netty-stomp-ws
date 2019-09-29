import io.netty.util.AttributeKey;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ServerRuntime {
    public final static AttributeKey<String> sessionAttribute = AttributeKey.newInstance("session-id");
    private ConcurrentHashMap<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    public void addSessionInfo(String sessionId, SessionInfo sessionInfo) {
        System.out.println("Session added " + sessionId);
        sessions.put(sessionId,sessionInfo);
    }

    public void removeSession(String sessionId) {
        System.out.println("Session removed " + sessionId);
        sessions.remove(sessionId);
    }

    public SessionInfo getSessionInfo(String sessionId) {
        return sessions.get(sessionId);
    }


    public Collection<SessionInfo> sessions() {
        return sessions.values();
    }

    public CharSequence searchSubscriptionId(String session, String destination) {
        SessionInfo sessionInfo = sessions.get(session);
        if (sessionInfo != null) {
            String subscriptionId = sessionInfo.subscriptionId(destination);
            if (subscriptionId != null)
                return subscriptionId;
        }
        System.out.println("Subscription not found session " + session + " destination " + destination);
        return "";
    }
}
