import { Component } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css'
})
export class LayoutComponent {

  constructor(private router: Router) {}

    logout(): void {
    // Borra datos del localStorage o sessionStorage
    localStorage.removeItem('token'); // o localStorage.clear() si quieres borrar todo
    // También podrías borrar cualquier otro dato, como usuario, rol, etc.
    
    // Redirige al login
    this.router.navigate(['/auth/login']);
  }

}
