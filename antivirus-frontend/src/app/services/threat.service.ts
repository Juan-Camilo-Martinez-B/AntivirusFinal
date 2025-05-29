import axios from 'axios';

const BASE_URL = 'http://localhost:8090/threats';

export const obtenerAmenazas = async () => {
  try {
    const response = await axios.get(`${BASE_URL}/getThreats`);
    return response.data;
  } catch (error) {
    console.error("⚠ Error al obtener amenazas:", error);
    return null;
  }
};
