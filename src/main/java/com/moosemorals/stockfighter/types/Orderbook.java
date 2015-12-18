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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.json.stream.JsonParser;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A list of current bid/asks for a stock on a venue.
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
public class Orderbook {

    private static final Logger log = LoggerFactory.getLogger(Orderbook.class);

    private static final DateTimeFormatter dateParser = ISODateTimeFormat.dateTimeParser().withZoneUTC();
    private boolean isOk;
    private String errorStr;
    private String venue;
    private String symbol;
    private Entry[] bids;
    private Entry[] asks;
    private DateTime ts;

    public Orderbook(JsonParser parser) {
        while (parser.hasNext()) {
            JsonParser.Event next = parser.next();
            switch (next) {
                case KEY_NAME:
                    String key = parser.getString();
                    parser.next();
                    switch (key) {
                        case "ok":
                            isOk = next == JsonParser.Event.VALUE_TRUE;
                            break;
                        case "error":
                            errorStr = parser.getString();
                            break;
                        case "venue":
                            venue = parser.getString();
                            break;
                        case "symbol":
                            symbol = parser.getString();
                            break;
                        case "bids":
                            List<Entry> bidList = new ArrayList<>();
                            while (next != JsonParser.Event.END_ARRAY) {
                                bidList.add(new Entry(parser));
                                next = parser.next();
                            }
                            bids = new Entry[bidList.size()];
                            bidList.toArray(bids);
                            break;
                        case "asks":
                            List<Entry> askList = new ArrayList<>();
                            while (next != JsonParser.Event.END_ARRAY) {
                                askList.add(new Entry(parser));
                                if (parser.hasNext()) {
                                    next = parser.next();
                                } else {
                                    return;
                                }
                            }
                            asks = new Entry[askList.size()];
                            askList.toArray(asks);
                            break;

                        case "ts":
                            ts = dateParser.parseDateTime(parser.getString());
                            break;
                        default:
                            log.warn("Ignoring unexpected entry in fill [{}]", key);
                            break;
                    }
                    break;
                case END_OBJECT:
                    return;
            }
        }
    }

    public boolean isIsOk() {
        return isOk;
    }

    public String getErrorStr() {
        return errorStr;
    }

    public String getVenue() {
        return venue;
    }

    public String getSymbol() {
        return symbol;
    }

    public Entry[] getBids() {
        return bids;
    }

    public Entry[] getAsks() {
        return asks;
    }

    public DateTime getTs() {
        return ts;
    }

    @Override
    public String toString() {
        return "Orderbook{" + "isOk=" + isOk + ", errorStr=" + errorStr + ", venue=" + venue + ", symbol=" + symbol + ", bids=" + Arrays.toString(bids) + ", asks=" + Arrays.toString(asks) + ", ts=" + ts + '}';
    }

    public static class Entry {

        private int price;
        private int quantity;
        private boolean isBuy;

        private Entry(JsonParser parser) {
            while (parser.hasNext()) {
                JsonParser.Event next = parser.next();
                switch (next) {
                    case KEY_NAME:
                        String key = parser.getString();
                        next = parser.next();
                        switch (key) {
                            case "price":
                                price = parser.getInt();
                                break;
                            case "qty":
                                quantity = parser.getInt();
                                break;
                            case "isBuy":
                                isBuy = next == JsonParser.Event.VALUE_TRUE;
                                break;
                            default:
                                log.warn("Ignoring unexpected entry in fill [{}]", key);
                                break;
                        }
                        break;
                    case END_OBJECT:
                        return;
                }
            }
        }

        public int getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        public boolean isIsBuy() {
            return isBuy;
        }

        @Override
        public String toString() {
            return "Entry{" + "price=" + price + ", quantity=" + quantity + ", isBuy=" + isBuy + '}';
        }

    }
}
