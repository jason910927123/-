// App.js
import React from 'react';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import {Grid, createTheme, ThemeProvider, Typography} from '@mui/material';
import Header from './Header';
import Content from './Content.jsx';
import Travel from './Travel.jsx';

const theme = createTheme({
    palette: {primary: {main: '#1976d2'}, secondary: {main: '#dc004e'}},
    typography: {fontFamily: 'Arial', h4: {fontSize: '2rem', fontWeight: 'bold'}, body1: {fontSize: '1rem'}},
    components: {MuiButton: {styleOverrides: {root: {borderRadius: 8, padding: '8px 16px'}}}},
});

const App = () => (
    <ThemeProvider theme={theme}>
        <Grid container direction="column" style={{minHeight: '100vh'}}>
            {/*<Grid item><Header/></Grid>*/}
            <Grid item xs>
                <Grid container>
                    <Routes>
                        <Route path="/" element={<Content/>}/>
                        <Route path="/travel" element={<Travel/>}/>
                        <Route path="*" element={<Typography variant="h4">404 - 找不到頁面</Typography>}/>
                    </Routes>
                </Grid>
            </Grid>
        </Grid>
    </ThemeProvider>
);

export default App;
