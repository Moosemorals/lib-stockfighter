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
import com.moosemorals.stockfighter.types.Receipt;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
public class Trader {

    private final Logger log = LoggerFactory.getLogger(Trader.class);

    private static final String base_url = "https://api.stockfighter.io/ob/api";

    private final String api_key;

    public Trader(String api_key) {
        this.api_key = api_key;
    }

    public Receipt postOrder(Order order) throws IOException {
        URL target;
        try {
            target = new URL(base_url + "/venues/" + order.getVenue() + "/stocks/" + order.getSymbol() + "/orders");
        } catch (MalformedURLException ex) {
            throw new IOException("Can't build url: " + ex.getMessage(), ex);
        }

        HttpURLConnection conn = (HttpURLConnection) target.openConnection();

        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-Starfighter-Authorization", api_key);
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"))) {
            if (!log.isDebugEnabled()) {
                JsonGenerator gen = Json.createGenerator(out);
                order.toJson(gen);
            } else {
                StringWriter buff = new StringWriter();
                try (JsonGenerator gen = Json.createGenerator(buff)) {
                    order.toJson(gen);
                }
                log.debug("Sending order {}", buff.toString());
                out.write(buff.toString());
            }
            out.flush();
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            StringBuilder temp = new StringBuilder();
            char[] buff = new char[4096];
            int read;
            while ((read = in.read(buff, 0, buff.length)) > 0) {
                temp.append(buff, 0, read);
            }
            log.debug("Result {}", temp.toString());

            try (JsonParser parser = Json.createParser(new StringReader(temp.toString()))) {
                Receipt receipt = new Receipt(parser);
                log.debug("Trade completed: {}", receipt);
                return receipt;
            }
        }
    }

    public Receipt cancelOrder(String venue, String symbol, int id) throws IOException {
        StringBuilder target = new StringBuilder();
        target.append(base_url)
                .append("/venues/").append(venue)
                .append("/stocks/").append(symbol)
                .append("/orders/").append(id);

        URL url;
        try {
            url = new URL(target.toString());
        } catch (MalformedURLException ex) {
            throw new IOException("Can't build URL:" + ex.getMessage(), ex);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("X-Starfighter-Authorization", api_key);

        conn.connect();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            StringBuilder temp = new StringBuilder();
            char[] buff = new char[4096];
            int read;
            while ((read = in.read(buff, 0, buff.length)) > 0) {
                temp.append(buff, 0, read);
            }
            log.debug("Result {}", temp.toString());

            try (JsonParser parser = Json.createParser(new StringReader(temp.toString()))) {
                Receipt receipt = new Receipt(parser);
                log.debug("Trade completed: {}", receipt);
                return receipt;
            }
        }

    }
}
