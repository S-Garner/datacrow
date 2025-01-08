import { createContext, useContext, useState, type JSX } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { fakeAuthProvider } from "../security/authentication_provider";

interface AuthContextType {
	token: string;
	user: string;
	signin: (user: string, password: string, callback: VoidFunction) => void;
	signout: (callback: VoidFunction) => void;
}

export const AuthContext = createContext<AuthContextType>(null!);

export function useAuth() {
	return useContext(AuthContext);
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
	let [user, setUser] = useState<any>(null);
	let [token, setToken] = useState<any>(null);

	let signin = (newUser: string, newPassword: string, callback: VoidFunction) => {
		return fakeAuthProvider.signin(newUser, newPassword, () => {
			setUser(newUser);
			setToken("44971974981");
			callback();
		});
	};

	let signout = (callback: VoidFunction) => {
		return fakeAuthProvider.signout(() => {
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

	if (!auth.token) {
		// Redirect the unknown user to the /login page, saving the location the user came from
		return <Navigate to="/login" state={{ from: location }} replace />;
	}

	return children;
}