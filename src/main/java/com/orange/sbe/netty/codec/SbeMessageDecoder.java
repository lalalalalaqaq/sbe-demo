package com.orange.sbe.netty.codec;

import com.orange.sbe.model.WithdrawRequest;
import com.orange.sbe.stub.MessageHeaderDecoder;
import com.orange.sbe.stub.WithdrawRequestDecoder;
import com.orange.sbe.stub.WithdrawRequestGroupDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.agrona.concurrent.UnsafeBuffer;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ooorangezhang
 */

@Slf4j
public class SbeMessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(72);
        byte[] bytes = new byte[72];
        // 写入前 32 个字节到 byteBuffer1
        for (int i = 0; i < 72; i++) {
            byte b = byteBuf.getByte(i);
            bytes[i] = b;
            System.out.print(b + " ");
        }
        byteBuffer.put(bytes);
        List<WithdrawRequest> withdrawRequests = decodeRequests(new UnsafeBuffer(byteBuffer));
        System.out.println(withdrawRequests);
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

    private static List<WithdrawRequest> decodeRequests(UnsafeBuffer buffer) {
        WithdrawRequestGroupDecoder decoder = new WithdrawRequestGroupDecoder();
        final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
        int bufferOffset = 0;
        headerDecoder.wrap(buffer, bufferOffset);
        final int actingBlockLength = headerDecoder.blockLength();
        final int actingVersion = headerDecoder.version();
        bufferOffset += headerDecoder.encodedLength();
        decoder.wrap(buffer, bufferOffset, actingBlockLength, actingVersion);
        ArrayList<WithdrawRequest> result = new ArrayList<>();
        for (final WithdrawRequestGroupDecoder.RequestDecoder requestDecoder : decoder.request()) {
            BigDecimal resAmount = BigDecimal.valueOf(requestDecoder.amount().mantissa())
                    .scaleByPowerOfTen((int) requestDecoder.amount().exponent());
            result.add(new WithdrawRequest(requestDecoder.accountId(), resAmount,
                    requestDecoder.currency(),requestDecoder.market()));
        }
        return result;
    }
}
