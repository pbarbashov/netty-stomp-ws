package ru.server;

import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class ServerRuntime {
    public final static AttributeKey<String> sessionAttribute = AttributeKey.newInstance("session-id");
    private ConcurrentHashMap<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    public void addSessionInfo(String sessionId, SessionInfo sessionInfo) {
        log.debug("Session added " + sessionId);
        sessions.put(sessionId,sessionInfo);
    }

    public void removeSession(String sessionId) {
        log.debug("Session removed " + sessionId);
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
        log.debug("Subscription not found session " + session + " destination " + destination);
        return "";
    }
}
