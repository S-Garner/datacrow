import ColorStyleMenu from "./color_style_menu";
import { useAuth } from "../context/authentication_context";
import { useNavigate } from "react-router-dom";
import { Button } from "react-bootstrap";


export function UserStatus() {
	let auth = useAuth();
	let navigate = useNavigate();

	if (!auth.user) {
		return <div />;
	}


	return (
		<div style={{ float: "right", top: "0", right: "0"}}>
			<a
				onClick={() => {
					auth.signout(() => navigate("/"));
				}}
			> 
			<i className="bi bi-person-fill"></i>
			
			</a>
		</div>
	);
}


export default function MainMenuBar() {
	return (
		<div className="main-menu-bar">
			<UserStatus />
			<ColorStyleMenu />
		</div>
	);
}
