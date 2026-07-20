import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

import { AuthService } from './auth.service';
import { studentGuard } from './student.guard';

describe('studentGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: AuthService,
          useValue: {
            ensureUserLoaded: () => Promise.resolve(),
            isStudent: () => false
          }
        },
        {
          provide: Router,
          useValue: {
            parseUrl: jasmine.createSpy('parseUrl').and.returnValue({} as UrlTree)
          }
        }
      ]
    });
  });

  it('should redirect non-student users to dashboard', async () => {
    const router = TestBed.inject(Router);
    const result = await TestBed.runInInjectionContext(() =>
      studentGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    expect(router.parseUrl).toHaveBeenCalledWith('/dashboard');
    expect(result).toEqual({} as UrlTree);
  });
});
