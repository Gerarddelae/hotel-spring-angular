import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HotelService } from './services/hotel.service';
import { HotelResponse, HotelUpdateRequest } from './models';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css'],
})
export class SettingsComponent implements OnInit {
  hotelForm!: FormGroup;
  loading = false;
  saving = false;
  errorMessage = '';
  successMessage = '';
  hotelData: HotelResponse | null = null;

  constructor(
    private fb: FormBuilder,
    private hotelService: HotelService
  ) {
    this.initializeForm();
  }

  ngOnInit(): void {
    this.loadHotelData();
  }

  private initializeForm(): void {
    this.hotelForm = this.fb.group({
      name: ['', [Validators.required]],
      address: [''],
      phone: ['', [Validators.pattern(/^\+?[0-9]{7,15}$/)]],
      description: [''],
    });
  }

  private loadHotelData(): void {
    this.loading = true;
    this.errorMessage = '';

    this.hotelService.getCurrentHotel().subscribe({
      next: (hotel) => {
        this.hotelData = hotel;
        this.hotelForm.patchValue({
          name: hotel.name || '',
          address: hotel.address || '',
          phone: hotel.phone || '',
          description: hotel.description || '',
        });
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al cargar datos del hotel:', error);
        this.errorMessage = 'No se pudo cargar la información del hotel. Por favor, intenta de nuevo.';
        this.loading = false;
      },
    });
  }

  onSubmit(): void {
    if (this.hotelForm.invalid) {
      this.hotelForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const updateData: HotelUpdateRequest = this.hotelForm.value;

    this.hotelService.updateCurrentHotel(updateData).subscribe({
      next: (response) => {
        this.hotelData = response;
        this.successMessage = '¡Información del hotel actualizada exitosamente!';
        this.saving = false;
        
        // Actualizar el nombre del hotel en localStorage si cambió
        if (response.name) {
          localStorage.setItem('hotelName', response.name);
          // Disparar evento para actualizar el layout
          window.location.reload();
        }

        // Limpiar mensaje después de 5 segundos
        setTimeout(() => {
          this.successMessage = '';
        }, 5000);
      },
      error: (error) => {
        console.error('Error al actualizar hotel:', error);
        this.errorMessage = error.error?.message || 'No se pudo actualizar la información. Por favor, verifica los datos.';
        this.saving = false;

        // Limpiar mensaje después de 5 segundos
        setTimeout(() => {
          this.errorMessage = '';
        }, 5000);
      },
    });
  }

  onReset(): void {
    if (this.hotelData) {
      this.hotelForm.patchValue({
        name: this.hotelData.name || '',
        address: this.hotelData.address || '',
        phone: this.hotelData.phone || '',
        description: this.hotelData.description || '',
      });
      this.errorMessage = '';
      this.successMessage = '';
    }
  }

  // Getters para validación en la plantilla
  get name() {
    return this.hotelForm.get('name');
  }

  get phone() {
    return this.hotelForm.get('phone');
  }
}
