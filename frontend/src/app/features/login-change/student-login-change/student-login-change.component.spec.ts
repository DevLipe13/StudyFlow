import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { StudentLoginChangeComponent } from './student-login-change.component';
import { LoginChangeService } from '../login-change.service';

describe('StudentLoginChangeComponent', () => {
  let fixture: ComponentFixture<StudentLoginChangeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StudentLoginChangeComponent],
      providers: [
        {
          provide: LoginChangeService,
          useValue: {
            list: () => of([]),
            create: () => of({}),
            complete: () => of({})
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(StudentLoginChangeComponent);
    fixture.detectChanges();
  });

  it('should render request and complete forms', () => {
    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('Nova solicitação');
    expect(element.textContent).toContain('Concluir solicitação aprovada');
  });
});
