package com.hotelsa.backend.aop.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)  // Se aplicará a métodos
@Retention(RetentionPolicy.RUNTIME)  // Disponible en tiempo de ejecución
@Documented
public @interface AdminOnly {
}
