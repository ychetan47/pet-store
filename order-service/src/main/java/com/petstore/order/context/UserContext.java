package com.petstore.order.context;

public class UserContext {

    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserRole = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserEmail = new ThreadLocal<>();

    public static Long getUserId() {
        return currentUserId.get();
    }

    public static void setUserId(Long userId) {
        currentUserId.set(userId);
    }

    public static String getUserRole() {
        return currentUserRole.get();
    }

    public static void setUserRole(String role) {
        currentUserRole.set(role);
    }

    public static String getUserEmail() {
        return currentUserEmail.get();
    }

    public static void setUserEmail(String email) {
        currentUserEmail.set(email);
    }

    public static void clear() {
        currentUserId.remove();
        currentUserRole.remove();
        currentUserEmail.remove();
    }
}
