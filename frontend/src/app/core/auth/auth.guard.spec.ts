import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthGuardData } from 'keycloak-angular';
import Keycloak from 'keycloak-js';

import { AuthService } from './auth.service';
import { canActivateAuthenticated } from './auth.guard';
import { UserResponse } from '../models/api.models';

describe('canActivateAuthenticated', () => {
  const user: UserResponse = {
    id: '1',
    keycloakId: 'kc-1',
    name: 'Ana',
    email: 'ana@example.com',
    cpf: '39053344705',
    birthDate: '2000-01-01',
    educationLevel: 'medio',
    profile: 'estudante',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
    disabled: false
  };

  it('should redirect to keycloak login when unauthenticated', async () => {
    const keycloak = jasmine.createSpyObj<Keycloak>('Keycloak', ['login']);
    keycloak.login.and.returnValue(Promise.resolve());

    TestBed.configureTestingModule({
      providers: [
        { provide: Keycloak, useValue: keycloak },
        {
          provide: AuthService,
          useValue: {
            ensureUserLoaded: jasmine.createSpy('ensureUserLoaded'),
            logout: jasmine.createSpy('logout'),
            user: signal(null)
          }
        },
        { provide: Router, useValue: { parseUrl: (url: string) => url as unknown as UrlTree } }
      ]
    });

    await TestBed.runInInjectionContext(async () => {
      const result = await canActivateAuthenticated(
        {} as ActivatedRouteSnapshot,
        { url: '/dashboard' } as RouterStateSnapshot,
        { authenticated: false } as AuthGuardData
      );
      expect(result).toBeFalse();
    });

    expect(keycloak.login).toHaveBeenCalled();
  });

  it('should deny access when user is disabled', async () => {
    const logout = jasmine.createSpy('logout').and.returnValue(Promise.resolve());
    const parseUrl = jasmine.createSpy('parseUrl').and.returnValue('/' as unknown as UrlTree);

    TestBed.configureTestingModule({
      providers: [
        { provide: Keycloak, useValue: jasmine.createSpyObj<Keycloak>('Keycloak', ['login']) },
        {
          provide: AuthService,
          useValue: {
            ensureUserLoaded: jasmine.createSpy('ensureUserLoaded').and.returnValue(Promise.resolve()),
            logout,
            user: signal({ ...user, disabled: true })
          }
        },
        { provide: Router, useValue: { parseUrl } }
      ]
    });

    await TestBed.runInInjectionContext(async () => {
      const result = await canActivateAuthenticated(
        {} as ActivatedRouteSnapshot,
        { url: '/dashboard' } as RouterStateSnapshot,
        { authenticated: true } as AuthGuardData
      );
      expect(result).toBe('/' as unknown as UrlTree);
    });

    expect(logout).toHaveBeenCalled();
  });

  it('should allow access when authenticated with active user', async () => {
    TestBed.configureTestingModule({
      providers: [
        { provide: Keycloak, useValue: jasmine.createSpyObj<Keycloak>('Keycloak', ['login']) },
        {
          provide: AuthService,
          useValue: {
            ensureUserLoaded: jasmine.createSpy('ensureUserLoaded').and.returnValue(Promise.resolve()),
            logout: jasmine.createSpy('logout'),
            user: signal(user)
          }
        },
        { provide: Router, useValue: { parseUrl: (url: string) => url as unknown as UrlTree } }
      ]
    });

    await TestBed.runInInjectionContext(async () => {
      const result = await canActivateAuthenticated(
        {} as ActivatedRouteSnapshot,
        { url: '/dashboard' } as RouterStateSnapshot,
        { authenticated: true } as AuthGuardData
      );
      expect(result).toBeTrue();
    });
  });
});
