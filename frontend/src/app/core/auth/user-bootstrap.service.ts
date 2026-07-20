import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import Keycloak from 'keycloak-js';

import { API_V1_BASE } from '../api/api.constants';
import { UserBootstrapRequest, UserResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class UserBootstrapService {
  private readonly http = inject(HttpClient);
  private readonly keycloak = inject(Keycloak);

  async bootstrap(payload: UserBootstrapRequest): Promise<UserResponse> {
    return firstValueFrom(
      this.http.post<UserResponse>(`${API_V1_BASE}/users/me/bootstrap`, payload)
    );
  }

  async bootstrapFromToken(): Promise<UserResponse> {
    const tokenParsed = this.keycloak.tokenParsed as Record<string, unknown> | undefined;

    const payload: UserBootstrapRequest = {
      name: this.readString(tokenParsed, 'name') ?? this.readString(tokenParsed, 'preferred_username') ?? '',
      cpf: this.readString(tokenParsed, 'cpf') ?? '',
      birthDate: this.readString(tokenParsed, 'birthDate') ?? '',
      educationLevel: this.readEducationLevel(tokenParsed)
    };

    return this.bootstrap(payload);
  }

  private readString(source: Record<string, unknown> | undefined, key: string): string | undefined {
    const value = source?.[key];
    return typeof value === 'string' ? value : undefined;
  }

  private readEducationLevel(source: Record<string, unknown> | undefined): UserBootstrapRequest['educationLevel'] {
    const value = this.readString(source, 'educationLevel');
    if (
      value === 'fundamental' ||
      value === 'medio' ||
      value === 'graduacao' ||
      value === 'posGraduacao' ||
      value === 'outro'
    ) {
      return value;
    }

    return 'outro';
  }
}
