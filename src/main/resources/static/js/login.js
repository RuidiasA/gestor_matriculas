// =============================================
//   REFERENCIAS A LOS 3 FORMULARIOS
// =============================================
const formLogin = document.getElementById("form-login");
const formVerificacion = document.getElementById("form-verificacion");
const formPassword = document.getElementById("form-password");

// Botones / links
const linkValidar = document.getElementById("link-validar");
const volverLogin = document.getElementById("volver-login");
const volverVerificacion = document.getElementById("volver-verificacion");

const btnValidar = document.getElementById("btn-validar");
const btnGuardarPass = document.getElementById("btn-guardar-pass");


// =============================================
//   FUNCIÓN GENERAL DE TRANSICIÓN ANIMADA
// =============================================
function cambiarVista(actual, siguiente) {

    actual.classList.add("fade-out");

    setTimeout(() => {
        actual.classList.add("hidden");
        actual.classList.remove("fade-out");

        siguiente.classList.remove("hidden");
        siguiente.classList.add("fade-in");

        setTimeout(() => {
            siguiente.classList.remove("fade-in");
        }, 250);

    }, 250);
}


// =============================================
//     EVENTOS CAMBIO DE FORMULARIO (UI)
// =============================================
if (linkValidar) {
    linkValidar.onclick = () => cambiarVista(formLogin, formVerificacion);
}
if (volverLogin) {
    volverLogin.onclick = () => cambiarVista(formVerificacion, formLogin);
}
if (volverVerificacion) {
    volverVerificacion.onclick = () => cambiarVista(formPassword, formVerificacion);
}


// =============================================
//     VALIDAR IDENTIDAD — LADO SERVIDOR
// =============================================
if (btnValidar) {
    btnValidar.onclick = async () => {

        const dni = document.getElementById("dni").value.trim();
        const codigo = document.getElementById("codigo").value.trim();
        const correo = document.getElementById("correoVerif").value.trim();
        const nombres = document.getElementById("nombresCompletos").value.trim();

        if (!dni || !codigo || !correo || !nombres) {
            alert("Complete todos los campos.");
            return;
        }

        try {
            const resp = await fetch("/password/verificar", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({ dni, codigo, correo, nombresCompletos: nombres })
            });

            if (!resp.ok) {
                alert("Los datos no coinciden con ningún usuario.");
                return;
            }

            // Identidad validada → mostrar form cambio contraseña
            cambiarVista(formVerificacion, formPassword);

        } catch (e) {
            alert("Error al validar identidad.");
        }
    };
}


// =============================================
//     GUARDAR NUEVA CONTRASEÑA
// =============================================
if (btnGuardarPass) {
    btnGuardarPass.onclick = async () => {

        const newPass = document.getElementById("newPass").value;
        const confirmPass = document.getElementById("confirmPass").value;

        if (newPass !== confirmPass) {
            alert("Las contraseñas no coinciden.");
            return;
        }

        const correo = document.getElementById("correoVerif").value.trim();

        try {
            const resp = await fetch("/password/cambiar", {
                method: "POST",
                headers: {"Content-Type":"application/json"},
                body: JSON.stringify({
                    correo,
                    nuevaPassword: newPass
                })
            });

            if (!resp.ok) {
                alert("No se pudo cambiar la contraseña.");
                return;
            }

            alert("Contraseña actualizada correctamente.");

            // Animación suave antes de redirigir
            cambiarVista(formPassword, formLogin);

            setTimeout(() => {
                window.location.href = "/login";
            }, 300);

        } catch (e) {
            alert("Error al cambiar contraseña.");
        }
    };
}
