import ModuleMenu from "../../components/module_menu";
import { RequireAuth } from "../login/authentication";
import { ItemOverview } from "../../components/item_overview";
import MainMenuBar from "../../components/main_menu_bar";

export function OverviewPage() {
	return (
		<RequireAuth>
			<div>
				<ModuleMenu>
					<ItemOverview />
				</ModuleMenu>
			</div>
		</RequireAuth>);
};