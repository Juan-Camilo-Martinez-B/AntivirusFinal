import axios from 'axios';

const BASE_URL = 'http://localhost:8090/scan';

// Función que inicia el escaneo y acepta opcionalmente una señal para cancelar
export const iniciarEscaneo = async (tipo: string, signal?: AbortSignal) => {
  try {
    const config = signal ? { signal } : {};
    const response = await axios.get(`${BASE_URL}/scanSystem?scanType=${tipo}`, config);
    console.log('🔍 Respuesta completa del backend:', response.data);
    return response.data;
  } catch (error) {
    console.error("⚠ Error al iniciar escaneo:", error);
    return null;
  }
};
