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
public class Execution {

    private static final DateTimeFormatter dateParser = ISODateTimeFormat.dateTimeParser().withZoneUTC();
    private final Logger log = LoggerFactory.getLogger(Execution.class);

    private boolean ok;
    private String errorStr;
    private String account;
    private String venue;
    private String symbol;
    private OrderStatus order;
    private int standingId;
    private int incomingId;
    private int price;
    private int filled;
    private DateTime filledAt;
    private boolean standingComplete;
    private boolean incommingComplete;

    public Execution(JsonParser parser) {
        while (parser.hasNext()) {
            switch (parser.next()) {
                case KEY_NAME:
                    String key = parser.getString();
                    JsonParser.Event next = parser.next();

                    switch (key) {
                        case "ok":
                            ok = next == JsonParser.Event.VALUE_TRUE;
                            break;
                        case "error":
                            errorStr = parser.getString();
                            break;
                        case "symbol":
                            symbol = parser.getString();
                            break;
                        case "venue":
                            venue = parser.getString();
                            break;
                        case "account":
                            account = parser.getString();
                            break;
                        case "order":
                            order = new OrderStatus(parser);
                            break;
                        case "standingId":
                            standingId = parser.getInt();
                            break;
                        case "incomingId":
                            incomingId = parser.getInt();
                            break;
                        case "price":
                            price = parser.getInt();
                            break;
                        case "filled":
                            filled = parser.getInt();
                            break;
                        case "filledAt":
                            filledAt = dateParser.parseDateTime(parser.getString());
                            break;
                        case "standingComplete":
                            standingComplete = next == JsonParser.Event.VALUE_TRUE;
                            break;
                        case "incomingComplete":
                            incommingComplete = next == JsonParser.Event.VALUE_TRUE;
                            break;
                        default:
                            log.warn("Unexpeted key in execution tick: [{}]", key);
                            break;
                    }
                    break;
                case END_OBJECT:
                    return;
            }
        }
    }

    @Override
    public String toString() {
        return "Execution{" + "ok=" + ok + ", errorStr=" + errorStr + ", account=" + account + ", venue=" + venue + ", symbol=" + symbol + ", order=" + order + ", standingId=" + standingId + ", incomingId=" + incomingId + ", price=" + price + ", filled=" + filled + ", filledAt=" + filledAt + ", standingComplete=" + standingComplete + ", incommingComplete=" + incommingComplete + '}';
    }

    public boolean isOk() {
        return ok;
    }

    public String getErrorStr() {
        return errorStr;
    }

    public String getAccount() {
        return account;
    }

    public String getVenue() {
        return venue;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderStatus getOrder() {
        return order;
    }

    public int getStandingId() {
        return standingId;
    }

    public int getIncomingId() {
        return incomingId;
    }

    public int getPrice() {
        return price;
    }

    public int getFilled() {
        return filled;
    }

    public DateTime getFilledAt() {
        return filledAt;
    }

    public boolean isStandingComplete() {
        return standingComplete;
    }

    public boolean isIncommingComplete() {
        return incommingComplete;
    }

}
