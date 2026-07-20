import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

import { UsersService } from '../users.service';
import {
  EDUCATION_LEVEL_LABELS,
  PROFILE_LABELS,
  UserResponse
} from '../../../core/models/api.models';

@Component({
  selector: 'app-user-detail',
  imports: [CommonModule, RouterLink],
  templateUrl: './user-detail.component.html',
  styleUrl: './user-detail.component.scss'
})
export class UserDetailComponent {
  private readonly usersService = inject(UsersService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly user = signal<UserResponse | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly actionError = signal<string | null>(null);

  readonly educationLabels = EDUCATION_LEVEL_LABELS;
  readonly profileLabels = PROFILE_LABELS;

  constructor() {
    const id = this.route.snapshot.paramMap.get('userId');
    if (!id) {
      this.loading.set(false);
      this.error.set('Usuário não encontrado.');
      return;
    }

    this.usersService.getById(id).subscribe({
      next: (user) => {
        this.user.set(user);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Não foi possível carregar o usuário.');
        this.loading.set(false);
      }
    });
  }

  disableUser(): void {
    const current = this.user();
    if (!current) {
      return;
    }

    this.actionError.set(null);
    this.usersService.disable(current.id).subscribe({
      next: () => this.reloadUser(current.id),
      error: () => this.actionError.set('Não foi possível desabilitar o usuário.')
    });
  }

  enableUser(): void {
    const current = this.user();
    if (!current) {
      return;
    }

    this.actionError.set(null);
    this.usersService.enable(current.id).subscribe({
      next: () => this.reloadUser(current.id),
      error: () => this.actionError.set('Não foi possível reabilitar o usuário.')
    });
  }

  private reloadUser(userId: string): void {
    this.usersService.getById(userId).subscribe({
      next: (user) => this.user.set(user)
    });
  }
}
