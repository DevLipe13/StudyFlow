import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';

export const studentGuard: CanActivateFn = async () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  await authService.ensureUserLoaded();

  if (!authService.isStudent()) {
    return router.parseUrl('/dashboard');
  }

  return true;
};
