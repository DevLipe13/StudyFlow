import { HttpErrorResponse } from '@angular/common/http';
import { ErrorHandler, Injectable, inject } from '@angular/core';

import { AuthService } from './auth.service';

@Injectable()
export class AuthErrorHandler implements ErrorHandler {
  private readonly authService = inject(AuthService);

  handleError(error: unknown): void {
    if (error instanceof HttpErrorResponse) {
      this.handleHttpError(error);
      return;
    }

    console.error(error);
  }

  handleHttpError(error: HttpErrorResponse): void {
    if (error.status === 401) {
      void this.authService.logout();
      return;
    }

    if (error.status === 403) {
      console.warn('Acesso negado:', error.message);
      return;
    }

    console.error(error);
  }
}
