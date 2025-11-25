# Guía de Integración Frontend - Bill Response DTO

## Ejemplo de Uso en el Frontend (Angular/React/Vue)

### Estructura TypeScript/JavaScript

```typescript
interface BillResponseDTO {
  id: number;
  bookingId: number;
  
  // Información del huésped
  guestId?: number;
  guestName?: string;
  
  // Información de la habitación
  roomId?: number;
  roomNumber?: string;
  
  // Información de fechas
  checkInDate?: string; // formato: "YYYY-MM-DD"
  checkOutDate?: string; // formato: "YYYY-MM-DD"
  nights?: number;
  
  // Información de precios
  roomPricePerNight?: number;
  accommodationSubtotal?: number;
  addonsSubtotal?: number;
  
  // Información de la factura
  notes?: string;
  status: 'UNPAID' | 'PAID' | 'CANCELED';
  paymentMethod?: 'CASH' | 'CREDIT_CARD' | 'DEBIT_CARD' | 'TRANSFER' | 'CHECK' | 'OTHER';
  createdAt: string;
  totalAmount: number;
  
  addons: BillAddonDTO[];
}

interface BillAddonDTO {
  addonId: number;
  addonName: string;
  description?: string;
  unitPrice: number;
  quantity: number;
  totalPrice: number;
}
```

---

## Componente de Visualización de Factura

### Angular Example

```typescript
// bill-detail.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BillService } from './bill.service';

@Component({
  selector: 'app-bill-detail',
  templateUrl: './bill-detail.component.html'
})
export class BillDetailComponent implements OnInit {
  bill?: BillResponseDTO;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private billService: BillService
  ) {}

  ngOnInit(): void {
    const billId = this.route.snapshot.params['id'];
    this.loadBill(billId);
  }

  loadBill(id: number): void {
    this.billService.getBillById(id).subscribe({
      next: (data) => {
        this.bill = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading bill', error);
        this.loading = false;
      }
    });
  }
}
```

```html
<!-- bill-detail.component.html -->
<div class="bill-container" *ngIf="bill">
  <!-- Encabezado -->
  <div class="bill-header">
    <h2>Factura #{{ bill.id }}</h2>
    <span class="status-badge" [ngClass]="bill.status.toLowerCase()">
      {{ bill.status }}
    </span>
  </div>

  <!-- Información del Huésped y Habitación -->
  <div class="bill-info-grid">
    <div class="info-section">
      <h3>Información del Huésped</h3>
      <p><strong>Nombre:</strong> {{ bill.guestName || 'N/A' }}</p>
      <p><strong>ID:</strong> {{ bill.guestId || 'N/A' }}</p>
    </div>

    <div class="info-section">
      <h3>Información de la Habitación</h3>
      <p><strong>Habitación:</strong> {{ bill.roomNumber || 'N/A' }}</p>
      <p><strong>ID:</strong> {{ bill.roomId || 'N/A' }}</p>
    </div>

    <div class="info-section">
      <h3>Estadía</h3>
      <p><strong>Check-in:</strong> {{ bill.checkInDate | date:'dd/MM/yyyy' }}</p>
      <p><strong>Check-out:</strong> {{ bill.checkOutDate | date:'dd/MM/yyyy' }}</p>
      <p><strong>Noches:</strong> {{ bill.nights }}</p>
    </div>
  </div>

  <!-- Desglose de Precios -->
  <div class="bill-breakdown">
    <h3>Desglose de Costos</h3>
    
    <!-- Alojamiento -->
    <div class="breakdown-item">
      <span>Alojamiento ({{ bill.nights }} noches × ${{ bill.roomPricePerNight }})</span>
      <span class="amount">${{ bill.accommodationSubtotal | number:'1.2-2' }}</span>
    </div>

    <!-- Servicios Adicionales -->
    <div class="breakdown-section" *ngIf="bill.addons && bill.addons.length > 0">
      <h4>Servicios Adicionales</h4>
      <div class="breakdown-item" *ngFor="let addon of bill.addons">
        <span>{{ addon.addonName }} ({{ addon.quantity }} × ${{ addon.unitPrice }})</span>
        <span class="amount">${{ addon.totalPrice | number:'1.2-2' }}</span>
      </div>
      <div class="breakdown-subtotal">
        <span>Subtotal Servicios:</span>
        <span class="amount">${{ bill.addonsSubtotal | number:'1.2-2' }}</span>
      </div>
    </div>

    <!-- Total -->
    <div class="breakdown-total">
      <span>Total</span>
      <span class="amount">${{ bill.totalAmount | number:'1.2-2' }}</span>
    </div>
  </div>

  <!-- Notas -->
  <div class="bill-notes" *ngIf="bill.notes">
    <h3>Notas</h3>
    <p>{{ bill.notes }}</p>
  </div>

  <!-- Información de Pago -->
  <div class="payment-info" *ngIf="bill.paymentMethod">
    <p><strong>Método de Pago:</strong> {{ bill.paymentMethod }}</p>
  </div>

  <!-- Fecha de Creación -->
  <div class="bill-footer">
    <p class="created-at">Creada el {{ bill.createdAt | date:'dd/MM/yyyy HH:mm' }}</p>
  </div>
</div>
```

---

## React Example

```tsx
// BillDetail.tsx
import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getBillById } from './billService';

interface BillDetailProps {}

const BillDetail: React.FC<BillDetailProps> = () => {
  const { id } = useParams<{ id: string }>();
  const [bill, setBill] = useState<BillResponseDTO | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      loadBill(parseInt(id));
    }
  }, [id]);

  const loadBill = async (billId: number) => {
    try {
      const data = await getBillById(billId);
      setBill(data);
    } catch (error) {
      console.error('Error loading bill', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Cargando...</div>;
  if (!bill) return <div>Factura no encontrada</div>;

  return (
    <div className="bill-container">
      <div className="bill-header">
        <h2>Factura #{bill.id}</h2>
        <span className={`status-badge ${bill.status.toLowerCase()}`}>
          {bill.status}
        </span>
      </div>

      <div className="bill-info-grid">
        <div className="info-section">
          <h3>Información del Huésped</h3>
          <p><strong>Nombre:</strong> {bill.guestName || 'N/A'}</p>
          <p><strong>ID:</strong> {bill.guestId || 'N/A'}</p>
        </div>

        <div className="info-section">
          <h3>Información de la Habitación</h3>
          <p><strong>Habitación:</strong> {bill.roomNumber || 'N/A'}</p>
          <p><strong>Precio por noche:</strong> ${bill.roomPricePerNight?.toFixed(2)}</p>
        </div>

        <div className="info-section">
          <h3>Estadía</h3>
          <p><strong>Check-in:</strong> {bill.checkInDate}</p>
          <p><strong>Check-out:</strong> {bill.checkOutDate}</p>
          <p><strong>Noches:</strong> {bill.nights}</p>
        </div>
      </div>

      <div className="bill-breakdown">
        <h3>Desglose de Costos</h3>
        
        <div className="breakdown-item">
          <span>
            Alojamiento ({bill.nights} noches × ${bill.roomPricePerNight?.toFixed(2)})
          </span>
          <span className="amount">${bill.accommodationSubtotal?.toFixed(2)}</span>
        </div>

        {bill.addons && bill.addons.length > 0 && (
          <div className="breakdown-section">
            <h4>Servicios Adicionales</h4>
            {bill.addons.map((addon) => (
              <div key={addon.addonId} className="breakdown-item">
                <span>
                  {addon.addonName} ({addon.quantity} × ${addon.unitPrice.toFixed(2)})
                </span>
                <span className="amount">${addon.totalPrice.toFixed(2)}</span>
              </div>
            ))}
            <div className="breakdown-subtotal">
              <span>Subtotal Servicios:</span>
              <span className="amount">${bill.addonsSubtotal?.toFixed(2)}</span>
            </div>
          </div>
        )}

        <div className="breakdown-total">
          <span>Total</span>
          <span className="amount">${bill.totalAmount.toFixed(2)}</span>
        </div>
      </div>
    </div>
  );
};

export default BillDetail;
```

---

## Servicio HTTP

### Angular Service

```typescript
// bill.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BillService {
  private apiUrl = 'http://localhost:8080/api/bills';

  constructor(private http: HttpClient) {}

  getBills(): Observable<BillResponseDTO[]> {
    return this.http.get<BillResponseDTO[]>(this.apiUrl);
  }

  getBillById(id: number): Observable<BillResponseDTO> {
    return this.http.get<BillResponseDTO>(`${this.apiUrl}/${id}`);
  }

  createBill(bookingId: number, data: any): Observable<BillResponseDTO> {
    return this.http.post<BillResponseDTO>(
      `${this.apiUrl}?bookingId=${bookingId}`,
      data
    );
  }
}
```

### React/Axios Service

```typescript
// billService.ts
import axios from 'axios';

const API_URL = 'http://localhost:8080/api/bills';

export const getBills = async (): Promise<BillResponseDTO[]> => {
  const response = await axios.get<BillResponseDTO[]>(API_URL);
  return response.data;
};

export const getBillById = async (id: number): Promise<BillResponseDTO> => {
  const response = await axios.get<BillResponseDTO>(`${API_URL}/${id}`);
  return response.data;
};

export const createBill = async (
  bookingId: number, 
  data: any
): Promise<BillResponseDTO> => {
  const response = await axios.post<BillResponseDTO>(
    `${API_URL}?bookingId=${bookingId}`,
    data
  );
  return response.data;
};
```

---

## CSS Ejemplo

```css
/* bill-detail.css */
.bill-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.bill-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #eee;
}

.status-badge {
  padding: 5px 15px;
  border-radius: 20px;
  font-weight: bold;
  text-transform: uppercase;
  font-size: 0.9em;
}

.status-badge.paid {
  background-color: #d4edda;
  color: #155724;
}

.status-badge.unpaid {
  background-color: #fff3cd;
  color: #856404;
}

.status-badge.canceled {
  background-color: #f8d7da;
  color: #721c24;
}

.bill-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.info-section {
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 5px;
}

.info-section h3 {
  margin-top: 0;
  color: #495057;
  font-size: 1em;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.bill-breakdown {
  margin: 30px 0;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 5px;
}

.breakdown-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #dee2e6;
}

.breakdown-subtotal {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  margin-top: 10px;
  font-weight: 600;
  border-top: 2px solid #dee2e6;
}

.breakdown-total {
  display: flex;
  justify-content: space-between;
  padding: 15px 0;
  margin-top: 15px;
  font-size: 1.3em;
  font-weight: bold;
  border-top: 3px solid #495057;
}

.amount {
  color: #28a745;
  font-weight: 600;
}

.bill-notes {
  margin: 20px 0;
  padding: 15px;
  background-color: #fff3cd;
  border-left: 4px solid #ffc107;
  border-radius: 4px;
}

.bill-footer {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #dee2e6;
  text-align: right;
  color: #6c757d;
  font-size: 0.9em;
}
```

---

## Ventajas de esta Implementación

1. **Una sola llamada HTTP**: No necesitas hacer múltiples llamadas para obtener información del guest y room
2. **Cálculos automáticos**: Los subtotales y noches ya vienen calculados desde el backend
3. **Menos lógica en el frontend**: El backend maneja toda la lógica de cálculo
4. **Mejor UX**: Carga más rápida y menos peticiones al servidor
5. **Type-safe**: Con TypeScript tienes autocompletado y verificación de tipos

---

## Testing en el Frontend

```typescript
// bill-detail.component.spec.ts (Angular)
describe('BillDetailComponent', () => {
  it('should display guest information', () => {
    const mockBill: BillResponseDTO = {
      id: 1,
      guestName: 'Juan Pérez',
      guestId: 1,
      roomNumber: '101',
      // ... otros campos
    };
    
    component.bill = mockBill;
    fixture.detectChanges();
    
    const guestName = fixture.nativeElement.querySelector('.guest-name');
    expect(guestName.textContent).toContain('Juan Pérez');
  });
});
```

---

¡Listo para integrar en tu frontend! 🚀

