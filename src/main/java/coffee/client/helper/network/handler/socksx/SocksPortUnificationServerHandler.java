/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.network.handler.socksx;

import coffee.client.helper.network.handler.socksx.v4.Socks4ServerDecoder;
import coffee.client.helper.network.handler.socksx.v4.Socks4ServerEncoder;
import coffee.client.helper.network.handler.socksx.v5.Socks5AddressEncoder;
import coffee.client.helper.network.handler.socksx.v5.Socks5InitialRequestDecoder;
import coffee.client.helper.network.handler.socksx.v5.Socks5ServerEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.List;

/**
 * Detects the version of the current SOCKS connection and initializes the pipeline with
 * {@link Socks4ServerDecoder} or {@link Socks5InitialRequestDecoder}.
 */
public class SocksPortUnificationServerHandler extends ByteToMessageDecoder {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SocksPortUnificationServerHandler.class);

    private final Socks5ServerEncoder socks5encoder;

    /**
     * Creates a new instance with the default configuration.
     */
    public SocksPortUnificationServerHandler() {
        this(Socks5ServerEncoder.DEFAULT);
    }

    /**
     * Creates a new instance with the specified {@link Socks5ServerEncoder}.
     * This constructor is useful when a user wants to use an alternative {@link Socks5AddressEncoder}.
     */
    public SocksPortUnificationServerHandler(Socks5ServerEncoder socks5encoder) {
        this.socks5encoder = ObjectUtil.checkNotNull(socks5encoder, "socks5encoder");
    }

    private static void logKnownVersion(ChannelHandlerContext ctx, SocksVersion version) {
        logger.debug("{} Protocol version: {}({})", ctx.channel(), version);
    }

    private static void logUnknownVersion(ChannelHandlerContext ctx, byte versionVal) {
        if (logger.isDebugEnabled()) {
            logger.debug("{} Unknown protocol version: {}", ctx.channel(), versionVal & 0xFF);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        final int readerIndex = in.readerIndex();
        if (in.writerIndex() == readerIndex) {
            return;
        }

        ChannelPipeline p = ctx.pipeline();
        final byte versionVal = in.getByte(readerIndex);
        SocksVersion version = SocksVersion.valueOf(versionVal);

        switch (version) {
            case SOCKS4a -> {
                logKnownVersion(ctx, version);
                p.addAfter(ctx.name(), null, Socks4ServerEncoder.INSTANCE);
                p.addAfter(ctx.name(), null, new Socks4ServerDecoder());
            }
            case SOCKS5 -> {
                logKnownVersion(ctx, version);
                p.addAfter(ctx.name(), null, socks5encoder);
                p.addAfter(ctx.name(), null, new Socks5InitialRequestDecoder());
            }
            default -> {
                logUnknownVersion(ctx, versionVal);
                in.skipBytes(in.readableBytes());
                ctx.close();
                return;
            }
        }

        p.remove(this);
    }
}
