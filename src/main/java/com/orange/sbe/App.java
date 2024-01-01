package com.orange.sbe;

import com.orange.sbe.model.WithdrawRequest;
import com.orange.sbe.stub.*;
import org.agrona.concurrent.UnsafeBuffer;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author ooorangezhang
 */
public class App {


    public static void main(String[] args) {
        singleEncode();
        repeatEncode();
    }


    /**
     * WithdrawRequest : 20 byte
     *  - accountId 2 byte
     *  - amount   16 byte
     *  - currency 1 byte
     *  - market 1 byte
     * MessageHeader : 8 byte
     *  - blockLength 2 byte
     *  - templateId 2 byte
     *  - schemaId 2 byte
     *  - version  2 byte
     * UnsafeBuffer : 20 + 8 = 28 byte
     */
    private static void singleEncode() {
        WithdrawRequest withdrawRequest = mockSingleReq();
        UnsafeBuffer unsafeBuffer = encodeRequest(withdrawRequest);
        System.out.println(withdrawRequest.equals(decodeRequests(unsafeBuffer)));
    }


    /**
     * WithdrawRequest : 20 byte * 3 = 60 byte
     *  - accountId 2 byte
     *  - amount   16 byte
     *  - currency 1 byte
     *  - market 1 byte
     * groupSizeEncoding : 4 byte
     *  - numInGroup  2 byte
     *  - blockLength 2 byte
     * MessageHeader : 8 byte
     *  - blockLength 2 byte
     *  - templateId 2 byte
     *  - schemaId 2 byte
     *  - version  2 byte
     * UnsafeBuffer : 60 + 4 + 8 = 72 byte
     */
    private static void repeatEncode() {
        List<WithdrawRequest> withdrawRequests = mockRepeatReq();
        UnsafeBuffer unsafeBuffer = encodeRequests(withdrawRequests);
        System.out.println(withdrawRequests.equals(decodeRequests(unsafeBuffer)));

    }

    private static WithdrawRequest mockSingleReq() {
        return new WithdrawRequest(810975, BigDecimal.valueOf(10.12), Currency.BTC, Market.NASDAQ);
    }

    /**
     * byte[] bytes = {0, 0, 2, 0, 1, 0, 0, 0, 20, 0, 3, 0, 1, 2, 17, 39, -12, 3, 0, 0, 0, 0, 0, 0, -2, -1, -1, -1, -1, -1, -1, -1, 1, 0, 33, 78, -11, 3, 0, 0, 0, 0, 0, 0, -2, -1, -1, -1, -1, -1, -1, -1, 1, 1, 49, 117, -10, 3, 0, 0, 0, 0, 0, 0, -2, -1, -1, -1, -1, -1, -1, -1};
     */
    private static List<WithdrawRequest> mockRepeatReq() {
        WithdrawRequest withdrawRequest1 = new WithdrawRequest(810975, BigDecimal.valueOf(10.12), Currency.BTC, Market.NASDAQ);
        WithdrawRequest withdrawRequest2 = new WithdrawRequest(810976, BigDecimal.valueOf(10.13), Currency.CNY, Market.NASDAQ);
        WithdrawRequest withdrawRequest3 = new WithdrawRequest(810977, BigDecimal.valueOf(10.14), Currency.USD, Market.NASDAQ);
        return new ArrayList<>(List.of(withdrawRequest1,withdrawRequest2,withdrawRequest3));
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
