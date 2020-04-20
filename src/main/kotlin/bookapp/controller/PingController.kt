package bookapp.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import java.util.*

@RestController
@EnableWebMvc
class PingController {
    @RequestMapping(path = ["/ping"], method = [RequestMethod.GET])
    fun ping(): Map<String, String> {
        val pong: MutableMap<String, String> = HashMap()
        pong["pong"] = "Hello, World!"
        return pong
    }
}