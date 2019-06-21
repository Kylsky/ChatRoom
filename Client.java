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
        //�����̳߳أ��̳߳�������ɿͻ��˵Ķ�д����
        EventLoopGroup group = new NioEventLoopGroup(1);
        //��������������
        Bootstrap b = new Bootstrap();

        //����
        try {
            //�����̳߳�
            f = b.group(group)
                    //����channel����
                    .channel(NioSocketChannel.class)
                    //����channel��ʼ���࣬���ڳ�ʼ��channel
                    .handler(new ClientChannelInitializer())
                    //�������ӵ�ַ���˿�
                    .connect("localhost", 9999);
            f.addListener((future) -> {
                if (future.isSuccess()) {
                    System.out.println("success");
                } else {
                    System.out.println("failed");
                }
            });
            //�ȴ�ִ�����
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
                //����Ϣת��ΪBytebuf
                buf = (ByteBuf) msg;
                //�����ֽ����飬���ó���Ϊbytebuf�ɶ�����
                byte[] bytes = new byte[buf.readableBytes()];
                //��buf������д�뵽�ֽ�������
                buf.getBytes(buf.readerIndex(), bytes);
                //��ӡ��Ϣ
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
                    //�ͷ�bytebuf
                    ReferenceCountUtil.release(buf);
                }
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx){
//        super.channelActive(ctx);
            //��channel��һ������ʱʹ��
            ByteBuf buf = Unpooled.copiedBuffer((ctx.channel().localAddress().toString()+" ����������").getBytes());
            ctx.writeAndFlush(buf);     //�����Ѿ�flush�ˣ����Բ���Ҫ�ͷ�bytebuf
        }
    }
}




