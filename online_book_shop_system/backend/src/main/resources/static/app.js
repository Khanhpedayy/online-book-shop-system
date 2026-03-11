const API_BASE = '';
const USER_ID = 1;

const booksContainer = document.getElementById('booksContainer');
const cartContainer = document.getElementById('cartContainer');
const headerCartCount = document.getElementById('headerCartCount');
const productCount = document.getElementById('productCount');
const sortSelect = document.getElementById('sortSelect');

let currentBooks = [];

async function apiGet(path){
    const resp = await fetch(API_BASE + path);
    return resp.json();
}

async function apiPost(path,body){
    const resp = await fetch(API_BASE + path,{
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify(body)
    });
    return resp.json();
}

async function apiDelete(path){
    await fetch(API_BASE + path,{method:'DELETE'});
}

function sortBooks(books,value){
    const list=[...books];

    if(value==='title-asc')
        list.sort((a,b)=>a.title.localeCompare(b.title));

    if(value==='title-desc')
        list.sort((a,b)=>b.title.localeCompare(a.title));

    if(value==='price-asc')
        list.sort((a,b)=>a.salePrice-b.salePrice);

    if(value==='price-desc')
        list.sort((a,b)=>b.salePrice-a.salePrice);

    return list;
}

function renderBooks(books){

    booksContainer.innerHTML='';

    const sorted=sortBooks(books,sortSelect?sortSelect.value:'featured');

    sorted.forEach(b=>{

        const div=document.createElement('div');
        div.className='book';

        const cover = 'https://covers.openlibrary.org/b/isbn/'+(b.isbn||'0385533229')+'-M.jpg';
            //b.coverUrl || "/images/default-book.png";

        div.innerHTML=`
        <a href="book.html?id=${b.id}">
            <img class="book-cover" src="${cover}">
        </a>

        <div class="book-info">
            <div class="book-title">
                <a href="book.html?id=${b.id}">
                    ${b.title}
                </a>
            </div>

            <div class="book-price">$${b.salePrice ?? b.price ?? (b.variant?.salePrice ?? 0)}</div>

            <div class="book-actions">
                <button data-add="${b.id}" class="btn">Add to Cart</button>
            </div>
        </div>
        `;

        booksContainer.appendChild(div);
    });

    document.querySelectorAll('[data-add]').forEach(btn=>{
        btn.addEventListener('click',async()=>{
            const id=btn.getAttribute('data-add');
            await apiPost(`/api/cart/user/${USER_ID}/items`,{
                variantId:id,
                quantity:1
            });
            loadCart();
        });
    });

}

async function loadBooks(){

    const books=await apiGet('/api/books');

    currentBooks=books;

    if(productCount)
        productCount.textContent=books.length+' products';

    renderBooks(books);
}

async function loadCart() {

    const container = document.getElementById("cartContainer");
    if (!container) return;

    const items = await apiGet(`/api/cart/user/${USER_ID}`);

    container.innerHTML = "";

    let total = 0;

    items.forEach(ci => {

        const price = ci.variant.salePrice;
        total += price * ci.quantity;

        const div = document.createElement("div");
        div.className = "cart-item";

        div.innerHTML = `
            <span>${ci.variant.book.title} × ${ci.quantity}</span>
            <span>$${price}</span>
            <button data-remove="${ci.variant.id}" class="btn-secondary">Remove</button>
        `;

        container.appendChild(div);
    });

    document.querySelectorAll("[data-remove]").forEach(btn => {
        btn.addEventListener("click", async () => {
            const id = btn.getAttribute("data-remove");
            await apiDelete(`/api/cart/user/${USER_ID}/items/${id}`);
            loadCart();
        });
    });
}

if(sortSelect)
    sortSelect.addEventListener('change',()=>{
        renderBooks(currentBooks);
    });

if(booksContainer)
    loadBooks();

loadCart();

const orderForm = document.getElementById("orderForm");

if (orderForm) {

    orderForm.addEventListener("submit", async (e) => {

        e.preventDefault();

        const email = document.getElementById("orderEmail").value;
        const address = document.getElementById("orderAddress").value;
        const recipient = document.getElementById("orderRecipient").value;
        const result = document.getElementById("orderResult");

        try {

            await apiPost(`/api/orders/from-cart/${USER_ID}`, {
                email: email,
                shippingAddress: address,
                recipientName: recipient,
                customerId:USER_ID
            });

            result.textContent = "Order placed successfully!";
            result.className = "success";

            loadCart();

        } catch (err) {

            result.textContent = "Order failed.";
            result.className = "error";
        }

    });

}

async function loadBooksByCategory(categoryId){

    const res = await fetch(`/api/books/category/${categoryId}`);
    const books = await res.json();

    currentBooks = books;

    if(productCount)
        productCount.textContent = books.length + " products";

    renderBooks(books);

}

async function loadCategories(){

    const res = await fetch("/api/categories");
    const categories = await res.json();

    const list = document.getElementById("categoryList");

    list.innerHTML = `
        <li>
            <a href="#" onclick="loadBooks()">All Books</a>
        </li>
    `;

    categories.forEach(cat => {

        list.innerHTML += `
            <li>
                <a href="#" onclick="loadBooksByCategory(${cat.id})">
                    ${cat.name}
                </a>
            </li>
        `;

    });

}

if(booksContainer)
    loadBooks();

loadCart();

if(document.getElementById("categoryList"))
    loadCategories();