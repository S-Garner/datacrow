import { Button } from 'react-bootstrap';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/authentication_context';
import type { LoginCallBack, User } from 'src/services/datacrow_api';

export function LoginPage() {
    
	let navigate = useNavigate();
	let auth = useAuth();
	
	function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
		event.preventDefault();

		let formData = new FormData(event.currentTarget);
		let username = formData.get("username") as string;
		let password = formData.get("password") as string;

		auth.signin(username, password, callback);
	}

    function callback(user : User) {
        if (user)
            navigate("/", { replace: true });            
    }
 
	return (
		<div style={{position:"absolute", top:"50%", left:"50%", marginTop: "-50px", marginLeft: "-200px", width: "400px", height: "100px"}}>
			<form onSubmit={handleSubmit}>
				<div className="row mb-2">
					<input name="username" type="text" placeholder="Username" className="form-control" id="input-username" />
				</div>

				<div className="row mb-2">
					<input name="password" type="Password" placeholder="Password" className="form-control" id="input-password" />
				</div>

				<div className="row mb-2">
					<Button type="submit">Login</Button>
				</div>
			</form>
		</div>
	);
}