export type ApiResponse<T> = {
  success: boolean;
  data: T | null;
  message: string | null;
  code: string | null;
};

export type UserRole = 'USER' | 'ADMIN';

export type UserSummary = {
  id: number | null;
  email: string;
  name: string;
  role: UserRole;
};

export type LoginResponse = {
  accessToken: string;
  tokenType: 'Bearer';
  user: UserSummary;
};

export type Station = {
  id: number | null;
  name: string;
  code: string;
  city: string;
};

export type StationPayload = {
  name: string;
  code: string;
  city: string;
};

export type Route = {
  id: number | null;
  name: string;
  originStationId: number | null;
  originStationName: string;
  originStationCode: string;
  destinationStationId: number | null;
  destinationStationName: string;
  destinationStationCode: string;
};

export type RoutePayload = {
  name: string;
  originStationId: number;
  destinationStationId: number;
};

export type Train = {
  id: number | null;
  trainNo: string;
  trainType: string;
  name: string;
};

export type TrainPayload = {
  trainNo: string;
  trainType: string;
  name: string;
};

export type Car = {
  id: number | null;
  trainId: number | null;
  trainNo: string;
  trainType: string;
  carNo: number;
  seatCount: number;
};

export type CarPayload = {
  carNo: number;
  seatCount: number;
};

export type SeatType = 'STANDARD' | 'PRIORITY' | 'WINDOW' | 'AISLE';

export type Seat = {
  id: number | null;
  carId: number | null;
  trainId: number | null;
  carNo: number;
  seatNo: string;
  seatType: SeatType;
};

export type SeatPayload = {
  seatNo: string;
  seatType: SeatType;
};

export type TrainScheduleStatus = 'SCHEDULED' | 'DELAYED' | 'CANCELED' | 'COMPLETED';

export type TrainSchedule = {
  id: number | null;
  trainId: number | null;
  trainNo: string;
  trainType: string;
  routeId: number | null;
  routeName: string;
  originStationId: number | null;
  originStationName: string;
  originStationCode: string;
  destinationStationId: number | null;
  destinationStationName: string;
  destinationStationCode: string;
  operationDate: string;
  departureTime: string;
  arrivalTime: string;
  status: TrainScheduleStatus;
};

export type TrainScheduleSearchResult = {
  scheduleId: number | null;
  trainNo: string;
  trainType: string;
  routeName: string;
  origin: string;
  destination: string;
  departureTime: string;
  arrivalTime: string;
  status: TrainScheduleStatus;
};

export type TrainSchedulePayload = {
  trainId: number;
  routeId: number;
  operationDate: string;
  departureTime: string;
  arrivalTime: string;
};
export type ScheduleSeatStatus = 'AVAILABLE' | 'HELD' | 'RESERVED' | 'BLOCKED';

export type ScheduleSeat = {
  scheduleSeatId: number | null;
  seatId: number | null;
  seatNo: string;
  seatType: SeatType;
  status: ScheduleSeatStatus;
  holdExpiresAt: string | null;
};

export type CarSeatMap = {
  carId: number | null;
  carNo: number;
  seats: ScheduleSeat[];
};

export type ScheduleSeatMap = {
  scheduleId: number | null;
  cars: CarSeatMap[];
};
export type ReservationHoldPayload = {
  scheduleId: number;
  scheduleSeatIds: number[];
};

export type ReservationStatus = 'PENDING_PAYMENT' | 'CONFIRMED' | 'CANCELED' | 'EXPIRED' | 'PAYMENT_FAILED';

export type PaymentStatus = 'READY' | 'SUCCESS' | 'FAILED' | 'CANCELED' | 'EXPIRED';

export type ReservationHoldResponse = {
  reservationId: number | null;
  reservationNo: string;
  paymentId: number | null;
  paymentNo: string;
  scheduleId: number | null;
  scheduleSeatIds: number[];
  status: ReservationStatus;
  amount: number;
  holdExpiresAt: string;
};

export type PaymentResultResponse = {
  paymentId: number | null;
  paymentNo: string;
  reservationId: number | null;
  paymentStatus: PaymentStatus;
  reservationStatus: ReservationStatus;
  amount: number;
  processedAt: string;
};