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