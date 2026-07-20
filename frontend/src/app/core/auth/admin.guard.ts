import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';

export const adminGuard: CanActivateFn = async () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  await authService.ensureUserLoaded();

  if (!authService.isAdmin()) {
    return router.parseUrl('/dashboard');
  }

  return true;
};
