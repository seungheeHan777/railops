import { apiRequest } from './client';
import type { Station, StationPayload } from '../types/api';

export function fetchStations() {
  return apiRequest<Station[]>('/stations');
}

export function searchStations(keyword: string) {
  const query = new URLSearchParams({ keyword }).toString();
  return apiRequest<Station[]>(`/stations/search?${query}`);
}

export function fetchAdminStations(token: string) {
  return apiRequest<Station[]>('/admin/stations', { token });
}

export function createStation(token: string, payload: StationPayload) {
  return apiRequest<Station>('/admin/stations', {
    method: 'POST',
    token,
    body: payload,
  });
}

export function updateStation(token: string, stationId: number, payload: StationPayload) {
  return apiRequest<Station>(`/admin/stations/${stationId}`, {
    method: 'PATCH',
    token,
    body: payload,
  });
}

export function deleteStation(token: string, stationId: number) {
  return apiRequest<void>(`/admin/stations/${stationId}`, {
    method: 'DELETE',
    token,
  });
}