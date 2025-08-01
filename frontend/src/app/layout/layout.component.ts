// layout.component.ts
import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
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
export class LayoutComponent implements OnInit, OnDestroy {
  // Estados del tema y sidebar
  isDarkMode = false;
  sidebarCollapsed = false;
  isMobile = false;
  isMobileSidebarOpen = false;

  // Información de la página actual
  currentPageTitle = 'Dashboard';
  currentPageSubtitle = 'Bienvenido al sistema de gestión hotelera';

  // Usuario actual
  user?: JwtPayload | null = null;

  constructor(
    private router: Router,
    private titleService: Title,
    private activatedRoute: ActivatedRoute,
    private authService: AuthService
  ) {}

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.checkScreenSize();
  }

  ngOnInit(): void {
    this.initializeTheme();
    this.checkScreenSize();
    this.loadUser();
    this.setupRouteListener();
    this.updatePageInfoFromCurrentRoute();
  }

  ngOnDestroy(): void {
    this.closeMobileSidebar();
  }

  // ==================== SIDEBAR METHODS ====================

  get isCollapsed(): boolean {
    return this.isMobile ? false : this.sidebarCollapsed;
  }

  toggleDesktopSidebar(): void {
    if (!this.isMobile) {
      this.sidebarCollapsed = !this.sidebarCollapsed;
      console.log('Desktop sidebar toggled:', this.sidebarCollapsed);
    }
  }

  openMobileSidebar(): void {
    if (this.isMobile) {
      this.isMobileSidebarOpen = true;
      console.log('Mobile sidebar opened');
      // Prevent body scroll when sidebar is open
      document.body.style.overflow = 'hidden';
    }
  }

  closeMobileSidebar(): void {
    if (this.isMobile && this.isMobileSidebarOpen) {
      this.isMobileSidebarOpen = false;
      console.log('Mobile sidebar closed');
      // Restore body scroll
      document.body.style.overflow = '';
    }
  }

  onNavigate(): void {
    // Close mobile sidebar when navigating
    if (this.isMobile) {
      this.closeMobileSidebar();
    }
  }

  getSidebarClasses(): string {
    if (this.isMobile) {
      return this.isMobileSidebarOpen 
        ? 'sidebar-mobile-open' 
        : 'sidebar-mobile-closed';
    } else {
      return this.sidebarCollapsed 
        ? 'sidebar-desktop-collapsed' 
        : 'sidebar-desktop-expanded';
    }
  }

  getMainClasses(): string {
    if (this.isMobile) {
      return 'main-mobile';
    } else {
      return this.sidebarCollapsed 
        ? 'main-desktop-collapsed' 
        : 'main-desktop-expanded';
    }
  }

  // ==================== UTILITY METHODS ====================

  checkScreenSize(): void {
    const wasMobile = this.isMobile;
    this.isMobile = window.innerWidth < 1024; // lg breakpoint in Tailwind
    
    console.log('Screen check - Width:', window.innerWidth, 'isMobile:', this.isMobile);

    // If changing from mobile to desktop, close mobile sidebar
    if (wasMobile && !this.isMobile) {
      this.closeMobileSidebar();
    }

    // If changing from desktop to mobile, ensure mobile sidebar is closed
    if (!wasMobile && this.isMobile) {
      this.closeMobileSidebar();
    }
  }

  toggleDarkMode(): void {
    this.isDarkMode = !this.isDarkMode;
    this.applyTheme();
    localStorage.setItem('theme', this.isDarkMode ? 'dark' : 'light');
  }

  logout(): void {
    // Close mobile sidebar if open
    this.closeMobileSidebar();
    
    // Clear storage
    localStorage.removeItem('token');
    localStorage.removeItem('user');

    // Navigate to login
    this.router.navigate(['/auth/login']);
  }

  // ==================== INITIALIZATION METHODS ====================

  private initializeTheme(): void {
    const savedTheme = localStorage.getItem('theme');
    const systemPrefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

    this.isDarkMode = savedTheme === 'dark' || (!savedTheme && systemPrefersDark);
    this.applyTheme();
  }

  private applyTheme(): void {
    if (this.isDarkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }

  private loadUser(): void {
    this.authService.user$.subscribe((user) => {
      console.log('👤 Usuario recibido en LayoutComponent:', user);
      this.user = user;
    });
  }

  private setupRouteListener(): void {
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
        // Close mobile sidebar when navigating
        if (this.isMobile) {
          this.closeMobileSidebar();
        }
      });
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
      this.titleService.setTitle(`${data['title']} - Hotel SPA`);
    } else {
      this.currentPageTitle = 'Hotel SPA';
      this.currentPageSubtitle = 'Sistema de gestión hotelera';
      this.titleService.setTitle('Hotel SPA - Management System');
    }
  }

  // ==================== PUBLIC UTILITY METHODS ====================

  getCurrentRoute(): string {
    return this.router.url;
  }

  isRouteActive(route: string): boolean {
    return this.router.url === route;
  }
}