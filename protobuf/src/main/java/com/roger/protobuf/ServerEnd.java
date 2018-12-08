package com.roger.protobuf;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class ServerEnd {
    public void bind(int port) throws Exception {
        // set nio thread groups
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).
                    childHandler(new ServerInitializer());

            // bind port
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

            // wait for server closing listening
            channelFuture.channel().closeFuture().sync();
        } finally {

            // quit and release resources
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static class ServerInitializer extends ChannelInitializer<SocketChannel> {

        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
            ch.pipeline().addLast(new ProtobufDecoder(UserInfoProto.Person.getDefaultInstance()));
            ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
            ch.pipeline().addLast(new ProtobufEncoder());
            ch.pipeline().addLast(new ServerHandler());
        }
    }

    public static class ServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            UserInfoProto.Person person = (UserInfoProto.Person) msg;
            if (person == null) {
                System.out.println("receive not person object");
            } else {
                ctx.writeAndFlush(response(person));
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println("get error:" + cause);
            ctx.close(); // exception cause, close socket
        }

        private UserInfoProto.Person response(UserInfoProto.Person person) {
            UserInfoProto.Person.Builder builder = UserInfoProto.Person.newBuilder();
            builder.setName(person.getName());
            builder.setId(person.getId() + 1);
            builder.setEmail(person.getEmail());
            for (int i = 0; i < person.getPhonesCount(); ++i) {
                builder.addPhones(i, person.getPhones(i));
            }
            return builder.build();
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

        System.out.println("start server end...");
        new ServerEnd().bind(port);
    }
}
