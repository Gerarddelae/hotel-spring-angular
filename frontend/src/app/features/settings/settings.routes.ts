import { Routes } from '@angular/router';
import { SettingsComponent } from './settings.component';

export const settingsRoutes: Routes = [
  {
    path: '',
    component: SettingsComponent,
    data: {
      title: 'Configuración',
      subtitle: 'Administra la información de tu hotel',
      icon: 'pi-cog',
    },
  },
];
