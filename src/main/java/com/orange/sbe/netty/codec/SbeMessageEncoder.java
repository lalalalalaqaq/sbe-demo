package com.orange.sbe.netty.codec;

import com.orange.sbe.model.WithdrawRequest;
import com.orange.sbe.stub.MessageHeaderEncoder;
import com.orange.sbe.stub.WithdrawRequestEncoder;
import com.orange.sbe.stub.WithdrawRequestGroupEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.agrona.concurrent.UnsafeBuffer;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.List;

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
        List<WithdrawRequest> obj = (List<WithdrawRequest>) message;
        UnsafeBuffer unsafeBuffer = encodeRequests(obj);
        System.out.println(obj);
        ByteBuffer res = unsafeBuffer.byteBuffer();
        byteBuf.writeBytes(res);
        byte[] bytes = new byte[72];
        for (int i = 0; i < 72; i++) {
            byte b = byteBuf.getByte(i);
            bytes[i] = b;
            System.out.print(b + " ");
        }
    }

    private static UnsafeBuffer encodeRequests(List<WithdrawRequest> withdrawRequests) {
        // At least 72 bytes need to be allocated.
        UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(72));
        WithdrawRequestGroupEncoder withdrawRequestGroupEncoder = new WithdrawRequestGroupEncoder();
        withdrawRequestGroupEncoder.wrapAndApplyHeader(buffer, 0,  new MessageHeaderEncoder());
        WithdrawRequestGroupEncoder.RequestEncoder requestEncoder = withdrawRequestGroupEncoder.requestCount(withdrawRequests.size());
        for (WithdrawRequest withdrawRequest : withdrawRequests) {
            requestEncoder.next();
            BigDecimal amount = withdrawRequest.getAmount();
            int priceMantissa = amount.scaleByPowerOfTen(amount.scale()).intValue();
            int priceExponent = amount.scale() * -1;
            requestEncoder.market(withdrawRequest.getMarket())
                    .currency(withdrawRequest.getCurrency())
                    .accountId(withdrawRequest.getAccountId())
                    .amount().mantissa(priceMantissa).exponent(priceExponent);
        }
        return buffer;
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
