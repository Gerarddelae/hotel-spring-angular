package com.hotelsa.backend.aop;

import com.hotelsa.backend.user.enums.Role;
import com.hotelsa.backend.user.model.User;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AdminOnlyAspect {

    @Before("@annotation(com.hotelsa.backend.aop.annotation.AdminOnly)")
    public void checkAdminPrivileges() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User currentUser)) {
            throw new AccessDeniedException("User is not authenticated");
        }

        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can perform this action");
        }

        if (currentUser.getHotel() == null) {
            throw new AccessDeniedException("Admin must belong to a hotel");
        }
    }
}


