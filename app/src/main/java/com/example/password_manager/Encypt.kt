import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

val key: String = "MySecretKey12345"
val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")

fun main() {
    val plainText = "Hello, Aakash!"
    println("Plain Text $plainText")
    // Encrypt the plaintext
    val encryptedText = encryption(plainText)
    println("Encrypted Text: $encryptedText")

    // Decrypt the encrypted text
    val decryptedText = decryption(encryptedText)
    println("Decrypted Text: $decryptedText")
}

private fun encryption(string: String): String {
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
    val encryptByte = cipher.doFinal(string.toByteArray(Charsets.UTF_8))
    return Base64.getEncoder().encodeToString(encryptByte)
}

private fun decryption(string: String): String {
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
    val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(string))
    return String(decryptedBytes, Charsets.UTF_8)
}
