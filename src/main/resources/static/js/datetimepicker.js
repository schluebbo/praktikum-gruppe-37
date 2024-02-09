var startTime = ["09:30", "09:45", "10:00", "10:15", "10:30", "10:45", "11:00", "11:15", "11:30", "11:45", "12:00", "12:15", "12:30", "12:45", "13:00", "13:15"];
var endTime = ["09:45", "10:00", "10:15", "10:30", "10:45", "11:00", "11:15", "11:30", "11:45", "12:00", "12:15", "12:30", "12:45", "13:00", "13:15", "13:30"];

const myDatePicker = MCDatepicker.create({
    el: '#date',
    dateFormat: 'dd.mm.YYYY',
})
jQuery('#startTime').datetimepicker({
    format: 'H:i',
    datepicker: false,
    allowTimes: startTime
});
jQuery('#endTime').datetimepicker({
    format: 'H:i',
    datepicker: false,
    allowTimes: endTime
});