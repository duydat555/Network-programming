package com.example.desktop.system;

import com.example.desktop.api.AuthApiClient;

public class UserSession {

    private static AuthApiClient.UserInfo currentUser;

    private UserSession() {}

    public static void setUser(AuthApiClient.UserInfo user) {
        currentUser = user;
    }

    public static AuthApiClient.UserInfo getUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clear() {
        currentUser = null;
    }
}