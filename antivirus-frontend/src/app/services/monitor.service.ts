import axios from 'axios';

const BASE_URL = 'http://localhost:8090/monitor';

export const iniciarMonitoreo = async (ruta: string) => {
  try {
    const encodedRuta = encodeURIComponent(ruta);
    const response = await axios.post(`${BASE_URL}/startMonitoring?directoryPath=${encodedRuta}`);
    return response.data;
  } catch (error) {
    console.error("⚠ Error al iniciar monitoreo:", error);
    return null;
  }
};

export const detenerMonitoreo = async () => {
  try {
    const response = await axios.post(`${BASE_URL}/stopMonitoring`);
    return response.data;
  } catch (error) {
    console.error("⚠ Error al detener monitoreo:", error);
    return null;
  }
};

export const obtenerCambiosMonitoreados = async () => {
  try {
    const response = await axios.get(`${BASE_URL}/getMonitoredChanges`);
    return response.data;
  } catch (error) {
    console.error("⚠ Error al obtener cambios monitoreados:", error);
    return null;
  }
};
