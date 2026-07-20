import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-menu',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.scss'
})
export class MenuComponent {
  private readonly authService = inject(AuthService);

  readonly user = this.authService.user;
  readonly showStudentTasks = computed(() => this.authService.isStudent());
  readonly showAdminUsers = computed(() => this.authService.isAdmin());

  logout(): void {
    void this.authService.logout();
  }
}
