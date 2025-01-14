import { Button } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/authentication_context';
import type { User } from '../../services/datacrow_api';
import { MessageBox, MessageBoxType } from '../../components/message_box';
import { useState } from 'react';

export function LoginPage() {
    
    const [success, setSuccess] = useState(true);
    
	let navigate = useNavigate();
	let auth = useAuth();
	
	function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
		event.preventDefault();

		let formData = new FormData(event.currentTarget);
		let username = formData.get("username") as string;
		let password = formData.get("password") as string;

		auth.signin(username, password, callback);
		setSuccess(false);
	}

    function callback(user : User | null) {
        if (user) {
            setSuccess(true);
            localStorage.setItem("token", user.token);
            navigate("/", { replace: true });
        } else {
            setSuccess(false);
            localStorage.removeItem("token");
            console.debug("The user not authorized");
        }
    }
 
	return (
		<div style={{position:"absolute", top:"50%", left:"50%", marginTop: "-50px", marginLeft: "-200px", width: "400px", height: "100px"}}>
		    
		    { !success && <MessageBox
                    header="Login failed"
                    message="Invalid credentials were provided. Please enter the correct username and password" 
                    type={MessageBoxType.warning} /> }
		
			<form onSubmit={handleSubmit}>
				<div className="row mb-2">
					<input name="username" type="text" placeholder="Username" className="form-control" id="input-username" required={true} />
				</div>

				<div className="row mb-2">
					<input name="password" type="Password" placeholder="Password" className="form-control" id="input-password" required={true} />
				</div>

				<div className="row mb-2">
					<Button type="submit">Login</Button>
				</div>
			</form>
		</div>
	);
}