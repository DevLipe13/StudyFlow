import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { MenuComponent } from '../menu/menu.component';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, MenuComponent],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss'
})
export class ShellComponent {
  protected readonly authService = inject(AuthService);
}
