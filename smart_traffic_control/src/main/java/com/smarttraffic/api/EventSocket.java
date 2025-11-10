package com.smarttraffic.api;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EventSocket extends WebSocketServer {

    // Mantém o conjunto de conexões ativas (clientes conectados)
    private static final Set<WebSocket> connections = Collections.synchronizedSet(new HashSet<>());

    // Construtor
    public EventSocket(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        System.out.println("✅ Nova conexão WebSocket: " + conn.getRemoteSocketAddress());
        conn.send("Conexão WebSocket estabelecida com o servidor Java!");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("📩 Mensagem recebida do cliente: " + message);
        conn.send("Eco do servidor: " + message);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        System.out.println("❌ Conexão WebSocket encerrada: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("⚠️ Erro no WebSocket: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("🚀 Servidor WebSocket iniciado na porta " + getPort());
    }

    // =========================================================
    // MÉTODO ESTÁTICO: Envia mensagens para todos os clientes
    // =========================================================
    public static void broadcastMessage(String message) {
        synchronized (connections) {
            for (WebSocket conn : connections) {
                conn.send(message);
            }
        }
    }

    public static void waitForConnection() {
        System.out.println("⏳ Aguardando conexão WebSocket...");
        while (true) {
            synchronized (connections) {
                if (!connections.isEmpty()) {
                    System.out.println("✅ Conexão WebSocket detectada!");
                    break;
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
