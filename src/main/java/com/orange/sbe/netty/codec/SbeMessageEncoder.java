package com.orange.sbe.netty.codec;

import com.orange.sbe.model.WithdrawRequest;
import com.orange.sbe.stub.MessageHeaderEncoder;
import com.orange.sbe.stub.WithdrawRequestEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.agrona.concurrent.UnsafeBuffer;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

/**
 * @author ooorangezhang
 */

@Slf4j
public class SbeMessageEncoder extends MessageToByteEncoder  {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object message, ByteBuf byteBuf) throws Exception {
        if (message == null) {
            throw new Exception("encode fail, no data!");
        }
        WithdrawRequest obj = (WithdrawRequest) message;
        UnsafeBuffer unsafeBuffer = encodeRequest(obj);
        System.out.println(obj);
        ByteBuffer res = unsafeBuffer.byteBuffer();
        byteBuf.writeBytes(res);
        byte[] bytes = new byte[64];
        for (int i = 0; i < 64; i++) {
            byte b = byteBuf.getByte(i);
            bytes[i] = b;
            System.out.print(b + " ");
        }
    }

    private static UnsafeBuffer encodeRequest(WithdrawRequest withdrawRequest) {
        // At least 28 bytes need to be allocated.
        UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocate(32));
        WithdrawRequestEncoder withdrawRequestEncoder = new WithdrawRequestEncoder();
        MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
        withdrawRequestEncoder.wrapAndApplyHeader(buffer,0, messageHeaderEncoder);
        BigDecimal amount = withdrawRequest.getAmount();
        int priceMantissa = amount.scaleByPowerOfTen(amount.scale()).intValue();
        int priceExponent = amount.scale() * -1;
        withdrawRequestEncoder.market(withdrawRequest.getMarket())
                .currency(withdrawRequest.getCurrency())
                .accountId(withdrawRequest.getAccountId())
                .amount().mantissa(priceMantissa).exponent(priceExponent);
        return buffer;
    }
}
