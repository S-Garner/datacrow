import './App.scss';
import { Routes, Route, Outlet } from "react-router-dom";
import { LoginPage } from "./pages/login/authentication";
import { OverviewPage } from './pages/overview/overview';
import MainMenuBar from './components/menu/main_menu_bar';
import { ItemPage } from './pages/item/item_edit';
import { AuthProvider } from './context/authentication_context';
import { ModuleProvider } from './context/module_context';
import { TranslationProvider } from './context/translation_context';
import { MessageProvider } from './context/message_context';
import { ItemCreatePage } from './pages/item/item_create';
import { FieldSettingsPage } from './pages/settings/field_settings';
import { ItemViewPage } from './pages/item/item_view';
import { OverviewFieldSettingsPage } from './pages/settings/overview_field_settings';
import ScrollToHashElement from './hooks/scroll_to_hash_element';

export default function App() {

    return (
        <TranslationProvider>

            <ScrollToHashElement />

            <MessageProvider>
                <AuthProvider>
                    <ModuleProvider>
                        <Routes>
                            <Route element={<Layout />}>
                                <Route path="/" element={<OverviewPage />} />
                                <Route path="/item_edit" element={<ItemPage />} />
                                <Route path="/item_create" element={<ItemCreatePage />} />
                                <Route path="/item_view" element={<ItemViewPage />} />
                                <Route path="/login" element={<LoginPage />} />
                                <Route path="/fieldsettings" element={<FieldSettingsPage />} />
                                <Route path="/overviewfieldsettings" element={<OverviewFieldSettingsPage />} />
                            </Route>
                        </Routes>
                    </ModuleProvider>
                </AuthProvider>
            </MessageProvider>
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

