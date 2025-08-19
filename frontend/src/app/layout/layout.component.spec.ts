import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { LayoutComponent } from './layout.component';
import { AuthService } from '../auth/auth.service';
import { BehaviorSubject, of } from 'rxjs';
import { Title } from '@angular/platform-browser';
import { NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { CurrentUser } from '../auth/interfaces/current-user.interface';

// Simulación de un usuario
const mockUser: CurrentUser = {
  username: 'adminuser',
  hotelName: 'Hotel Test',
  authorities: ['ADMIN']
};

// Mock AuthService
class MockAuthService {
  private userSubject = new BehaviorSubject(mockUser);
  user$ = this.userSubject.asObservable();
  emitUser(user: any) {
    this.userSubject.next(user);
  }
}

describe('LayoutComponent', () => {
  let component: LayoutComponent;
  let fixture: ComponentFixture<LayoutComponent>;
  let authService: MockAuthService;
  let titleService: Title;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        CommonModule,
        LayoutComponent, // Standalone
        RouterTestingModule.withRoutes([]) // Simula el Router
      ],
      providers: [
        { provide: AuthService, useClass: MockAuthService },
        Title
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LayoutComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as unknown as MockAuthService;
    titleService = TestBed.inject(Title);

    fixture.detectChanges(); // Dispara ngOnInit
  });

  // ===================== TESTS =====================

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });

  it('ngOnInit debería inicializar tema y cargar usuario', () => {
    expect(component.user).toEqual(mockUser);
    expect(typeof component.isDarkMode).toBe('boolean');
  });

  it('toggleDarkMode debería cambiar el estado y aplicar el tema', () => {
    const initialState = component.isDarkMode;
    component.toggleDarkMode();
    expect(component.isDarkMode).toBe(!initialState);
    // Verifica que se cambió en localStorage
    expect(localStorage.getItem('theme')).toBe(component.isDarkMode ? 'dark' : 'light');
  });

  it('logout debería limpiar localStorage y navegar a /auth/login', () => {
    localStorage.setItem('token', '123');
    const router = TestBed.inject(RouterTestingModule);
    const navigateSpy = spyOn(component['router'], 'navigate');

    component.logout();

    expect(localStorage.getItem('token')).toBeNull();
    expect(navigateSpy).toHaveBeenCalledWith(['/auth/login']);
  });

  it('getSidebarClasses debería devolver clases correctas en modo desktop', () => {
    component.isMobile = false;
    component.sidebarCollapsed = false;
    expect(component.getSidebarClasses()).toBe('sidebar-desktop-expanded');

    component.sidebarCollapsed = true;
    expect(component.getSidebarClasses()).toBe('sidebar-desktop-collapsed');
  });

  it('getSidebarClasses debería devolver clases correctas en modo mobile', () => {
    component.isMobile = true;
    component.isMobileSidebarOpen = true;
    expect(component.getSidebarClasses()).toBe('sidebar-mobile-open');

    component.isMobileSidebarOpen = false;
    expect(component.getSidebarClasses()).toBe('sidebar-mobile-closed');
  });

  it('getMainClasses debería devolver clases correctas según el estado', () => {
    component.isMobile = true;
    expect(component.getMainClasses()).toBe('main-mobile');

    component.isMobile = false;
    component.sidebarCollapsed = false;
    expect(component.getMainClasses()).toBe('main-desktop-expanded');

    component.sidebarCollapsed = true;
    expect(component.getMainClasses()).toBe('main-desktop-collapsed');
  });

  it('debería actualizar el título cuando recibe datos de ruta', () => {
    const setTitleSpy = spyOn(titleService, 'setTitle');
    (component as any).updatePageInfoFromRouteData({ title: 'Dashboard', subtitle: 'Test subtitle' });
    expect(component.currentPageTitle).toBe('Dashboard');
    expect(component.currentPageSubtitle).toBe('Test subtitle');
    expect(setTitleSpy).toHaveBeenCalledWith('Dashboard - Hotel SPA');
  });

  it('setupRouteListener debería reaccionar a NavigationEnd', () => {
    const setTitleSpy = spyOn(titleService, 'setTitle');
    const router = TestBed.inject(RouterTestingModule) as any;
    // Dispara un evento de navegación simulado
    (component as any).router.events.next(new NavigationEnd(1, '/home', '/home'));

    // Simula que updatePageInfoFromRouteData fue llamado
    (component as any).updatePageInfoFromRouteData({ title: 'Home' });

    expect(setTitleSpy).toHaveBeenCalledWith('Home - Hotel SPA');
  });
});
