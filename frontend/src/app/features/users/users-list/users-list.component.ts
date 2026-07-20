import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

import { UsersService } from '../users.service';
import { EDUCATION_LEVEL_LABELS, PROFILE_LABELS, UserResponse } from '../../../core/models/api.models';

@Component({
  selector: 'app-users-list',
  imports: [CommonModule, RouterLink],
  templateUrl: './users-list.component.html',
  styleUrl: './users-list.component.scss'
})
export class UsersListComponent {
  private readonly usersService = inject(UsersService);

  readonly users = signal<UserResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly includeDisabled = signal(false);

  readonly educationLabels = EDUCATION_LEVEL_LABELS;
  readonly profileLabels = PROFILE_LABELS;

  constructor() {
    this.loadUsers();
  }

  toggleIncludeDisabled(): void {
    this.includeDisabled.update((value) => !value);
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading.set(true);
    this.error.set(null);

    this.usersService.list(this.includeDisabled()).subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Não foi possível carregar os usuários.');
        this.loading.set(false);
      }
    });
  }
}
