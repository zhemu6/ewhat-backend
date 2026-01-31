package com.lushihao.ewhatbackend.context;

/**
 * Request-scoped tenant context.
 *
 * Tenant id in this project is schoolId (column: school_id).
 */
public class TenantContextHolder {

    private static final ThreadLocal<Long> SCHOOL_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Integer> ROLE_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> TENANT_BYPASS_HOLDER = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void setSchoolId(Long schoolId) {
        SCHOOL_ID_HOLDER.set(schoolId);
    }

    public static Long getSchoolId() {
        return SCHOOL_ID_HOLDER.get();
    }

    /**
     * 1 = school admin, 2 = super admin.
     */
    public static void setRole(Integer role) {
        ROLE_HOLDER.set(role);
    }

    public static Integer getRole() {
        return ROLE_HOLDER.get();
    }

    public static void setTenantBypass(Boolean bypass) {
        TENANT_BYPASS_HOLDER.set(bypass);
    }

    public static boolean isTenantBypass() {
        Boolean bypass = TENANT_BYPASS_HOLDER.get();
        return bypass != null && bypass;
    }

    public static void clear() {
        SCHOOL_ID_HOLDER.remove();
        ROLE_HOLDER.remove();
        TENANT_BYPASS_HOLDER.remove();
    }
}
