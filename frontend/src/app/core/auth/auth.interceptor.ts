import { HttpErrorResponse, HttpHandlerFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, from, switchMap, throwError } from 'rxjs';
import Keycloak from 'keycloak-js';

import { shouldAttachBearerToken } from '../api/api.constants';
import { AuthErrorHandler } from './auth-error.handler';

export function authInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn) {
  const keycloak = inject(Keycloak);
  const authErrorHandler = inject(AuthErrorHandler);

  if (!shouldAttachBearerToken(req.url)) {
    return next(req).pipe(catchError((error) => handleAuthHttpError(error, authErrorHandler)));
  }

  if (!keycloak.authenticated) {
    return next(req).pipe(catchError((error) => handleAuthHttpError(error, authErrorHandler)));
  }

  return from(keycloak.updateToken(30)).pipe(
    switchMap(() => {
      const token = keycloak.token;
      if (!token) {
        return next(req);
      }

      return next(
        req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`
          }
        })
      );
    }),
    catchError((error) => handleAuthHttpError(error, authErrorHandler))
  );
}

function handleAuthHttpError(error: unknown, authErrorHandler: AuthErrorHandler) {
  if (error instanceof HttpErrorResponse) {
    authErrorHandler.handleHttpError(error);
  }

  return throwError(() => error);
}
