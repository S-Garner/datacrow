import React, { createContext, useContext, useState, useRef, type JSX } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { authenticationProvider } from "../security/authentication_provider";
import type { LoginCallBack, User } from "../services/datacrow_api";

interface AuthContextType {
	user: User;
	signin: (user: string, password: string, callback: LoginCallBack) => void;
	signout: (callback: VoidFunction) => void;
}

export const AuthContext = createContext<AuthContextType>(null!);

export function useAuth() {
	return useContext(AuthContext);
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
	let [user, setUser] = useState<any>(null);
    const savedCallback = useRef<LoginCallBack | null>(null);
    
	let signin = (newUser: string, newPassword: string, callback: LoginCallBack) => {
        if (savedCallback.current === null) {
            savedCallback.current = callback;
        }

		return authenticationProvider.signin(newUser, newPassword, (user : User | null) => {
            setUser(user);
            savedCallback.current && savedCallback.current(user);
        });
	};
	
	let signout = (callback: VoidFunction) => {
		return authenticationProvider.signout(() => {
			setUser(null);
			callback();
		});
	};

	let value = { user, signin, signout };

	return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function RequireAuth({ children }: { children: JSX.Element }) {
	let auth = useAuth();
	let location = useLocation();

	if (!auth.user) {
		// Redirect the unknown user to the /login page, saving the location the user came from
		return <Navigate to="/login" state={{ from: location }} replace />;
	}

	return children;
}