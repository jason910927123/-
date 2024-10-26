// Header.js
import React from 'react';
import { AppBar, Toolbar, Typography, Button } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';

const Header = () => (
    <AppBar position="static" sx={{ backgroundColor: 'black' }}>
        <Toolbar>
            <Typography variant="h6" style={{ flexGrow: 1 }}>
                不知道要取什麼
            </Typography>
            <Button color="inherit" component={RouterLink} to="/">首頁</Button>
            <Button color="inherit" component={RouterLink} to="/about">關於我</Button>
            <Button color="inherit" component={RouterLink} to="/portfolio">我的作品集</Button>
        </Toolbar>
    </AppBar>
);

export default Header;
