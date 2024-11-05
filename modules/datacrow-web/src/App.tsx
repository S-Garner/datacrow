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
			<div className="mode-switch" style={{ display: "flex", flexWrap: "wrap" }}>
				<button type="button" className="dropdown-item d-flex align-items-center" data-bs-theme-value="light" aria-pressed="false">
					<i className="bi bi-sun"></i>
				</button>

				<button type="button" className="dropdown-item d-flex align-items-center" data-bs-theme-value="dark" aria-pressed="false">
					<i className="bi bi-moon"></i>
				</button>
				<button type="button" className="dropdown-item d-flex align-items-center" data-bs-theme-value="auto" aria-pressed="false">
					<i className="bi bi-display"></i>
				</button>
			</div>
			<AuthStatus />
			<Outlet />
		</div>
	);
}

