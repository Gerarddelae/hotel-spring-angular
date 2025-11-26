import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { KpiData } from '../../models';

@Component({
  selector: 'app-kpi-cards',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './kpi-cards.component.html'
})
export class KpiCardsComponent {
  @Input() kpis: KpiData[] = [];
  @Input() isLoading = false;

  /**
   * Returns the appropriate background and icon color classes based on the KPI index
   */
  getColorClasses(index: number): { bg: string; icon: string } {
    const colors = [
      { bg: 'bg-yellow-100 dark:bg-yellow-900/30', icon: 'text-yellow-600 dark:text-yellow-400' },
      { bg: 'bg-green-100 dark:bg-green-900/30', icon: 'text-green-600 dark:text-green-400' },
      { bg: 'bg-blue-100 dark:bg-blue-900/30', icon: 'text-blue-600 dark:text-blue-400' },
      { bg: 'bg-indigo-100 dark:bg-indigo-900/30', icon: 'text-indigo-600 dark:text-indigo-400' },
      { bg: 'bg-teal-100 dark:bg-teal-900/30', icon: 'text-teal-600 dark:text-teal-400' },
      { bg: 'bg-orange-100 dark:bg-orange-900/30', icon: 'text-orange-600 dark:text-orange-400' },
      { bg: 'bg-purple-100 dark:bg-purple-900/30', icon: 'text-purple-600 dark:text-purple-400' }
    ];
    return colors[index % colors.length];
  }
}
