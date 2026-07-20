import { ApplicationConfig, ErrorHandler, provideZoneChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { AuthErrorHandler } from './core/auth/auth-error.handler';
import { authInterceptor } from './core/auth/auth.interceptor';
import { provideStudyFlowKeycloak } from './core/auth/keycloak.config';

export const appConfig: ApplicationConfig = {
  providers: [
    provideStudyFlowKeycloak(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    { provide: ErrorHandler, useClass: AuthErrorHandler }
  ]
};
