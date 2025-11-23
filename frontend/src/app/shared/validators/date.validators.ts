import { AbstractControl, ValidationErrors, ValidatorFn, FormGroup } from '@angular/forms';

/**
 * Validador que verifica que una fecha final sea posterior a una fecha inicial
 * @param startField Nombre del campo de fecha inicial
 * @param endField Nombre del campo de fecha final
 * @returns ValidatorFn
 */
export function dateRangeValidator(startField: string, endField: string): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const formGroup = control as FormGroup;
    const startControl = formGroup.get(startField);
    const endControl = formGroup.get(endField);

    if (!startControl || !endControl) {
      return null;
    }

    const startValue = startControl.value;
    const endValue = endControl.value;

    if (!startValue || !endValue) {
      return null;
    }

    const startDate = new Date(startValue);
    const endDate = new Date(endValue);

    if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
      return null;
    }

    if (endDate <= startDate) {
      return { 
        dateRange: {
          message: 'La fecha de check-out debe ser posterior a la fecha de check-in',
          startDate: startValue,
          endDate: endValue
        }
      };
    }

    return null;
  };
}

/**
 * Validador que verifica que una fecha no sea en el pasado
 * @returns ValidatorFn
 */
export function futureDateValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null;
    }

    const inputDate = new Date(control.value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (isNaN(inputDate.getTime())) {
      return null;
    }

    if (inputDate < today) {
      return { 
        futureDate: {
          message: 'La fecha no puede ser en el pasado',
          date: control.value
        }
      };
    }

    return null;
  };
}

/**
 * Validador que verifica que una fecha esté dentro de un rango permitido
 * @param minDays Número mínimo de días desde hoy
 * @param maxDays Número máximo de días desde hoy
 * @returns ValidatorFn
 */
export function dateRangeLimitValidator(minDays: number = 0, maxDays: number = 365): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null;
    }

    const inputDate = new Date(control.value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const minDate = new Date(today);
    minDate.setDate(minDate.getDate() + minDays);

    const maxDate = new Date(today);
    maxDate.setDate(maxDate.getDate() + maxDays);

    if (isNaN(inputDate.getTime())) {
      return null;
    }

    if (inputDate < minDate || inputDate > maxDate) {
      return { 
        dateRangeLimit: {
          message: `La fecha debe estar entre ${minDate.toLocaleDateString()} y ${maxDate.toLocaleDateString()}`,
          date: control.value,
          minDate: minDate.toISOString(),
          maxDate: maxDate.toISOString()
        }
      };
    }

    return null;
  };
}

/**
 * Validador que verifica que la diferencia entre dos fechas no exceda un máximo
 * @param startField Nombre del campo de fecha inicial
 * @param endField Nombre del campo de fecha final
 * @param maxDays Número máximo de días permitidos
 * @returns ValidatorFn
 */
export function maxDateRangeValidator(
  startField: string, 
  endField: string, 
  maxDays: number
): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const formGroup = control as FormGroup;
    const startControl = formGroup.get(startField);
    const endControl = formGroup.get(endField);

    if (!startControl || !endControl) {
      return null;
    }

    const startValue = startControl.value;
    const endValue = endControl.value;

    if (!startValue || !endValue) {
      return null;
    }

    const startDate = new Date(startValue);
    const endDate = new Date(endValue);

    if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
      return null;
    }

    const diffTime = Math.abs(endDate.getTime() - startDate.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays > maxDays) {
      return { 
        maxDateRange: {
          message: `La reserva no puede exceder ${maxDays} días`,
          days: diffDays,
          maxDays: maxDays
        }
      };
    }

    return null;
  };
}

/**
 * Validador que verifica que haya un mínimo de días entre dos fechas
 * @param startField Nombre del campo de fecha inicial
 * @param endField Nombre del campo de fecha final
 * @param minDays Número mínimo de días requeridos
 * @returns ValidatorFn
 */
export function minDateRangeValidator(
  startField: string, 
  endField: string, 
  minDays: number
): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const formGroup = control as FormGroup;
    const startControl = formGroup.get(startField);
    const endControl = formGroup.get(endField);

    if (!startControl || !endControl) {
      return null;
    }

    const startValue = startControl.value;
    const endValue = endControl.value;

    if (!startValue || !endValue) {
      return null;
    }

    const startDate = new Date(startValue);
    const endDate = new Date(endValue);

    if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
      return null;
    }

    const diffTime = Math.abs(endDate.getTime() - startDate.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays < minDays) {
      return { 
        minDateRange: {
          message: `La reserva debe ser de al menos ${minDays} día(s)`,
          days: diffDays,
          minDays: minDays
        }
      };
    }

    return null;
  };
}
