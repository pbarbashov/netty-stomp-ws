import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.netty.channel.Channel;


public class SessionInfo {
    private Channel channel;
    private BiMap<String, String> subscriptions = HashBiMap.create();
    private int clientHeartBeatMs = 0;
    private int serverHeartBeatMs = 0;

    public SessionInfo(Channel channel) {
        this.channel = channel;
    }

    public SessionInfo(Channel channel, int clientHeartBeatMs, int serverHeartBeatMs) {
        this.channel = channel;
        this.clientHeartBeatMs = clientHeartBeatMs;
        this.serverHeartBeatMs = serverHeartBeatMs;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void subscribe(String destination, String subscriptionId) {
        System.out.println("Subscribe " + subscriptionId);
        subscriptions.put(destination,subscriptionId);
    }

    public void unsubscribe(String subscriptionId) {
        System.out.println("Unsubscribe " + subscriptionId);
        subscriptions.inverse().remove(subscriptionId);
    }

    public String subscriptionId(String destination) {
        return subscriptions.get(destination);
    }

    public int getClientHeartBeatMs() {
        return clientHeartBeatMs;
    }

    public void setClientHeartBeatMs(int clientHeartBeatMs) {
        this.clientHeartBeatMs = clientHeartBeatMs;
    }

    public int getServerHeartBeatMs() {
        return serverHeartBeatMs;
    }

    public void setServerHeartBeatMs(int serverHeartBeatMs) {
        this.serverHeartBeatMs = serverHeartBeatMs;
    }

    @Override
    public String toString() {
        return "SessionInfo{" +
                "channel=" + channel +
                ", subscriptions=" + subscriptions +
                ", clientHeartBeatMs=" + clientHeartBeatMs +
                ", serverHeartBeatMs=" + serverHeartBeatMs +
                '}';
    }
}
