import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';

import { AuthErrorHandler } from './auth-error.handler';
import { AuthService } from './auth.service';

describe('AuthErrorHandler', () => {
  let handler: AuthErrorHandler;
  let logoutSpy: jasmine.Spy;

  beforeEach(() => {
    logoutSpy = jasmine.createSpy('logout');
    TestBed.configureTestingModule({
      providers: [
        AuthErrorHandler,
        { provide: AuthService, useValue: { logout: logoutSpy } }
      ]
    });
    handler = TestBed.inject(AuthErrorHandler);
  });

  it('should logout on 401', () => {
    handler.handleHttpError(new HttpErrorResponse({ status: 401, statusText: 'Unauthorized' }));
    expect(logoutSpy).toHaveBeenCalled();
  });

  it('should not logout on 403', () => {
    spyOn(console, 'warn');
    handler.handleHttpError(new HttpErrorResponse({ status: 403, statusText: 'Forbidden' }));
    expect(logoutSpy).not.toHaveBeenCalled();
    expect(console.warn).toHaveBeenCalled();
  });
});
