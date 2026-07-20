import { Routes } from '@angular/router';

import { adminGuard } from './core/auth/admin.guard';
import { authGuard } from './core/auth/auth.guard';
import { studentGuard } from './core/auth/student.guard';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { AdminLoginChangeComponent } from './features/login-change/admin-login-change/admin-login-change.component';
import { StudentLoginChangeComponent } from './features/login-change/student-login-change/student-login-change.component';
import { TasksPlaceholderComponent } from './features/tasks-placeholder/tasks-placeholder.component';
import { UserDetailComponent } from './features/users/user-detail/user-detail.component';
import { UserFormComponent } from './features/users/user-form/user-form.component';
import { UsersListComponent } from './features/users/users-list/users-list.component';
import { ShellComponent } from './layout/shell/shell.component';

export const routes: Routes = [
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardComponent },
      {
        path: 'tarefas',
        component: TasksPlaceholderComponent,
        canActivate: [studentGuard]
      },
      {
        path: 'usuarios',
        canActivate: [adminGuard],
        children: [
          { path: '', component: UsersListComponent },
          { path: 'novo', component: UserFormComponent },
          { path: ':userId', component: UserDetailComponent },
          { path: ':userId/editar', component: UserFormComponent }
        ]
      },
      {
        path: 'alteracao-login',
        component: StudentLoginChangeComponent,
        canActivate: [studentGuard]
      },
      {
        path: 'alteracao-login/admin',
        component: AdminLoginChangeComponent,
        canActivate: [adminGuard]
      }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
