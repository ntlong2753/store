# LUONG MUA HANG — NexGen PC Store
# Doc tu tren xuong duoi theo thu tu — moi khoi la 1 buoc
# Ky tu ─ ├ └ │ duoc dung de ve cay phan cap goi ham
# ═══════════════════════════════════════════════════════════════════════


══════════════════════════════════════════════════════
  MOI REQUEST — Chay truoc tat ca, khong ngoai le
══════════════════════════════════════════════════════

[Security Check]
    class  : SecurityConfig
    method : filterChain(HttpSecurity http)
    file   : config/SecurityConfig.java
    ├── URL "/" "/register" "/login" "/search" "/product/**"
    │   "/api/**" "/images/**" "/css/**" "/js/**"
    │   └── .permitAll() → khong can dang nhap
    │
    ├── URL "/admin/**"
    │   └── .hasRole("ADMIN") → chi Admin vao duoc
    │
    └── URL con lai ("/cart/**" "/checkout" "/orders" "/profile/**")
        └── .authenticated() → phai dang nhap
            └── Chua login → Spring Security tu redirect /login

[GlobalControllerAdvice — chay song song moi request, inject vao header]
    class  : GlobalControllerAdvice
    file   : controller/GlobalControllerAdvice.java
    ├── addGlobalUsernameToModel()
    │    └── principal.getName() → "username" → hien ten tren header
    ├── addGlobalAvatarToModel()
    │    └── userRepository → user.getUserAvatar().getPath()
    │        → "globalAvatar" → hien anh dai dien tren header
    └── addCartCountToModel()
         └── cartRepository.findByUserId() → cart.getItems().size()
             → "cartCount" → so do tren icon gio hang header


══════════════════════════════════════════════════════
  BUOC 1 — VAO TRANG CHU
══════════════════════════════════════════════════════

[GET /]
    class  : HomeController
    method : home()
    file   : controller/HomeController.java   (dong 52)
    │
    ├── Doc param ?cat= va ?page= (mac dinh page=0)
    ├── PageRequest.of(page, 50) → moi trang toi da 50 san pham
    │
    ├── Neu khong co ?cat  → productRepository.findAll(pageable)
    ├── Neu ?cat=cpu       → cpuRepository.findAll(pageable)
    ├── Neu ?cat=ram       → ramRepository.findAll(pageable)
    ├── Neu ?cat=vga       → vgaRepository.findAll(pageable)
    ├── Neu ?cat=storage   → storageRepository.findAll(pageable)
    ├── Neu ?cat=mainboard → mainboardRepository.findAll(pageable)
    ├── Neu ?cat=casepc    → casepcRepository.findAll(pageable)
    └── Neu ?cat=psu       → psuRepository.findAll(pageable)
         │
         ├── model("cpus")        = productPage.getContent()
         ├── model("currentPage") = page
         ├── model("totalPages")  = productPage.getTotalPages()
         └── model("catTitle")    = "Vi Xu Ly (CPU)" / "Bo Nho Trong (RAM)" / ...

    ├── Neu request AJAX (X-Requested-With: XMLHttpRequest)
    │   └── return "store/product-list"
    │        VIEW: templates/store/product-list.html
    │         └── th:each="cpu : ${cpus}" → ve tung the san pham (anh, ten, gia, nut)
    │
    └── Neu request binh thuong
        └── return "store/home"
             VIEW: templates/store/home.html
              ├── INCLUDE store/fragments/header :: header-css  → /css/store.css
              ├── INCLUDE store/fragments/header :: header-body → thanh header
              │    (logo, search bar, gio hang badge, menu tai khoan)
              ├── INCLUDE store/product-list → luoi san pham
              └── INCLUDE store/fragments/header :: header-js   → /js/store.js

[GET /api/search?keyword=RTX]    ← AJAX tu o tim kiem real-time
    class  : HomeController
    method : searchApi()
    file   : controller/HomeController.java   (dong 175)
    └── productRepository.findByNameContainingIgnoreCase(keyword)
    └── @ResponseBody → tra ve JSON [{id, name, price, image}, ...]
        → hien dropdown goi y khi go


══════════════════════════════════════════════════════
  BUOC 2 — DANG KY TAI KHOAN
══════════════════════════════════════════════════════

[GET /register]
    class  : AuthController
    method : showRegisterForm()
    file   : controller/AuthController.java   (dong 32)
    ├── model.addAttribute("user", new UserRegisterDTO())
    └── return "user/register"
         VIEW: templates/user/register.html
          └── form th:object="${user}" → bind vao UserRegisterDTO

[POST /register]
    class  : AuthController
    method : registerUser()
    file   : controller/AuthController.java   (dong 39)
    │
    ├── @Valid validate UserRegisterDTO (file: dto/UserRegisterDTO.java):
    │    ├── username : 4-20 ky tu, chi chu/so  @Size @Pattern
    │    ├── password : toi thieu 6 ky tu         @Size
    │    ├── email    : dung dinh dang            @Email
    │    └── phone    : 10 so, Viettel/Mobi/Vina  @Pattern
    │
    ├── Check trung DB:
    │    ├── userRepository.existsByUsername(dto.getUsername())
    │    ├── userRepository.existsByPhone(dto.getPhone())
    │    └── userRepository.existsByEmail(dto.getEmail())
    │
    ├── bindingResult.hasErrors()?
    │    └── return "user/register"  ← quay ve form, Thymeleaf hien loi do
    │
    └── Hop le → tao User moi:
         ├── passwordEncoder.encode(password) → BCrypt hash
         │    (chuoi "$2a$10$..." luu vao DB, khong luu mat khau goc)
         ├── roleRepository.findByName("ROLE_USER") → gan quyen
         ├── userRepository.save(user) → INSERT bang `user`
         └── return "redirect:/login?success"


══════════════════════════════════════════════════════
  BUOC 3 — DANG NHAP
══════════════════════════════════════════════════════

[GET /login]
    class  : AuthController
    method : showLoginForm()
    file   : controller/AuthController.java   (dong 82)
    └── return "user/login"
         VIEW: templates/user/login.html
          ├── th:if="${param.error}"   → hien "Sai mat khau"
          └── th:if="${param.success}" → hien "Dang ky thanh cong!"

[POST /login]  ← Spring Security TU XU LY, KHONG qua Controller
    class  : CustomUserDetailsService
    method : loadUserByUsername(username)
    file   : service/CustomUserDetailsService.java   (dong 25)
    ├── userRepository.findByUsername(username)
    │    └── Khong tim thay → UsernameNotFoundException → redirect /login?error
    │
    └── Tim thay → tra ve UserDetails(username, bcryptHash, [ROLE_USER/ADMIN])

    → Spring Security tu goi: BCryptPasswordEncoder.matches(raw, hash)
         ├── Khop  → Tao SecurityContext → redirect "/" (trang chu)
         └── Sai   → redirect "/login?error"


══════════════════════════════════════════════════════
  BUOC 4 — XEM CHI TIET SAN PHAM
══════════════════════════════════════════════════════

[GET /product/{id}]
    class  : HomeController
    method : productDetail()
    file   : controller/HomeController.java   (dong 151)
    ├── productRepository.findById(id)   ← tim duoc MOI loai linh kien
    │    └── Null → return "redirect:/"
    ├── model.addAttribute("cpu", product)   ← ten bien "cpu" du la RAM/VGA/...
    └── return "store/product-detail"
         VIEW: templates/store/product-detail.html
          ├── Hien anh san pham (gallery nhieu anh, nut next/prev)
          ├── Ten, ma SP, thuong hieu, badge Con/Het hang
          ├── Thong so ky thuat (phan nhanh theo class):
          │    ├── th:if="${cpu.class.simpleName == 'Cpu'}"       → cores, socket, threads
          │    ├── th:if="${cpu.class.simpleName == 'Ram'}"       → capacity, ramStandard
          │    ├── th:if="${cpu.class.simpleName == 'Vga'}"       → vram, gpuModel, series
          │    ├── th:if="${cpu.class.simpleName == 'Mainboard'}" → socket, chipset, ramStandard
          │    ├── th:if="${cpu.class.simpleName == 'Casepc'}"    → formFactor, brand
          │    ├── th:if="${cpu.class.simpleName == 'Psu'}"       → wattage, formFactor
          │    └── th:if="${cpu.class.simpleName == 'Storage'}"   → capacity, type
          ├── Gia: ${cpu.price}
          ├── Nut "Them vao gio hang" (an voi Admin):
          │    └── form POST /cart/add  (productId, quantity=1)
          └── Nut "Mua ngay" (an voi Admin):
               └── form POST /cart/buy-now  (productId, quantity=1)


══════════════════════════════════════════════════════
  BUOC 5 — THEM VAO GIO HANG
══════════════════════════════════════════════════════

[POST /cart/add?productId=15&quantity=1]
    → Security check: /cart/** can authenticated
         └── Chua login → redirect /login
         └── Da login   → tiep tuc

    class  : CartController
    method : addToCart()
    file   : controller/CartController.java   (dong 48)
    │
    ├── userRepository.findByUsername(principal.getName())
    ├── productRepository.findById(productId)
    │
    ├── cartRepository.findByUserId(user.getId())
    │    ├── Tim thay → dung cart cu
    │    └── Khong co → new Cart(), cart.setUser(user)
    │
    ├── cart.getItems().stream()
    │    .filter(item → item.getProduct().getId() == productId)
    │    .findFirst()
    │    ├── Da co trong gio → item.setQuantity(cu + moi)
    │    └── Chua co         → new CartItem(product, quantity) → them vao list
    │
    ├── cartRepository.save(cart) → Hibernate INSERT/UPDATE bang cart + cart_item
    ├── redirect.addFlashAttribute("successMessage", "Da them [ten SP] vao gio!")
    └── return "redirect:" + request.getHeader("Referer")  ← ve trang cu

[POST /cart/buy-now]
    class  : CartController
    method : buyNow()
    file   : controller/CartController.java   (dong 94)
    ├── Logic them gio giong het addToCart()
    └── return "redirect:/cart"    ← khac: chuyen thang sang trang gio


══════════════════════════════════════════════════════
  BUOC 6 — XEM GIO HANG
══════════════════════════════════════════════════════

[GET /cart]
    class  : CartController
    method : viewCart()
    file   : controller/CartController.java   (dong 34)
    ├── userRepository.findByUsername(principal.getName())
    ├── cartRepository.findByUserId(user.getId()).orElse(new Cart())
    ├── model.addAttribute("cart", cart)
    └── return "store/cart"
         VIEW: templates/store/cart.html
          ├── Gio trong → hien "Chua co san pham" + link ve trang chu
          └── Co hang → hien bang:
               ├── th:each="item : ${cart.items}" → tung dong san pham
               │    ├── Anh, ten, don gia, so luong, thanh tien
               │    ├── Nut [+/-] → form POST /cart/update (itemId, quantity)
               │    └── Nut [Xoa] → form POST /cart/remove/{itemId}
               ├── Tong tien: ${cart.totalPrice}  ← tinh trong Cart.java
               └── Nut "Thanh toan" → link GET /checkout

[POST /cart/update?itemId=3&quantity=2]
    class  : CartController
    method : updateQuantity()
    file   : controller/CartController.java   (dong 149)
    ├── Math.max(1, quantity)  → chan so am / bang 0
    └── cartRepository.save(cart) → redirect /cart

[POST /cart/remove/{itemId}]
    class  : CartController
    method : removeItem()
    file   : controller/CartController.java   (dong 132)
    ├── cart.getItems().removeIf(item.getId() == itemId)
    └── cartRepository.save(cart)
         └── orphanRemoval=true → Hibernate tu DELETE dong do trong DB
    → redirect /cart


══════════════════════════════════════════════════════
  BUOC 7 — TRANG THANH TOAN
══════════════════════════════════════════════════════

[GET /checkout]
    class  : OrderController
    method : showCheckoutPage()
    file   : controller/OrderController.java   (dong 36)
    │
    ├── cartRepository.findByUserId(user.getId())
    │    └── Gio trong → flash "Gio hang dang trong!" → redirect /cart
    │
    ├── userAddressRepository.findByUserIdAndIsDefaultTrue(userId)
    │    ├── Co dia chi mac dinh → dien san vao form
    │    └── Chua co → form trong, user tu dien
    │
    ├── model("cart", cart)
    ├── model("user", user)
    ├── model("defaultAddress", defaultAddress)
    └── return "store/checkout"
         VIEW: templates/store/checkout.html
          ├── Col TRAI: form thong tin nguoi nhan
          │    ├── receiverName    (dien san: defaultAddress.receiverName || user.fullName)
          │    ├── receiverPhone   (dien san: defaultAddress.phone || user.phone)
          │    ├── receiverEmail   (dien san: user.email)
          │    └── shippingAddress (dien san: defaultAddress.fullAddress)
          │
          └── Col PHAI: preview don hang
               ├── th:each="item : ${cart.items}" → tung san pham
               ├── Tong tien: ${cart.totalPrice}
               └── Nut "DAT HANG NGAY" → form POST /order/create
                    └── Phuong thuc: COD (thanh toan khi nhan hang)


══════════════════════════════════════════════════════
  BUOC 8 — DAT HANG (TAO ORDER)
══════════════════════════════════════════════════════

[POST /order/create]
    class  : OrderController
    method : createOrder()
    file   : controller/OrderController.java   (dong 60)
    │
    ├── Nhan params tu form: receiverName, receiverPhone, receiverEmail, shippingAddress
    └── orderService.createOrderFromCart(user, ...)
         │
         class  : OrderServiceImpl
         method : createOrderFromCart()
         file   : service/impl/OrderServiceImpl.java   (dong 31)
         @Transactional  ← tat ca thanh cong hoac rollback het
         │
         ├── [1] Validate gio hang khong rong
         │
         ├── [2] Luu/cap nhat dia chi:
         │    ├── Chua co dia chi mac dinh
         │    │    └── new UserAddress() → isDefault=true → save()
         │    └── Da co → cap nhat receiverName, phone, fullAddress → save()
         │
         ├── [3] Tao Order moi:
         │    ├── order.setStatus(OrderStatus.PENDING)    ← "Cho xac nhan"
         │    ├── order.setOrderDate(LocalDateTime.now())
         │    └── order.setTotalPrice(cart.getTotalPrice())
         │
         ├── [4] Vong lap tung CartItem:
         │    ├── Kiem tra: product.getStock() < quantity?
         │    │    └── Thieu hang → throw RuntimeException → @Transactional ROLLBACK
         │    ├── Tru stock: product.setStock(stock - quantity)
         │    ├── productRepository.save(product)
         │    └── Tao OrderItem:
         │         ├── setQuantity(cartItem.getQuantity())
         │         └── setPrice(product.getPrice())  ← CHUP GIA TAI THOI DIEM DAT HANG
         │
         ├── [5] orderRepository.save(order)
         │    └── CascadeType.ALL → tu luu luon danh sach OrderItem theo
         │
         └── [6] Xoa sach gio hang:
              ├── cart.getItems().clear()
              └── cartRepository.save(cart)
                   └── orphanRemoval=true → Hibernate DELETE toan bo cart_item

    ├── Thanh cong:
    │    ├── flash "Dat hang thanh cong! Ma don #15"
    │    └── return "redirect:/orders"
    │
    └── That bai (het hang / loi DB):
         ├── flash "Loi dat hang: ..."
         └── return "redirect:/checkout"


══════════════════════════════════════════════════════
  BUOC 9 — XEM DON HANG DANG XU LY
══════════════════════════════════════════════════════

[GET /orders]
    class  : OrderController
    method : viewUserOrders()
    file   : controller/OrderController.java   (dong 83)
    └── orderService.getActiveUserOrders(user.getId())
         class  : OrderServiceImpl
         method : getActiveUserOrders()
         file   : service/impl/OrderServiceImpl.java   (dong 132)
         └── orderRepository.findByUserIdAndStatusInOrderByOrderDateDesc(
                  userId,
                  [PENDING, APPROVED, SHIPPING]  ← KHONG lay DELIVERED, REJECTED
             )
    └── return "store/orders"
         VIEW: templates/store/orders.html
          └── th:each="order : ${orders}"
               ├── Badge trang thai:
               │    ├── PENDING   → cam   "Cho xac nhan"
               │    ├── APPROVED  → xanh lam  "Da duyet"
               │    ├── SHIPPING  → xanh duong "Dang giao"
               │    ├── DELIVERED → xanh la    "Da nhan hang"
               │    └── REJECTED  → do         "Khong duyet"
               ├── Danh sach san pham trong don:
               │    └── item.price * item.quantity  ← GIA CO DINH LUC DAT, khong doi
               └── Nut "Xac nhan da nhan hang":
                    └── th:if="${order.status.name() == 'SHIPPING'}"
                        → Chi hien khi dang o SHIPPING
                        → form POST /order/{id}/confirm-received


══════════════════════════════════════════════════════
  BUOC 10 — XAC NHAN NHAN HANG (KET THUC LUONG)
══════════════════════════════════════════════════════

[POST /order/{id}/confirm-received]
    class  : OrderController
    method : confirmReceived()
    file   : controller/OrderController.java   (dong 97)
    ├── orderService.getOrderById(orderId)
    ├── Kiem tra: order.getStatus() == SHIPPING?
    │    ├── Dung → orderService.updateOrderStatus(orderId, DELIVERED)
    │    │    └── OrderServiceImpl.updateOrderStatus() (dong 98)
    │    │         └── order.setStatus(DELIVERED) → orderRepository.save()
    │    │    flash "Xac nhan nhan hang thanh cong! Cam on ban da mua sam."
    │    └── Sai  → flash "Khong the xac nhan don hang nay."
    └── return "redirect:/orders"

    Sau khi DELIVERED:
         ├── Don BIEN MAT khoi /orders
         │    (getActiveUserOrders chi lay PENDING/APPROVED/SHIPPING)
         └── Don xuat hien o /profile/orders (lich su)
              class  : ProfileController
              method : showOrders()
              └── orderService.getCompletedUserOrders()
                   └── findByUserIdAndStatusIn([DELIVERED, REJECTED])
              └── VIEW: templates/profile/orders.html


══════════════════════════════════════════════════════
  VONG DOI TRANG THAI DON HANG
══════════════════════════════════════════════════════

                Admin duyet              Admin giao van
  PENDING  ─────────────────►  APPROVED ──────────────► SHIPPING
     │                                                       │
     │                                               User xac nhan
     │                                                       ▼
     └── Admin tu choi ──► REJECTED              DELIVERED ✓
                                │
                     Stock duoc HOAN LAI tu dong
                     OrderServiceImpl.updateOrderStatus():
                         product.setStock(stock + item.quantity)


══════════════════════════════════════════════════════
  TOM TAT FILE QUAN TRONG
══════════════════════════════════════════════════════

  Controller (nhan request → dieu phoi → goi service → tra view):
    controller/HomeController.java           → trang chu, chi tiet SP, tim kiem
    controller/AuthController.java           → dang ky, form dang nhap
    controller/CartController.java           → gio hang
    controller/OrderController.java          → checkout, tao don, xem don
    controller/AdminController.java          → dashboard admin
    controller/AdminOrderController.java     → admin quan ly don hang
    controller/ProfileController.java        → trang ca nhan, dia chi, doi mat khau
    controller/GlobalControllerAdvice.java   → inject username/avatar/cartCount

  Service (xu ly nghiep vu):
    service/CustomUserDetailsService.java    → Spring Security dang nhap
    service/impl/OrderServiceImpl.java       → tao don, cap nhat trang thai

  Config (cau hinh):
    config/SecurityConfig.java              → phan quyen URL
    config/DatabaseInitializer.java         → tao tai khoan admin mac dinh khi khoi dong

  View (giao dien Thymeleaf):
    templates/store/home.html               → trang chu
    templates/store/product-list.html       → luoi san pham (dung chung + AJAX)
    templates/store/product-detail.html     → chi tiet san pham
    templates/store/cart.html               → gio hang
    templates/store/checkout.html           → thanh toan
    templates/store/orders.html             → don hang dang xu ly
    templates/store/fragments/header.html   → header dung chung moi trang
    templates/user/register.html            → form dang ky
    templates/user/login.html               → form dang nhap
    templates/profile/orders.html           → lich su don (DELIVERED / REJECTED)
