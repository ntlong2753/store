document.addEventListener("DOMContentLoaded", function() {
    document.body.addEventListener('click', function(e) {
        const paginationLink = e.target.closest('.pagination a');
        if (paginationLink) {
            e.preventDefault();
            const url = paginationLink.getAttribute('href');
            
            fetch(url)
                .then(response => response.text())
                .then(html => {
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(html, 'text/html');
                    
                    const newProductList = doc.getElementById('ajax-product-list');
                    const currentProductList = document.getElementById('ajax-product-list');
                    
                    if (newProductList && currentProductList) {
                        currentProductList.innerHTML = newProductList.innerHTML;
                        window.history.pushState({path: url}, '', url);
                        
                        // Scroll lên khu vực danh sách sản phẩm mượt mà
                        const sectionHeader = document.querySelector('.section-header');
                        if (sectionHeader) {
                            sectionHeader.scrollIntoView({ behavior: 'smooth' });
                        } else {
                            currentProductList.scrollIntoView({ behavior: 'smooth' });
                        }
                    }
                })
                .catch(err => console.error("Lỗi khi tải trang:", err));
        }
    });
    
    // Bắt sự kiện khi người dùng nhấn nút Back/Forward của trình duyệt
    window.addEventListener("popstate", function() {
        window.location.reload();
    });
});
