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
package com.moosemorals.stockfighter;

import com.moosemorals.stockfighter.types.Order;
import com.moosemorals.stockfighter.types.OrderStatus;
import com.moosemorals.stockfighter.types.Orderbook;
import com.moosemorals.stockfighter.types.Quote;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * These aren't very comprehensive tests, and they rely on the API working, but
 * still, better than nothing.
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
public class TraderNGTest {

    private static final String ACCOUNT = "EXB123456";
    private static final String VENUE = "TESTEX";
    private static final String SYMBOL = "FOOBAR";

    @Test
    public void test_listStocks() throws Exception {
        Trader t = new Trader(loadApiKey());

        Map<String, String> stocks = t.listSymbols(VENUE);

        for (Map.Entry<String, String> entry : stocks.entrySet()) {
            System.out.printf("%s (%s)\n", entry.getValue(), entry.getKey());
        }
    }

    @Test
    public void test_heartbeat() throws Exception {
        Trader t = new Trader(loadApiKey());

        assertTrue(t.heartBeat());
    }

    @Test
    public void test_venueHeartbeat() throws Exception {
        Trader t = new Trader(loadApiKey());

        assertTrue(t.heartBeat(VENUE));
    }

    @Test
    public void test_getOrderbook() throws Exception {
        Trader t = new Trader(loadApiKey());

        Orderbook orderbook = t.getOrderbook(VENUE, SYMBOL);

        System.out.println(orderbook);
    }

    @Test
    public void test_getQuote() throws Exception {
        Trader t = new Trader(loadApiKey());

        Quote quote = t.getQuote(VENUE, SYMBOL);

        System.out.println(quote);
    }

    @Test
    public void test_buyStock() throws Exception {
        Trader t = new Trader(loadApiKey());

        Order o = new Order();
        o.setAccount(ACCOUNT);
        o.setVenue(VENUE);
        o.setSymbol(SYMBOL);
        o.setPrice(4000);
        o.setQuantity(5);
        o.setBuy(false);
        o.setType(Order.OrderType.Limit);

        OrderStatus postOrder = t.postOrder(o);
        System.out.println("Posted:   " + postOrder);

        OrderStatus orderStatus = t.getOrderStatus(VENUE, SYMBOL, postOrder.getId());

        System.out.println("Status:   " + orderStatus);

        OrderStatus cancelOrder = t.cancelOrder(VENUE, SYMBOL, postOrder.getId());

        System.out.println("Canceled: " + cancelOrder);
    }

    /*
    @Test
    public void test_cancelWrongOrder() throws Exception {
        Trader t = new Trader(loadApiKey());
        OrderStatus cancelOrder = t.cancelOrder(VENUE, SYMBOL, 15);
        System.out.println("Canceled: " + cancelOrder);
    }

    @Test
    public void test_getOrderStatus() throws Exception {
        Trader t = new Trader(loadApiKey());

        List<OrderStatus> orders = t.getOrderStatus(VENUE, ACCOUNT);

        System.out.println("Got " + orders.size() + " orders");

    }
     */
    private static String loadApiKey() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(TraderNGTest.class.getResourceAsStream("/apiKey")));
        return in.readLine();
    }
}
