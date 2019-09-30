package ru.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true, fluent = true)
public class ServerConfiguration {
    private int listenPort = 8080;
    private ThreadPoolConfiguration threadPoolConfiguration;
    private HttpCodecConfiguration httpCodecConfiguration;
    private HttpObjectAggregatorConfiguration httpObjectAggregatorConfiguration;
    private HttpHandlerConfiguration httpHandlerConfiguration;
    private WebSocketServerProtocolHandlerConfiguration webSocketServerProtocolHandlerConfiguration;
    private StompSubframeDecoderConfiguration stompSubframeDecoderConfiguration;
    private StompSubframeAggregatorConfiguration stompSubframeAggregatorConfiguration;
    private StompRootHandlerConfiguration stompRootHandlerConfiguration;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true, fluent = true)
    public static class HttpCodecConfiguration {
        private int maxInitialLineLength = 4096;
        private int maxHeaderSize = 8192;
        private int maxChunkSize = 8192;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true, fluent = true)
    public static class HttpObjectAggregatorConfiguration {
        private int maxContentLength = 64 * 1024;
        private boolean closeOnExpectationFailed = false;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true, fluent = true)
    public static class HttpHandlerConfiguration {
        private String baseUri = "/srv";
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true, fluent = true)
    public static class WebSocketServerProtocolHandlerConfiguration {
        private String websocketPath = "/srv";
        private String subprotocols;
        private boolean allowExtensions = false;
        private int maxFrameSize = 1024*1024;
        private boolean allowMaskMismatch = false;
        private boolean checkStartsWith = true;
        private long handshakeTimeoutMillis = 2000L;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true, fluent = true)
    public static class StompSubframeDecoderConfiguration {
        private int maxLineLength = 1024;
        private int maxChunkSize = 8132;
        private boolean validateHeaders = false;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true, fluent = true)
    public static class StompSubframeAggregatorConfiguration {
        private int maxContentLength = 2*1024*1024;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true, fluent = true)
    public class StompRootHandlerConfiguration {
        private int serverHeartbeat = 0;
        private int clientDesiredHeartBeat = 30000;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true, fluent = true)
    public static class ThreadPoolConfiguration {
        private int acceptThreads = 1;
        private int rwWorkerThreads = Runtime.getRuntime().availableProcessors();
        private int stompWorkerThreads = Runtime.getRuntime().availableProcessors();
    }
}
