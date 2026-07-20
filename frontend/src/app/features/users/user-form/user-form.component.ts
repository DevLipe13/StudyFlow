import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { UsersService } from '../users.service';
import {
  AdminCreateUserRequest,
  AdminUpdateUserRequest,
  EDUCATION_LEVEL_LABELS,
  EducationLevel,
  PROFILE_LABELS,
  ProfileType
} from '../../../core/models/api.models';

@Component({
  selector: 'app-user-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.scss'
})
export class UserFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly usersService = inject(UsersService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly isEditMode = signal(false);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly educationLevels: EducationLevel[] = [
    'fundamental',
    'medio',
    'graduacao',
    'posGraduacao',
    'outro'
  ];

  readonly profiles: ProfileType[] = ['estudante', 'admin'];
  readonly educationLevelLabels = EDUCATION_LEVEL_LABELS;
  readonly profileLabels = PROFILE_LABELS;

  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    cpf: ['', Validators.required],
    birthDate: ['', Validators.required],
    educationLevel: ['medio' as EducationLevel, Validators.required],
    profile: ['estudante' as ProfileType, Validators.required],
    password: ['']
  });

  constructor() {
    const userId = this.route.snapshot.paramMap.get('userId');
    if (!userId) {
      this.form.controls.password.setValidators([Validators.required, Validators.minLength(8)]);
      return;
    }

    this.isEditMode.set(true);
    this.form.controls.email.disable();
    this.form.controls.password.disable();

    this.usersService.getById(userId).subscribe({
      next: (user) => {
        this.form.patchValue({
          name: user.name,
          email: user.email,
          cpf: user.cpf,
          birthDate: user.birthDate,
          educationLevel: user.educationLevel,
          profile: user.profile
        });
      },
      error: () => this.error.set('Não foi possível carregar o usuário.')
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    const userId = this.route.snapshot.paramMap.get('userId');

    if (!userId) {
      const payload = this.form.getRawValue() as AdminCreateUserRequest;
      this.usersService.create(payload).subscribe({
        next: (user) => {
          void this.router.navigate(['/usuarios', user.id]);
        },
        error: () => {
          this.error.set('Não foi possível criar o usuário.');
          this.loading.set(false);
        }
      });
      return;
    }

    const { name, cpf, birthDate, educationLevel, profile } = this.form.getRawValue();
    const payload: AdminUpdateUserRequest = { name, cpf, birthDate, educationLevel, profile };

    this.usersService.update(userId, payload).subscribe({
      next: (user) => {
        void this.router.navigate(['/usuarios', user.id]);
      },
      error: () => {
        this.error.set('Não foi possível atualizar o usuário.');
        this.loading.set(false);
      }
    });
  }
}
