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
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
public class Receipt {

    private final Logger log = LoggerFactory.getLogger(Receipt.class);
    private static final DateTimeFormatter dateParser = ISODateTimeFormat.dateTimeParser().withZoneUTC();

    private boolean ok;
    private String errorStr;
    private String symbol;
    private String venue;
    private String account;
    private boolean buy;
    private int originalQuantity;
    private int remainingQuantity;
    private int price;
    private Order.OrderType type;
    private int id;
    private DateTime ts;
    private int totalFilled;
    private boolean open;
    private final Fill[] fills;

    public Receipt(JsonParser parser) {
        List<Fill> f = new ArrayList();
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
                        case "direction":
                            buy = parser.getString().equals("buy");
                            break;
                        case "originalQty":
                            originalQuantity = parser.getInt();
                            break;
                        case "qty":
                            remainingQuantity = parser.getInt();
                            break;
                        case "price":
                            price = parser.getInt();
                            break;
                        case "type":
                            type = Order.OrderType.fromString(parser.getString());
                            break;
                        case "id":
                            id = parser.getInt();
                            break;
                        case "ts":
                            ts = dateParser.parseDateTime(parser.getString());
                            break;
                        case "fills":
                            next = parser.next();
                            while (next != JsonParser.Event.END_ARRAY) {
                                f.add(new Fill(parser));
                                next = parser.next();
                            }
                            break;
                        case "totalFilled":
                            totalFilled = parser.getInt();
                            break;
                        case "open":
                            open = next == JsonParser.Event.VALUE_TRUE;
                            break;
                    }
            }
        }

        fills = new Fill[f.size()];
        f.toArray(fills);
    }

    public Logger getLog() {
        return log;
    }

    public boolean isOk() {
        return ok;
    }

    public String getErrorStr() {
        return errorStr;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getVenue() {
        return venue;
    }

    public String getAccount() {
        return account;
    }

    public boolean isBuy() {
        return buy;
    }

    public int getOriginalQuantity() {
        return originalQuantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public int getPrice() {
        return price;
    }

    public Order.OrderType getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public DateTime getTs() {
        return ts;
    }

    public int getTotalFilled() {
        return totalFilled;
    }

    public boolean isOpen() {
        return open;
    }

    public Fill[] getFills() {
        return fills;
    }

    @Override
    public String toString() {
        return "Receipt{" + "log=" + log + ", ok=" + ok + ", errorStr=" + errorStr + ", symbol=" + symbol + ", venue=" + venue + ", account=" + account + ", buy=" + buy + ", originalQuantity=" + originalQuantity + ", remainingQuantity=" + remainingQuantity + ", price=" + price + ", type=" + type + ", id=" + id + ", ts=" + ts + ", totalFilled=" + totalFilled + ", open=" + open + ", fills=" + Arrays.toString(fills) + '}';
    }

}
