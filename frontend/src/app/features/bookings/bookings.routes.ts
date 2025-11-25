import { Routes } from '@angular/router';
import { BookingsComponent } from './bookings.component';
import { BookingListComponent } from './pages/booking-list/booking-list.component';
import { BookingDetailComponent } from './pages/booking-detail/booking-detail.component';
import { authGuard } from '../../auth/auth.guard';

export const bookingsRoutes: Routes = [
  {
    path: '',
    component: BookingsComponent,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        component: BookingListComponent
      },
      {
        path: ':id',
        component: BookingDetailComponent
      }
    ]
  }
];
