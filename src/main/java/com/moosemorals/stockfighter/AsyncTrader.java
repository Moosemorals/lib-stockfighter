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
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
public class AsyncTrader {

    private final Logger log = LoggerFactory.getLogger(AsyncTrader.class);

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final BlockingQueue<Message> outboundQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<OrderStatus> inboundQueue = new LinkedBlockingQueue<>();
    private final Set<Listener> listeners = new HashSet<>();

    private Thread outbound;
    private Thread inbound;
    private final String venue;
    private final String symbol;

    private final Trader trader;

    public AsyncTrader(Trader trader, String venue, String symbol) {
        this.trader = trader;
        this.venue = venue;
        this.symbol = symbol;
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            outbound = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (running.get()) {
                        try {
                            Message next = outboundQueue.take();
                            switch (next.type) {
                                case POST:
                                    trader.postOrder(next.order);
                                    break;
                                case CLOSE:
                                    inboundQueue.put(trader.cancelOrder(venue, symbol, next.id));
                                    break;
                            }
                        } catch (InterruptedException ex) {
                            running.set(false);
                            log.info("Closing down AsyncTrader");
                            inbound.interrupt();
                            return;
                        } catch (IOException ex) {
                            throw new RuntimeException("IO Error: " + ex.getMessage(), ex);
                        }
                    }
                }
            }, "AsyncTrader-Outbound");
            outbound.start();

            inbound = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (running.get()) {

                        try {
                            notifyListeners(inboundQueue.take());
                        } catch (InterruptedException ex) {
                            running.set(false);
                            log.info("Closing down AsyncTrader");
                            outbound.interrupt();
                        }
                    }
                }
            }, "AsyncTrader-Inbound");
            inbound.start();
        }
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            outbound.interrupt();
            inbound.interrupt();
        }
    }

    public void post(Order o) {
        Message m = new Message();
        m.type = Message.Type.POST;
        m.order = o;
        outboundQueue.add(m);
    }

    public void cancel(int id) {
        Message m = new Message();
        m.id = id;
        m.type = Message.Type.CLOSE;
        outboundQueue.add(m);
    }

    private static class Message {

        public enum Type {
            POST, CLOSE
        };
        public Order order;
        public int id;
        public Type type;
    }

    public void addListener(Listener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removeListers(Listener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    private void notifyListeners(OrderStatus status) {
        synchronized (listeners) {
            for (Listener l : listeners) {
                l.onCancel(status);
            }
        }
    }

    public interface Listener {

        void onCancel(OrderStatus status);
    }
}
