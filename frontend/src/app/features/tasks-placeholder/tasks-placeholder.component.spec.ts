import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TasksPlaceholderComponent } from './tasks-placeholder.component';

describe('TasksPlaceholderComponent', () => {
  let fixture: ComponentFixture<TasksPlaceholderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TasksPlaceholderComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(TasksPlaceholderComponent);
    fixture.detectChanges();
  });

  it('should show em breve placeholder', () => {
    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('Em breve');
  });
});
