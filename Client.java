package NettyStudy.netty.Version4;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;


public class Client {
    private ClientFrame clientFrame;
    private ChannelFuture f;

    Client(ClientFrame clientFrame) {
        this.clientFrame = clientFrame;
    }

    public void start(){
        //创建线程池，线程池用来完成客户端的读写操作
        EventLoopGroup group = new NioEventLoopGroup(1);
        //创建辅助启动类
        Bootstrap b = new Bootstrap();

        //启动
        try {
            //设置线程池
            f = b.group(group)
                    //设置channel类型
                    .channel(NioSocketChannel.class)
                    //设置channel初始化类，用于初始化channel
                    .handler(new ClientChannelInitializer())
                    //设置连接地址及端口
                    .connect("localhost", 9999);
            f.addListener((future) -> {
                if (future.isSuccess()) {
                    System.out.println("success");
                } else {
                    System.out.println("failed");
                }
            });
            //等待执行完毕
            f.sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

//    public Channel getChannel() {
//        return channel;
//    }

    void sendMsg(String text)throws Exception{
        f.channel().writeAndFlush(Unpooled.copiedBuffer((f.channel().localAddress()+": "+text).getBytes("gbk")));
    }

    void closeConnect()throws Exception{
        sendMsg("_bye_");
    }
//    public static void main(String[] args) {
//
//    }

    class ClientChannelInitializer extends ChannelInitializer {
        @Override
        protected void initChannel(Channel ch){
            ch.pipeline().addLast(new ClientHandler());
        }
    }

    class ClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg){
//        super.channelRead(ctx, msg);
            ByteBuf buf = null;
            try {
                //将消息转换为Bytebuf
                buf = (ByteBuf) msg;
                //创建字节数组，设置长度为bytebuf可读长度
                byte[] bytes = new byte[buf.readableBytes()];
                //将buf中数据写入到字节数组中
                buf.getBytes(buf.readerIndex(), bytes);
                //打印消息
                String addr = ctx.channel().localAddress().toString();
                String addr1 = new String(bytes);
                if (addr1.contains(addr)) {
                    addr1="Me: "+addr1.substring(addr1.indexOf(" "));
                    ClientFrame.INSTANCE.updateText(clientFrame.ta.getText() + addr1+ "\n");
//                    System.out.println(clientFrame.ta.getText());
                }else {
                    ClientFrame.INSTANCE.updateText(clientFrame.ta.getText()+new String(bytes)+"\n");
                }
            } finally {
                if (buf != null) {
                    //释放bytebuf
                    ReferenceCountUtil.release(buf);
                }
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx){
//        super.channelActive(ctx);
            //当channel第一次连上时使用
            ByteBuf buf = Unpooled.copiedBuffer((ctx.channel().localAddress().toString()+" 进入聊天室").getBytes());
            ctx.writeAndFlush(buf);     //这里已经flush了，所以不需要释放bytebuf
        }
    }
}




