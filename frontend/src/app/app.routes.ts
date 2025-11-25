import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { LayoutComponent } from './layout/layout.component';
import { authGuard } from './auth/auth.guard';
import { roleGuard } from './auth/role.guard';

export const routes: Routes = [
  {
    path: 'auth/login',
    component: LoginComponent,
  },
  {
    path: 'auth/register',
    component: RegisterComponent,
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(
            (m) => m.DashboardComponent
          ),
        canActivate: [() => roleGuard(['ADMIN'])],
        data: {
          title: 'Dashboard',
          subtitle: 'Bienvenido al sistema de gestión hotelera',
          icon: 'pi-chart-line',
        },
      },
      /* 'reservations' route removed - replaced by 'bookings' for employees/users */
      {
        path: 'users',
        loadComponent: () =>
          import('./features/users/users.component').then(
            (m) => m.UsersComponent
          ),
        canActivate: [() => roleGuard(['ADMIN'])],
        data: {
          title: 'Usuarios',
          subtitle: 'Lista de empleados y administradores',
          icon: 'pi-id-card',
        },
      },
      {
        path: 'rooms',
        loadComponent: () =>
          import('./features/rooms/rooms.component').then(
            (m) => m.RoomsComponent
          ),
        canActivate: [() => roleGuard(['ADMIN'])],
        data: {
          title: 'Habitaciones',
          subtitle: 'Gestión de habitaciones del hotel',
          icon: 'pi-home',
        },
      },
      {
        path: 'guests',
        loadComponent: () =>
          import('./features/guests/guests.component').then(
            (m) => m.GuestsComponent
          ),
        canActivate: [() => roleGuard(['ADMIN', 'EMPLOYEE', 'USER'])],
        data: {
          title: 'Huéspedes',
          subtitle: 'Gestión de huéspedes del hotel',
          icon: 'pi-users',
        },
      },
      {
        path: 'addons',
        loadComponent: () =>
          import('./features/addons/addons.component').then(
            (m) => m.AddonsComponent
          ),
        canActivate: [() => roleGuard(['ADMIN', 'EMPLOYEE'])],
        data: {
          title: 'Servicios Adicionales',
          subtitle: 'Gestión de servicios adicionales del hotel',
          icon: 'pi-shopping-cart',
        },
      },
      {
        path: 'bookings',
        loadChildren: () =>
          import('./features/bookings/bookings.routes').then(
            (m) => m.bookingsRoutes
          ),
        canActivate: [() => roleGuard(['ADMIN', 'EMPLOYEE', 'USER'])],
        data: {
          title: 'Reservas',
          subtitle: 'Gestión completa de reservas del hotel',
          icon: 'pi-bookmark',
        },
      },
      {
        path: 'bills',
        loadChildren: () =>
          import('./features/billing/billing.routes').then(
            (m) => m.billingRoutes
          ),
        canActivate: [() => roleGuard(['ADMIN', 'EMPLOYEE', 'USER'])],
        data: {
          title: 'Facturación',
          subtitle: 'Gestión de facturas del hotel',
          icon: 'pi-receipt',
        },
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full',
      },
    ],
  },
  {
    path: '**',
    redirectTo: 'auth/login',
  },
];
