import { Routes } from '@angular/router';
import { authGuard } from '../../auth/auth.guard';

export const calendarEntriesRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./calendar-entries-page.component').then(
        (m) => m.CalendarEntriesPageComponent
      ),
    canActivate: [authGuard],
  },
];
