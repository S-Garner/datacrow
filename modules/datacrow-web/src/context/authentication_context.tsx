import { createContext, useContext, useState } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { fakeAuthProvider } from "../security/authentication_provider";

interface AuthContextType {
	user: any;
	signin: (user: string, callback: VoidFunction) => void;
	signout: (callback: VoidFunction) => void;
}

export const AuthContext = createContext<AuthContextType>(null!);

export function useAuth() {
	return useContext(AuthContext);
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
	let [user, setUser] = useState<any>(null);

	let signin = (newUser: string, callback: VoidFunction) => {
		return fakeAuthProvider.signin(() => {
			setUser(newUser);
			callback();
		});
	};

	let signout = (callback: VoidFunction) => {
		return fakeAuthProvider.signout(() => {
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
		// Redirect the unknown user to the /login page, saving the location the use came from
		return <Navigate to="/login" state={{ from: location }} replace />;
	}

	return children;
}