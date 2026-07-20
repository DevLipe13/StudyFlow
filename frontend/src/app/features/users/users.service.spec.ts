import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { UsersService } from './users.service';
import { API_V1_BASE } from '../../core/api/api.constants';

describe('UsersService', () => {
  let service: UsersService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UsersService]
    });

    service = TestBed.inject(UsersService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should list users', () => {
    service.list().subscribe();
    const req = httpMock.expectOne((request) => request.url === `${API_V1_BASE}/users`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should disable user', () => {
    service.disable('user-1').subscribe();
    const req = httpMock.expectOne(`${API_V1_BASE}/users/user-1/disable`);
    expect(req.request.method).toBe('POST');
    req.flush(null);
  });
});
