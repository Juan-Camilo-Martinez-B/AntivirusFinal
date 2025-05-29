import axios from 'axios';

const BASE_URL = 'http://localhost:8090/scan';

export const iniciarEscaneo = async (tipo: string) => {
  try {
    const response = await axios.get(`${BASE_URL}/scanSystem?scanType=${tipo}`);
    console.log('🔍 Respuesta completa del backend:', response.data); // ✅ Verifica los datos que llegan
    return response.data;
  } catch (error) {
    console.error("⚠ Error al iniciar escaneo:", error);
    return null;
  }
};
