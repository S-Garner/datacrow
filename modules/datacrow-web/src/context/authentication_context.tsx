import React, { createContext, useContext, useState, useEffect, useRef } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { authenticationProvider } from "../security/authentication_provider";
import type { LoginCallBack, User } from "src/services/datacrow_api";

interface AuthContextType {
	token: string;
	user: string;
	signin: (user: string, password: string, callback: LoginCallBack) => void;
	signout: (callback: VoidFunction) => void;
}

export const AuthContext = createContext<AuthContextType>(null!);

export function useAuth() {
	return useContext(AuthContext);
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
	let [user, setUser] = useState<any>(null);
	let [token, setToken] = useState<any>(null);
	
    const savedCallback = useRef<LoginCallBack | null>(null);
    
	let signin = (newUser: string, newPassword: string, callback: LoginCallBack) => {
        if (savedCallback.current === null) {
            savedCallback.current = callback;
        }

		return authenticationProvider.signin(newUser, newPassword, myfunc);
	};
	
	 let myfunc = function callback(user : User) {
        setUser(user);
        console.log("User has been passed to highest level " + savedCallback.current);
        savedCallback.current && savedCallback.current(user);
    }

	let signout = (callback: VoidFunction) => {
		return authenticationProvider.signout(() => {
			setUser(null);
			setToken(null);
			callback();
		});
	};

	let value = { token, user, signin, signout };

	return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}


export function RequireAuth({ children }: { children: JSX.Element }) {
	let auth = useAuth();
	let location = useLocation();

    console.log("current user: " + auth.user);
    console.log("current token: " + auth.token);

	if (!auth.user) {
		// Redirect the unknown user to the /login page, saving the location the user came from
		return <Navigate to="/login" state={{ from: location }} replace />;
	}

	return children;
}