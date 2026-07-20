import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { LoginChangeService } from '../login-change.service';
import {
  LOGIN_CHANGE_STATUS_LABELS,
  LOGIN_CHANGE_TYPE_LABELS,
  LoginChangeResponse
} from '../../../core/models/api.models';

@Component({
  selector: 'app-admin-login-change',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-login-change.component.html',
  styleUrl: './admin-login-change.component.scss'
})
export class AdminLoginChangeComponent {
  private readonly loginChangeService = inject(LoginChangeService);
  private readonly fb = inject(FormBuilder);

  readonly requests = signal<LoginChangeResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly statusLabels = LOGIN_CHANGE_STATUS_LABELS;
  readonly typeLabels = LOGIN_CHANGE_TYPE_LABELS;

  readonly rejectForm = this.fb.nonNullable.group({
    requestId: [''],
    rejectionReason: ['']
  });

  constructor() {
    this.loadRequests();
  }

  loadRequests(): void {
    this.loading.set(true);
    this.error.set(null);

    this.loginChangeService.list().subscribe({
      next: (requests) => {
        this.requests.set(requests.filter((request) => request.status === 'pending'));
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Não foi possível carregar as solicitações.');
        this.loading.set(false);
      }
    });
  }

  approve(requestId: string): void {
    this.error.set(null);
    this.loginChangeService.approve(requestId).subscribe({
      next: () => this.loadRequests(),
      error: () => this.error.set('Não foi possível aprovar a solicitação.')
    });
  }

  reject(requestId: string): void {
    this.error.set(null);
    const reason = this.rejectForm.controls.rejectionReason.value || undefined;

    this.loginChangeService.reject(requestId, reason).subscribe({
      next: () => {
        this.rejectForm.reset({ requestId: '', rejectionReason: '' });
        this.loadRequests();
      },
      error: () => this.error.set('Não foi possível rejeitar a solicitação.')
    });
  }
}
