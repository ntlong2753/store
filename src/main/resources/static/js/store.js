document.addEventListener("DOMContentLoaded", function() {
    // === 1. TÍNH NĂNG TÌM KIẾM ===
    const searchInput = document.getElementById("searchInput");
    if (searchInput) {
        let searchDropdown = document.querySelector(".search-dropdown");
        if (!searchDropdown) {
            searchDropdown = document.createElement("div");
            searchDropdown.className = "search-dropdown";
            searchInput.parentNode.appendChild(searchDropdown);
        }
        searchInput.addEventListener("input", function() {
            const keyword = searchInput.value.trim();
            if (keyword.length === 0) { searchDropdown.style.display = "none"; return; }
            fetch(`/api/search?keyword=${encodeURIComponent(keyword)}`)
                .then(response => response.json())
                .then(data => {
                    if (data.length === 0) {
                        searchDropdown.innerHTML = '<div class="search-no-result">Không tìm thấy linh kiện nào khớp!</div>';
                        searchDropdown.style.display = "block";
                        return;
                    }
                    searchDropdown.innerHTML = "";
                    data.forEach(cpu => {
                        const item = document.createElement("a");
                        item.href = `/product/${cpu.id}`;
                        item.className = "search-item";
                        item.innerHTML = `
                            <img src="${cpu.image != null ? cpu.image : 'https://via.placeholder.com/150x150?text=No+Image'}" alt="img">
                            <div class="search-item-info">
                                <div class="search-item-name">${cpu.name}</div>
                                <div class="search-item-price">${cpu.price}</div>
                            </div>
                        `;
                        searchDropdown.appendChild(item);
                    });
                    searchDropdown.style.display = "block";
                }).catch(error => console.error('Lỗi khi tìm kiếm:', error));
        });
        document.addEventListener("click", function(event) {
            if (!searchInput.parentNode.contains(event.target)) searchDropdown.style.display = "none";
        });
        searchInput.addEventListener("focus", function() {
            if (searchInput.value.trim().length > 0) searchDropdown.style.display = "block";
        });
    }
    
    // TÍNH NĂNG GIỎ HÀNG AJAX
    const cartForms = document.querySelectorAll('.form-add-cart');
    cartForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            const btn = this.querySelector('button');
            if (!btn) return;
            const originalText = btn.innerHTML;
            btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
            btn.disabled = true;

            fetch(this.action, { method: 'POST', body: new FormData(this) })
                .then(response => {
                    if (response.url.includes('/login')) {
                        window.location.href = '/login';
                        throw new Error("Cần đăng nhập");
                    }
                    return response.text();
                })
                .then(html => {
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(html, 'text/html');
                    const newCountEl = doc.querySelector('.cart-count');

                    if (newCountEl) {
                        document.querySelectorAll('.cart-count').forEach(el => {
                            el.innerText = newCountEl.innerText;
                            el.style.transform = "scale(1.5)";
                            setTimeout(() => el.style.transform = "scale(1)", 200);
                        });
                        alert("Đã thêm sản phẩm vào giỏ hàng!");
                    } else {
                        alert("Có lỗi xảy ra, thử lại sau!");
                    }
                    btn.innerHTML = originalText;
                    btn.disabled = false;
                }).catch(error => {
                console.error(error);
                if(btn) { btn.innerHTML = originalText; btn.disabled = false; }
            });
        });
    });
});

// === 3. TÍNH NĂNG PHÂN TRANG BẰNG AJAX ===
document.addEventListener("click", function(e) {
    const pageLink = e.target.closest('.pagination a');

    if (pageLink) {
        e.preventDefault();

        const productList = document.getElementById('ajax-product-list');
        if (productList) {
            productList.style.opacity = '0.5';

            const url = pageLink.href;

            fetch(url, {
                headers: {
                    "X-Requested-With": "XMLHttpRequest"
                }
            })
                .then(response => response.text())
                .then(html => {
                    productList.outerHTML = html;
                    document.querySelector('.section-header')?.scrollIntoView({ behavior: 'smooth' });
                })
                .catch(err => {
                    console.error("Lỗi chuyển trang: ", err);
                    productList.style.opacity = '1';
                });
        }
    }
});

    // ==========================================
    // 3. TÍNH NĂNG CHUYỂN ẢNH (GALLERY)
    // ==========================================
    let currentImageIndex = 0;

    window.changeImage = function(element, path, index) {
        const mainImg = document.getElementById('mainProductImg');
        const thumbnails = document.querySelectorAll('.thumbnail');
        if(!mainImg) return;
        mainImg.src = path;
        currentImageIndex = index;

        thumbnails.forEach(thumb => thumb.classList.remove('active'));
        if(element) element.classList.add('active');
    };

    window.prevImage = function() {
        const thumbnails = document.querySelectorAll('.thumbnail');
        if (thumbnails.length === 0) return;
        currentImageIndex--;
        if (currentImageIndex < 0) currentImageIndex = thumbnails.length - 1;

        const targetThumb = thumbnails[currentImageIndex];
        window.changeImage(targetThumb, targetThumb.getAttribute('data-path'), currentImageIndex);
    };

    window.nextImage = function() {
        const thumbnails = document.querySelectorAll('.thumbnail');
        if (thumbnails.length === 0) return;
        currentImageIndex++;
        if (currentImageIndex >= thumbnails.length) currentImageIndex = 0;

        const targetThumb = thumbnails[currentImageIndex];
        window.changeImage(targetThumb, targetThumb.getAttribute('data-path'), currentImageIndex);
    };

    // ==========================================
    // 4. TÍNH NĂNG TĂNG GIẢM SỐ LƯỢNG GIỎ HÀNG
    // ==========================================
    document.querySelectorAll('.btn-quantity').forEach(btn => {
        btn.addEventListener('click', function() {
            const action = this.getAttribute('data-action');
            const form = this.closest('form');
            const input = form.querySelector('.quantity-input');
            let val = parseInt(input.value);
            
            if (action === 'increase') {
                input.value = val + 1;
            } else if (action === 'decrease' && val > 1) {
                input.value = val - 1;
            }
            
            form.submit();
        });
    });
