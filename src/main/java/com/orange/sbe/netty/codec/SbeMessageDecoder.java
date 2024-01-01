package com.orange.sbe.netty.codec;

import com.orange.sbe.model.WithdrawRequest;
import com.orange.sbe.stub.MessageHeaderDecoder;
import com.orange.sbe.stub.WithdrawRequestDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.agrona.concurrent.UnsafeBuffer;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author ooorangezhang
 */

@Slf4j
public class SbeMessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(byteBuf.readableBytes());
        ByteBuffer byteBuffer = ByteBuffer.allocate(64);
        byte[] bytes = new byte[64];
        // 写入前 32 个字节到 byteBuffer1
        for (int i = 0; i < 64; i++) {
            byte b = byteBuf.getByte(i);
            bytes[i] = b;
            System.out.print(b + " ");
        }
        byteBuffer.put(bytes);
        WithdrawRequest withdrawRequest = decodeRequest(new UnsafeBuffer(byteBuffer));
        list.add(withdrawRequest);
        System.out.println(withdrawRequest);
        byteBuf.skipBytes(byteBuf.readableBytes());
    }

    private static WithdrawRequest decodeRequest(UnsafeBuffer buffer) {
        WithdrawRequestDecoder withdrawRequestDecoder = new WithdrawRequestDecoder();
        withdrawRequestDecoder.wrapAndApplyHeader(buffer, 0, new MessageHeaderDecoder());
        BigDecimal resAmount = BigDecimal.valueOf(withdrawRequestDecoder.amount().mantissa())
                .scaleByPowerOfTen((int) withdrawRequestDecoder.amount().exponent());
        return new WithdrawRequest(withdrawRequestDecoder.accountId(), resAmount,
                withdrawRequestDecoder.currency(),withdrawRequestDecoder.market());
    }
}
