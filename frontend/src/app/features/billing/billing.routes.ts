import { Routes } from '@angular/router';
import { BillingComponent } from './billing.component';
import { BillListComponent } from './pages/bill-list/bill-list.component';
import { BillDetailComponent } from './pages/bill-detail/bill-detail.component';
import { authGuard } from '../../auth/auth.guard';

export const billingRoutes: Routes = [
  {
    path: '',
    component: BillingComponent,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        component: BillListComponent
      },
      {
        path: ':id',
        component: BillDetailComponent
      }
    ]
  }
];
