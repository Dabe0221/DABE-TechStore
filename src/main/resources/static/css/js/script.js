/* ===========================================
   TechStore Shopping Cart
   =========================================== */

let cart = JSON.parse(localStorage.getItem("cart")) || [];
let wishlist = JSON.parse(localStorage.getItem("wishlist")) || [];

/* ===========================
   Product Data
=========================== */

const products = [
    {
        id: 1,
        name: "Laptop Pro",
        price: 999,
        image: "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=600"
    },
    {
        id: 2,
        name: "Mechanical Keyboard",
        price: 89,
        image: "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600"
    },
    {
        id: 3,
        name: "Gaming Mouse",
        price: 29,
        image: "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=600"
    },
    {
        id: 4,
        name: "USB-C Charger",
        price: 24,
        image: "https://images.unsplash.com/photo-1580894908361-967195033215?w=600"
    }
];

/* ===========================
   Initialize
=========================== */

document.addEventListener("DOMContentLoaded", () => {

    updateCartCount();

    setupSearch();

});

/* ===========================
   Add To Cart
=========================== */

function addToCart(id){

    let product = products.find(p => p.id === id);

    if(!product) return;

    let existing = cart.find(item => item.id === id);

    if(existing){

        existing.quantity++;

    }else{

        cart.push({

            ...product,

            quantity:1

        });

    }

    saveCart();

    updateCartCount();

    showToast(product.name + " added to cart");

}

/* ===========================
   Remove Item
=========================== */

function removeItem(id){

    cart = cart.filter(item => item.id !== id);

    saveCart();

    updateCartCount();

    renderCart();

}

/* ===========================
   Quantity
=========================== */

function increase(id){

    let item = cart.find(p=>p.id===id);

    if(item){

        item.quantity++;

        saveCart();

        renderCart();

        updateCartCount();

    }

}

function decrease(id){

    let item = cart.find(p=>p.id===id);

    if(!item) return;

    item.quantity--;

    if(item.quantity<=0){

        removeItem(id);

        return;

    }

    saveCart();

    renderCart();

    updateCartCount();

}

/* ===========================
   Wishlist
=========================== */

function addWishlist(id){

    if(wishlist.includes(id)){

        showToast("Already in wishlist");

        return;

    }

    wishlist.push(id);

    localStorage.setItem("wishlist",JSON.stringify(wishlist));

    showToast("Added to wishlist ");

}

/* ===========================
   Cart Count
=========================== */

function updateCartCount(){

    const badge=document.querySelector(".badge");

    if(!badge) return;

    let total=0;

    cart.forEach(item=>{

        total+=item.quantity;

    });

    badge.innerHTML=total;

}

/* ===========================
   Total Price
=========================== */

function getTotal(){

    let total=0;

    cart.forEach(item=>{

        total+=item.price*item.quantity;

    });

    return total.toFixed(2);

}

/* ===========================
   Save Cart
=========================== */

function saveCart(){

    localStorage.setItem("cart",JSON.stringify(cart));

}

/* ===========================
   Search
=========================== */

function setupSearch(){

    const input=document.querySelector(".form-control");

    if(!input) return;

    input.addEventListener("keyup",function(){

        const keyword=this.value.toLowerCase();

        document.querySelectorAll(".product-card").forEach(card=>{

            let text=card.innerText.toLowerCase();

            if(text.includes(keyword)){

                card.parentElement.style.display="block";

            }else{

                card.parentElement.style.display="none";

            }

        });

    });

}

/* ===========================
   Toast Notification
=========================== */

function showToast(message){

    let toast=document.createElement("div");

    toast.className="toast-message";

    toast.innerHTML=message;

    document.body.appendChild(toast);

    setTimeout(()=>{

        toast.classList.add("show");

    },100);

    setTimeout(()=>{

        toast.classList.remove("show");

        setTimeout(()=>{

            toast.remove();

        },300);

    },2500);

}

/* ===========================
   Cart Modal
=========================== */

function renderCart(){

    let body=document.getElementById("cartBody");

    if(!body) return;

    body.innerHTML="";

    cart.forEach(item=>{

        body.innerHTML+=`

        <div class="d-flex align-items-center border-bottom py-3">

            <img src="${item.image}"
            width="70"
            class="rounded">

            <div class="ms-3 flex-grow-1">

                <h6>${item.name}</h6>

                <p>$${item.price}</p>

                <button
                class="btn btn-sm btn-secondary"
                onclick="decrease(${item.id})">

                -

                </button>

                ${item.quantity}

                <button
                class="btn btn-sm btn-secondary"
                onclick="increase(${item.id})">

                +

                </button>

            </div>

            <button
            class="btn btn-danger btn-sm"
            onclick="removeItem(${item.id})">

            Remove

            </button>

        </div>

        `;

    });

    body.innerHTML+=`

    <hr>

    <h4 class="text-end">

    Total: $${getTotal()}

    </h4>

    `;

}

/* ===========================
   Checkout
=========================== */

function checkout(){

    if(cart.length===0){

        showToast("Cart is empty.");

        return;

    }

    alert("Checkout Successful!\n\nTotal: $"+getTotal());

    cart=[];

    saveCart();

    renderCart();

    updateCartCount();

}

/* ===========================
   Dark Mode
=========================== */

function toggleDarkMode(){

    document.body.classList.toggle("dark-mode");

}

/* ===========================
   Scroll To Top
=========================== */

window.addEventListener("scroll",()=>{

    const btn=document.getElementById("topBtn");

    if(!btn) return;

    if(window.scrollY>300){

        btn.style.display="block";

    }else{

        btn.style.display="none";

    }

});

function topFunction(){

    window.scrollTo({

        top:0,

        behavior:"smooth"

    });

}