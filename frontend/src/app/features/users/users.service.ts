import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_V1_BASE } from '../../core/api/api.constants';
import {
  AdminCreateUserRequest,
  AdminUpdateUserRequest,
  UserResponse
} from '../../core/models/api.models';

@Injectable({ providedIn: 'root' })
export class UsersService {
  private readonly http = inject(HttpClient);

  list(includeDisabled = false): Observable<UserResponse[]> {
    const params = new HttpParams().set('includeDisabled', String(includeDisabled));
    return this.http.get<UserResponse[]>(`${API_V1_BASE}/users`, { params });
  }

  getById(userId: string): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${API_V1_BASE}/users/${userId}`);
  }

  create(payload: AdminCreateUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${API_V1_BASE}/users`, payload);
  }

  update(userId: string, payload: AdminUpdateUserRequest): Observable<UserResponse> {
    return this.http.patch<UserResponse>(`${API_V1_BASE}/users/${userId}`, payload);
  }

  disable(userId: string): Observable<void> {
    return this.http.post<void>(`${API_V1_BASE}/users/${userId}/disable`, null);
  }

  enable(userId: string): Observable<void> {
    return this.http.post<void>(`${API_V1_BASE}/users/${userId}/enable`, null);
  }
}
