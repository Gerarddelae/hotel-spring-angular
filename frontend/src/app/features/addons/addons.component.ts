import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableComponent } from '../../shared/components/table/table.component';
import { AddonsService } from './addons.service';
import { Observable, Subject, takeUntil } from 'rxjs';
import { map } from 'rxjs/operators';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';
import { AddonsFormModalComponent } from './addons-form-modal.component';
import { AddonResponse } from './models/addon-response.interface';

@Component({
  selector: 'app-addons',
  standalone: true,
  imports: [
    CommonModule,
    TableComponent,
    MatDialogModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    FormsModule
  ],
  templateUrl: './addons.component.html',
  styleUrls: ['./addons.component.css']
})
export class AddonsComponent implements OnDestroy {
  addons$: Observable<AddonResponse[]>;
  private destroy$ = new Subject<void>();
  showDeleteModal = false;
  addonToDelete: { id: number } | null = null;

  columns = ['id', 'name', 'description', 'price'];

  headersMap: Record<string, string> = {
    id: 'ID',
    name: 'Nombre',
    description: 'Descripción',
    price: 'Precio'
  };

  columnTransformMap: Record<string, (value: any) => any> = {
    price: (value: number) => new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(value)
  };

  searchTerm: string = '';

  constructor(private dialog: MatDialog, private addonsService: AddonsService) {
    // Strip out fields we don't want displayed (e.g., createdAt) before passing to the table
    this.addons$ = this.addonsService.addons$.pipe(
      map(items => items.map(it => {
        const { createdAt, ...rest } = it as any;
        return rest as AddonResponse;
      })),
      takeUntil(this.destroy$)
    );
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  refresh() {
    this.addonsService.loadAddons();
  }

  onSearch() {
    if (this.searchTerm.trim()) {
      this.addonsService.search(this.searchTerm.trim());
    } else {
      this.refresh();
    }
  }

  openForm(addon?: AddonResponse) {
    const dialogRef = this.dialog.open(AddonsFormModalComponent, {
      width: '600px',
      data: { addon }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (addon) {
          this.addonsService.update(addon.id, result).subscribe(() => this.refresh());
        } else {
          this.addonsService.create(result).subscribe(() => this.refresh());
        }
      }
    });
  }

  onDelete(id: number) {
    this.addonToDelete = { id };
    this.showDeleteModal = true;
  }

  confirmDelete() {
    if (this.addonToDelete) {
      this.addonsService.delete(this.addonToDelete.id).subscribe(() => {
        this.refresh();
        this.cancelDelete();
      });
    }
  }

  cancelDelete() {
    this.showDeleteModal = false;
    this.addonToDelete = null;
  }
}
