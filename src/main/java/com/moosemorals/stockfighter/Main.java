/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moosemorals.stockfighter;

import com.moosemorals.stockfighter.types.Execution;
import com.moosemorals.stockfighter.types.Order;
import com.moosemorals.stockfighter.types.Receipt;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final String ACCOUNT = "EXB123456";
    private static final String VENUE = "TESTEX";
    private static final String SYMBOL = "FOOBAR";

    public static void main(String[] args) throws IOException, InterruptedException {

        String apiKey = loadApiKey();
        //   QuoteTicker qt = new QuoteTicker(apiKey);
        ExecutionTicker et = new ExecutionTicker(apiKey);

        et.addListener(new ExecutionTicker.Listener() {
            @Override
            public void onExecute(Execution ex) {
                log.debug("Execution : {}", ex);
            }
        });

        et.connect(ACCOUNT, VENUE);

        Thread.sleep(2000);

        final Trader t = new Trader(apiKey);

        Order o = new Order();
        o.setPrice(1);
        o.setQuantity(4);
        o.setBuy(false);
        o.setAccount(ACCOUNT);
        o.setVenue(VENUE);
        o.setType(Order.OrderType.Market);
        o.setSymbol(SYMBOL);

        try {
            Receipt postOrder = t.postOrder(o);
            log.debug("Posted an order: {}", postOrder);
            Receipt cancelOrder = t.cancelOrder(VENUE, SYMBOL, postOrder.getId());
            log.debug("And then canceled it: {}", cancelOrder);

        } catch (IOException ex) {
            log.debug("Can't post trade: {}", ex.getMessage(), ex);
        }

        /*
        qt.addListener(new QuoteTicker.Listener() {
            DateTime lastTrade = new DateTime(0);

            @Override
            public void onQuote(Quote q) {

                System.out.print(".");
                System.out.flush();

                if (q.getLastTrade().isAfter(lastTrade)) {
                    log.info("Someone traded at {}", q.getLast());
                    lastTrade = q.getLastTrade();
                }

                Order o = new Order();
                o.setPrice(q.getLast() - 1);
                o.setQuantity(4);
                o.setBuy(true);
                o.setAccount(ACCOUNT);
                o.setVenue(VENUE);
                o.setType(Order.OrderType.FOK);
                o.setSymbol(SYMBOL);

                try {
                    Receipt postOrder = t.postOrder(o);

                    log.debug("Posted an order: {}", postOrder);

                } catch (IOException ex) {
                    log.debug("Can't post trade: {}", ex.getMessage(), ex);
                }

            }
        });
         */
        //    qt.connect(ACCOUNT, VENUE);
        System.in.read();
        //    qt.disconnect();
        //    et.disconnect();
    }

    private static String loadApiKey() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/apiKey")));
        return in.readLine();
    }
}
