import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { MenuComponent } from './menu.component';
import { AuthService } from '../../core/auth/auth.service';
import { UserResponse } from '../../core/models/api.models';

describe('MenuComponent', () => {
  let fixture: ComponentFixture<MenuComponent>;
  let logoutSpy: jasmine.Spy;

  const baseUser: UserResponse = {
    id: '1',
    keycloakId: 'kc-1',
    name: 'Ana',
    email: 'ana@example.com',
    cpf: '123',
    birthDate: '2000-01-01',
    educationLevel: 'medio',
    profile: 'estudante',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
    disabled: false
  };

  function setup(profile: UserResponse['profile']): HTMLElement {
    const userSignal = signal({ ...baseUser, profile });
    logoutSpy = jasmine.createSpy('logout');

    TestBed.configureTestingModule({
      imports: [MenuComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            user: userSignal.asReadonly(),
            isStudent: signal(profile === 'estudante'),
            isAdmin: signal(profile === 'admin'),
            logout: logoutSpy
          }
        }
      ]
    });

    fixture = TestBed.createComponent(MenuComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('should show student menu items including login change', () => {
    const element = setup('estudante');
    expect(element.textContent).toContain('Dashboard');
    expect(element.textContent).toContain('Criar tarefas');
    expect(element.textContent).toContain('Alterar login');
    expect(element.textContent).not.toContain('Usuários');
    expect(element.textContent).toContain('Sair');
  });

  it('should show admin menu items without tasks link', () => {
    const element = setup('admin');
    expect(element.textContent).toContain('Dashboard');
    expect(element.textContent).toContain('Usuários');
    expect(element.textContent).toContain('Aprovar login');
    expect(element.textContent).not.toContain('Criar tarefas');
    expect(element.textContent).toContain('Sair');
  });

  it('should call logout when Sair is clicked', () => {
    const element = setup('estudante');
    const button = Array.from(element.querySelectorAll('button')).find((item) =>
      item.textContent?.includes('Sair')
    );
    button?.dispatchEvent(new Event('click'));
    expect(logoutSpy).toHaveBeenCalled();
  });
});
