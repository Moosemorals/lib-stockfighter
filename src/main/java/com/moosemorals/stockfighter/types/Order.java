/*
 * The MIT License
 *
 * Copyright 2015 Osric Wilkinson <osric@fluffypeople.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.moosemorals.stockfighter.types;

import javax.json.stream.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
public class Order {

    private static final Logger log = LoggerFactory.getLogger(Order.class);

    private String account;
    private String venue;
    private String symbol;
    private int price;
    private int quantity;
    private boolean buy;
    private OrderType type;

    public enum OrderType {

        Limit("limit"), Market("market"), FOK("fill-or-kill"), IOC("immediate-or-cancel");

        private final String apiValue;

        private OrderType(String apiValue) {
            this.apiValue = apiValue;
        }

        @Override
        public String toString() {
            return apiValue;
        }

        public static OrderType fromString(String raw) {
            switch (raw) {
                case "limit":
                    return Limit;
                case "market":
                    return Market;
                case "fill-or-kill":
                    return FOK;
                case "immediate-or-cancel":
                    return IOC;
                default:
                    throw new IllegalArgumentException("Unrecognised OrderType [" + raw + "]");
            }
        }
    }

    public void toJson(JsonGenerator out) {
        out.writeStartObject()
                .write("account", account)
                .write("venue", venue)
                .write("stock", symbol)
                .write("qty", quantity)
                .write("price", price)
                .write("direction", buy ? "buy" : "sell")
                .write("orderType", type.toString())
                .writeEnd();
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isBuy() {
        return buy;
    }

    public void setBuy(boolean buy) {
        this.buy = buy;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Order{" + "account=" + account + ", venue=" + venue + ", symbol=" + symbol + ", price=" + price + ", quantity=" + quantity + ", buy=" + buy + ", type=" + type + '}';
    }

}
