import { Component, OnInit } from '@angular/core';
import {
  ActivatedRoute,
  NavigationEnd,
  Router,
  RouterOutlet,
} from '@angular/router';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Title } from '@angular/platform-browser';
import { filter, map, switchMap } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { JwtPayload } from '../auth/interfaces/jwt-payload.interface';



@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css',
})
export class LayoutComponent implements OnInit {
  isDarkMode = false;
  sidebarCollapsed = false;

  currentPageTitle = 'Dashboard';
  currentPageSubtitle = 'Bienvenido al sistema de gestión hotelera';

  user?: JwtPayload | null = null;

  constructor(
    private router: Router,
    private titleService: Title,
    private activatedRoute: ActivatedRoute,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Restaurar tema guardado
    const savedTheme = localStorage.getItem('theme');
    const systemPrefersDark = window.matchMedia(
      '(prefers-color-scheme: dark)'
    ).matches;

    this.isDarkMode =
      savedTheme === 'dark' || (!savedTheme && systemPrefersDark);
    this.applyTheme();

    // Cargar usuario desde el token
  this.authService.user$.subscribe(user => {
    console.log('👤 Usuario recibido en LayoutComponent:', user);
    console.log(user?.authorities[0].authority);
    this.user = user;
  });

    // Escuchar cambios de ruta y obtener datos de la ruta
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        map(() => this.activatedRoute),
        map((route) => {
          while (route.firstChild) {
            route = route.firstChild;
          }
          return route;
        }),
        switchMap((route) => route.data)
      )
      .subscribe((data) => {
        this.updatePageInfoFromRouteData(data);
      });

    // Establecer información inicial de la página
    this.updatePageInfoFromCurrentRoute();
  }

  private updatePageInfoFromCurrentRoute(): void {
    let route = this.activatedRoute;
    while (route.firstChild) {
      route = route.firstChild;
    }

    route.data.subscribe((data) => {
      this.updatePageInfoFromRouteData(data);
    });
  }

  private updatePageInfoFromRouteData(data: any): void {
    if (data && data['title']) {
      this.currentPageTitle = data['title'];
      this.currentPageSubtitle = data['subtitle'] || '';

      // Actualizar el título del navegador
      this.titleService.setTitle(`${data['title']} - Hotel SPA`);
    } else {
      // Valores por defecto
      this.currentPageTitle = 'Hotel SPA';
      this.currentPageSubtitle = 'Sistema de gestión hotelera';
      this.titleService.setTitle('Hotel SPA - Management System');
    }
  }

  // Método público para obtener información de la página actual
  getCurrentRoute(): string {
    return this.router.url;
  }

  // Método para verificar si una ruta está activa
  isRouteActive(route: string): boolean {
    return this.router.url === route;
  }

  toggleDarkMode(): void {
    this.isDarkMode = !this.isDarkMode;
    this.applyTheme();
    localStorage.setItem('theme', this.isDarkMode ? 'dark' : 'light');
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  logout(): void {
    // Borra datos del localStorage o sessionStorage
    localStorage.removeItem('token');
    localStorage.removeItem('user');

    // Redirige al login
    this.router.navigate(['/auth/login']);
  }

  private applyTheme(): void {
    if (this.isDarkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }
}
