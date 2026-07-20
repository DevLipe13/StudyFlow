import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { UsersListComponent } from './users-list.component';
import { UsersService } from '../users.service';

describe('UsersListComponent', () => {
  let fixture: ComponentFixture<UsersListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsersListComponent],
      providers: [
        provideRouter([]),
        {
          provide: UsersService,
          useValue: {
            list: () =>
              of([
                {
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
                }
              ])
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UsersListComponent);
    fixture.detectChanges();
  });

  it('should render user list', () => {
    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('Ana');
  });
});
