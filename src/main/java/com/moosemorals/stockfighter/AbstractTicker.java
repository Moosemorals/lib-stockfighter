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

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.websocket.ClientEndpoint;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
@ClientEndpoint
public abstract class AbstractTicker {

    public static final String BASE_URL = "wss://api.stockfighter.io/ob/api/ws";

    private static final Logger log = LoggerFactory.getLogger(AbstractTicker.class);

    private final String api_key;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    protected final URI endpoint;
    private Session websocket = null;

    public AbstractTicker(String api_key, URI endpoint) {
        this.api_key = api_key;
        this.endpoint = endpoint;

    }

    public void connect(String account, String venue) throws IOException {

        final ClientEndpointConfig.Builder configBuilder = ClientEndpointConfig.Builder.create();
        configBuilder.configurator(new ClientEndpointConfig.Configurator() {
            @Override
            public void beforeRequest(Map<String, List<String>> headers) {
                headers.put("X-Starfighter-Authorization", Arrays.asList(api_key));
            }

        });

        ClientManager.ReconnectHandler reconnectHandler = new ClientManager.ReconnectHandler() {

            private int counter = 0;

            @Override
            public boolean onDisconnect(CloseReason closeReason) {
                counter++;
                System.out.println("### Reconnecting... (reconnect count: " + counter + ")");
                return true;
            }

            @Override
            public boolean onConnectFailure(Exception exception) {
                counter++;
                System.out.println("### Reconnecting... (reconnect count: " + counter + ") " + exception.getMessage());
                return true;
            }

            @Override
            public long getDelay() {
                return 1;
            }
        };

        ClientManager client = ClientManager.createClient();
        client.getProperties().put(ClientProperties.REDIRECT_ENABLED, true);
        client.getProperties().put(ClientProperties.RECONNECT_HANDLER, reconnectHandler);

        if (connected.compareAndSet(false, true)) {
            try {
                websocket = client.connectToServer(new Endpoint() {
                    @Override
                    public void onOpen(Session session, EndpointConfig ec) {
                        log.debug("Websocket open: {}", session.getRequestURI());
                        session.addMessageHandler(new MessageHandler.Whole<String>() {

                            @Override
                            public void onMessage(String message) {
                                AbstractTicker.this.onMessage(message);
                            }
                        });
                    }

                    @Override
                    public void onError(Session session, Throwable thr) {
                        log.debug("Websocket error: {}, {}", session.getRequestURI(), thr.getMessage(), thr);
                    }

                    @Override
                    public void onClose(Session session, CloseReason closeReason) {
                        log.debug("Websocket close: {}, {}", session.getRequestURI(), closeReason.getReasonPhrase());
                    }

                }, configBuilder.build(), endpoint);
            } catch (DeploymentException ex) {
                log.error("Websocket problem: {}", ex.getMessage(), ex);
                connected.set(false);
                throw new IOException("Websocket problem: " + ex.getMessage(), ex);
            }
        }
    }

    protected abstract void onMessage(String message);

    public void disconnect() throws IOException {
        if (connected.compareAndSet(true, false)) {
            websocket.close();
        } else {
            log.warn("Not connectd");
        }
    }

}
