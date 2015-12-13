/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moosemorals.stockfighter;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
@ClientEndpoint
public class Firehose {

    private final Logger log = LoggerFactory.getLogger(Firehose.class);

    @OnOpen
    public void onOpen(Session userSession) {
        log.debug("Connected to {}", userSession);
    }

    @OnMessage
    public void onMessage(String message) {
        log.debug("Got a message : {}", message);
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        log.debug("Socket {} closed because {}", userSession, reason);
    }
}
