// --- 1. JS Xử lý Chuyển đổi trạng thái Xem / Sửa ---
const btnEditInfo = document.getElementById('btnEditInfo');
const btnSaveInfo = document.getElementById('btnSaveInfo');
const btnCancelInfo = document.getElementById('btnCancelInfo');
const avatarUploadDiv = document.getElementById('avatarUploadDiv');
const editableFields = document.querySelectorAll('.editable-field');

if (btnEditInfo) {
    // Khi bấm nút "Chỉnh sửa thông tin"
    btnEditInfo.addEventListener('click', function() {
        // Giấu nút Sửa
        btnEditInfo.classList.add('hidden');
        // Hiện nút Lưu, Hủy, và Chọn ảnh mới
        btnSaveInfo.classList.remove('hidden');
        btnCancelInfo.classList.remove('hidden');
        avatarUploadDiv.classList.remove('hidden');

        // Bỏ khóa Readonly cho các ô text (sẽ chuyển từ xám sang trắng)
        editableFields.forEach(field => {
            field.removeAttribute('readonly');
        });
    });
}

if (btnCancelInfo) {
    // Khi bấm nút "Hủy"
    btnCancelInfo.addEventListener('click', function() {
        // Đơn giản nhất là Tải lại trang (F5) để dọn dẹp mọi thay đổi chưa lưu
        window.location.reload();
    });
}

// --- 2. JS Hiển thị ảnh ngay khi vừa chọn ---
const avatarInput = document.getElementById('avatarInput');
const avatarPreview = document.getElementById('avatarPreview');

if (avatarInput) {
    avatarInput.addEventListener('change', function(event) {
        if(event.target.files && event.target.files[0]) {
            const reader = new FileReader();
            reader.onload = function(e) {
                avatarPreview.src = e.target.result;
            }
            reader.readAsDataURL(event.target.files[0]);
        }
    });
}
