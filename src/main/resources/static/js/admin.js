document.addEventListener('DOMContentLoaded', function() {
    const imageInput = document.getElementById('imageInput');
    const previewContainer = document.getElementById('previewContainer');
    
    if (imageInput && previewContainer) {
        let selectedFiles = new DataTransfer();

        imageInput.addEventListener('change', function(event) {
            const files = event.target.files;
            for(let i = 0; i < files.length; i++) {
                selectedFiles.items.add(files[i]);
            }
            updateImagePreview();
        });

        function updateImagePreview() {
            previewContainer.innerHTML = '';
            imageInput.files = selectedFiles.files;
            Array.from(selectedFiles.files).forEach((file, index) => {
                const reader = new FileReader();
                reader.onload = function(e) {
                    const wrapper = document.createElement('div');
                    wrapper.style.position = 'relative';
                    wrapper.style.display = 'inline-block';
                    const img = document.createElement('img');
                    img.src = e.target.result;
                    img.style.width = '100px';
                    img.style.height = '100px';
                    img.style.objectFit = 'cover';
                    img.style.borderRadius = '6px';
                    img.style.border = '1px solid #ebedf3';
                    const deleteBtn = document.createElement('button');
                    deleteBtn.innerHTML = '<i class="fas fa-times"></i>';
                    deleteBtn.style.position = 'absolute';
                    deleteBtn.style.top = '-5px';
                    deleteBtn.style.right = '-5px';
                    deleteBtn.style.background = '#f64e60';
                    deleteBtn.style.color = 'white';
                    deleteBtn.style.border = 'none';
                    deleteBtn.style.borderRadius = '50%';
                    deleteBtn.style.width = '24px';
                    deleteBtn.style.height = '24px';
                    deleteBtn.style.cursor = 'pointer';
                    deleteBtn.onclick = function(e) {
                        e.preventDefault();
                        removeFile(index);
                    };
                    wrapper.appendChild(img);
                    wrapper.appendChild(deleteBtn);
                    previewContainer.appendChild(wrapper);
                }
                reader.readAsDataURL(file);
            });
        }

        function removeFile(indexToRemove) {
            const newFiles = new DataTransfer();
            const currentFiles = selectedFiles.files;
            for(let i = 0; i < currentFiles.length; i++) {
                if(i !== indexToRemove) {
                    newFiles.items.add(currentFiles[i]);
                }
            }
            selectedFiles = newFiles;
            updateImagePreview();
        }
    }

    // Logic for Storage Type toggle
    const storageType = document.getElementById('storageType');
    const ssdSection = document.getElementById('ssd-section');
    const hddSection = document.getElementById('hdd-section');

    if (storageType && ssdSection && hddSection) {
        function toggleStorageSections() {
            if (storageType.value === 'SSD') {
                ssdSection.style.display = 'block';
                hddSection.style.display = 'none';
            } else if (storageType.value === 'HDD') {
                ssdSection.style.display = 'none';
                hddSection.style.display = 'block';
            }
        }
        
        // Initial setup on page load
        toggleStorageSections();

        // Listen for changes
        storageType.addEventListener('change', toggleStorageSections);
    }
});

/**
 * Xóa ảnh cũ khỏi giao diện và thêm ID vào form để gửi lên server.
 * Hàm này được gọi từ nút X trên mỗi ảnh cũ trong form chỉnh sửa sản phẩm.
 * @param {number} imgId - ID của ảnh cần xóa trong database
 */
function deleteOldImage(imgId) {
    // 1. Kiểm tra nếu đã đánh dấu xóa rồi thì bỏ qua
    if (document.getElementById('del-img-id-' + imgId)) return;

    // 2. Tìm container của ảnh đó và ẩn nó đi với hiệu ứng
    const container = document.getElementById('old-img-container-' + imgId);
    if (container) {
        container.style.transition = 'opacity 0.2s ease, transform 0.2s ease';
        container.style.opacity = '0';
        container.style.transform = 'scale(0.8)';
        setTimeout(() => container.remove(), 200);
    }

    // 3. Thêm hidden input mới vào form chứa nút xóa (hoặc form chỉnh sửa sản phẩm)
    //    Spring @RequestParam List<Long> sẽ tự gom tất cả cùng tên thành danh sách
    const form = container ? container.closest('form') : document.querySelector('form[enctype="multipart/form-data"]');
    if (form) {
        const hiddenInput = document.createElement('input');
        hiddenInput.type = 'hidden';
        hiddenInput.name = 'deletedImageIds';
        hiddenInput.id = 'del-img-id-' + imgId;
        hiddenInput.value = imgId;
        form.appendChild(hiddenInput);
    }
}
