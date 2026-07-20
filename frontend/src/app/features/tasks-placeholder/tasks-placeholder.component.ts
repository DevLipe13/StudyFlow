import { Component } from '@angular/core';

@Component({
  selector: 'app-tasks-placeholder',
  template: `
    <section class="page">
      <h1>Criar tarefas</h1>
      <p class="placeholder">Em breve</p>
    </section>
  `,
  styles: [
    `
      .page h1 {
        margin: 0 0 0.5rem;
        font-size: 1.5rem;
      }

      .placeholder {
        margin: 0;
        padding: 2rem 1rem;
        text-align: center;
        color: #64748b;
        border: 1px dashed #cbd5e1;
        border-radius: 0.5rem;
        background: #f8fafc;
      }
    `
  ]
})
export class TasksPlaceholderComponent {}
