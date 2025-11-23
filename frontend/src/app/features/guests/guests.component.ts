import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableComponent } from '../../shared/components/table/table.component';
import { GuestsService } from './guests.service';
import { Observable, Subject, takeUntil } from 'rxjs';
import { map } from 'rxjs/operators';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { GuestsFormModalComponent } from './guests-form-modal.component';
import { GuestResponse } from './models/guest-response.interface';

@Component({
  selector: 'app-guests',
  standalone: true,
  imports: [CommonModule, TableComponent, MatDialogModule, MatButtonModule],
  templateUrl: './guests.component.html',
  styleUrls: ['./guests.component.css']
})
export class GuestsComponent implements OnDestroy {
  guests$: Observable<GuestResponse[]>;
  private destroy$ = new Subject<void>();
  showDeleteModal = false;
  guestToDelete: { id: number } | null = null;

  // hide hotel info and analytics metrics from table view
  columns = ['id', 'fullName', 'documentType', 'documentNumber', 'email', 'phone'];

  headersMap: Record<string,string> = {
    id: 'ID',
    fullName: 'Nombre completo',
    documentType: 'Tipo doc',
    documentNumber: 'N° documento',
    email: 'Correo',
    phone: 'Teléfono'
  };

  constructor(private dialog: MatDialog, private guestsService: GuestsService) {
    this.guestsService.loadGuests();
    // strip out hotel and analytics fields from the observable used by the table
    this.guests$ = this.guestsService.guests$.pipe(
      map(items => items.map(it => {
        const { hotelId, hotelName, previousCancellations, totalBookingsClient, ...rest } = it as any;
        return rest;
      })),
      takeUntil(this.destroy$)
    );
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  refresh() {
    this.guestsService.loadGuests();
  }

  openForm(guest?: GuestResponse) {
    const dialogRef = this.dialog.open(GuestsFormModalComponent, {
      width: '600px',
      data: { guest }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (guest) {
          this.guestsService.update(guest.id, result).subscribe(() => this.refresh());
        } else {
          this.guestsService.create(result).subscribe(() => this.refresh());
        }
      }
    });
  }

  onDelete(id: number) {
    this.guestToDelete = { id };
    this.showDeleteModal = true;
  }

  confirmDelete() {
    if (this.guestToDelete) {
      this.guestsService.delete(this.guestToDelete.id).subscribe(() => {
        this.refresh();
        this.cancelDelete();
      });
    }
  }

  cancelDelete() {
    this.showDeleteModal = false;
    this.guestToDelete = null;
  }
}
