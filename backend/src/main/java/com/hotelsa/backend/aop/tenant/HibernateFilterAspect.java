package com.hotelsa.backend.aop.tenant;

import com.hotelsa.backend.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class HibernateFilterAspect {

    private final EntityManager entityManager;

    @Around("@annotation(transactional)")
    public Object applyFiltersAroundTransactional(ProceedingJoinPoint joinPoint, Transactional transactional) throws Throwable {
        Session session = entityManager.unwrap(Session.class);

        try {
            // 👇 Habilitar filtros
            session.enableFilter("deletedFilter").setParameter("isDeleted", false);

            Long currentTenant = TenantContext.getCurrentTenant();
            if (currentTenant != null) {
                session.enableFilter("tenantFilter").setParameter("hotelId", currentTenant);
                log.debug("✅ Tenant filter aplicado con hotelId={}", currentTenant);
            } else {
                log.warn("⚠️ No hay tenant actual en el contexto.");
            }

            // Ejecutar el método del servicio

            return joinPoint.proceed();

        } finally {
            // 👇 Deshabilitar filtros al final
            session.disableFilter("deletedFilter");
            session.disableFilter("tenantFilter");
        }
    }
}
