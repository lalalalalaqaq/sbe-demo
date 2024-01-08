package com.orange.sbe.model;


import com.orange.sbe.stub.Currency;
import com.orange.sbe.stub.Market;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author ooorangezhang
 */
public class WithdrawRequest {

    /**
     * 2 byte
     */
    private Long accountId;

    /**
     * 8 byte +  8 byte
     */
    private BigDecimal amount;

    /**
     * 1 byte
     */
    private Currency currency;

    /**
     * 1 byte
     */
    private Market market;

    public WithdrawRequest(Long accountId, BigDecimal amount, Currency currency, Market market) {
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
        this.market = market;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Market getMarket() {
        return market;
    }

    public void setMarket(Market market) {
        this.market = market;
    }

    @Override
    public String toString() {
        return "WithdrawRequest{" +
                "accountId=" + accountId +
                ", amount=" + amount +
                ", currency=" + currency +
                ", market=" + market +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        WithdrawRequest that = (WithdrawRequest) object;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(amount, that.amount) &&
                currency == that.currency &&
                market == that.market;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, amount, currency, market);
    }
}
