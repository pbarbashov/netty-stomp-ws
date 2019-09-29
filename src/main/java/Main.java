import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.stomp.*;

import java.net.InetSocketAddress;

public class Main {

    private final int port;
    private final ServerRuntime serverRuntime = new ServerRuntime();
    public Main(int port) {
        this.port = port;
    }


    public static void main(String[] args) throws InterruptedException {
        if (args.length != 1) {
            System.out.println("Args must have port");
            return;
        }
        int port = Integer.parseInt(args[0]);
        new Main(port).start();
    }

    private void start() throws InterruptedException {
      /* ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            Collection<SessionInfo> sessions = serverRuntime.sessions();
            for (SessionInfo session : sessions) {
                if (session.getChannel().isActive()) {
                    Attribute<String> sessionId = session.getChannel().attr(ServerRuntime.sessionAttribute);
                    session.getChannel().eventLoop().execute(() -> {
                        System.out.println("Writing to session " + sessionId);
                        StompFrame stompFrame = new DefaultStompFrame(StompCommand.MESSAGE);
                        String destination = "/user/proxy/kafka";
                        stompFrame.headers().add(StompHeaders.DESTINATION, destination);
                        stompFrame.headers().add(StompHeaders.CONTENT_TYPE,"application/json;charset=UTF-8");
                        stompFrame.headers().add(StompHeaders.SUBSCRIPTION, serverRuntime.searchSubscriptionId(sessionId.get(),destination));
                        stompFrame.content().writeCharSequence("{\"service\":1}",StandardCharsets.UTF_8);
                        session.getChannel().writeAndFlush(stompFrame);
                    });
                }
            }
        },5,5, TimeUnit.SECONDS);*/

        final EventLoopGroup acceptLoopGroup = new NioEventLoopGroup(1);
        final EventLoopGroup rwLoopGroup = new NioEventLoopGroup(2);
        try {
            final ServerBootstrap b = new ServerBootstrap();
            b.group(acceptLoopGroup,rwLoopGroup)
                    .localAddress(new InetSocketAddress(port))
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(64*1024));
                            ch.pipeline().addLast(new HttpRequestHandler("/uawSrv",serverRuntime));
                            ch.pipeline().addLast(new WebSocketServerProtocolHandler("/uawSrv",null,false,10*1024*1024,false,true,10000L));
                            ch.pipeline().addLast("sockjsDecoder",new SockJsDecoder());
                            ch.pipeline().addLast(new SockJsEncoder());
                            ch.pipeline().addLast(new StompSubframeDecoder());
                            ch.pipeline().addLast(new StompSubframeAggregator(10*1024*1024));
                            ch.pipeline().addLast(new MyStompSubframeEncoder());
                            ch.pipeline().addLast("stompHandler",new StompMessageHandler(serverRuntime, 0, 30000));
                        }
                    });

            ChannelFuture sync = b.bind().sync();
            sync.channel().closeFuture().sync();
        }
        finally {
            acceptLoopGroup.shutdownGracefully().sync();
            rwLoopGroup.shutdownGracefully().sync();
        }

    }
}
