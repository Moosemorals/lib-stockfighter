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
public class Fill {

    private static final DateTimeFormatter dateParser = ISODateTimeFormat.dateTimeParser().withZoneUTC();

    private final Logger log = LoggerFactory.getLogger(Fill.class);

    private int price;
    private int quantity;
    private DateTime ts;

    public Fill(JsonParser parser) {
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
                        case "ts":
                            ts = dateParser.parseDateTime(parser.getString());
                            break;
                        default:
                            log.warn("Ignoring unexpected entry in fill [{}]", key);
                            break;
                    }
                    break;
                case END_ARRAY:
                case END_OBJECT:
                    return;
            }
        }
    }

    @Override
    public String toString() {
        return "Fill{" + "price=" + price + ", quantity=" + quantity + ", ts=" + ts + '}';
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public DateTime getTs() {
        return ts;
    }

}
