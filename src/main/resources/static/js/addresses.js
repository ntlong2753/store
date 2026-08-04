function toggleForm() {
    var form = document.getElementById("addAddressForm");
    if (form) {
        if (form.style.display === "block") {
            form.style.display = "none";
        } else {
            form.style.display = "block";
        }
    }
}
