import ModuleMenu from "../../components/module_menu";
import { RequireAuth } from "../login/authentication";
import { ItemOverview } from "../../components/item_overview";

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