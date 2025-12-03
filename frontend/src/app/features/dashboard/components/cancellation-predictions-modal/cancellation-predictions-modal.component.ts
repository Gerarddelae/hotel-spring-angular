import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { PredictionService } from '../../services/prediction.service';
import { CancellationPrediction, PredictionResponse } from '../../models';

@Component({
  selector: 'app-cancellation-predictions-modal',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatButtonModule
  ],
  templateUrl: './cancellation-predictions-modal.component.html',
  styleUrls: ['./cancellation-predictions-modal.component.scss']
})
export class CancellationPredictionsModalComponent implements OnInit {
  isLoading = true;
  predictions: CancellationPrediction[] = [];
  summary: {
    totalBookings: number;
    highRiskCount: number;
    mediumRiskCount: number;
    lowRiskCount: number;
    averageCancellationProbability: number;
  } | null = null;
  error: string | null = null;

  constructor(
    private predictionService: PredictionService,
    private dialogRef: MatDialogRef<CancellationPredictionsModalComponent>
  ) {}

  ngOnInit(): void {
    this.loadPredictions();
  }

  loadPredictions(): void {
    this.isLoading = true;
    this.error = null;

    this.predictionService.getPendingBookingPredictions().subscribe({
      next: (response: PredictionResponse) => {
        // Sort by cancellation probability descending (highest risk first)
        this.predictions = [...response.predictions].sort(
          (a, b) => b.cancellationProbability - a.cancellationProbability
        );
        this.summary = {
          totalBookings: response.totalBookings,
          highRiskCount: response.highRiskCount,
          mediumRiskCount: response.mediumRiskCount,
          lowRiskCount: response.lowRiskCount,
          averageCancellationProbability: response.averageCancellationProbability
        };
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading predictions:', err);
        this.error = 'Error al cargar las predicciones. Por favor, intenta de nuevo.';
        this.isLoading = false;
      }
    });
  }

  /**
   * Get risk badge color classes based on risk level
   */
  getRiskColorClasses(riskLevel: string): string {
    switch (riskLevel) {
      case 'HIGH':
        return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400';
      case 'MEDIUM':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400';
      case 'LOW':
        return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400';
    }
  }

  /**
   * Get risk level label in Spanish
   */
  getRiskLevelLabel(riskLevel: string): string {
    switch (riskLevel) {
      case 'HIGH':
        return 'Alto';
      case 'MEDIUM':
        return 'Medio';
      case 'LOW':
        return 'Bajo';
      default:
        return riskLevel;
    }
  }

  /**
   * Format probability as percentage
   */
  formatProbability(probability: number): string {
    return `${(probability * 100).toFixed(1)}%`;
  }

  /**
   * Format currency
   */
  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-419', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(amount);
  }

  /**
   * Close the modal
   */
  close(): void {
    this.dialogRef.close();
  }
}
