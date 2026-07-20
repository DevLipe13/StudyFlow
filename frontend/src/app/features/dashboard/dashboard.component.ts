import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  template: `
    <section class="page">
      <h1>Dashboard</h1>
      <p>Bem-vindo ao StudyFlow. Em breve você verá suas tarefas aqui.</p>
    </section>
  `,
  styles: [
    `
      .page h1 {
        margin: 0 0 0.5rem;
        font-size: 1.5rem;
      }

      .page p {
        margin: 0;
        color: #475569;
      }
    `
  ]
})
export class DashboardComponent {}
