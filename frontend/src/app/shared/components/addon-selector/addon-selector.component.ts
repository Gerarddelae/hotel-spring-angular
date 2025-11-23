import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { BookingAddon } from '../../../features/bookings/models/booking.interface';

@Component({
  selector: 'app-addon-selector',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    MatCardModule,
    MatChipsModule
  ],
  templateUrl: './addon-selector.component.html',
  styleUrls: ['./addon-selector.component.scss']
})
export class AddonSelectorComponent implements OnInit {
  @Input() availableAddons: any[] = [];
  @Input() selectedAddons: BookingAddon[] = [];
  @Output() addonsChange = new EventEmitter<BookingAddon[]>();

  selectedAddonId: number | null = null;

  ngOnInit(): void {
    // Inicialización si es necesario
  }

  /**
   * Añade un addon a la lista
   */
  addAddon(): void {
    if (!this.selectedAddonId) {
      return;
    }

    const addon = this.availableAddons.find(a => a.id === this.selectedAddonId);
    if (!addon) {
      return;
    }

    // Verificar si el addon ya está en la lista
    const existingAddon = this.selectedAddons.find(a => a.addonId === addon.id);
    if (existingAddon) {
      this.increaseQuantity(existingAddon);
      this.selectedAddonId = null;
      return;
    }

    const newAddon: BookingAddon = {
      addonId: addon.id,
      addonName: addon.name,
      price: addon.price,
      quantity: 1,
      subtotal: addon.price
    };

    this.selectedAddons.push(newAddon);
    this.emitChanges();
    this.selectedAddonId = null;
  }

  /**
   * Incrementa la cantidad de un addon
   */
  increaseQuantity(addon: BookingAddon): void {
    addon.quantity++;
    addon.subtotal = addon.price * addon.quantity;
    this.emitChanges();
  }

  /**
   * Decrementa la cantidad de un addon
   */
  decreaseQuantity(addon: BookingAddon): void {
    if (addon.quantity > 1) {
      addon.quantity--;
      addon.subtotal = addon.price * addon.quantity;
      this.emitChanges();
    }
  }

  /**
   * Actualiza la cantidad de un addon manualmente
   */
  updateQuantity(addon: BookingAddon, event: any): void {
    const value = parseInt(event.target.value, 10);
    if (value >= 1) {
      addon.quantity = value;
      addon.subtotal = addon.price * addon.quantity;
      this.emitChanges();
    } else {
      // Si el valor es inválido, restaurar a 1
      addon.quantity = 1;
      addon.subtotal = addon.price;
      event.target.value = '1';
      this.emitChanges();
    }
  }

  /**
   * Elimina un addon de la lista
   */
  removeAddon(addon: BookingAddon): void {
    const index = this.selectedAddons.indexOf(addon);
    if (index > -1) {
      this.selectedAddons.splice(index, 1);
      this.emitChanges();
    }
  }

  /**
   * Calcula el total de todos los addons
   */
  calculateTotal(): number {
    return this.selectedAddons.reduce((sum, addon) => sum + (addon.subtotal || 0), 0);
  }

  /**
   * Obtiene los addons disponibles que aún no están seleccionados
   */
  getAvailableAddonsForSelection(): any[] {
    return this.availableAddons.filter(addon => 
      !this.selectedAddons.some(selected => selected.addonId === addon.id)
    );
  }

  /**
   * Emite los cambios al componente padre
   */
  private emitChanges(): void {
    this.addonsChange.emit([...this.selectedAddons]);
  }
}
