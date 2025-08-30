import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
  return (route, state) => {
    const router = inject(Router);
    
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        router.navigate(['/auth/login']);
        return false;
      }
      
      const tokenData = JSON.parse(atob(token.split('.')[1]));
      const hasRequiredRole = allowedRoles.some(role => 
        tokenData.authorities?.includes(role)
      );

      if (!hasRequiredRole) {
        router.navigate(['/']); // Redirige a la página principal si no tiene el rol
        return false;
      }

      return true;
    } catch (e) {
      console.error('Error al verificar roles:', e);
      router.navigate(['/auth/login']);
      return false;
    }
  };
};