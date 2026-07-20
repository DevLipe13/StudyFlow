import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import Keycloak from 'keycloak-js';

import { AuthService } from './auth.service';
import { UserBootstrapService } from './user-bootstrap.service';
import { API_V1_BASE } from '../api/api.constants';
import { UserResponse } from '../models/api.models';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let keycloak: jasmine.SpyObj<Keycloak>;

  const user: UserResponse = {
    id: '1',
    keycloakId: 'kc-1',
    name: 'Ana',
    email: 'ana@example.com',
    cpf: '123',
    birthDate: '2000-01-01',
    educationLevel: 'medio',
    profile: 'estudante',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
    disabled: false
  };

  beforeEach(() => {
    keycloak = jasmine.createSpyObj<Keycloak>('Keycloak', ['login', 'logout', 'updateToken'], {
      authenticated: true,
      token: 'token-abc'
    });

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        UserBootstrapService,
        { provide: Keycloak, useValue: keycloak }
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should load current user', async () => {
    const promise = service.loadCurrentUser();
    const req = httpMock.expectOne(`${API_V1_BASE}/users/me`);
    expect(req.request.method).toBe('GET');
    req.flush(user);

    await promise;
    expect(service.user()).toEqual(user);
    expect(service.isStudent()).toBeTrue();
  });

  it('should login via keycloak', async () => {
    keycloak.login.and.returnValue(Promise.resolve());
    await service.login();
    expect(keycloak.login).toHaveBeenCalled();
  });

  it('should logout and clear user', async () => {
    keycloak.logout.and.returnValue(Promise.resolve());
    await service.logout();
    expect(service.user()).toBeNull();
    expect(keycloak.logout).toHaveBeenCalled();
  });

  it('should refresh token when authenticated', async () => {
    keycloak.updateToken.and.returnValue(Promise.resolve(true));
    const refreshed = await service.refreshToken();
    expect(refreshed).toBeTrue();
  });
});
