import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { LoginChangeService } from './login-change.service';
import { API_V1_BASE } from '../../core/api/api.constants';

describe('LoginChangeService', () => {
  let service: LoginChangeService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LoginChangeService]
    });

    service = TestBed.inject(LoginChangeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create login change request', () => {
    service.create({ changeType: 'email', proposedEmail: 'novo@example.com' }).subscribe();
    const req = httpMock.expectOne(`${API_V1_BASE}/login-change-requests`);
    expect(req.request.method).toBe('POST');
    req.flush({
      id: '1',
      requesterUserId: 'u1',
      changeType: 'email',
      status: 'pending',
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z'
    });
  });

  it('should approve request', () => {
    service.approve('req-1').subscribe();
    const req = httpMock.expectOne(`${API_V1_BASE}/login-change-requests/req-1/approve`);
    expect(req.request.method).toBe('POST');
    req.flush({
      id: 'req-1',
      requesterUserId: 'u1',
      changeType: 'email',
      status: 'approved',
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z'
    });
  });
});
