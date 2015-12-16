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
import com.moosemorals.stockfighter.types.Orderbook;
import com.moosemorals.stockfighter.types.Quote;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Check The API Is Up.
     *
     * @return true if the API is working
     * @throws IOException on network problems, or if the API is down.
     */
    public boolean heartBeat() throws IOException {
        URL target;
        try {
            target = new URL(base_url + "/heartbeat");
        } catch (MalformedURLException ex) {
            throw new IOException("Cany build URL: " + ex.getMessage(), ex);
        }

        HttpURLConnection conn = (HttpURLConnection) target.openConnection();
        conn.setRequestProperty("X-Starfighter-Authorization", api_key);
        conn.connect();

        try (JsonParser parser = Json.createParser(conn.getInputStream())) {
            outerwhile:
            while (parser.hasNext()) {
                switch (parser.next()) {
                    case KEY_NAME:
                        String key = parser.getString();
                        switch (key) {
                            case "ok":
                                if (parser.next() == JsonParser.Event.VALUE_TRUE) {
                                    return true;
                                }
                                break;
                            case "error":
                                throw new IOException("Server side problem: " + parser.getString());
                            default:
                                log.warn("Unexpected key [{}] when checking server availiblity", key);
                                break;
                        }
                        break;
                    case END_OBJECT:
                        break outerwhile;
                }
            }
        }
        // Shouldn't get here, but may get bad Json.
        return false;
    }

    /**
     * Check A Venue Is Up
     *
     * @param venue String id of venue to check
     * @return true if the venue is working
     * @throws IOException on network problems, or if the venue is down.
     */
    public boolean heartBeat(String venue) throws IOException {
        URL target;
        try {
            target = new URL(base_url + "/venues/" + venue + "/heartbeat");
        } catch (MalformedURLException ex) {
            throw new IOException("Cany build URL: " + ex.getMessage(), ex);
        }

        HttpURLConnection conn = (HttpURLConnection) target.openConnection();
        conn.setRequestProperty("X-Starfighter-Authorization", api_key);
        conn.connect();

        try (JsonParser parser = Json.createParser(conn.getInputStream())) {
            outerwhile:
            while (parser.hasNext()) {
                switch (parser.next()) {
                    case KEY_NAME:
                        String key = parser.getString();
                        switch (key) {
                            case "ok":
                                if (parser.next() == JsonParser.Event.VALUE_TRUE) {
                                    return true;
                                }
                                break;
                            case "error":
                                throw new IOException("Venue not availible: " + parser.getString());
                            default:
                                log.warn("Unexpected key [{}] when checking venue availiblity", key);
                                break;
                        }
                        break;
                    case END_OBJECT:
                        break outerwhile;
                }
            }
        }

        // Shouldn't get here, but may get bad Json.
        return false;
    }

    public Map<String, String> listSymbols(String venue) throws IOException {
        URL target;
        try {
            target = new URL(base_url + "/venues/" + venue + "/stocks");
        } catch (MalformedURLException ex) {
            throw new IOException("Cany build URL: " + ex.getMessage(), ex);
        }

        HttpURLConnection conn = (HttpURLConnection) target.openConnection();
        conn.setRequestProperty("X-Starfighter-Authorization", api_key);
        conn.connect();

        try (JsonParser parser = Json.createParser(conn.getInputStream())) {
            outerwhile:
            while (parser.hasNext()) {
                switch (parser.next()) {
                    case KEY_NAME:
                        switch (parser.getString()) {
                            case "ok":
                                // ignored
                                break;
                            case "error":
                                throw new IOException("Can't get list of stocks: " + parser.getString());
                            case "symbols":
                                return parseSymbols(parser);
                        }
                        break;
                    case END_OBJECT:
                        break outerwhile;
                }
            }
        }
        // Shouldn't get here, but since we can, return an empty map
        return new HashMap<>();
    }

    private Map<String, String> parseSymbols(JsonParser parser) {
        Map<String, String> stocks = new HashMap<>();

        String name = null, symbol = null, key;

        outerwhile:
        while (parser.hasNext()) {
            switch (parser.next()) {
                case START_OBJECT:
                    name = null;
                    symbol = null;
                    break;
                case KEY_NAME:
                    key = parser.getString();
                    parser.next();
                    switch (key) {
                        case "name":
                            name = parser.getString();
                            break;
                        case "symbol":
                            symbol = parser.getString();
                            break;
                    }
                    break;
                case END_OBJECT:
                    if (name != null && symbol != null) {
                        stocks.put(symbol, name);
                    }
                    break;
                case END_ARRAY:
                    break outerwhile;
            }
        }

        return stocks;
    }

    public Orderbook getOrderbook(String venue, String symbol) throws IOException {
        URL target;
        try {
            target = new URL(base_url + "/venues/" + venue + "/stocks/" + symbol);
        } catch (MalformedURLException ex) {
            throw new IOException("Cany build URL: " + ex.getMessage(), ex);
        }

        HttpURLConnection conn = (HttpURLConnection) target.openConnection();
        conn.setRequestProperty("X-Starfighter-Authorization", api_key);
        conn.connect();

        try (JsonParser parser = Json.createParser(conn.getInputStream())) {
            return new Orderbook(parser);
        }
    }

    public OrderStatus postOrder(Order order) throws IOException {
        StringBuilder target = new StringBuilder();
        target.append(base_url)
                .append("/venues/").append(order.getVenue())
                .append("/stocks/").append(order.getSymbol())
                .append("/orders");

        URL url;
        try {
            url = new URL(target.toString());
        } catch (MalformedURLException ex) {
            throw new IOException("Can't build URL:" + ex.getMessage(), ex);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-Starfighter-Authorization", api_key);
        conn.setDoOutput(true);

        try (JsonGenerator gen = Json.createGenerator(conn.getOutputStream())) {
            order.toJson(gen);
        }

        try (JsonParser parser = Json.createParser(conn.getInputStream())) {
            return new OrderStatus(parser);
        }
    }

    public OrderStatus getOrderStatus(String venue, String symbol, int id) throws IOException {
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
        conn.setRequestProperty("X-Starfighter-Authorization", api_key);
        conn.connect();

        try (JsonParser parser = Json.createParser(conn.getInputStream())) {
            return new OrderStatus(parser);
        }
    }

    public List<OrderStatus> getOrderStatus(String venue, String account) throws IOException {
        StringBuilder target = new StringBuilder();
        target.append(base_url)
                .append("/venues/").append(venue)
                .append("/accounts/").append(account)
                .append("/orders/");

        URL url;
        try {
            url = new URL(target.toString());
        } catch (MalformedURLException ex) {
            throw new IOException("Can't build URL:" + ex.getMessage(), ex);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("X-Starfighter-Authorization", api_key);
        conn.connect();

        List<OrderStatus> result = new ArrayList<>();
        try (JsonParser parser = Json.createParser(conn.getInputStream())) {
            outerwhile:
            while (parser.hasNext()) {
                switch (parser.next()) {
                    case KEY_NAME:
                        String key = parser.getString();
                        switch (key) {
                            case "ok":
                                // ignored;
                                break;
                            case "error":
                                throw new IOException("Can't get list of orders: " + parser.getString());
                            case "orders":
                                innerwhile:
                                while (parser.hasNext()) {
                                    switch (parser.next()) {
                                        case START_OBJECT:
                                            result.add(new OrderStatus(parser));
                                            break;
                                        case END_ARRAY:
                                            break innerwhile;
                                    }
                                }
                                break;
                        }
                        break;

                    case END_OBJECT:
                        break outerwhile;
                }
            }
        }
        return result;
    }

    public List<OrderStatus> getOrderStatus(String venue, String account, String symbol) throws IOException {
        StringBuilder target = new StringBuilder();
        target.append(base_url)
                .append("/venues/").append(venue)
                .append("/accounts/").append(account)
                .append("/stocks/").append(symbol)
                .append("/orders");

        URL url;
        try {
            url = new URL(target.toString());
        } catch (MalformedURLException ex) {
            throw new IOException("Can't build URL:" + ex.getMessage(), ex);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("X-Starfighter-Authorization", api_key);
        conn.connect();

        List<OrderStatus> result = new ArrayList<>();
        try (JsonParser parser = Json.createParser(conn.getInputStream())) {
            outerwhile:
            while (parser.hasNext()) {
                switch (parser.next()) {
                    case KEY_NAME:
                        String key = parser.getString();
                        switch (key) {
                            case "ok":
                                // ignored;
                                break;
                            case "error":
                                throw new IOException("Can't get list of orders: " + parser.getString());
                            case "orders":
                                innerwhile:
                                while (parser.hasNext()) {
                                    switch (parser.next()) {
                                        case START_OBJECT:
                                            result.add(new OrderStatus(parser));
                                            break;
                                        case END_ARRAY:
                                            break innerwhile;
                                    }
                                }
                                break;
                        }
                        break;

                    case END_OBJECT:
                        break outerwhile;
                }
            }
        }
        return result;
    }

    public OrderStatus cancelOrder(String venue, String symbol, int id) throws IOException {
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

        try (JsonParser parser = Json.createParser(conn.getInputStream())) {
            return new OrderStatus(parser);
        }
    }

    public Quote getQuote(String venue, String symbol) throws IOException {
        URL target;
        try {
            target = new URL(base_url + "/venues/" + venue + "/stocks/" + symbol + "/quote");
        } catch (MalformedURLException ex) {
            throw new IOException("Cany build URL: " + ex.getMessage(), ex);
        }

        HttpURLConnection conn = (HttpURLConnection) target.openConnection();
        conn.setRequestProperty("X-Starfighter-Authorization", api_key);
        conn.connect();

        try (JsonParser parser = Json.createParser(conn.getInputStream())) {
            return new Quote(parser);
        }
    }

}
