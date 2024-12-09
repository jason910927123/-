import React, { useState } from 'react';
import {
    CardContent,
    Button,
    TextField,
    Select,
    MenuItem,
    FormControl,
    InputLabel,
    Grid,
    Box,
    Paper,
    Typography,
    CircularProgress,
    Dialog,
    DialogContent
} from '@mui/material';
import { fetchData } from '../api.js';
import { useNavigate } from "react-router-dom";

const TravelSearchForm = () => {
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [budget, setBudget] = useState('');
    const [destination, setDestination] = useState('');
    const [travelType, setTravelType] = useState('');
    const [transportationType, setTransportationType] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const navigate = useNavigate();

    const calculateDays = (start, end) => {
        const startDateTime = new Date(start);
        const endDateTime = new Date(end);
        return Math.ceil(((endDateTime - startDateTime) / (1000 * 60 * 60 * 24)) + 1);
    };

    const handleDateChange = (setter, value, compareDate, isStart) => {
        setter(value);
        if (value && compareDate) {
            const start = isStart ? value : compareDate;
            const end = isStart ? compareDate : value;
            if (new Date(start) > new Date(end)) {
                alert('日期範圍不正確，請檢查。');
                setter('');
            }
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // 確認所有欄位均已填寫
        if (!startDate || !endDate || !budget || !destination || !travelType || !transportationType) {
            alert('所有欄位均為必填，請檢查。');
            return;
        }

        // 映射旅遊類型的中文描述
        const purposeMap = {
            'leisure': '休閒度假',
            'adventure': '冒險探索',
            'culture': '文化體驗',
            'food': '美食之旅',
            'shopping': '購物行程'
        };

        const data = {
            budget,
            purpose: purposeMap[travelType] || '家庭旅遊',
            startDate,
            endDate,
            day: calculateDays(startDate, endDate),
            place: destination,
            commuting: transportationType === 'drive' ? '自駕' : '大眾運輸工具'
        };

        setIsLoading(true);
        try {
            const responseData = await fetchData(data);

            if (responseData) {
                navigate('/Travel', { state: { data: responseData } });
            } else {
                alert('搜尋失敗，請檢查輸入。');
            }
        } catch (error) {
            alert('發生錯誤：' + error.message);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <>
            <Paper sx={{ maxWidth: "50%", minHeight: "50%", mt: 2, backgroundColor: '#fffd', padding: '10px', borderRadius: "2rem" }}>
                <Typography variant="h4" sx={{ mb: 2 }}>
                    開始規劃你的旅程
                </Typography>

                <CardContent>
                    <Box component="form" onSubmit={handleSubmit} sx={{ '& .MuiTextField-root': { mb: 2 } }}>
                        <Grid container spacing={2}>
                            <Grid item xs={12} sm={6}>
                                <TextField
                                    fullWidth
                                    type="date"
                                    label="出發日期"
                                    value={startDate}
                                    onChange={(e) => handleDateChange(setStartDate, e.target.value, endDate, true)}
                                    InputLabelProps={{ shrink: true }}
                                    required
                                />
                            </Grid>
                            <Grid item xs={12} sm={6}>
                                <TextField
                                    fullWidth
                                    type="date"
                                    label="回程日期"
                                    value={endDate}
                                    onChange={(e) => handleDateChange(setEndDate, e.target.value, startDate, false)}
                                    InputLabelProps={{ shrink: true }}
                                    required
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    fullWidth
                                    type="number"
                                    label="預算 (TWD)"
                                    value={budget}
                                    onChange={(e) => setBudget(e.target.value)}
                                    placeholder="請輸入預算金額"
                                    required
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    fullWidth
                                    label="目的地"
                                    value={destination}
                                    onChange={(e) => setDestination(e.target.value)}
                                    placeholder="請輸入目的地"
                                    required
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <FormControl fullWidth required>
                                    <InputLabel>通勤方式</InputLabel>
                                    <Select
                                        value={transportationType}
                                        label="通勤方式"
                                        onChange={(e) => setTransportationType(e.target.value)}
                                    >
                                        <MenuItem value="drive">自駕</MenuItem>
                                        <MenuItem value="public">大眾運輸工具</MenuItem>
                                    </Select>
                                </FormControl>
                            </Grid>
                            <Grid item xs={12}>
                                <FormControl fullWidth required>
                                    <InputLabel>旅遊類型</InputLabel>
                                    <Select
                                        value={travelType}
                                        label="旅遊類型"
                                        onChange={(e) => setTravelType(e.target.value)}
                                    >
                                        <MenuItem value="leisure">休閒度假</MenuItem>
                                        <MenuItem value="adventure">冒險探索</MenuItem>
                                        <MenuItem value="culture">文化體驗</MenuItem>
                                        <MenuItem value="food">美食之旅</MenuItem>
                                        <MenuItem value="shopping">購物行程</MenuItem>
                                    </Select>
                                </FormControl>
                            </Grid>
                            <Grid item xs={12}>
                                <Button
                                    type="submit"
                                    variant="contained"
                                    fullWidth
                                    disabled={isLoading}
                                    sx={{ bgcolor: 'black', '&:hover': { bgcolor: 'grey.800' } }}
                                >
                                    {isLoading ? '處理中...' : '搜尋'}
                                </Button>
                            </Grid>
                        </Grid>
                    </Box>
                </CardContent>
            </Paper>

            <Dialog open={isLoading} aria-labelledby="loading-dialog">
                <DialogContent sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    padding: 4
                }}>
                    <CircularProgress sx={{ mb: 2 }} />
                    <Typography variant="h6">
                        正在處理您的旅行搜尋...
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                        請稍候，我們正在為您尋找最佳旅行方案
                    </Typography>
                </DialogContent>
            </Dialog>
        </>
    );
};

export default TravelSearchForm;