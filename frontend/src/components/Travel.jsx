// Content.js
import React from 'react';
import TravelForm from './Travel/TravelForm.jsx';
import {Grid} from '@mui/material';


const Content = () => (
    <Grid
        container
        direction="column"
        alignItems="center"
        justifyContent="center"
        sx={{ minHeight: '100vh', textAlign: 'center' }}
    >

        <TravelForm/>
    </Grid>
);

export default Content;
