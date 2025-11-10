// src/services/api.js
import axios from "axios";

// Base da API (pode mover para .env futuramente)
const api = axios.create({
  baseURL: "http://localhost:8080/api",
  headers: { "Content-Type": "application/json" },
});

/**
 * Lista todos os pontos de spawn do sistema.
 * @returns {Promise<Array>}
 */
export const getSpawns = async () => {
  const response = await api.get("/spawns");
  return response.data;
};

/**
 * Lista todos os carros ativos.
 * @returns {Promise<Array>}
 */
export const getCars = async () => {
  const response = await api.get("/cars");
  return response.data;
};

/**
 * Adiciona N carros a partir de um spawn específico (ou aleatório).
 * @param {number} n - Quantidade de carros
 * @param {string|null} spawn - Nome do spawn (opcional)
 * @returns {Promise<string>}
 */
export const addCars = async (n, spawn = null) => {
  const body = { n, spawn };
  const response = await api.post("/cars/add", body);
  return response.data;
};

/**
 * Remove um carro pelo nome.
 * @param {string} name - Nome do carro (ex: "Car1")
 * @returns {Promise<string>}
 */
export const removeCar = async (name) => {
  const response = await api.delete(`/remove/${name}`);
  return response.data;
};

/**
 * Retorna o status geral do sistema (ativos, carros, semáforos, etc).
 * @returns {Promise<Object>}
 */
export const getSystemStatus = async () => {
  const response = await api.get("/status");
  return response.data;
};

/**
 * Encerra o sistema JADE via API.
 * @returns {Promise<string>}
 */
export const shutdownSystem = async () => {
  const response = await api.post("/shutdown");
  return response.data;
};

export default {
  getSpawns,
  getCars,
  addCars,
  removeCar,
  getSystemStatus,
  shutdownSystem,
};