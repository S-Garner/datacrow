import './App.scss';
import {
	Routes,
	Route,
	Outlet,
} from "react-router-dom";
import { AuthProvider, AuthStatus, LoginPage } from "./pages/login/authentication";
import { OverviewPage } from './pages/overview/overview';

export default function App() {
	return (
		<AuthProvider>
			<Routes>
				<Route element={<Layout />}>
					<Route path="/" element={<OverviewPage />} />
					<Route path="/login" element={<LoginPage />} />
				</Route>
			</Routes>
		</AuthProvider>
	);
}

function Layout() {
	return (
		<div>
			<AuthStatus />
			<Outlet />
		</div>
	);
}

