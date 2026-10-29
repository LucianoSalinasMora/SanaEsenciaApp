package com.sanaesencia.app.data.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import java.net.URLEncoder

// ==========================================
// Ayudante de Integración de Contacto y Mensajería
// ==========================================
object ContactHelper {

    // ------------------------------------------
    // Apertura e Interacción con WhatsApp
    // ------------------------------------------
    /**
     * Redirige directamente a la app de WhatsApp o a su web con un mensaje predeterminado.
     */
    fun openWhatsApp(context: Context, rawPhone: String, message: String = "") {
        try {
            val cleanPhone = rawPhone
                .replace("+", "")
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "")
                .trim()

            val encodedMsg = if (message.isNotBlank()) URLEncoder.encode(message, "UTF-8") else ""
            val url = if (encodedMsg.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg"
            } else {
                "https://api.whatsapp.com/send?phone=$cleanPhone"
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir WhatsApp o no está instalado", Toast.LENGTH_SHORT).show()
        }
    }

    // ------------------------------------------
    // Guardado Nativo en Libreta de Contactos
    // ------------------------------------------
    /**
     * Abre el formulario nativo del teléfono para agregar a Centro Sanaesencia a los contactos.
     */
    fun addContact(
        context: Context,
        name: String = "Centro Sanaesencia",
        phone: String = "+56923665073",
        email: String = "contacto@centrosanaesencia.cl",
        address: String = "Caupolicán 220, Los Vilos"
    ) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                type = ContactsContract.RawContacts.CONTENT_TYPE
                putExtra(ContactsContract.Intents.Insert.NAME, name)
                putExtra(ContactsContract.Intents.Insert.PHONE, phone)
                putExtra(ContactsContract.Intents.Insert.EMAIL, email)
                putExtra(ContactsContract.Intents.Insert.POSTAL, address)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir la libreta de contactos", Toast.LENGTH_SHORT).show()
        }
    }
}
