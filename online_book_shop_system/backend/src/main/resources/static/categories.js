fetch("/api/categories")
    .then(res => res.json())
    .then(categories => {

        const list = document.getElementById("categoryList");

        categories.forEach(cat => {

            const li = document.createElement("li");
            li.innerText = cat.name;

            li.onclick = () => {
                loadBooksByCategory(cat.id);
            };

            list.appendChild(li);

        });

    });

// Function to load books by category
function loadBooksByCategory(categoryId) {
    fetch(`/api/books?categoryId=${categoryId}`)
        .then(res => res.json())
        .then(books => {
            const booksContainer = document.getElementById("booksContainer");
            booksContainer.innerHTML = ""; // Clear existing books

            for (const book of books) {
                const bookElement = document.createElement("div");
                bookElement.className = "book-item";
                bookElement.innerHTML = `
                    <h3>${book.title}</h3>
                    <p>Author: ${book.author || "Unknown"}</p>
                    <p>Price: $${book.price || 0}</p>
                `;
                booksContainer.appendChild(bookElement);
            }
        })
        .catch(err => {
            console.error("Failed to load books by category:", err);
        });
}