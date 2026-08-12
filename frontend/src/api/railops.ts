import { apiRequest } from './client';
import type {
  Car,
  CarPayload,
  Route,
  RoutePayload,
  Seat,
  SeatPayload,
  ScheduleSeat,
  ScheduleSeatMap,
  ReservationHoldPayload,
  ReservationHoldResponse,
  PaymentResultResponse,
  Train,
  TrainPayload,
  TrainSchedule,
  TrainSchedulePayload,
  TrainScheduleSearchResult,
  TrainScheduleStatus,
} from '../types/api';

export function fetchAdminRoutes(token: string) {
  return apiRequest<Route[]>('/admin/routes', { token });
}

export function createRoute(token: string, payload: RoutePayload) {
  return apiRequest<Route>('/admin/routes', { method: 'POST', token, body: payload });
}

export function updateRoute(token: string, routeId: number, payload: RoutePayload) {
  return apiRequest<Route>(`/admin/routes/${routeId}`, { method: 'PATCH', token, body: payload });
}

export function deleteRoute(token: string, routeId: number) {
  return apiRequest<void>(`/admin/routes/${routeId}`, { method: 'DELETE', token });
}

export function fetchAdminTrains(token: string) {
  return apiRequest<Train[]>('/admin/trains', { token });
}

export function createTrain(token: string, payload: TrainPayload) {
  return apiRequest<Train>('/admin/trains', { method: 'POST', token, body: payload });
}

export function updateTrain(token: string, trainId: number, payload: TrainPayload) {
  return apiRequest<Train>(`/admin/trains/${trainId}`, { method: 'PATCH', token, body: payload });
}

export function deleteTrain(token: string, trainId: number) {
  return apiRequest<void>(`/admin/trains/${trainId}`, { method: 'DELETE', token });
}

export function fetchCarsByTrain(token: string, trainId: number) {
  return apiRequest<Car[]>(`/admin/trains/${trainId}/cars`, { token });
}

export function createCar(token: string, trainId: number, payload: CarPayload) {
  return apiRequest<Car>(`/admin/trains/${trainId}/cars`, { method: 'POST', token, body: payload });
}

export function updateCar(token: string, carId: number, payload: CarPayload) {
  return apiRequest<Car>(`/admin/cars/${carId}`, { method: 'PATCH', token, body: payload });
}

export function deleteCar(token: string, carId: number) {
  return apiRequest<void>(`/admin/cars/${carId}`, { method: 'DELETE', token });
}

export function fetchSeatsByCar(token: string, carId: number) {
  return apiRequest<Seat[]>(`/admin/cars/${carId}/seats`, { token });
}

export function createSeat(token: string, carId: number, payload: SeatPayload) {
  return apiRequest<Seat>(`/admin/cars/${carId}/seats`, { method: 'POST', token, body: payload });
}

export function updateSeat(token: string, seatId: number, payload: SeatPayload) {
  return apiRequest<Seat>(`/admin/seats/${seatId}`, { method: 'PATCH', token, body: payload });
}

export function deleteSeat(token: string, seatId: number) {
  return apiRequest<void>(`/admin/seats/${seatId}`, { method: 'DELETE', token });
}

export function searchTrainSchedules(from: string, to: string, date: string) {
  const query = new URLSearchParams({ from, to, date }).toString();
  return apiRequest<TrainScheduleSearchResult[]>(`/train-schedules?${query}`);
}

export function fetchTrainSchedule(scheduleId: number) {
  return apiRequest<TrainSchedule>(`/train-schedules/${scheduleId}`);
}

export function fetchAdminTrainSchedules(token: string) {
  return apiRequest<TrainSchedule[]>('/admin/train-schedules', { token });
}

export function createTrainSchedule(token: string, payload: TrainSchedulePayload) {
  return apiRequest<TrainSchedule>('/admin/train-schedules', { method: 'POST', token, body: payload });
}

export function updateTrainSchedule(token: string, scheduleId: number, payload: TrainSchedulePayload) {
  return apiRequest<TrainSchedule>(`/admin/train-schedules/${scheduleId}`, { method: 'PATCH', token, body: payload });
}

export function updateTrainScheduleStatus(token: string, scheduleId: number, status: TrainScheduleStatus) {
  return apiRequest<TrainSchedule>(`/admin/train-schedules/${scheduleId}/status`, {
    method: 'PATCH',
    token,
    body: { status },
  });
}

export function deleteTrainSchedule(token: string, scheduleId: number) {
  return apiRequest<void>(`/admin/train-schedules/${scheduleId}`, { method: 'DELETE', token });
}
export function fetchScheduleSeatMap(scheduleId: number) {
  return apiRequest<ScheduleSeatMap>(`/train-schedules/${scheduleId}/seats`);
}

export function blockScheduleSeat(token: string, scheduleSeatId: number) {
  return apiRequest<ScheduleSeat>(`/admin/schedule-seats/${scheduleSeatId}/block`, {
    method: 'PATCH',
    token,
  });
}

export function unblockScheduleSeat(token: string, scheduleSeatId: number) {
  return apiRequest<ScheduleSeat>(`/admin/schedule-seats/${scheduleSeatId}/unblock`, {
    method: 'PATCH',
    token,
  });
}
export function holdReservation(token: string, payload: ReservationHoldPayload) {
  return apiRequest<ReservationHoldResponse>('/reservations/hold', {
    method: 'POST',
    token,
    body: payload,
  });
}
export function simulatePaymentSuccess(token: string, paymentId: number) {
  return apiRequest<PaymentResultResponse>(`/payments/${paymentId}/simulate-success`, {
    method: 'POST',
    token,
  });
}

export function simulatePaymentFail(token: string, paymentId: number) {
  return apiRequest<PaymentResultResponse>(`/payments/${paymentId}/simulate-fail`, {
    method: 'POST',
    token,
  });
}

export function simulatePaymentCancel(token: string, paymentId: number) {
  return apiRequest<PaymentResultResponse>(`/payments/${paymentId}/simulate-cancel`, {
    method: 'POST',
    token,
  });
}