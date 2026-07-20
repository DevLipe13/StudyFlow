import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

import { adminGuard } from './admin.guard';
import { AuthService } from './auth.service';

describe('adminGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: AuthService,
          useValue: {
            ensureUserLoaded: () => Promise.resolve(),
            isAdmin: () => false
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

  it('should redirect non-admin users to dashboard', async () => {
    const router = TestBed.inject(Router);
    const result = await TestBed.runInInjectionContext(() =>
      adminGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    expect(router.parseUrl).toHaveBeenCalledWith('/dashboard');
    expect(result).toEqual({} as UrlTree);
  });
});
