import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import Keycloak from 'keycloak-js';

import { UserBootstrapService } from './user-bootstrap.service';
import { API_V1_BASE } from '../api/api.constants';
import { UserResponse } from '../models/api.models';

describe('UserBootstrapService', () => {
  let service: UserBootstrapService;
  let httpMock: HttpTestingController;

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
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        UserBootstrapService,
        {
          provide: Keycloak,
          useValue: {
            tokenParsed: {
              name: 'Ana',
              cpf: '123',
              birthDate: '2000-01-01',
              educationLevel: 'medio'
            }
          }
        }
      ]
    });

    service = TestBed.inject(UserBootstrapService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should bootstrap user from token claims', async () => {
    const promise = service.bootstrapFromToken();
    const req = httpMock.expectOne(`${API_V1_BASE}/users/me/bootstrap`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.name).toBe('Ana');
    req.flush(user);

    await expectAsync(promise).toBeResolvedTo(user);
  });
});
