import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';

// URL do WebSocket Nativo (note o ws:// em vez de http://)
const SOCKET_URL = 'ws://localhost:8080/ws';

export const useWebSocket = (
  topic: string,
  onMessageReceived: (message: any) => void,
  addLog: (log: string) => void
) => {
  const [isConnected, setIsConnected] = useState(false);
  // Usamos useRef para manter a mesma instância do cliente entre renderizações
  const stompClientRef = useRef<Client | null>(null);

  useEffect(() => {
    addLog('Iniciando conexão WebSocket...');

    // 1. Cria o novo cliente STOMP
    const client = new Client({
      // 2. Aponta para o nosso endpoint (ws://)
      brokerURL: SOCKET_URL,

      // Desativa logs de debug no console
      debug: (str) => {
        // console.log(new Date(), str);
      },

      // Callback de sucesso na conexão
      onConnect: () => {
        setIsConnected(true);
        addLog('Conexão WebSocket estabelecida.');

        // 3. Se inscreve no tópico de broadcast
        client.subscribe(topic, (message) => {
          try {
            const body = JSON.parse(message.body);
            onMessageReceived(body);
          } catch (e) {
            addLog(`Erro ao processar mensagem: ${e}`);
          }
        });
      },

      // Callback de erro
      onStompError: (frame) => {
        setIsConnected(false);
        addLog(`Erro no STOMP: ${frame.headers['message']} | ${frame.body}`);
      },
      
      // Callback ao fechar (ex: servidor caiu)
      onWebSocketClose: () => {
        if (isConnected) { // Só loga se estávamos conectados
          setIsConnected(false);
          addLog('Desconectado do WebSocket.');
        }
      },
      
      // Tenta reconectar a cada 5 segundos
      reconnectDelay: 5000,
    });

    // 4. Salva a referência e ativa a conexão
    stompClientRef.current = client;
    client.activate();

    // 5. Efeito de limpeza: desativa ao desmontar o componente
    return () => {
      addLog('Desconectando...');
      client.deactivate();
      setIsConnected(false);
    };
    
  }, [topic, onMessageReceived, addLog]);

  return { isConnected };
};