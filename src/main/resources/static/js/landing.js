/* ==========================================================================
   Mundo Manitas · Página pública de inicio
   Interacciones mínimas: menú móvil y animación de entrada por sección.
   ========================================================================== */

(function () {
    "use strict";

    var body = document.body;
    var reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)");

    body.classList.add("lp-js");

    /* ---------------------------------------------------------------
       Menú de navegación en pantallas pequeñas
       --------------------------------------------------------------- */

    var toggle = document.querySelector(".lp-nav-toggle");
    var nav = document.getElementById("lp-nav");

    if (toggle && nav) {
        var closeNav = function () {
            nav.classList.remove("is-open");
            toggle.setAttribute("aria-expanded", "false");
            toggle.setAttribute("aria-label", "Abrir menú de navegación");
        };

        toggle.addEventListener("click", function () {
            var open = nav.classList.toggle("is-open");
            toggle.setAttribute("aria-expanded", open ? "true" : "false");
            toggle.setAttribute(
                "aria-label",
                open ? "Cerrar menú de navegación" : "Abrir menú de navegación"
            );
        });

        nav.addEventListener("click", function (event) {
            if (event.target.closest("a")) {
                closeNav();
            }
        });

        document.addEventListener("keydown", function (event) {
            if (event.key === "Escape" && nav.classList.contains("is-open")) {
                closeNav();
                toggle.focus();
            }
        });

        document.addEventListener("click", function (event) {
            if (!nav.classList.contains("is-open")) {
                return;
            }
            if (!nav.contains(event.target) && !toggle.contains(event.target)) {
                closeNav();
            }
        });

        window.addEventListener("resize", function () {
            if (window.innerWidth > 860) {
                closeNav();
            }
        });
    }

    /* ---------------------------------------------------------------
       Aparición progresiva de bloques al hacer scroll
       --------------------------------------------------------------- */

    var targets = Array.prototype.slice.call(document.querySelectorAll("[data-reveal]"));

    if (!targets.length) {
        return;
    }

    if (reducedMotion.matches || !("IntersectionObserver" in window)) {
        targets.forEach(function (el) {
            el.classList.add("is-visible");
        });
        return;
    }

    var observer = new IntersectionObserver(
        function (entries) {
            entries.forEach(function (entry) {
                if (!entry.isIntersecting) {
                    return;
                }
                entry.target.classList.add("is-visible");
                observer.unobserve(entry.target);
            });
        },
        { rootMargin: "0px 0px -12% 0px", threshold: 0.12 }
    );

    targets.forEach(function (el, index) {
        el.style.transitionDelay = Math.min(index * 60, 240) + "ms";
        observer.observe(el);
    });
})();
