import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_V1_BASE } from '../../core/api/api.constants';
import {
  LoginChangeCompleteRequest,
  LoginChangeCreateRequest,
  LoginChangeResponse
} from '../../core/models/api.models';

@Injectable({ providedIn: 'root' })
export class LoginChangeService {
  private readonly http = inject(HttpClient);

  list(): Observable<LoginChangeResponse[]> {
    return this.http.get<LoginChangeResponse[]>(`${API_V1_BASE}/login-change-requests`);
  }

  create(payload: LoginChangeCreateRequest): Observable<LoginChangeResponse> {
    return this.http.post<LoginChangeResponse>(`${API_V1_BASE}/login-change-requests`, payload);
  }

  approve(requestId: string): Observable<LoginChangeResponse> {
    return this.http.post<LoginChangeResponse>(
      `${API_V1_BASE}/login-change-requests/${requestId}/approve`,
      null
    );
  }

  reject(requestId: string, rejectionReason?: string): Observable<LoginChangeResponse> {
    return this.http.post<LoginChangeResponse>(
      `${API_V1_BASE}/login-change-requests/${requestId}/reject`,
      rejectionReason ? { rejectionReason } : {}
    );
  }

  complete(requestId: string, payload: LoginChangeCompleteRequest): Observable<LoginChangeResponse> {
    return this.http.post<LoginChangeResponse>(
      `${API_V1_BASE}/login-change-requests/${requestId}/complete`,
      payload
    );
  }
}
