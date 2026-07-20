import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import Keycloak from 'keycloak-js';

import { API_V1_BASE } from '../api/api.constants';
import { UserResponse } from '../models/api.models';
import { UserBootstrapService } from './user-bootstrap.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly keycloak = inject(Keycloak);
  private readonly http = inject(HttpClient);
  private readonly userBootstrapService = inject(UserBootstrapService);

  private readonly userSignal = signal<UserResponse | null>(null);
  private loadPromise: Promise<void> | null = null;

  readonly user = this.userSignal.asReadonly();
  readonly isAdmin = computed(() => this.userSignal()?.profile === 'admin');
  readonly isStudent = computed(() => this.userSignal()?.profile === 'estudante');
  readonly isAuthenticated = computed(() => {
    const user = this.userSignal();
    return !!user && !user.disabled;
  });

  isLoggedIn(): boolean {
    return !!this.keycloak.authenticated;
  }

  async login(): Promise<void> {
    await this.keycloak.login({
      redirectUri: window.location.origin
    });
  }

  async logout(): Promise<void> {
    this.userSignal.set(null);
    this.loadPromise = null;
    await this.keycloak.logout({
      redirectUri: window.location.origin
    });
  }

  async refreshToken(minValidity = 30): Promise<boolean> {
    if (!this.keycloak.authenticated) {
      return false;
    }

    try {
      return await this.keycloak.updateToken(minValidity);
    } catch {
      return false;
    }
  }

  getToken(): string | undefined {
    return this.keycloak.token;
  }

  async ensureUserLoaded(): Promise<void> {
    if (!this.keycloak.authenticated) {
      this.userSignal.set(null);
      return;
    }

    if (this.userSignal()) {
      return;
    }

    if (!this.loadPromise) {
      this.loadPromise = this.loadCurrentUser().finally(() => {
        this.loadPromise = null;
      });
    }

    await this.loadPromise;
  }

  async loadCurrentUser(): Promise<void> {
    if (!this.keycloak.authenticated) {
      this.userSignal.set(null);
      return;
    }

    try {
      const user = await firstValueFrom(this.http.get<UserResponse>(`${API_V1_BASE}/users/me`));
      this.userSignal.set(user);
    } catch (error) {
      if (error instanceof HttpErrorResponse && error.status === 404) {
        const bootstrapped = await this.userBootstrapService.bootstrapFromToken();
        this.userSignal.set(bootstrapped);
        return;
      }

      throw error;
    }
  }

  clearUser(): void {
    this.userSignal.set(null);
    this.loadPromise = null;
  }
}
