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

import javax.json.stream.JsonParser;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
public class Quote {

    private static final DateTimeFormatter dateParser = ISODateTimeFormat.dateTimeParser().withZoneUTC();

    private static final Logger log = LoggerFactory.getLogger(Quote.class);

    private String symbol;
    private String venue;
    private int bid;
    private int ask;
    private int bidSize;
    private int askSize;
    private int bidDepth;
    private int askDepth;
    private int last;
    private int lastSize;
    private DateTime lastTrade;
    private DateTime quoteTime;

    public Quote(JsonParser parser) {
        while (parser.hasNext()) {
            switch (parser.next()) {
                case KEY_NAME:
                    String key = parser.getString();
                    parser.next();

                    switch (key) {
                        case "symbol":
                            symbol = parser.getString();
                            break;
                        case "venue":
                            venue = parser.getString();
                            break;
                        case "bid":
                            bid = parser.getInt();
                            break;
                        case "ask":
                            ask = parser.getInt();
                            break;
                        case "bidSize":
                            bidSize = parser.getInt();
                            break;
                        case "askSize":
                            askSize = parser.getInt();
                            break;
                        case "bidDepth":
                            bidDepth = parser.getInt();
                            break;
                        case "askDepth":
                            askDepth = parser.getInt();
                            break;
                        case "last":
                            last = parser.getInt();
                            break;
                        case "lastSize":
                            lastSize = parser.getInt();
                            break;
                        case "lastTrade":
                            lastTrade = dateParser.parseDateTime(parser.getString());
                            break;
                        case "quoteTime":
                            quoteTime = dateParser.parseDateTime(parser.getString());
                            break;
                        default:
                            log.debug("Unexpected json key {}", key);
                    }
            }
        }
    }

    public String getSymbol() {
        return symbol;
    }

    public String getVenue() {
        return venue;
    }

    public int getBid() {
        return bid;
    }

    public int getAsk() {
        return ask;
    }

    public int getBidSize() {
        return bidSize;
    }

    public int getAskSize() {
        return askSize;
    }

    public int getBidDepth() {
        return bidDepth;
    }

    public int getAskDepth() {
        return askDepth;
    }

    public int getLast() {
        return last;
    }

    public int getLastSize() {
        return lastSize;
    }

    public DateTime getLastTrade() {
        return lastTrade;
    }

    public DateTime getQuoteTime() {
        return quoteTime;
    }

    @Override
    public String toString() {
        return "Quote{" + "symbol=" + symbol + ", venue=" + venue + ", ask=" + ask + ", bidSize=" + bidSize + ", askSize=" + askSize + ", bidDepth=" + bidDepth + ", askDepth=" + askDepth + ", last=" + last + ", lastSize=" + lastSize + ", lastTrade=" + lastTrade + ", quoteTime=" + quoteTime + '}';
    }

}
