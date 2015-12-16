/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moosemorals.stockfighter;

import com.moosemorals.stockfighter.types.Execution;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.json.Json;
import javax.json.stream.JsonParser;
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
public class ExecutionTicker {

    private static final String base_url = "ws://api.stockfighter.io/ob/api";

    private static final Logger log = LoggerFactory.getLogger(ExecutionTicker.class);

    private final String api_key;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final Set<Listener> listeners;

    private Session websocket = null;

    public ExecutionTicker(String api_key) {
        this.api_key = api_key;
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

    private void notifyListeners(Execution message) {
        synchronized (listeners) {
            for (Listener l : listeners) {
                l.onExecute(message);
            }
        }
    }

    public void connect(String account, String venue) throws IOException {

        final ClientEndpointConfig.Builder configBuilder = ClientEndpointConfig.Builder.create();
        configBuilder.configurator(new ClientEndpointConfig.Configurator() {
            @Override
            public void beforeRequest(Map<String, List<String>> headers) {
                headers.put("X-Starfighter-Authorization", Arrays.asList(api_key));
            }

        });

        ClientManager client = ClientManager.createClient();
        client.getProperties().put(ClientProperties.REDIRECT_ENABLED, true);

        if (connected.compareAndSet(false, true)) {
            try {
                websocket = client.connectToServer(new Endpoint() {
                    @Override
                    public void onOpen(Session session, EndpointConfig ec) {
                        log.debug("Websocket open: {}", session.getRequestURI());
                        session.addMessageHandler(new MessageHandler.Whole<String>() {

                            @Override
                            public void onMessage(String message) {
                                try (JsonParser parser = Json.createParser(new StringReader(message))) {

                                    notifyListeners(new Execution(parser));

                                }
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

                }, configBuilder.build(), new URI(base_url + "/ws/" + account + "/venues/" + venue + "/executions"));
            } catch (DeploymentException | URISyntaxException ex) {
                log.error("Websocket problem: {}", ex.getMessage(), ex);
                connected.set(false);
                throw new IOException("Websocket problem: " + ex.getMessage(), ex);
            }
        }
    }

    public void disconnect() throws IOException {
        if (connected.compareAndSet(true, false)) {
            websocket.close();
        } else {
            log.warn("Not connectd");
        }
    }

    public interface Listener {

        void onExecute(Execution ex);
    }
}
