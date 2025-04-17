import { Button, Form } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/authentication_context';
import { fetchResources, type User } from '../../services/datacrow_api';
import { MessageBox, MessageBoxType } from '../../components/message/message_box';
import { useEffect, useState } from 'react';
import { useTranslation, languageArray } from '../../context/translation_context';

export function LoginPage() {
    
    
    const [apiReachable, setApiReachable] = useState(0);
    
    const [success, setSuccess] = useState(true);
    const [configLoaded, setConfigLoaded] = useState<boolean>();
    
	let navigate = useNavigate();
	let auth = useAuth();
	
	const { setTranslations, t, language, setLanguage } = useTranslation();
    const [ selectedLanguage, setSelectedLanguage ] = useState(language);
    
    const handleLanguageSelect = (event: React.ChangeEvent<HTMLSelectElement>): void => {
        applyLanguage(event.target.value);
    }
    
    function applyLanguage(key: string) {
        for (const lang of languageArray) {
            if (lang.key === key) {
                setSelectedLanguage(lang.language);
                localStorage.setItem("language", lang.key);
                setLanguage(selectedLanguage);
            }
        }
    }

    /**
     * Fetch the cvonfiguration from /configuration/config.json. This file holds the URL to the API.
     */
    useEffect(() => {
        fetch('/configuration/config.json')
            .then((response) => {
                if (!response.ok) {
                    throw new Error('Error in retrieving the file');
                }
                return response.json();
            })
            .then((jsonData) => {
                (globalThis as any).apiUrl = jsonData.apiUrl;
                setConfigLoaded(true);
            })
            .catch((error) => console.error('Error fetching configuration from /configuration/config.json:', error));
    }, []);

    useEffect(() => {
        if (configLoaded) {
            fetchResources(selectedLanguage).
            then((data) => {
                setApiReachable(1);
                setTranslations(data);
            }).catch(error => {
                setApiReachable(-1);
                console.log(error);
                if (error.status === 401) {
                    navigate("/login");    
                }
            });
        }
    }, [selectedLanguage, configLoaded]);

	function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
		event.preventDefault();

		let formData = new FormData(event.currentTarget);
		let username = formData.get("username") as string;
		let password = formData.get("password") as string;

		auth.signin(username, password, callback);
		
        setTimeout(function() {
            setSuccess(false);
        }, 1500);
	}
	
	/**
     * Purely used to make sure we can display labels when the resources have not yet been retrieved, 
     * or if there was an error in retrieving the resources.
     */
	function translateOrDefault(tag : string, fallback : string) {
        let s = t(tag);
        
        if (s === tag || s === "undefined")
            s = fallback;
        
        return String(s);
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
                    header={translateOrDefault("msgLoginFailed", "Login Failed")}
                    message={translateOrDefault("msgLoginFailedDetails", "Invalid credentials were provided. Please enter the correct username and password.")}
                    type={MessageBoxType.warning} /> }
		
            <form onSubmit={handleSubmit}>
                {apiReachable === 1 && (
                    <div className="row mb-2" >
                        <Form.Select onChange={(event) => handleLanguageSelect(event)} key="language-menu" defaultValue={language}>
                            {languageArray.map((lang) => (
                                <option value={lang.key} key={lang.key}>
                                    {lang.label}
                                </option>
                            ))}
                        </Form.Select>
                    </div>)
                }
                
                {apiReachable === -1 &&  
                
                    <div className="row mb-2">
                        <p>The API server cannot be reached. Please check the API server port that is used (or contact your system administrator) on the server and make sure to allow traffic to and from this port on the server.</p>  
                        <p>You can check whether this API is reachable and functioning correctly via this link: <br /> {<a href={(globalThis as any).apiUrl + 'status'}>{(globalThis as any).apiUrl + 'status'}</a>}</p>   
                    </div>} 
			
			    {apiReachable === 1 && (
                     <>
        				<div className="row mb-2">
        					<input name="username" type="text" placeholder={translateOrDefault("lblUsername", "Username")} className="form-control" id="input-username" required={true} />
        				</div>
        
        				<div className="row mb-2">
        					<input name="password" type="Password" placeholder={translateOrDefault("lblPassword", "Password")} className="form-control" id="input-password" required={false} />
        				</div>
        
        				<div className="row mb-2">
        					<Button type="submit">{translateOrDefault("lblLogin", "Login")}</Button>
        				</div>
    				</>)
                }
			</form>
		</div>
	);
}