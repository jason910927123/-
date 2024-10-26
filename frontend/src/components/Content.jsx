// Content.js
import React from 'react';
import TravelSearchForm from './TravelSearchForm.jsx';
import { Typography } from '@mui/material';

const Home = () => (
    <div>
        <Typography variant="h4" sx={{ textAlign: 'center', mt: 4, mb: 2 }}>開始規劃你的旅程</Typography>
        <TravelSearchForm />
    </div>
);

export default Home;
