import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthGuardData, createAuthGuard } from 'keycloak-angular';
import Keycloak from 'keycloak-js';

import { AuthService } from './auth.service';

export async function canActivateAuthenticated(
  _route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot,
  authData: AuthGuardData
): Promise<boolean | UrlTree> {
  const router = inject(Router);
  const authService = inject(AuthService);
  const keycloak = inject(Keycloak);

  if (!authData.authenticated) {
    await keycloak.login({ redirectUri: window.location.origin + state.url });
    return false;
  }

  try {
    await authService.ensureUserLoaded();
  } catch {
    await authService.logout();
    return false;
  }

  const user = authService.user();
  if (!user || user.disabled) {
    await authService.logout();
    return router.parseUrl('/');
  }

  return true;
}

export const authGuard: CanActivateFn = createAuthGuard(canActivateAuthenticated);
