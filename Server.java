package NettyStudy.netty.Version4;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

public class Server {
    public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static void main(String[] args)throws Exception{

//        ServerSocket serverSocket = new ServerSocket();
//        serverSocket.bind(new InetSocketAddress("127.0.0.1",8888));
//        serverSocket.accept();
//        System.out.println("connected");
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup(2);
        try {
            ServerBootstrap b = new ServerBootstrap();
            ChannelFuture future = b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    //指定handler处理客户端
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch){
//                            System.out.println(ch);
                            //创建pipeline，相当于一条责任链
                            ChannelPipeline pl = ch.pipeline();
                            //为责任链加入过滤器
                            pl.addLast(new ServerChildHandler());
                        }
                    }).bind(9999).sync();

            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

}

class ServerChildHandler extends ChannelInboundHandlerAdapter{
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Server.clients.add(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        super.channelRead(ctx, msg);
            ByteBuf buf = (ByteBuf) msg;
        try {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(),bytes);
            String s = new String(bytes);
            System.out.println(s);
            if (s.equals("_bye_")){
                Server.clients.remove(ctx.channel());
                ctx.close();
            }else {
                Server.clients.writeAndFlush(msg);
            }
//            System.out.println(buf.refCnt());
//            ctx.writeAndFlush(msg);
        }finally {
//            if (buf!=null){
//                ReferenceCountUtil.release(buf);
//                System.out.println(buf.refCnt());
//            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        Server.clients.remove(ctx.channel());
        ctx.close();
    }
}