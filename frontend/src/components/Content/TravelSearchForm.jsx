// TravelSearchForm.js
import React, {useEffect, useState} from 'react';
import {
    Card,
    CardContent,
    Button,
    TextField,
    Select,
    MenuItem,
    FormControl,
    InputLabel,
    Grid,
    Box,
    Paper, Typography
} from '@mui/material';
import {fetchData} from '../api.js';
import {useNavigate} from "react-router-dom";

const TravelSearchForm = () => {
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [budget, setBudget] = useState('');
    const [destination, setDestination] = useState('');
    const [travelType, setTravelType] = useState('');
    const navigate = useNavigate(); // Initialize the navigation hook

    const calculateDays = (start, end) => {
        const startDateTime = new Date(start);
        const endDateTime = new Date(end);
        return Math.ceil((endDateTime - startDateTime) / (1000 * 60 * 60 * 24));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const data = {
            startDate: startDate,
            endDate,
            day: calculateDays(startDate, endDate),
            budget,
            place: destination,
            purpose: travelType
        };

        try {
            const responseData = await fetchData(data);

            if (responseData.status === 200) { // Check for a successful status
                console.log('Success:', responseData);
                alert('搜尋成功！');
                navigate('/Travel'); // Redirect to the /Travel route
            } else {
                alert('搜尋失敗，請檢查輸入。');
            }
        } catch (error) {
            alert('發生錯誤：' + error.message);
        }
    };

    const handleDateChange = (setter, value, otherDate, checkFunc) => {
        setter(value);
        if (value && otherDate) {
            if (checkFunc(value, otherDate) > 0) {
                alert('日期範圍不正確');
                setter('');
            }
        }
    };

    return (
        <Paper sx={{ maxWidth: "50%",minHeight:"50%", mt: 2,backgroundColor: '#fffd',padding: '10px',borderRadius:"2rem" }}>
            <Typography variant="h4" sx={{ mb: 2 }}>
                開始規劃你的旅程
            </Typography>

            <CardContent>
                <Box component="form" onSubmit={handleSubmit} sx={{ '& .MuiTextField-root': { mb: 2 } }}>
                    <Grid container spacing={2}>
                        <Grid item xs={12} sm={6}>
                            <TextField fullWidth type="date" label="出發日期" value={startDate} onChange={(e) => handleDateChange(setStartDate, e.target.value, endDate, calculateDays)} InputLabelProps={{ shrink: true }} required />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField fullWidth type="date" label="回程日期" value={endDate} onChange={(e) => handleDateChange(setEndDate, e.target.value, startDate, calculateDays)} InputLabelProps={{ shrink: true }} required />
                        </Grid>
                        <Grid item xs={12}>
                            <TextField fullWidth type="number" label="預算 (TWD)" value={budget} onChange={(e) => setBudget(e.target.value)} placeholder="請輸入預算金額" required />
                        </Grid>
                        <Grid item xs={12}>
                            <TextField fullWidth label="目的地" value={destination} onChange={(e) => setDestination(e.target.value)} placeholder="請輸入目的地" required />
                        </Grid>
                        <Grid item xs={12}>
                            <FormControl fullWidth required>
                                <InputLabel>旅遊類型</InputLabel>
                                <Select value={travelType} label="旅遊類型" onChange={(e) => setTravelType(e.target.value)}>
                                    <MenuItem value="leisure">休閒度假</MenuItem>
                                    <MenuItem value="adventure">冒險探索</MenuItem>
                                    <MenuItem value="culture">文化體驗</MenuItem>
                                    <MenuItem value="food">美食之旅</MenuItem>
                                    <MenuItem value="shopping">購物行程</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>
                        <Grid item xs={12}>
                            <Button type="submit" variant="contained" fullWidth sx={{ bgcolor: 'black', '&:hover': { bgcolor: 'grey.800' } }}>搜尋</Button>
                        </Grid>
                    </Grid>
                </Box>
            </CardContent>
        </Paper>
    );
};

export default TravelSearchForm;
