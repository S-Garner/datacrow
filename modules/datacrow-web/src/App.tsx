import './App.scss';
import {Routes, Route, Outlet} from "react-router-dom";
import { LoginPage } from "./pages/login/authentication";
import { OverviewPage } from './pages/overview/overview';
import MainMenuBar from './components/main_menu_bar';
import { ItemPage } from './pages/item/item_details';
import { AuthProvider } from './context/authentication_context';

export default function App() {
	return (
		<AuthProvider>
			<Routes>
				<Route element={<Layout />}>
					<Route path="/" element={<OverviewPage />} />
					<Route path="/item" element={<ItemPage />} />
					<Route path="/login" element={<LoginPage />} />
				</Route>
			</Routes>
		</AuthProvider>
	);
}

function Layout() {
	return (
		<div>
			<MainMenuBar />
			<Outlet />
		</div>
	);
}

