import { login, type User } from "../services/datacrow_api";

/**
 * This represents some generic auth provider API, like Firebase.
 */
const fakeAuthProvider = {
    isAuthenticated: false,
    
    signin(username : string, password: string, callback: VoidFunction) {
        login(username, password).then(user => handleLogin(user, callback));
    },
    signout(callback: VoidFunction) {
        fakeAuthProvider.isAuthenticated = false;
        setTimeout(callback, 100);
    },
};

function handleLogin(user: User, callback: VoidFunction) {
    console.log(user);
    setTimeout(callback, 100)
}

export { fakeAuthProvider };
