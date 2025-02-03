import './App.scss';
import { Routes, Route, Outlet } from "react-router-dom";
import { LoginPage } from "./pages/login/authentication";
import { OverviewPage } from './pages/overview/overview';
import MainMenuBar from './components/main_menu_bar';
import { ItemPage } from './pages/item/item_details';
import { AuthProvider } from './context/authentication_context';
import { ModuleProvider } from './context/module_context';
import { TranslationProvider } from './context/translation_context';

export default function App() {
    return (
        <TranslationProvider>
            <AuthProvider>
                <ModuleProvider>
                    <Routes>
                        <Route element={<Layout />}>
                            <Route path="/datacrow" element={<OverviewPage />} />
                            <Route path="/datacrow/item" element={<ItemPage />} />
                            <Route path="/datacrow/login" element={<LoginPage />} />
                        </Route>
                    </Routes>
                </ModuleProvider>
            </AuthProvider>
        </TranslationProvider>
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

