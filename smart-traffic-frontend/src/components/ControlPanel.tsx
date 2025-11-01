import React from 'react';

interface ControlPanelProps {
  isConnected: boolean;
  logs: string[];
  onCommand: (action: string, body: unknown) => void;
}

export const ControlPanel: React.FC<ControlPanelProps> = ({
  isConnected,
  logs,
  onCommand,
}) => {
  
  /**
   * Botão "Adicionar Carro"
   * Envia um comando para o EventController (Spring).
   * O EventController envia uma mensagem ACL para o TrafficLightAgent (JADE).
   * O TrafficLightAgent (JADE) cria o novo CarAgent com uma posição aleatória.
   */
  const handleCreateCar = () => {
    // Não precisamos enviar coordenadas. O agente JADE vai gerá-las.
    onCommand('createCar', null);
  };

  /**
   * Botão "Inverter Semáforos" (Simulação)
   * Envia um comando de simulação para o EventController.
   */
  const handleToggleLight = () => {
    // Usamos 'toggleLightSim' para o comando de simulação
    onCommand('toggleLightSim', null); 
  };

  return (
    <aside className="sidebar">
      <h1>🚦 SmartTraffic</h1>
      <div
        className={`connection-status ${
          isConnected ? 'status-connected' : 'status-disconnected'
        }`}
      >
        {isConnected ? 'CONECTADO AO SERVIDOR' : 'DESCONECTADO'}
      </div>

      <div className="control-panel">
        <h2>Painel de Comandos</h2>
        
        {/* O botão "Adicionar Carro" está de volta */}
        <button onClick={handleCreateCar}>Adicionar Carro</button>
        
        <button onClick={handleToggleLight}>Inverter Semáforos (Simulação)</button>
      </div>

      <div className="log-panel">
        <h2>Logs de Eventos</h2>
        <div className="log-container">
          {logs.map((log, index) => (
            <div key={index} className="log-entry">
              {log}
            </div>
          ))}
        </div>
      </div>
    </aside>
  );
};

