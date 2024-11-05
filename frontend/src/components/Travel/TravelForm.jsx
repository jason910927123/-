ㄇ// Content.js
import React, { useState } from 'react';
import {
    Collapse,
    Divider,
    Grid,
    List,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    ListSubheader,
    Paper,
    Typography,
} from '@mui/material';
import { StarBorder, ExpandLess, ExpandMore, Event, WbSunny, WbTwilight, Bedtime } from "@mui/icons-material";

// Sample JSON response data
const itinerary = {
    overview: {
        budget: "NT$5000",
        purpose: "culture",
        startDate: "2024-11-07",
        endDate: "2024-11-07",
        durationDays: "1",
        destination: "Taichung",
        summary: "A cultural day trip to explore Taichung's historical and cultural attractions, including visits to museums and local cuisine.",
    },
    dailyPlan: [
        {
            day: "1",
            date: "2024-11-07",
            activities: [
                {
                    timeOfDay: "Morning",
                    activity: "Visit to the National Taiwan Museum of Fine Arts",
                    description: "Explore important works of art and exhibitions in Taiwan.",
                    suggestedTime: "3 hours",
                    budgetAllocation: "Free Admission",
                },
                {
                    timeOfDay: "Afternoon",
                    activity: "Visit Taiwan Chinese Creative Park",
                    description: "Experience local cultural and creative products and exhibitions.",
                    suggestedTime: "2 hours",
                    budgetAllocation: "NT$200",
                },
                {
                    timeOfDay: "Evening",
                    activity: "Gaomei Wetland",
                    description: "Capture the scenic sunset views over the wetlands.",
                    suggestedTime: "2 hours",
                    budgetAllocation: "NT$200",
                },
            ],
        },
    ],
};

// Icon selection function
const getIconForTimeOfDay = (timeOfDay) => {
    switch (timeOfDay) {
        case 'Morning':
            return <WbSunny />;
        case 'Afternoon':
            return <WbTwilight />;
        case 'Evening':
            return <Bedtime />;
        default:
            return <StarBorder />;
    }
};

const Content = () => {
    const [open, setOpen] = useState(true);

    const handleClick = () => {
        setOpen(!open);
    };

    return (
        <Grid container justifyContent="center" sx={{ mt: 4 }}>
            <Paper
                sx={{
                    minWidth: "800px",
                    maxWidth: "50%",
                    backgroundColor: '#fffd',
                    padding: '20px',
                    borderRadius: "1.5rem",
                }}
            >
                <Typography variant="h4" align="center" sx={{ mb: 3 }}>
                    已為您規劃行程
                </Typography>

                <Typography variant="body1" align="center" sx={{ mb: 2 }}>
                    {itinerary.overview.summary}
                </Typography>

                <Divider variant="middle" sx={{ mb: 2 }} />

                <List
                    sx={{ width: '100%', backgroundColor: '#fff0' }}
                    component="nav"
                    aria-labelledby="nested-list-subheader"
                    subheader={
                        <ListSubheader component="div" id="nested-list-subheader" sx={{ backgroundColor: '#fff0' }}>
                            <Typography variant="h5" align="center">
                                行程列表 - {itinerary.overview.destination}
                            </Typography>
                        </ListSubheader>
                    }
                >
                    <ListItemButton onClick={handleClick}>
                        <ListItemIcon>
                            <Event />
                        </ListItemIcon>
                        <ListItemText primary={`Day ${itinerary.dailyPlan[0].day}: ${itinerary.dailyPlan[0].date}`} />
                        {open ? <ExpandLess /> : <ExpandMore />}
                    </ListItemButton>
                    <Collapse in={open} timeout="auto" unmountOnExit>
                        {itinerary.dailyPlan[0].activities.map((activity, index) => (
                            <React.Fragment key={index}>
                                <List component="div" disablePadding>
                                    <ListItemButton sx={{ pl: 4 }}>
                                        <ListItemIcon>
                                            {getIconForTimeOfDay(activity.timeOfDay)}
                                        </ListItemIcon>
                                        <ListItemText
                                            primary={`${activity.timeOfDay} - ${activity.activity}`}
                                            secondary={`${activity.description} | 時間: ${activity.suggestedTime} | 預算: ${activity.budgetAllocation}`}
                                        />
                                    </ListItemButton>
                                </List>
                                {index < itinerary.dailyPlan[0].activities.length - 1 && (
                                    <Divider variant="inset" component="li" />
                                )}
                            </React.Fragment>
                        ))}
                    </Collapse>
                </List>
            </Paper>
        </Grid>
    );
};

export default Content;
