import ModuleMenu from "../../components/menu/module_menu";
import { ItemOverview } from "../../components/overview/item_overview";
import { RequireAuth } from "../../context/authentication_context";

export function OverviewPage() {
	return (
		<RequireAuth>
			<div style={{display: "inline-block", width:"100%"}}>
				<ModuleMenu>
					<ItemOverview />
				</ModuleMenu>
			</div>
		</RequireAuth>);
};