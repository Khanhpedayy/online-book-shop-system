const API_BASE = 'http://localhost:8080';
const USER_ID = 1;

const booksContainer = document.getElementById('booksContainer');
const cartContainer = document.getElementById("cartContainer");
const headerCartCount = document.getElementById('headerCartCount');
const productCount = document.getElementById('productCount');
const sortSelect = document.getElementById('sortSelect');
const searchInput = document.getElementById('searchInput');

const orderForm = document.getElementById("orderForm");
const orderResult = document.getElementById("orderResult");
const orderEmailInput = document.getElementById("orderEmail");
const orderAddressInput = document.getElementById("orderAddress");
const orderRecipientInput = document.getElementById("orderRecipient");

let currentBooks = [];
let currentPage = 1;
let booksPerPage = 8;

async function apiGet(path){
    const resp = await fetch(`${API_BASE}${path}`);
    if(!resp.ok) throw new Error(await resp.text());
    return resp.json();
}

async function apiPost(path, body){
    const resp = await fetch(`${API_BASE}${path}`,{
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify(body)
    });
    if(!resp.ok) throw new Error(await resp.text());
    return resp.json();
}

async function apiDelete(path) {

    const resp = await fetch(`${API_BASE}${path}`, {
        method: "DELETE"
    });

    if (!resp.ok && resp.status !== 204) {

        const text = await resp.text();

        throw new Error(`DELETE ${path} failed: ${resp.status} ${text}`);

    }

}

function sortBooks(books,value){

    const list=[...books];

    switch(value){

        case 'title-asc':
            list.sort((a,b)=>a.title.localeCompare(b.title));
            break;

        case 'title-desc':
            list.sort((a,b)=>b.title.localeCompare(a.title));
            break;

        case 'price-asc':
            list.sort((a,b)=>(a.salePrice??0)-(b.salePrice??0));
            break;

        case 'price-desc':
            list.sort((a,b)=>(b.salePrice??0)-(a.salePrice??0));
            break;

    }

    return list;
}


function renderBooks(list = currentBooks){

    if(!booksContainer) return;

    let sorted = sortBooks(list, sortSelect.value);

    let start = (currentPage-1)*booksPerPage;
    let end = start+booksPerPage;

    let books = sorted.slice(start,end);

    booksContainer.innerHTML="";

    books.forEach(b=>{

        const price = b.salePrice ?? 0;

        const coverUrl =
            'https://covers.openlibrary.org/b/isbn/' +
            (b.isbn || '0385533229') +
            '-M.jpg';

        booksContainer.innerHTML += `

<div class="book">

<img class="book-cover"
src="${coverUrl}"
onerror="this.src='https://via.placeholder.com/220x330?text=Book'">

<div class="book-info">

<div class="book-title">${b.title}</div>

<div class="book-price">$${price.toFixed(2)}</div>

<div class="book-actions">

<input type="number" value="1" min="1" id="qty-${b.id}">

<button class="btn"
onclick="addToCart(${b.id})">
Add to Cart
</button>

<button class="btn btn-secondary"
onclick="openBook(${b.id})">
Open Book
</button>

</div>

</div>

</div>

`;

    });

    createPagination(sorted.length);

    if(productCount)
        productCount.textContent = sorted.length + " products";

}


function createPagination(totalBooks){

    const pagination = document.getElementById("pagination");

    if(!pagination) return;

    let totalPages = Math.ceil(totalBooks/booksPerPage);

    pagination.innerHTML="";

    for(let i=1;i<=totalPages;i++){

        pagination.innerHTML += `

<button class="page-btn ${i===currentPage?'active':''}"
onclick="changePage(${i})">

${i}

</button>

`;

    }

}


function changePage(page){

    currentPage = page;

    renderBooks();

    window.scrollTo({top:0,behavior:'smooth'});

}


async function loadBooks(filters = {}){

    if(!booksContainer) return;

    booksContainer.innerHTML = "Loading...";

    try{

        let url = "/api/books";

        const params = new URLSearchParams(filters);

        if(params.toString())
            url += "?" + params.toString();

        const books = await apiGet(url);

        const map = new Map();

        books.forEach(b=>{
            if(!map.has(b.bookId)) map.set(b.bookId,b);
        });

        currentBooks = [...map.values()];

        currentPage = 1;

        renderBooks(currentBooks);

    }catch(e){

        booksContainer.innerHTML = "Error loading books";

        console.error(e);

    }

}


function addToCart(id){

    const qtyInput = document.getElementById("qty-"+id);
    const qty = qtyInput ? Number(qtyInput.value) : 1;

    apiPost(`/api/cart/user/${USER_ID}/items`,{
        variantId:id,
        quantity:qty
    })
        .then(()=>loadCart())
        .catch(err=>alert(err.message));

}


if(sortSelect){

    sortSelect.addEventListener("change",()=>{

        currentPage=1;

        renderBooks();

    });

}


if(searchInput){

    document.getElementById("searchBtn").addEventListener("click",()=>{

        const keyword = document.getElementById("searchInput").value.toLowerCase();

        const filtered = currentBooks.filter(b=>
            b.title.toLowerCase().includes(keyword)
        );

        currentPage = 1;

        renderBooks(filtered);

    });

}


loadBooks();
loadCart();

function updateHeaderCart(total, count) {
  if (!headerCartCount) return;
  headerCartCount.textContent = '$ ' + (total != null ? Number(total).toFixed(2) : '0.00') + ' (' + (count || 0) + ')';
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

if (orderForm) orderForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  orderResult.textContent = 'Placing order…';
  orderResult.className = '';
  try {
    const body = {
      email: orderEmailInput.value,
      shippingAddress: orderAddressInput.value,
      recipientName: orderRecipientInput.value,
      customerId: USER_ID,
    };
    const order = await apiPost(`/api/orders/from-cart/${USER_ID}`, body);
    orderResult.textContent = 'Order #' + order.id + ' created. Total: $' + order.totalAmount;
    orderResult.className = 'success';
    await loadCart();
  } catch (err) {
    console.error(err);
    orderResult.textContent = 'Error: ' + err.message;
    orderResult.className = 'error';
  }
});

if (sortSelect) sortSelect.addEventListener('change', () => {
  if (currentBooks.length) renderBooks(currentBooks);
});

document.getElementById("searchInput").addEventListener("input", e=>{
    loadBooks({
        keyword: e.target.value
    });
});

async function loadCategories() {

    const list = document.getElementById("categoryList");

    try {

        const categories = await apiGet("/api/categories");

        list.innerHTML = `
      <li class="active" data-id="">All</li>
    `;

        categories.forEach(c => {
            list.innerHTML += `
        <li data-id="${c.id}">
          ${c.name}
        </li>
      `;
        });

        document.querySelectorAll("#categoryList li").forEach(li => {

            li.addEventListener("click", () => {

                document.querySelectorAll("#categoryList li")
                    .forEach(x => x.classList.remove("active"));

                li.classList.add("active");

                filterBooks(li.dataset.id);

            });

        });

    } catch(e) {

        list.innerHTML = "<li>Error loading categories</li>";

    }
}

function openBook(id){

    window.location = "book.html?id=" + id

}

function closeModal(){

    document.getElementById("openBookModal").style.display="none";

}

function applyFilters(){

    const filters = {
        category: document.querySelector(".categories li.active")?.dataset.id,
        publisherName: document.getElementById("publisherFilter").value,
        minPrice: document.getElementById("minPrice").value,
        maxPrice: document.getElementById("maxPrice").value,
        format: document.getElementById("formatFilter").value,
        condition: document.getElementById("conditionFilter").value
    };

    loadBooks(filters);
}

function filterBooks(category){

    const filtered = currentBooks.filter(b =>
        b.category?.toLowerCase() === category
    )

    currentPage = 1

    renderBooks(filtered)

}

document.addEventListener("DOMContentLoaded", () => {

    if(booksContainer) loadBooks();

    if(cartContainer) loadCart();

    if(sortSelect){
        sortSelect.addEventListener("change",()=>{
            currentPage=1;
            renderBooks(currentBooks);
        });
    }

    loadCategories();

});

