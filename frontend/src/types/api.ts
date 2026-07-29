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