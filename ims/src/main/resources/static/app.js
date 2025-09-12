document.addEventListener("DOMContentLoaded", async () => {
  const API_BASE_URL = "http://localhost:8080/api";
  const mainContent = document.getElementById("main-content");
  const navLinks = document.querySelectorAll(".nav-link");
  const navIndicator = document.querySelector(".nav-indicator");
  let currentUser;

  // --- UTILITY & GENERIC FUNCTIONS ---
  const apiFetch = (url, options = {}) => {
    // The browser automatically handles session cookies for authentication.
    return fetch(url, {
      ...options,
      headers: { "Content-Type": "application/json", ...options.headers },
    });
  };

  const showNotification = (message, type = "info") => {
    const notification = document.createElement("div");
    notification.textContent = message;
    notification.style.cssText = `position: fixed; top: 20px; right: 20px; padding: 1rem 1.5rem; border-radius: 8px; color: white; font-weight: 600; z-index: 1000; box-shadow: 0 4px 6px rgba(0,0,0,0.1);`;
    notification.style.background =
      type === "success" ? "#48bb78" : type === "error" ? "#f56565" : "#4299e1";
    document.body.appendChild(notification);
    gsap.fromTo(
      notification,
      { x: "110%", opacity: 0 },
      {
        x: "0%",
        opacity: 1,
        duration: 0.5,
        ease: "power2.out",
        onComplete: () => {
          gsap.to(notification, {
            x: "110%",
            opacity: 0,
            duration: 0.5,
            delay: 2.5,
            onComplete: () => notification.remove(),
          });
        },
      }
    );
  };

  const setupUIForRoles = () => {
    const isAdmin = currentUser && currentUser.roles.includes("ROLE_ADMIN");
    const adminOnlyElements = document.querySelectorAll(".admin-only");
    if (!isAdmin) {
      adminOnlyElements.forEach((el) => (el.style.display = "none"));
    }
  };

  // --- NAVIGATION ---
  const updateNavIndicator = (activeLink) => {
    if (!activeLink) return;
    const rect = activeLink.getBoundingClientRect();
    const navRect = activeLink.parentNode.getBoundingClientRect();
    gsap.to(navIndicator, {
      duration: 0.4,
      ease: "power2.out",
      width: rect.width,
      height: rect.height,
      x: rect.left - navRect.left,
    });
  };

  const showPage = async (pageId = "dashboard") => {
    const activeLink = document.querySelector(`.nav-link[data-page="${pageId}"]`);

    // Update navigation
    navLinks.forEach((l) => l.classList.remove("active"));
    if (activeLink) activeLink.classList.add("active");

    updateNavIndicator(activeLink);

    try {
      // Fetch the page content from the pages directory
      console.log(pageId);
      const response = await fetch(`pages/${pageId}.html`);
      if (!response.ok) {
        throw new Error(`Failed to load page: ${pageId}`);
      }

      const pageContent = await response.text();
      console.log(pageContent)
      mainContent.innerHTML = pageContent;

      mainContent.querySelector('.page')?.classList.add('active');

      // Animate page transition
      gsap.fromTo(
        mainContent,
        { opacity: 0, y: 20 },
        { opacity: 1, y: 0, duration: 0.5, ease: "power2.out" }
      );

      // Initialize page-specific functionality after content is loaded
      if (pageId === "dashboard") initDashboardPage();
      else if (pageId === "products") initProductsPage();
      else if (pageId === "promotions") initPromotionsPage();
      else if (pageId === "orders") initOrdersPage();

      setupUIForRoles();

    } catch (error) {
      console.error(`Error loading page ${pageId}:`, error);
      mainContent.innerHTML = `
        <div class="error-page">
          <h2>Error Loading Page</h2>
          <p>Unable to load the ${pageId} page. Please try again.</p>
          <p>Error: ${error.message}</p>
        </div>
      `;
    }
  };

  // --- PAGE INITIALIZERS ---
  async function initDashboardPage() {
    const lowStockAlertsUl = document.getElementById("lowStockAlerts");
    const totalProductsSpan = document.getElementById("totalProducts");
    const totalValueSpan = document.getElementById("totalValue");
    const pendingOrdersSpan = document.getElementById("pendingOrders");

    if (!lowStockAlertsUl || !totalProductsSpan || !totalValueSpan || !pendingOrdersSpan) {
      console.warn("Dashboard elements not found");
      return;
    }

    try {
      const statsResponse = await apiFetch(`${API_BASE_URL}/analytics/dashboard-stats`);
      const stats = await statsResponse.json();
      totalProductsSpan.textContent = stats.totalProducts || 0;
      totalValueSpan.textContent = (stats.totalInventoryValue || 0).toFixed(2);
      pendingOrdersSpan.textContent = stats.pendingOrders || 0;

      const alertsResponse = await apiFetch(`${API_BASE_URL}/products/low-stock`);
      const alerts = await alertsResponse.json();
      lowStockAlertsUl.innerHTML = alerts.length
        ? alerts.map((p) => `<li>${p.name} - Only ${p.quantity} left</li>`).join("")
        : "<li>No low stock alerts</li>";
    } catch (error) {
      console.error("Error loading dashboard data:", error);
      // Set default values if API fails
      totalProductsSpan.textContent = "0";
      totalValueSpan.textContent = "0.00";
      pendingOrdersSpan.textContent = "0";
      lowStockAlertsUl.innerHTML = "<li>Unable to load alerts</li>";
    }
  }

  async function initProductsPage() {
    const productsTbody = document.getElementById("productsTbody");
    const addProductForm = document.getElementById("addProductForm");
    const createBundleForm = document.getElementById("createBundleForm");
    const importForm = document.getElementById("importForm");

    const fetchAndRenderProducts = async () => {
      if (!productsTbody) return;

      try {
        const response = await apiFetch(`${API_BASE_URL}/products`);
        const products = await response.json();
        productsTbody.innerHTML = products
          .map(
            (p) =>
              `<tr><td>${p.id}</td><td>${p.name}</td><td>$${p.price.toFixed(
                2
              )}</td><td>${p.quantity}</td><td>${
                p.isBundle ? "Yes" : "No"
              }</td></tr>`
          )
          .join("");
      } catch (error) {
        console.error("Error fetching products:", error);
        productsTbody.innerHTML = "<tr><td colspan='5'>Error loading products</td></tr>";
      }
    };

    if (addProductForm) {
      addProductForm.addEventListener("submit", async function (e) {
        e.preventDefault();
        try {
          const data = {
            name: document.getElementById("name").value,
            description: document.getElementById("description").value,
            price: parseFloat(document.getElementById("price").value),
            quantity: parseInt(document.getElementById("quantity").value),
            size: document.getElementById("size").value,
            color: document.getElementById("color").value,
          };
          await apiFetch(`${API_BASE_URL}/products`, {
            method: "POST",
            body: JSON.stringify(data),
          });
          this.reset();
          fetchAndRenderProducts();
          showNotification("Product added!", "success");
        } catch (error) {
          console.error("Error adding product:", error);
          showNotification("Error adding product", "error");
        }
      });
    }

    if (createBundleForm) {
      createBundleForm.addEventListener("submit", async function (e) {
        e.preventDefault();
        try {
          const bundleId = parseInt(document.getElementById("bundleProductId").value);
          const componentIds = document
            .getElementById("componentProductIds")
            .value.split(",")
            .map((id) => parseInt(id.trim()));
          const data = {
            bundleProductId: bundleId,
            componentProductIds: componentIds,
          };
          await apiFetch(`${API_BASE_URL}/products/bundles`, {
            method: "POST",
            body: JSON.stringify(data),
          });
          this.reset();
          fetchAndRenderProducts();
          showNotification("Bundle created!", "success");
        } catch (error) {
          console.error("Error creating bundle:", error);
          showNotification("Error creating bundle", "error");
        }
      });
    }

    if (importForm) {
      importForm.addEventListener("submit", async function (e) {
        e.preventDefault();
        const file = document.getElementById("csvFile").files[0];
        if (!file) return showNotification("Please select a file", "error");
        try {
          const formData = new FormData();
          formData.append("file", file);
          const response = await fetch(`${API_BASE_URL}/products/import`, {
            method: "POST",
            body: formData,
          });
          if (!response.ok) {
            showNotification("Import failed!", "error");
            return;
          }
          this.reset();
          fetchAndRenderProducts();
          showNotification("Products imported!", "success");
        } catch (error) {
          console.error("Error importing products:", error);
          showNotification("Import failed!", "error");
        }
      });
    }

    await fetchAndRenderProducts();
  }

  async function initPromotionsPage() {
    const promotionsTbody = document.getElementById("promotionsTbody");
    const addPromotionForm = document.getElementById("addPromotionForm");
    const addTieredPromotionForm = document.getElementById("addTieredPromotionForm");
    const addTierBtn = document.getElementById("addTierBtn");
    const currentTiersList = document.getElementById("currentTiersList");
    let currentTiers = [];

    const renderCurrentTiers = () => {
      if (!currentTiersList) return;
      currentTiersList.innerHTML = "";
      currentTiers.forEach((tier, index) => {
        currentTiersList.innerHTML += `<li>Qty ${
          tier.minQuantity
        }+ costs $${tier.pricePerItem.toFixed(
          2
        )}<button type="button" class="remove-tier-btn" data-index="${index}" style="margin-left: 10px; background-color: #e74c3c;">X</button></li>`;
      });
    };

    const fetchAndRenderPromotions = async () => {
      if (!promotionsTbody) return;

      try {
        const response = await apiFetch(`${API_BASE_URL}/promotions`);
        const promotions = await response.json();
        promotionsTbody.innerHTML = "";
        promotions.forEach((p) => {
          let details =
            p.promotionType === "TIERED"
              ? `<ul>${p.discountTiers
                  .map(
                    (t) =>
                      `<li>Qty ${t.minQuantity}+ @ $${t.pricePerItem.toFixed(
                        2
                      )}</li>`
                  )
                  .join("")}</ul>`
              : `Value: ${p.discountValue}`;
          promotionsTbody.innerHTML += `<tr><td>${p.id}</td><td>${p.description}</td><td>${p.promotionType}</td><td>${details}</td><td>${p.applicableProduct.name} (ID: ${p.applicableProduct.id})</td></tr>`;
        });
      } catch (error) {
        console.error("Error fetching promotions:", error);
        promotionsTbody.innerHTML = "<tr><td colspan='5'>Error loading promotions</td></tr>";
      }
    };

    if (addPromotionForm) {
      addPromotionForm.addEventListener("submit", async function (e) {
        e.preventDefault();
        try {
          const data = {
            description: document.getElementById("promoDescription").value,
            promotionType: document.getElementById("promoType").value,
            discountValue: parseFloat(document.getElementById("promoValue").value),
            applicableProductId: parseInt(document.getElementById("promoProductId").value),
          };
          await apiFetch(`${API_BASE_URL}/promotions`, {
            method: "POST",
            body: JSON.stringify(data),
          });
          this.reset();
          fetchAndRenderPromotions();
          showNotification("Promotion added!", "success");
        } catch (error) {
          console.error("Error adding promotion:", error);
          showNotification("Error adding promotion", "error");
        }
      });
    }

    if (addTierBtn) {
      addTierBtn.addEventListener("click", () => {
        const minQty = parseInt(document.getElementById("tierMinQty").value);
        const price = parseFloat(document.getElementById("tierPrice").value);
        if (isNaN(minQty) || isNaN(price))
          return showNotification("Fill in tier info.", "error");
        currentTiers.push({ minQuantity: minQty, pricePerItem: price });
        currentTiers.sort((a, b) => a.minQuantity - b.minQuantity);
        renderCurrentTiers();
        document.getElementById("tierMinQty").value = "";
        document.getElementById("tierPrice").value = "";
      });
    }

    if (currentTiersList) {
      currentTiersList.addEventListener("click", (e) => {
        if (e.target.classList.contains("remove-tier-btn")) {
          currentTiers.splice(parseInt(e.target.dataset.index), 1);
          renderCurrentTiers();
        }
      });
    }

    if (addTieredPromotionForm) {
      addTieredPromotionForm.addEventListener("submit", async function (e) {
        e.preventDefault();
        if (currentTiers.length === 0)
          return showNotification("Add at least one tier.", "error");
        try {
          const data = {
            description: document.getElementById("tieredPromoDesc").value,
            applicableProductId: parseInt(document.getElementById("tieredPromoProductId").value),
            tiers: currentTiers,
          };
          await apiFetch(`${API_BASE_URL}/promotions/tiered`, {
            method: "POST",
            body: JSON.stringify(data),
          });
          showNotification("Tiered promotion created!", "success");
          this.reset();
          currentTiers = [];
          renderCurrentTiers();
          fetchAndRenderPromotions();
        } catch (error) {
          console.error("Error creating tiered promotion:", error);
          showNotification("Error creating tiered promotion", "error");
        }
      });
    }

    await fetchAndRenderPromotions();
  }

  async function initOrdersPage() {
    const purchaseOrdersTbody = document.getElementById("purchaseOrdersTbody");
    const addItemForm = document.getElementById("addItemForm");
    const currentOrderItemsUl = document.getElementById("currentOrderItems");
    const placeOrderBtn = document.getElementById("placeOrderBtn");
    let currentOrderItems = [];

    const renderCurrentOrder = () => {
      if (!currentOrderItemsUl) return;
      currentOrderItemsUl.innerHTML = "";
      currentOrderItems.forEach((item) => {
        currentOrderItemsUl.innerHTML += `<li>${item.quantity} x Product ID: ${item.productId}</li>`;
      });
    };

    const fetchAndRenderPurchaseOrders = async () => {
      if (!purchaseOrdersTbody) return;

      try {
        const response = await apiFetch(`${API_BASE_URL}/purchaseorders`);
        const orders = await response.json();
        purchaseOrdersTbody.innerHTML = "";
        orders.forEach((o) => {
          const itemsHtml = o.items
            .map((item) => `<li>${item.quantity} x ${item.productName}</li>`)
            .join("");
          const actionButton =
            o.status === "PENDING"
              ? `<button class="complete-btn" data-order-id="${o.id}">Mark as Completed</button>`
              : "";
          purchaseOrdersTbody.innerHTML += `<tr><td>${o.id}</td><td>${new Date(
            o.orderDate
          ).toLocaleString()}</td><td>${
            o.status
          } ${actionButton}</td><td><ul>${itemsHtml}</ul></td></tr>`;
        });
        setupUIForRoles();
      } catch (error) {
        console.error("Error fetching purchase orders:", error);
        purchaseOrdersTbody.innerHTML = "<tr><td colspan='4'>Error loading orders</td></tr>";
      }
    };

    const handleUpdateStatus = async (orderId, newStatus) => {
      try {
        await apiFetch(`${API_BASE_URL}/purchaseorders/${orderId}/status`, {
          method: "PUT",
          body: newStatus,
        });
        fetchAndRenderPurchaseOrders();
        showNotification("Order status updated!", "success");
      } catch (error) {
        console.error("Error updating order status:", error);
        showNotification("Error updating order status", "error");
      }
    };

    if (addItemForm) {
      addItemForm.addEventListener("submit", (e) => {
        e.preventDefault();
        const pId = parseInt(document.getElementById("poProductId").value);
        const qty = parseInt(document.getElementById("poQuantity").value);
        if (isNaN(pId) || isNaN(qty))
          return showNotification("Enter a valid Product ID and Quantity.", "error");
        currentOrderItems.push({ productId: pId, quantity: qty });
        renderCurrentOrder();
        addItemForm.reset();
      });
    }

    if (placeOrderBtn) {
      placeOrderBtn.addEventListener("click", async () => {
        if (currentOrderItems.length === 0)
          return showNotification("Add items first.", "error");
        try {
          const orderData = { items: currentOrderItems };
          await apiFetch(`${API_BASE_URL}/purchaseorders`, {
            method: "POST",
            body: JSON.stringify(orderData),
          });
          currentOrderItems = [];
          renderCurrentOrder();
          await fetchAndRenderPurchaseOrders();
          await initDashboardPage();
          showNotification("Order placed!", "success");
        } catch (error) {
          console.error("Error placing order:", error);
          showNotification("Error placing order", "error");
        }
      });
    }

    if (purchaseOrdersTbody) {
      purchaseOrdersTbody.addEventListener("click", (e) => {
        if (e.target.classList.contains("complete-btn")) {
          handleUpdateStatus(e.target.dataset.orderId, "COMPLETED");
        }
      });
    }

    await fetchAndRenderPurchaseOrders();
  }

  // --- APP INITIALIZATION ---
  try {
    // Try to get user info, but don't redirect if it fails (for development)
    try {
      const userResponse = await apiFetch(`${API_BASE_URL}/users/me`);
      if (userResponse.ok) {
        currentUser = await userResponse.json();
      } else {
        // For development, create a mock user if API fails
        currentUser = { roles: ["ROLE_ADMIN"] };
        console.warn("Using mock user for development");
      }
    } catch (error) {
      // For development, create a mock user if API fails
      currentUser = { roles: ["ROLE_ADMIN"] };
      console.warn("Using mock user for development", error);
    }

    // Set up navigation
    navLinks.forEach((link) => {
      link.addEventListener("click", (e) => {
        e.preventDefault();
        const pageId = link.dataset.page;
        window.location.hash = pageId;
        showPage(pageId);
      });
    });

    // Handle browser back/forward buttons
    window.addEventListener('hashchange', () => {
      const pageId = window.location.hash.substring(1) || "dashboard";
      showPage(pageId);
    });

    // Show initial page
    const initialPage = window.location.hash.substring(1) || "dashboard";
    await showPage(initialPage);

  } catch (error) {
    console.error("Could not initialize app:", error);
    showNotification("Error initializing application", "error");
  }
});