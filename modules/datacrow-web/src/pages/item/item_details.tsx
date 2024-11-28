import { useLocation, useNavigate } from "react-router-dom";
import { RequireAuth } from "../login/authentication";
import { useEffect, useState } from "react";
import { fetchItem, type FieldValue, type Item } from "../../services/datacrow_api";
import { CurrentModuleContext, useCurrentModule } from "../../context/module_context";

export function ItemPage() {

	const navigate = useNavigate();
	const [item, setItem] = useState<Item>();
	const {state} = useLocation();
	
	const currentModule = useCurrentModule();
	
	useEffect(() => {
	    if (!state) {
			navigate('/');
		}
	  }, []);
	
	useEffect(() => {
			state && fetchItem(state.module.index, state.itemID).then((data) => setItem(data));
		}, []);
	
	
	function CreateItemField(field : FieldValue) {
		return (
			<div>{field.field.label}:{field.value}</div>
		);
	}
	
	
	
	if (state && item) {
		return (
			<RequireAuth>
				<div style={{display: "inline-block", width:"100%"}}>
					{item.fields.map((fieldValue) => (
						CreateItemField(fieldValue)				
					))}
				</div>
			</RequireAuth>
		);
	} 
}
