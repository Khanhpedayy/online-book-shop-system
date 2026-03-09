const params = new URLSearchParams(window.location.search);
const id = params.get("id");

fetch(`/api/books/${id}`)
    .then(res => res.json())
    .then(book => {

        document.getElementById("title").innerText = book.title;

        document.getElementById("author").innerText =
            "Author: " + (book.author || "Unknown");

        // price from variant
        const price = book.variants?.[0]?.salePrice || 0;
        document.getElementById("price").innerText = "$" + price;

        document.getElementById("description").innerText =
            book.subtitle || book.description || "";

        document.getElementById("bookImage").src =
            book.imageUrl || "https://via.placeholder.com/300x450";

        // store variant id for cart
        window.variantId = book.variants?.[0]?.id;
    });

document.getElementById("addCartBtn").addEventListener("click", () => {

    fetch("/api/cart/add", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            variantId: window.variantId,
            quantity: 1
        })
    })
        .then(res => res.json())
        .then(() => {
            alert("Book added to cart!");
        });
});