package com.pans.mono.mono_server.controller;

import com.pans.mono.mono_server.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());
    }

    @Test
    void shouldBrodcastMessageToPublicTopic() throws Exception {
        CompletableFuture<ChatMessage> resultKeeper = new CompletableFuture<>();

        String url = "ws://localhost:" + port + "/ws";

        StompSession session = stompClient
                .connectAsync(url, new StompSessionHandlerAdapter() {})
                .get(1, TimeUnit.SECONDS);

        session.subscribe("/topic/public", new StompSessionHandlerAdapter() {
            @Override
            public Type getPayloadType(StompHeaders headers){
                return ChatMessage.class;
            }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                resultKeeper.complete((ChatMessage) payload);
            }
        });

        ChatMessage testMessage = new ChatMessage();
        testMessage.setType(ChatMessage.MessageType.CHAT);
        testMessage.setSender("TestUser");
        testMessage.setContent("Message from integration test!");

        session.send("/app/chat.sendMessage", testMessage);

        ChatMessage receivedMessage = resultKeeper.get(3, TimeUnit.SECONDS);

        assertNotNull(receivedMessage);
        assertEquals("TestUser", receivedMessage.getSender());
        assertEquals("Message from integration test!", receivedMessage.getContent());
    }

}
