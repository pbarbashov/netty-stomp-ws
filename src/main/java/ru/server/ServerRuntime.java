package ru.server;

import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ServerRuntime {
    public final static AttributeKey<String> sessionAttribute = AttributeKey.newInstance("session-id");
    private ConcurrentHashMap<String, SessionInfo> sessions = new ConcurrentHashMap<>();
    private AtomicInteger sessionCnt = new AtomicInteger(0);

    public void addSessionInfo(String sessionId, SessionInfo sessionInfo) {
        log.trace("Session added " + sessionId);
        sessions.put(sessionId,sessionInfo);
        log.info("Total sessions: " + sessionCnt.incrementAndGet());
    }

    public void removeSession(String sessionId) {
        log.trace("Session removed " + sessionId);
        sessions.remove(sessionId);
        log.info("Total sessions: " + sessionCnt.decrementAndGet());
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
