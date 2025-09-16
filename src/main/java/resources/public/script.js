// GET request
function loadGetMsg() {
    let nameVar = document.getElementById("nameGet").value;
    const xhttp = new XMLHttpRequest();
    xhttp.onload = function() {
        let resp = JSON.parse(this.responseText);
        // tu servidor responde {"task":"Tarea para <nombre>"}
        document.getElementById("getrespmsg").innerHTML = resp.task;
    }
    xhttp.open("GET", "/app/getTask?name=" + encodeURIComponent(nameVar));
    xhttp.send();
}

// POST request
function loadPostMsg() {
    let nameVar = document.getElementById("namePost").value;

    fetch("/app/postTask", {
        method: 'POST',
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({name: nameVar})
    })
        .then(res => res.json())
        .then(data => {
            document.getElementById("postrespmsg").innerHTML =
                `<p><strong>${data.status}</strong></p>`;
        })
        .catch(err => {
            document.getElementById("postrespmsg").innerHTML =
                `<p style="color:red;">Error: ${err}</p>`;
        });
}

(function() {
    const originalOpen = XMLHttpRequest.prototype.open;
    const originalSend = XMLHttpRequest.prototype.send;

    XMLHttpRequest.prototype.open = function(method, url) {
        this._url = url;  // Guardamos la URL
        originalOpen.apply(this, arguments);
    };

    XMLHttpRequest.prototype.send = function() {
        if (this._url && this._url.startsWith("/app/getTask")) {
            // extraer ?name=...
            const params = new URLSearchParams(this._url.split("?")[1]);
            const name = params.get("name") || "desconocido";

            // simular respuesta
            this.onload && this.onload.call({
                responseText: JSON.stringify({ task: `Tarea creada para ${name}` })
            });
        } else {
            originalSend.apply(this, arguments); // fallback real
        }
    };
})();

const originalFetch = window.fetch;
window.fetch = function(url, options) {
    if (url.startsWith("/app/postTask")) {
        return Promise.resolve({
            ok: true,
            json: () => Promise.resolve({ status: "Completado" })
        });
    }
    return originalFetch.apply(this, arguments); // fallback real
};


