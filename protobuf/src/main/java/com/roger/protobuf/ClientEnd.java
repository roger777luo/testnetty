package com.roger.protobuf;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.util.ArrayList;
import java.util.List;

public class ClientEnd {
    public void connect(String host, int port) throws Exception {

        // set nio thread groups for client
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).
                    handler(new ClientInitializer());

            // run in async connect
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            // wait for closing socket
            channelFuture.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public static class ClientInitializer extends ChannelInitializer<SocketChannel> {

        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
            ch.pipeline().addLast(new ProtobufDecoder(UserInfoProto.Person.getDefaultInstance()));
            ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
            ch.pipeline().addLast(new ProtobufEncoder());
            ch.pipeline().addLast(new ClientHandler());
        }
    }

    public static class ClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            for (int i = 0; i < 10; ++i) {
                ctx.write(request(i));
            }
        }

        private UserInfoProto.Person request(int i) {
            UserInfoProto.Person.Builder builder = UserInfoProto.Person.newBuilder();
            builder.setName("roger" + i);
            builder.setId(i);
            builder.setEmail("123@qq.com");
            UserInfoProto.PhoneNumber.Builder phoneBuilder = UserInfoProto.PhoneNumber.newBuilder();
            phoneBuilder.setType(UserInfoProto.PhoneType.MOBILE);
            phoneBuilder.setNumber("1234567890123");
            List<UserInfoProto.PhoneNumber> numbers = new ArrayList<UserInfoProto.PhoneNumber>();
            numbers.add(phoneBuilder.build());
            builder.addAllPhones(numbers);
            return builder.build();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            UserInfoProto.Person person = (UserInfoProto.Person) msg;
            if (person == null) {
                System.out.println("receive invalid object from server");
            } else {
                System.out.println("receive id from server:" + person.getId() + ", name:" + person.getName());
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println("get error in client:" + cause);
            ctx.close(); // close the channel
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 1111;
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException exp) {
                // use default value
            }
        }

        System.out.println("start client end...");
        new ClientEnd().connect("127.0.0.1", port);
    }
}
