import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { AdminLoginChangeComponent } from './admin-login-change.component';
import { LoginChangeService } from '../login-change.service';

describe('AdminLoginChangeComponent', () => {
  let fixture: ComponentFixture<AdminLoginChangeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminLoginChangeComponent],
      providers: [
        {
          provide: LoginChangeService,
          useValue: {
            list: () =>
              of([
                {
                  id: 'req-1',
                  requesterUserId: 'u1',
                  changeType: 'email',
                  status: 'pending',
                  proposedEmail: 'novo@example.com',
                  createdAt: '2026-01-01T00:00:00Z',
                  updatedAt: '2026-01-01T00:00:00Z'
                }
              ]),
            approve: () => of({}),
            reject: () => of({})
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminLoginChangeComponent);
    fixture.detectChanges();
  });

  it('should list pending requests', () => {
    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('Aprovar');
    expect(element.textContent).toContain('Rejeitar');
  });
});
