import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { UserFormComponent } from './user-form.component';
import { UsersService } from '../users.service';

describe('UserFormComponent', () => {
  let fixture: ComponentFixture<UserFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserFormComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({}) } }
        },
        {
          provide: UsersService,
          useValue: {
            create: () => of({}),
            getById: () => of({}),
            update: () => of({})
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UserFormComponent);
    fixture.detectChanges();
  });

  it('should render Portuguese labels for education and profile options', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Médio');
    expect(text).toContain('Graduação');
    expect(text).toContain('Estudante');
    expect(text).toContain('Admin');
  });
});
