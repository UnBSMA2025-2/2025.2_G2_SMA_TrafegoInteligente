// frontend/src/services/mapConfig.js

/*
 * 1. DEFINIR OS ESTILOS (AGORA COM CORES, NÃO IMAGENS!)
 * Baseado no visual do 'SmartTraffic' (image_55d221.png)
 */
export const CELL_STYLES = {
  // O 'empty' é o fundo verde-escuro do grid
  'empty': { backgroundColor: '#1a471a' }, // Verde bem escuro
  
  // As ruas são cinza-escuro
  'road-ns': { backgroundColor: '#444' }, // Rua Norte-Sul
  'road-ew': { backgroundColor: '#444' }, // Rua Leste-Oeste
  'intersection': { backgroundColor: '#444' },
  
  // Spawns e Sensores parecem ruas normais
  'spawn': { backgroundColor: '#444' },
  'exit': { backgroundColor: '#444' },
  'sensor': { backgroundColor: '#444' },
};

/*
 * 2. LAYOUT DO MAPA
 * Um layout simples de "cruz" 10x10 como na imagem-objetivo.
 */
const mapLayout = [
  // 0       1       2       3       4       5       6       7       8       9
  ['empty', 'empty', 'empty', 'empty', 'road-ns', 'road-ns', 'empty', 'empty', 'empty', 'empty'], // y=0
  ['empty', 'empty', 'empty', 'empty', 'road-ns', 'road-ns', 'empty', 'empty', 'empty', 'empty'], // y=1
  ['empty', 'empty', 'empty', 'empty', 'road-ns', 'road-ns', 'empty', 'empty', 'empty', 'empty'], // y=2
  ['spawn', 'road-ew', 'road-ew', 'road-ew', 'intersection', 'intersection', 'road-ew', 'road-ew', 'road-ew', 'exit'],  // y=3
  ['spawn', 'road-ew', 'road-ew', 'road-ew', 'intersection', 'intersection', 'road-ew', 'road-ew', 'road-ew', 'exit'],  // y=4
  ['empty', 'empty', 'empty', 'empty', 'road-ns', 'road-ns', 'empty', 'empty', 'empty', 'empty'], // y=5
  ['empty', 'empty', 'empty', 'empty', 'road-ns', 'road-ns', 'empty', 'empty', 'empty', 'empty'], // y=6
  ['empty', 'empty', 'empty', 'empty', 'road-ns', 'road-ns', 'empty', 'empty', 'empty', 'empty'], // y=7
  ['empty', 'empty', 'empty', 'empty', 'road-ns', 'road-ns', 'empty', 'empty', 'empty', 'empty'], // y=8
  ['empty', 'empty', 'empty', 'empty', 'road-ns', 'road-ns', 'empty', 'empty', 'empty', 'empty'], // y=9
];

/*
 * 3. CONFIGURAÇÃO FINAL DO GRID
 */
export const gridConfig = {
  width: mapLayout[0].length,
  height: mapLayout.length,
  layout: mapLayout,
  
  agents: {
    cars: [
      // Carro vindo da esquerda
      { id: "CarMock1", x: 0, y: 3, direction: "LESTE" },
    ],
    trafficLights: [
      // Semáforos no cruzamento
      { id: "TL-Norte", x: 4, y: 2, state: "VERDE" }, // Semáforo de cima
      { id: "TL-Sul", x: 5, y: 5, state: "VERMELHO" }, // Semáforo de baixo
    ],
  },
};