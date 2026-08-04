document.addEventListener("DOMContentLoaded", function () {
    fetch('/api/current-date')
        .then(response => response.json())
        .then(data => {
            const dateEl = document.getElementById('admin-date');
            if (dateEl) {
                dateEl.innerText = "Hôm nay: " + data.date;
            }
        })
        .catch(err => console.error("Lỗi lấy ngày API: ", err));
});
