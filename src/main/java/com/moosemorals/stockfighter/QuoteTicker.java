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

import com.moosemorals.stockfighter.types.Quote;
import java.io.StringReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.websocket.ClientEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connect to the Quotes Websocket and parse incoming messages.
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
@ClientEndpoint
public class QuoteTicker extends AbstractTicker {

    private static final Logger log = LoggerFactory.getLogger(QuoteTicker.class);
    private final Set<Listener> listeners;

    public QuoteTicker(String api_key, URI endpoint) {
        super(api_key, endpoint);
        listeners = new HashSet<>();
    }

    public void addListener(Listener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removeListener(Listener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    private void notifyListeners(Quote message) {
        synchronized (listeners) {
            for (Listener l : listeners) {
                l.onQuote(message);
            }
        }
    }

    @Override
    public void onMessage(String message) {
        Quote q = parseQuote(message);
        if (q != null) {
            notifyListeners(q);
        }
    }

    private Quote parseQuote(String message) {
        try (JsonParser parser = Json.createParser(new StringReader(message))) {

            while (parser.hasNext()) {
                switch (parser.next()) {
                    case KEY_NAME:
                        switch (parser.getString()) {
                            case "ok":
                                if (parser.next() != JsonParser.Event.VALUE_TRUE) {
                                    log.warn("Not ok Quote: {}", message);
                                    return null;
                                }
                                break;
                            case "quote":
                                if (parser.next() != JsonParser.Event.START_OBJECT) {
                                    log.warn("Quote, but not an object: {}", message);
                                    return null;
                                }
                                return new Quote(parser);
                            default:
                                log.debug("Ignoring unexpected content from quote: {}", message);
                        }
                }
            }
        }
        return null;
    }

    public interface Listener {

        void onQuote(Quote q);
    }
}
