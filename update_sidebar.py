import os
import re

html_files = [
    r"d:\Du_lieu\CodeGym\Module_4\store\src\main\resources\templates\cpu\list-cpu.html",
    r"d:\Du_lieu\CodeGym\Module_4\store\src\main\resources\templates\cpu\add-cpu.html",
    r"d:\Du_lieu\CodeGym\Module_4\store\src\main\resources\templates\cpu\edit-cpu.html",
    r"d:\Du_lieu\CodeGym\Module_4\store\src\main\resources\templates\vga\list-vga.html",
    r"d:\Du_lieu\CodeGym\Module_4\store\src\main\resources\templates\vga\add-vga.html",
    r"d:\Du_lieu\CodeGym\Module_4\store\src\main\resources\templates\vga\edit-vga.html",
    r"d:\Du_lieu\CodeGym\Module_4\store\src\main\resources\templates\ram\list-ram.html",
    r"d:\Du_lieu\CodeGym\Module_4\store\src\main\resources\templates\ram\add-ram.html",
    r"d:\Du_lieu\CodeGym\Module_4\store\src\main\resources\templates\ram\edit-ram.html",
    r"d:\Du_lieu\CodeGym\Module_4\store\src\main\resources\templates\storage\list-storage.html",
    r"d:\Du_lieu\CodeGym\Module_4\store\src\main\resources\templates\storage\add-storage.html",
    r"d:\Du_lieu\CodeGym\Module_4\store\src\main\resources\templates\storage\edit-storage.html"
]

sidebar_template = '''    <div class="sidebar-menu">
        <div class="menu-title">Lối tắt</div>
        <a th:href="@{/}" class="menu-item">
            <i class="fas fa-store"></i> Ra Cửa Hàng
        </a>

        <div class="menu-title" style="margin-top: 15px;">Quản lý kho</div>
        <a th:href="@{/admin/cpu}" class="menu-item {cpu_active}">
            <i class="fas fa-microchip"></i> Vi xử lý (CPU)
        </a>
        <a th:href="@{/admin/vga}" class="menu-item {vga_active}">
            <i class="fas fa-tv"></i> Card đồ họa (VGA)
        </a>
        <a th:href="@{/admin/ram}" class="menu-item {ram_active}">
            <i class="fas fa-memory"></i> Bộ nhớ (RAM)
        </a>
        <a th:href="@{/admin/storage}" class="menu-item {storage_active}">
            <i class="fas fa-hdd"></i> Ổ cứng (Storage)
        </a>

        <div class="menu-title" style="margin-top: 15px;">Hệ thống</div>
        <a href="#" class="menu-item">
            <i class="fas fa-file-invoice"></i> Quản lý Đơn hàng
        </a>
    </div>'''

for filepath in html_files:
    if not os.path.exists(filepath):
        continue
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    cpu_active = "active" if "\\cpu\\" in filepath else ""
    vga_active = "active" if "\\vga\\" in filepath else ""
    ram_active = "active" if "\\ram\\" in filepath else ""
    storage_active = "active" if "\\storage\\" in filepath else ""

    new_sidebar = sidebar_template.format(
        cpu_active=cpu_active,
        vga_active=vga_active,
        ram_active=ram_active,
        storage_active=storage_active
    )

    # replace everything between <div class="sidebar-menu"> and </div>\n</div>\n\n<!-- KHU VỰC BÊN PHẢI -->
    pattern = re.compile(r'<div class="sidebar-menu">.*?(?=</div>\s*</div>\s*<!-- KHU VỰC BÊN PHẢI)', re.DOTALL)
    new_content = pattern.sub(new_sidebar, content)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(new_content)

print("Updated sidebars in all HTML files.")
