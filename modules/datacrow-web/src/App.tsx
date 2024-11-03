import './App.scss';
import {
	Routes,
	Route,
	useNavigate,
	useLocation,
	Navigate,
	Outlet,
} from "react-router-dom";
import ModuleMenu from "./components/module_menu";
import { AuthProvider, AuthStatus, LoginPage, RequireAuth } from "./security/authentication";
import { ItemOverview } from './components/item_overview';

export default function App() {
	return (
		<AuthProvider>
			<Routes>
				<Route element={<Layout />}>
					<Route path="/" element={
						<RequireAuth>
							<div>
								<ModuleMenu>
									<ItemOverview />
								</ModuleMenu>
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

