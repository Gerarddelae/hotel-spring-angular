import { Component, Input, OnInit, AfterViewInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-table',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatPaginatorModule, MatSortModule, FormsModule],
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.css']
})
export class TableComponent implements OnInit, AfterViewInit {

  @Input() data$!: Observable<any[]>;     // Observable de datos
  @Input() columnHeadersMap?: Record<string, string>; // Mapeo para nombres de columnas

  @Output() create = new EventEmitter<void>();
  @Output() edit = new EventEmitter<any>();
  @Output() delete = new EventEmitter<number>();

  dataSource = new MatTableDataSource<any>();
  displayedColumns: string[] = [];
  filterValue: string = '';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  ngOnInit(): void {
    this.data$.subscribe(res => {
      this.dataSource.data = res;
      if (res.length > 0) {
        this.displayedColumns = [...Object.keys(res[0]), 'actions']; // Agregamos columna acciones
      }
    });
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  getHeader(column: string): string {
    if (column === 'actions') return 'Acciones';
    return this.columnHeadersMap?.[column] ?? column;
  }

  onCreate() {
    this.create.emit();
  }

  onEdit(element: any) {
    this.edit.emit(element);
  }

  onDelete(id: number) {
    this.delete.emit(id);
  }
}
