import { apiRequest } from './client';
import type { LoginResponse, UserSummary } from '../types/api';

export type SignupInput = {
  email: string;
  password: string;
  name: string;
};

export type LoginInput = {
  email: string;
  password: string;
};

export function signup(input: SignupInput) {
  return apiRequest<UserSummary>('/auth/signup', {
    method: 'POST',
    body: input,
  });
}

export function login(input: LoginInput) {
  return apiRequest<LoginResponse>('/auth/login', {
    method: 'POST',
    body: input,
  });
}

export function fetchMe(token: string) {
  return apiRequest<UserSummary>('/auth/me', { token });
}

export function logout(token: string | null) {
  return apiRequest<void>('/auth/logout', {
    method: 'POST',
    token,
  });
}