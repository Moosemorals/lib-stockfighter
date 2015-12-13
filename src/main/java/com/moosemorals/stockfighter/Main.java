/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moosemorals.stockfighter;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.ClientEndpointConfig.Builder;
import javax.websocket.CloseReason;
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
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final String API_KEY = "52c2462b3610f6e8fcc281d8a15730a95d50acdd";
    private static final String base_url = "ws://api.stockfighter.io/ob/api";

    public static void main(String[] args) {
        try {
            final CountDownLatch messageLatch = new CountDownLatch(1);

            final Builder configBuilder = ClientEndpointConfig.Builder.create();
            configBuilder.configurator(new ClientEndpointConfig.Configurator() {
                @Override
                public void beforeRequest(Map<String, List<String>> headers) {
                    headers.put("X-Starfighter-Authorization", Arrays.asList(API_KEY));
                }

            });

            ClientManager client = ClientManager.createClient();
            client.getProperties().put(ClientProperties.REDIRECT_ENABLED, true);

            try (Session ws = client.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig ec) {
                    log.debug("Websocket open: {}", session.getRequestURI());
                    session.addMessageHandler(new MessageHandler.Whole<String>() {
                        @Override
                        public void onMessage(String message) {
                            log.debug("Received message: {}", message);
                        }
                    });
                }

                @Override
                public void onError(Session session, Throwable thr) {
                    log.debug("Websocket error: {}, {}", session, thr.getMessage(), thr);
                }

                @Override
                public void onClose(Session session, CloseReason closeReason) {
                    log.debug("Websocket close: {}, {}", session, closeReason);
                }

            }, configBuilder.build(), new URI(base_url + "/ws/TMB96530732/venues/ZXNEX/tickertape"))) {
                messageLatch.await(100, TimeUnit.SECONDS);
            }
        } catch (Exception ex) {
            log.error("Problem: {}", ex.getMessage(), ex);
        }
    }

}
