import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-bookings',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="bookings-container">
      <router-outlet></router-outlet>
    </div>
  `,
  styles: [`
    .bookings-container {
      min-height: 100%;
      animation: fadeIn 0.2s ease-in;
    }
    
    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }
  `]
})
export class BookingsComponent {}
