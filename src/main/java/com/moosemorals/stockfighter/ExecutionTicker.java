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

import com.moosemorals.stockfighter.types.Execution;
import java.io.StringReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import javax.json.Json;
import javax.json.stream.JsonParser;

/**
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
public class ExecutionTicker extends AbstractTicker {

    public ExecutionTicker(String api_key, URI endpoint) {
        super(api_key, endpoint);
        listeners = new HashSet<>();
    }

    private final Set<Listener> listeners;

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

    private void notifyListeners(Execution message) {
        synchronized (listeners) {
            for (Listener l : listeners) {
                l.onExecute(message);
            }
        }
    }

    public interface Listener {

        void onExecute(Execution ex);
    }

    @Override
    public void onMessage(String message) {
        try (JsonParser parser = Json.createParser(new StringReader(message))) {

            notifyListeners(new Execution(parser));

        }
    }

}
