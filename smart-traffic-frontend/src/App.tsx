import { useState, useCallback } from 'react';
import { useWebSocket } from './hooks/useWebSocket';
import { SimulationMap } from './components/SimulationMap';
import { ControlPanel } from './components/ControlPanel';

// --- Tipos de Estado ---

export interface TrafficLightState {
  id: string;
  state: 'GREEN' | 'RED' | 'YELLOW';
  x: number; // Posição X no mapa
  y: number; // Posição Y no mapa
}

export interface CarState {
  id: string;
  x: number;
  y: number;
  rotation: number; // Rotação em graus (0, 90, 180, 270)
}

export interface PardalState {
  id: string;
  x: number;
  y: number;
  triggered: boolean; // Se está "flashando"
}

// --- Tipo de Evento (Payload) ---
// Define a estrutura das mensagens que chegam do WebSocket
interface AgentEvent {
  type:
    | 'TRAFFIC_LIGHT_UPDATE'
    | 'CAR_POSITION_UPDATE'
    | 'PARDAL_TRIGGERED'
    | 'CAR_REMOVED'; // Adicionado
  payload: any;
}

// --- Posições Iniciais (Baseado no Layout Fixo de 1000x800) ---

const initialTrafficLights: TrafficLightState[] = [
  // Semáforo da pista Vertical (controla quem desce)
  { id: 'TL1', state: 'RED', x: 440, y: 340 }, 
  // Semáforo da pista Horizontal (controla quem vai para a direita)
  { id: 'TL2', state: 'GREEN', x: 530, y: 430 },
];

const initialPardal: PardalState = {
  id: 'P1',
  x: 530, // Perto do semáforo 2, para pegar quem fura o sinal
  y: 340,
  triggered: false,
};

// --- Componente Principal ---

function App() {
  const [logs, setLogs] = useState<string[]>([]);
  const [trafficLights, setTrafficLights] = useState<TrafficLightState[]>(
    initialTrafficLights
  );
  const [cars, setCars] = useState<CarState[]>([]);
  const [pardal, setPardal] = useState<PardalState>(initialPardal);

  // --- Funções de Callback (Memorizadas) ---

  const addLog = useCallback((message: string) => {
    const timestamp = new Date().toLocaleTimeString();
    setLogs((prevLogs) =>
      [`[${timestamp}] ${message}`, ...prevLogs].slice(0, 100)
    );
  }, []);

  const handleMessage = useCallback((message: AgentEvent) => {
    // Roteador de eventos: processa a mensagem do WebSocket
    switch (message.type) {
      
      case 'TRAFFIC_LIGHT_UPDATE': {
        const light = message.payload as TrafficLightState;
        setTrafficLights((prev) =>
          prev.map((l) => (l.id === light.id ? { ...l, state: light.state } : l))
        );
        break;
      }

      case 'CAR_POSITION_UPDATE': {
        const carUpdate = message.payload as CarState;
        setCars((prevCars) => {
          const carExists = prevCars.some((c) => c.id === carUpdate.id);
          if (carExists) {
            // Atualiza carro existente
            return prevCars.map((c) =>
              c.id === carUpdate.id ? { ...c, ...carUpdate } : c
            );
          } else {
            // Adiciona novo carro
            return [...prevCars, carUpdate];
          }
        });
        break;
      }

      // NOVO: Processa a remoção do carro
      case 'CAR_REMOVED': {
        const { id } = message.payload;
        addLog(`Evento recebido: CAR_REMOVED (id: ${id})`);
        setCars((prevCars) => prevCars.filter((c) => c.id !== id));
        break;
      }

      case 'PARDAL_TRIGGERED': {
        addLog(`Evento recebido: PARDAL_TRIGGERED (id: ${message.payload.id})`);
        // Ativa o "flash"
        setPardal((prev) => ({ ...prev, triggered: true }));
        // Desativa o "flash" após 100ms
        setTimeout(() => {
          setPardal((prev) => ({ ...prev, triggered: false }));
        }, 100);
        break;
      }
      
      default:
        addLog(`Tipo de evento desconhecido: ${message.type}`);
    }
  }, [addLog]); // Depende apenas de addLog (que também é memorizado)

  // --- Conexão WebSocket ---
  
  // Escuta o tópico /topic/agents, conforme definido no EventController
  const { isConnected } = useWebSocket('/topic/agents', handleMessage, addLog);

  // --- Envio de Comandos ---

  // Envia comandos para /api/command/{action}, conforme EventController
  const sendCommand = async (action: string, body: unknown) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/command/${action}`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: body ? JSON.stringify(body) : null,
        }
      );

      if (!response.ok) {
        throw new Error(`Falha ao enviar comando: ${response.statusText}`);
      }
      addLog(`Comando enviado: ${action}`);
    } catch (error) {
      addLog(`Erro no comando: ${String(error)}`);
    }
  };

  // --- Renderização ---

  return (
    <div className="app-container">
      <ControlPanel
        isConnected={isConnected}
        logs={logs}
        onCommand={sendCommand}
      />
      <SimulationMap
        trafficLights={trafficLights}
        cars={cars}
        pardal={pardal}
      />
    </div>
  );
}

export default App;

