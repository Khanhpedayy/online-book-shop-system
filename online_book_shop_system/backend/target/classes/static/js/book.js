const API = "/api"

const USER_ID = 1

const headerCartCount = document.getElementById("headerCartCount")

async function apiGet(url){
    const r = await fetch(url)
    return r.json()
}

async function apiPost(url,data){
    const r = await fetch(url,{
        method:"POST",
        headers:{
            "Content-Type":"application/json"
        },
        body:JSON.stringify(data)
    })
    return r.json()
}

async function loadBook(){

    const params = new URLSearchParams(window.location.search)

    const id = params.get("id")

    if(!id) return

    const book = await apiGet(API + "/books/" + id)
    document.getElementById("title").textContent = book.title
    document.getElementById("breadcrumbTitle").textContent = book.title
    document.getElementById("isbn").textContent = book.isbn13 || "N/A"
    document.getElementById("publisherName").textContent = book.publisherName || "N/A"
    document.getElementById("publicationYear").textContent = book.publicationYear || "N/A"
    document.getElementById("description").textContent = book.description || ""
    const variants = book.variants || []

    const variantSelect = document.getElementById("variantSelect")

    variantSelect.innerHTML = ""

    let selectedVariant = null

    variants.forEach(v => {

        const option = document.createElement("option")

        option.value = v.id
        option.textContent = v.sku + " - $" + v.salePrice

        variantSelect.appendChild(option)

        if(!selectedVariant) selectedVariant = v

    })

    if(selectedVariant){

        document.getElementById("price").textContent = "$" + selectedVariant.salePrice

        document.getElementById("stockStatus").textContent =
            selectedVariant.stockQuantity > 0
                ? "In Stock (" + selectedVariant.stockQuantity + ")"
                : "Out of Stock"

    }

    const img =
        "https://covers.openlibrary.org/b/isbn/" +
        (book.isbn13 || "0385533229") +
        "-L.jpg"

    document.getElementById("bookImage").src = img

    document.getElementById("addCartBtn").onclick = addToCart

    loadRelated(book.category)

}

async function addToCart(){

    const variantId = document.getElementById("variantSelect").value

    const qty = document.getElementById("qty").value

    await apiPost(`/api/cart/user/${USER_ID}/items`,{
        variantId:variantId,
        quantity:qty
    })

    alert("Added to cart")

    loadCartCount()

}

async function loadCart() {
    if (cartContainer) cartContainer.innerHTML = 'Loading…';
    try {
        const items = await apiGet(`/api/cart/user/${USER_ID}`);
        if (!Array.isArray(items) || items.length === 0) {
            if (cartContainer) cartContainer.innerHTML = 'Your cart is empty.';
            updateHeaderCart(0, 0);
            return;
        }
        let total = 0;
        if (cartContainer) cartContainer.innerHTML = '';
        items.forEach((ci) => {
            const v = ci.variant;
            const title = v?.book?.title ?? v?.sku ?? '(unknown)';
            const price = v?.salePrice ?? 0;
            const lineTotal = price * ci.quantity;
            total += lineTotal;
            const div = document.createElement('div');
            div.className = 'cart-item';
            div.innerHTML = `
        <span>${title.replace(/</g, '&lt;')} × ${ci.quantity} @ $${Number(price).toFixed(2)} = $${lineTotal.toFixed(2)}</span>
        <button type="button" class="btn btn-secondary" data-remove="${v?.id}">Remove</button>
      `;
            if (cartContainer) cartContainer.appendChild(div);
        });

        if (cartContainer) cartContainer.querySelectorAll('button[data-remove]').forEach((btn) => {
            btn.addEventListener('click', async () => {
                const variantId = Number(btn.getAttribute('data-remove'));
                try {
                    await apiDelete(`/api/cart/user/${USER_ID}/items/${variantId}`);
                    await loadCart();
                } catch (e) {
                    console.error(e);
                    alert(e.message);
                }
            });
        });

        updateHeaderCart(total, items.length);
    } catch (e) {
        console.error(e);
        if (cartContainer) cartContainer.innerHTML = 'Error loading cart: ' + e.message.replace(/</g, '&lt;');
    }
}

async function loadCartCount(){

    const items = await apiGet(`/api/cart/user/${USER_ID}`)

    if(!Array.isArray(items)){
        headerCartCount.textContent = "0"
        return
    }

    let count = 0

    items.forEach(i => count += i.quantity)

    headerCartCount.textContent = count

}

function updateHeaderCart(total, count) {
    if (!headerCartCount) return;
    headerCartCount.textContent = '$ ' + (total != null ? Number(total).toFixed(2) : '0.00') + ' (' + (count || 0) + ')';
}

async function loadRelated(category){

    const books = await apiGet("/api/books")

    const filtered = books
        .filter(b => b.category === category)
        .slice(0,4)

    const grid = document.getElementById("relatedBooks")

    grid.innerHTML=""

    filtered.forEach(b=>{

        const card = document.createElement("div")

        card.className="card"

        card.innerHTML = `
<img src="https://covers.openlibrary.org/b/isbn/${b.isbn}-M.jpg">
<p>${b.title}</p>
`

        card.onclick = () =>
            window.location = "book.html?id="+b.id

        grid.appendChild(card)

    })

}

loadBook()
loadCartCount()