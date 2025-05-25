import React, { useState } from "react";
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
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  MenuItem,
  Select,
  TextField,
  Box,
} from "@mui/material";
import { DragDropContext, Draggable, Droppable } from "react-beautiful-dnd";
import {
  StarBorder,
  ExpandLess,
  ExpandMore,
  Event,
  WbSunny,
  WbTwilight,
  Bedtime,
  Delete,
  Add,
  PictureAsPdf,
} from "@mui/icons-material";
import { useLocation } from "react-router-dom";
import html2pdf from "html2pdf.js";

function getIconForTimeOfDay(timeOfDay) {
  switch (timeOfDay) {
    case "Morning":
      return <WbSunny />;
    case "Noon":
      return <WbTwilight />;
    case "Night":
      return <Bedtime />;
    default:
      return <StarBorder />;
  }
}

function Content() {
  const [openDays, setOpenDays] = useState({});
  const location = useLocation();
    const itinerary = location.state?.data;
//   const itinerary = location.state?.data || generateMockData();//測試用(可刪除)

  const [dailyPlan, setDailyPlan] = useState(itinerary?.dailyPlan || []);
  const [sparePlan, setSparePlan] = useState(itinerary?.sparePlan || []);
  const [openAddDialog, setOpenAddDialog] = useState(false);
  const [openAddSpareDialog, setOpenAddSpareDialog] = useState(false);
  const [selectedSpareActivity, setSelectedSpareActivity] = useState(null);
  const [selectedDay, setSelectedDay] = useState(null);
  const [selectedTimeOfDay, setSelectedTimeOfDay] = useState("Custom");
  const [activityTime, setActivityTime] = useState("");
  const [newSpareActivity, setNewSpareActivity] = useState({
    activity: "",
    place: "",
    budgetAllocation: "",
    reason: "",
    alternativeActivity: "",
  });
  function handleClick(day) {
    setOpenDays((prevOpenDays) => ({
      ...prevOpenDays,
      [day]: !prevOpenDays[day],
    }));
  }

  // 展開所有天的函數
  const expandAllDays = () => {
    const allDaysExpanded = {};
    itinerary.dailyPlan.forEach((dayPlan) => {
      allDaysExpanded[dayPlan.day] = true;
    });
    setOpenDays(allDaysExpanded);
  };

  function handleDownloadPDF() {
    try {
      expandAllDays();

      setTimeout(() => {
        const element = document.querySelector(".PDF");
        if (!element) throw new Error("PDF element not found");



        const options = {
          margin: 10,
          filename: "行程計畫.pdf",
          image: { type: "jpeg", quality: 0.98 },
          html2canvas: {
            scale: 2,
            useCORS: true,
            scrollX: 0,
            scrollY: 0,
            windowWidth: document.documentElement.scrollWidth,
            windowHeight: document.documentElement.scrollHeight,
          },
          jsPDF: {
            unit: "mm",
            format: "a4",
            orientation: "portrait",
            compress: true,
          },
        };

        html2pdf()
          .from(element)
          .set(options)
          .save()
          .then(() => {
            // console.log("成功")
          })
          .catch((err) => {
            console.error("PDF generation failed:", err);
            document.body.removeChild(clone);
          });
      }, 500);
    } catch (error) {
      console.error("PDF error:", error);
    }
  }

  function handleDragEnd(result) {
    const { source, destination } = result;

    if (!destination) return;

    // 從 sparePlan 拖到 dailyPlan
    if (
      source.droppableId === "sparePlan" &&
      destination.droppableId.startsWith("dailyPlan")
    ) {
      const dayIndex = parseInt(destination.droppableId.split("-")[1], 10) - 1;
      const movedPlan = sparePlan[source.index];

      const updatedDailyPlan = [...dailyPlan];
      updatedDailyPlan[dayIndex].activities.splice(destination.index, 0, {
        ...movedPlan,
        timeOfDay: "Custom",
      });

      const updatedSparePlan = [...sparePlan];
      updatedSparePlan.splice(source.index, 1);

      setDailyPlan(updatedDailyPlan);
      setSparePlan(updatedSparePlan);
      return;
    }

    if (
      source.droppableId === "sparePlan" &&
      destination.droppableId === "sparePlan"
    ) {
      const reorderedSparePlan = [...sparePlan];
      const [removed] = reorderedSparePlan.splice(source.index, 1);
      reorderedSparePlan.splice(destination.index, 0, removed);
      setSparePlan(reorderedSparePlan);
      return;
    }

    if (
      source.droppableId.startsWith("dailyPlan") &&
      destination.droppableId.startsWith("dailyPlan") &&
      source.droppableId === destination.droppableId
    ) {
      const dayIndex = parseInt(source.droppableId.split("-")[1], 10) - 1;
      const updatedDailyPlan = [...dailyPlan];
      const [movedItem] = updatedDailyPlan[dayIndex].activities.splice(
        source.index,
        1
      );
      updatedDailyPlan[dayIndex].activities.splice(
        destination.index,
        0,
        movedItem
      );
      setDailyPlan(updatedDailyPlan);
      return;
    }
  }

  function handleDeleteActivity(dayIndex, activityIndex) {
    const updatedDailyPlan = [...dailyPlan];
    const deletedActivity = updatedDailyPlan[dayIndex].activities.splice(
      activityIndex,
      1
    )[0];

    setSparePlan([...sparePlan, deletedActivity]);
    setDailyPlan(updatedDailyPlan);
  }

  function handleOpenAddDialog(dayIndex) {
    setSelectedDay(dayIndex);
    setOpenAddDialog(true);
    setActivityTime("");
    setSelectedTimeOfDay("Custom");
  }

  function handleCloseAddDialog() {
    setOpenAddDialog(false);
    setSelectedSpareActivity(null);
    setSelectedTimeOfDay("Custom");
  }

  function handleConfirmAddActivity() {
    if (selectedSpareActivity !== null) {
      const updatedDailyPlan = [...dailyPlan];
      const activityToAdd = {
        ...sparePlan[selectedSpareActivity],
        timeOfDay: selectedTimeOfDay,
        activityTime: activityTime,
      };

      updatedDailyPlan[selectedDay].activities.push(activityToAdd);

      const updatedSparePlan = sparePlan.filter(
        (_, index) => index !== selectedSpareActivity
      );

      setDailyPlan(updatedDailyPlan);
      setSparePlan(updatedSparePlan);
      handleCloseAddDialog();
    }
  }

  function handleUpdateActivityTime(dayIndex, activityIndex, newTime) {
    const updatedDailyPlan = [...dailyPlan];
    updatedDailyPlan[dayIndex].activities[activityIndex].activityTime = newTime;
    setDailyPlan(updatedDailyPlan);
  }

  function handleOpenAddSpareDialog() {
    setOpenAddSpareDialog(true);
  }

  function handleCloseAddSpareDialog() {
    setOpenAddSpareDialog(false);
    setNewSpareActivity({
      activity: "",
      place: "",
      budgetAllocation: "",
      reason: "",
      alternativeActivity: "",
    });
  }

  function handleConfirmAddSpareActivity() {
    setSparePlan([...sparePlan, newSpareActivity]);
    handleCloseAddSpareDialog();
  }

  function handleDeleteSpareActivity(index) {
    const updatedSparePlan = [...sparePlan];
    updatedSparePlan.splice(index, 1);
    setSparePlan(updatedSparePlan);
  }

  //   假資料(測試用)
//   function generateMockData(days = 8) {
//     const mockDailyPlan = [];

//     for (let i = 1; i <= days; i++) {
//       mockDailyPlan.push({
//         day: i,
//         date: `2023-07-${i.toString().padStart(2, "0")}`,
//         dailyBudget: `NT$ ${Math.floor(Math.random() * 2000) + 2000}`,
//         activities: [
//           // 早晨活動
//           {
//             timeOfDay: "Morning",
//             activity: `早晨活動範例 ${i}`,
//             place: `地點 ${i}A`,
//             budgetAllocation: `NT$ ${Math.floor(Math.random() * 500) + 200}`,
//           },
//           // 中午活動
//           {
//             timeOfDay: "Noon",
//             activity: `中午活動範例 ${i}`,
//             place: `地點 ${i}B`,
//             budgetAllocation: `NT$ ${Math.floor(Math.random() * 800) + 300}`,
//           },
//           // 夜晚活動
//           {
//             timeOfDay: "Night",
//             activity: `夜晚活動範例 ${i}`,
//             place: `地點 ${i}C`,
//             budgetAllocation: `NT$ ${Math.floor(Math.random() * 1000) + 400}`,
//           },
//         ],
//       });
//     }

//     const mockSparePlan = [
//       {
//         activity: "中正紀念堂",
//         place: "中正紀念堂",
//         budgetAllocation: "NT$ 200",
//         reason: "若時間充裕",
//         alternativeActivity: "自由廣場拍照",
//       },
//       {
//         activity: "西門町購物",
//         place: "西門町商圈",
//         budgetAllocation: "NT$ 1,500",
//         reason: "若想購物",
//         alternativeActivity: "紅樓參觀",
//       },
//       {
//         activity: "貓空纜車",
//         place: "貓空纜車動物園站",
//         budgetAllocation: "NT$ 400",
//         reason: "若天氣良好",
//         alternativeActivity: "動物園參觀",
//       },
//     ];
//     return { dailyPlan: mockDailyPlan, sparePlan: mockSparePlan };
//   }

  return (
    <DragDropContext onDragEnd={handleDragEnd}>
      <Grid container justifyContent="center" sx={{ mt: 4 }}>
        <Paper
          className="PDF PDF-preview"
          sx={{
            minWidth: "800px",
            maxWidth: "50%",
            backgroundColor: "#fffd",
            padding: "20px",
            borderRadius: "1.5rem",
          }}
        >
          <Box
            sx={{
              display: "flex",
              justifyContent: "flex-end",
              width: "100%",
            }}
          >
            <PictureAsPdf
              onClick={handleDownloadPDF}
              sx={{
                cursor: "pointer",
                "&:hover": {
                  color: "primary.main",
                },
              }}
            />
          </Box>
          <Typography variant="h4"sx={{ mb: 3 }}>
            已為您規劃行程
          </Typography>

          <Divider variant="middle" sx={{ mb: 2 }} />

          <List
            sx={{ width: "100%", backgroundColor: "#fff0" }}
            component="nav"
            aria-labelledby="nested-list-subheader"
            subheader={
              <ListSubheader
                component="div"
                id="nested-list-subheader"
                sx={{ backgroundColor: "#fff0" }}
              >
                <Typography variant="h5">
                  行程列表
                </Typography>
              </ListSubheader>
            }
          >
            {dailyPlan.map((dayPlan, dayIndex) => (
              <React.Fragment key={dayIndex}>
                <ListItemButton onClick={() => handleClick(dayPlan.day)}>
                  <ListItemIcon>
                    <Event />
                  </ListItemIcon>
                  <ListItemText
                    primary={`Day ${dayPlan.day}: ${dayPlan.date} | 預算: ${dayPlan.dailyBudget}`}
                  />
                  {openDays[dayPlan.day] ? <ExpandLess /> : <ExpandMore />}
                  <IconButton
                    edge="end"
                    aria-label="add"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleOpenAddDialog(dayIndex);
                    }}
                  >
                    <Add />
                  </IconButton>
                </ListItemButton>
                <Collapse
                  in={openDays[dayPlan.day]}
                  timeout="auto"
                  unmountOnExit
                >
                  <Droppable droppableId={`dailyPlan-${dayPlan.day}`}>
                    {(provided) => (
                      <div {...provided.droppableProps} ref={provided.innerRef}>
                        {dayPlan.activities.map((activity, activityIndex) => (
                          <Draggable
                            key={`${dayPlan.day}-${activityIndex}`}
                            draggableId={`${dayPlan.day}-${activityIndex}`}
                            index={activityIndex}
                          >
                            {(provided) => (
                              <List
                                component="div"
                                disablePadding
                                ref={provided.innerRef}
                                {...provided.draggableProps}
                                {...provided.dragHandleProps}
                              >
                                <ListItemButton sx={{ pl: 4 }}>
                                  <ListItemIcon>
                                    {getIconForTimeOfDay(activity.timeOfDay)}
                                  </ListItemIcon>
                                  <ListItemText
                                    primary={`${activity.timeOfDay} - ${activity.activity}`}
                                    secondary={
                                      <>
                                        <Typography
                                          component="span"
                                          variant="body2"
                                        >
                                          地點：
                                          <a
                                            href={`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(
                                              activity.place
                                            )}`}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                          >
                                            {activity.place}
                                          </a>
                                        </Typography>
                                        <Typography
                                          component="span"
                                          variant="body2"
                                          sx={{ ml: 1 }}
                                        >
                                          預算：{activity.budgetAllocation}
                                        </Typography>
                                      </>
                                    }
                                  />
                                  <IconButton
                                    edge="end"
                                    aria-label="delete"
                                    onClick={() =>
                                      handleDeleteActivity(
                                        dayIndex,
                                        activityIndex
                                      )
                                    }
                                  >
                                    <Delete />
                                  </IconButton>
                                </ListItemButton>
                              </List>
                            )}
                          </Draggable>
                        ))}
                        {provided.placeholder}
                      </div>
                    )}
                  </Droppable>
                </Collapse>
              </React.Fragment>
            ))}
          </List>

          <Typography variant="h5" align="center" sx={{ flexGrow: 1 }}>
            備用計劃
            <IconButton
              edge="end"
              sx={{
                left: "40%",
                padding: 0,
                zIndex: 999,
              }}
              onClick={handleOpenAddSpareDialog}
            >
              <Add />
            </IconButton>
          </Typography>

          <Divider variant="middle" sx={{ mb: 2 }} />
          <List>
            {sparePlan.map((plan, index) => (
              <ListItemButton key={index}>
                <ListItemText
                  primary={plan.activity}
                  secondary={
                    <>
                      <Typography component="span" variant="body2">
                        地點：
                        <a
                          href={`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(
                            plan.place
                          )}`}
                          target="_blank"
                          rel="noopener noreferrer"
                        >
                          {plan.place}
                        </a>
                      </Typography>
                      <Typography
                        component="span"
                        variant="body2"
                        sx={{ ml: 1 }}
                      >
                        預算：{plan.budgetAllocation}
                      </Typography>
                      <Typography
                        component="span"
                        variant="body2"
                        sx={{ ml: 1 }}
                      >
                        原因：{plan.reason}
                      </Typography>
                      <Typography
                        component="span"
                        variant="body2"
                        sx={{ ml: 1 }}
                      >
                        備用活動：{plan.alternativeActivity}
                      </Typography>
                    </>
                  }
                />
                <IconButton
                  edge="end"
                  aria-label="delete"
                  onClick={() => handleDeleteSpareActivity(index)}
                >
                  <Delete />
                </IconButton>
              </ListItemButton>
            ))}
          </List>

          <IconButton
            edge="end"
            sx={{ position: "absolute", bottom: 16, right: 16 }}
            onClick={handleOpenAddSpareDialog}
          >
            <Add />
          </IconButton>

          {/* Add Spare Activity Dialog */}
          <Dialog open={openAddSpareDialog} onClose={handleCloseAddSpareDialog}>
            <DialogTitle>新增備用活動</DialogTitle>
            <DialogContent>
              <TextField
                label="活動名稱"
                value={newSpareActivity.activity}
                onChange={(e) =>
                  setNewSpareActivity({
                    ...newSpareActivity,
                    activity: e.target.value,
                  })
                }
                fullWidth
                margin="normal"
              />
              <TextField
                label="地點"
                value={newSpareActivity.place}
                onChange={(e) =>
                  setNewSpareActivity({
                    ...newSpareActivity,
                    place: e.target.value,
                  })
                }
                fullWidth
                margin="normal"
              />
              <TextField
                label="預算"
                value={newSpareActivity.budgetAllocation}
                onChange={(e) =>
                  setNewSpareActivity({
                    ...newSpareActivity,
                    budgetAllocation: e.target.value,
                  })
                }
                fullWidth
                margin="normal"
              />
              <TextField
                label="活動原因"
                value={newSpareActivity.reason}
                onChange={(e) =>
                  setNewSpareActivity({
                    ...newSpareActivity,
                    reason: e.target.value,
                  })
                }
                fullWidth
                margin="normal"
              />
              <TextField
                label="備用活動"
                value={newSpareActivity.alternativeActivity}
                onChange={(e) =>
                  setNewSpareActivity({
                    ...newSpareActivity,
                    alternativeActivity: e.target.value,
                  })
                }
                fullWidth
                margin="normal"
              />
            </DialogContent>
            <DialogActions>
              <Button onClick={handleCloseAddSpareDialog}>取消</Button>
              <Button onClick={handleConfirmAddSpareActivity}>確認</Button>
            </DialogActions>
          </Dialog>

          {/* Add Activity Dialog */}
          <Dialog open={openAddDialog} onClose={handleCloseAddDialog}>
            <DialogTitle>新增活動</DialogTitle>
            <DialogContent>
              <Select
                value={selectedSpareActivity}
                onChange={(e) => setSelectedSpareActivity(e.target.value)}
                fullWidth
                sx={{ mb: 2 }}
              >
                {sparePlan.map((activity, index) => (
                  <MenuItem key={index} value={index}>
                    {activity.activity}
                  </MenuItem>
                ))}
              </Select>
              <Select
                value={selectedTimeOfDay}
                onChange={(e) => setSelectedTimeOfDay(e.target.value)}
                fullWidth
                sx={{ mb: 2 }}
              >
                <MenuItem value="Morning">早晨</MenuItem>
                <MenuItem value="Noon">中午</MenuItem>
                <MenuItem value="Night">夜晚</MenuItem>
                <MenuItem value="Custom">自訂時間</MenuItem>
              </Select>
              {selectedTimeOfDay === "Custom" && (
                <TextField
                  label="活動時間"
                  type="time"
                  value={activityTime}
                  onChange={(e) => setActivityTime(e.target.value)}
                  fullWidth
                  size="small"
                  sx={{ mb: 2 }}
                />
              )}
            </DialogContent>
            <DialogActions>
              <Button onClick={handleCloseAddDialog}>取消</Button>
              <Button onClick={handleConfirmAddActivity}>確認</Button>
            </DialogActions>
          </Dialog>
        </Paper>
      </Grid>
    </DragDropContext>
  );
}

export default Content;
