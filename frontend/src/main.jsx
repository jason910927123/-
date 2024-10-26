import React from 'react';
import {createRoot} from 'react-dom/client';
import {BrowserRouter as Router} from 'react-router-dom';
import App from '@components/App.jsx'
import '@css/index.css'

const RootComponent = () => {
    return (
        <React.StrictMode>
            <Router>
                <App />
            </Router>
        </React.StrictMode>
    );
};

if (document.getElementById('root')) {
    const Index = document.getElementById('root');
    createRoot(Index).render(<RootComponent />);
}
