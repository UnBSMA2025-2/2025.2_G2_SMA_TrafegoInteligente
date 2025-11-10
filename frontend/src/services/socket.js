// src/services/socket.js
let socket = null;
let listeners = [];

/**
 * Inicia a conexão WebSocket com o servidor Java.
 * @param {function(string)} onMessage - callback para mensagens recebidas
 */
export function connectWebSocket(onMessage) {
  if (socket && socket.readyState === WebSocket.OPEN) return socket;

  socket = new WebSocket("ws://localhost:8081");

  socket.onopen = () => {
    console.log("✅ Conexão WebSocket estabelecida com o servidor Java!");
    onMessage("✅ Conectado ao servidor WebSocket (porta 8081)");
  };

  socket.onmessage = (event) => {
    const msg = event.data;
    console.log("📩 WebSocket:", msg);
    onMessage(msg);
  };

  socket.onclose = () => {
    console.warn("⚠️ Conexão WebSocket encerrada.");
    onMessage("⚠️ Conexão WebSocket encerrada.");
  };

  socket.onerror = (err) => {
    console.error("❌ Erro WebSocket:", err);
    onMessage("❌ Erro na conexão WebSocket.");
  };

  return socket;
}

/**
 * Permite o envio de mensagens (caso queira interagir com o servidor)
 */
export function sendMessage(message) {
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send(message);
  } else {
    console.warn("❌ Não é possível enviar, WebSocket não está aberto.");
  }
}

/**
 * Fecha a conexão (por exemplo, ao desmontar o componente)
 */
export function disconnectWebSocket() {
  if (socket) {
    socket.close();
    socket = null;
  }
}
