// frontend/src/components/Grid/AgentLayer.jsx
import React from 'react';
import { gridConfig } from '../../services/mapConfig';
import CarIcon from '../Agents/CarIcon';
import TrafficLightIcon from '../Agents/TrafficLightIcon';

const GRID_WIDTH = gridConfig.width;
const GRID_HEIGHT = gridConfig.height;

const AgentLayer = () => {
  const { cars, trafficLights } = gridConfig.agents;

  return (
    <div className="agent-layer">
      {/* Renderiza os Carros (centralizados por padrão) */}
      {cars.map(car => {
        const style = {
          left: `${(car.x / GRID_WIDTH) * 100}%`,
          top: `${(car.y / GRID_HEIGHT) * 100}%`,
          width: `${100 / GRID_WIDTH}%`,
          height: `${100 / GRID_HEIGHT}%`,
        };
        return (
          <div key={car.id} className="agent-wrapper" style={style}>
            <CarIcon direction={car.direction} />
          </div>
        );
      })}

      {/* Renderiza os Semáforos (deslocados) */}
      {trafficLights.map(light => {
        const style = {
          left: `${(light.x / GRID_WIDTH) * 100}%`,
          top: `${(light.y / GRID_HEIGHT) * 100}%`,
          width: `${100 / GRID_WIDTH}%`,
          height: `${100 / GRID_HEIGHT}%`,
          
          /* * A MÁGICA:
           * Desloca o ícone para a direita da célula.
           * Isso os coloca "ao lado" da estrada, não no meio.
           */
          justifyContent: 'flex-end',
        };
        return (
          <div key={light.id} className="agent-wrapper" style={style}>
            <TrafficLightIcon state={light.state} />
          </div>
        );
      })}
    </div>
  );
};

export default AgentLayer;