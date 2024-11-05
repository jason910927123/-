// Content.js
import React from 'react';
import TravelSearchForm from './Content/TravelSearchForm.jsx';
import {Grid, Typography} from '@mui/material';


const Content = () => (
    <Grid
        container
        direction="column"
        alignItems="center"
        justifyContent="center"
        sx={{ minHeight: '100vh', textAlign: 'center' }}
    >

        <TravelSearchForm />
    </Grid>
);

export default Content;
