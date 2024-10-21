import 'bootstrap/dist/css/bootstrap.min.css';
import {
	Routes,
	Route,
	useNavigate,
	useLocation,
	Navigate,
	Outlet,
} from "react-router-dom";
import ModuleMenu from "./components/module_menu";
import ItemOverview from "./components/item_overview";
import { AuthProvider, AuthStatus, LoginPage, RequireAuth } from "./security/authentication";

export default function App() {
	return (
		<AuthProvider>
			<Routes>
				<Route element={<Layout />}>
					<Route path="/" element={
						<RequireAuth>
							<div>
								<ModuleMenu />
								<ItemOverview />
							</div>
						</RequireAuth>
					} />
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

