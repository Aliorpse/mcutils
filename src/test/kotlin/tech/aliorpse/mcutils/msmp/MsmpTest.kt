package tech.aliorpse.mcutils.msmp

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import tech.aliorpse.mcutils.modules.server.msmp.MsmpClient

@Disabled("Manual test — requires running server locally")
class MsmpTest {
    val client = MsmpClient("ws://localhost:25585", "hCGB1YOFxgwlXYikq3PKottfzSaQhMuDZ90TKAoC")

    @Test
    fun allowlistTest() {
        runBlocking {
            client.withConnection {
                allowlist.clear()
                println(allowlist.add("Aliorpse", "MosCro"))
                println(allowlist.set("MosCro", "Technoblade", "Aliorpse"))
                println(allowlist.remove("MosCro", "Technoblade"))
                println(allowlist.get())
            }
        }
    }
}
