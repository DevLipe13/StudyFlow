import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { LoginChangeService } from '../login-change.service';
import {
  LOGIN_CHANGE_STATUS_LABELS,
  LOGIN_CHANGE_TYPE_LABELS,
  LoginChangeResponse,
  LoginChangeType
} from '../../../core/models/api.models';

@Component({
  selector: 'app-student-login-change',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './student-login-change.component.html',
  styleUrl: './student-login-change.component.scss'
})
export class StudentLoginChangeComponent {
  private readonly loginChangeService = inject(LoginChangeService);
  private readonly fb = inject(FormBuilder);

  readonly requests = signal<LoginChangeResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);

  readonly approvedRequests = computed(() =>
    this.requests().filter((request) => request.status === 'approved')
  );

  readonly changeTypes: LoginChangeType[] = ['email', 'password', 'emailAndPassword'];
  readonly statusLabels = LOGIN_CHANGE_STATUS_LABELS;
  readonly typeLabels = LOGIN_CHANGE_TYPE_LABELS;

  readonly requestForm = this.fb.nonNullable.group({
    changeType: ['email' as LoginChangeType, Validators.required],
    proposedEmail: ['']
  });

  readonly completeForm = this.fb.nonNullable.group({
    requestId: ['', Validators.required],
    cpf: ['', Validators.required],
    newEmail: [''],
    newPassword: ['']
  });

  constructor() {
    this.loadRequests();
  }

  loadRequests(): void {
    this.loading.set(true);
    this.error.set(null);

    this.loginChangeService.list().subscribe({
      next: (requests) => {
        this.requests.set(requests);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Não foi possível carregar suas solicitações.');
        this.loading.set(false);
      }
    });
  }

  selectApproved(request: LoginChangeResponse): void {
    if (request.status !== 'approved') {
      return;
    }
    this.completeForm.patchValue({
      requestId: request.id,
      newEmail: request.proposedEmail || ''
    });
  }

  submitRequest(): void {
    if (this.requestForm.invalid) {
      this.requestForm.markAllAsTouched();
      return;
    }

    this.error.set(null);
    this.success.set(null);

    const { changeType, proposedEmail } = this.requestForm.getRawValue();
    this.loginChangeService
      .create({
        changeType,
        proposedEmail: proposedEmail || undefined
      })
      .subscribe({
        next: () => {
          this.success.set('Solicitação enviada com sucesso.');
          this.requestForm.reset({ changeType: 'email', proposedEmail: '' });
          this.loadRequests();
        },
        error: () => this.error.set('Não foi possível enviar a solicitação.')
      });
  }

  submitComplete(): void {
    if (this.completeForm.invalid) {
      this.completeForm.markAllAsTouched();
      return;
    }

    this.error.set(null);
    this.success.set(null);

    const { requestId, cpf, newEmail, newPassword } = this.completeForm.getRawValue();
    this.loginChangeService
      .complete(requestId, {
        cpf,
        newEmail: newEmail || undefined,
        newPassword: newPassword || undefined
      })
      .subscribe({
        next: () => {
          this.success.set('Alteração concluída com sucesso.');
          this.completeForm.reset({ requestId: '', cpf: '', newEmail: '', newPassword: '' });
          this.loadRequests();
        },
        error: () => this.error.set('Não foi possível concluir a solicitação.')
      });
  }
}
