package bookapp

import bookapp.controller.PingController
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.web.servlet.HandlerAdapter
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@SpringBootApplication // We use direct @Import instead of @ComponentScan to speed up cold starts
// @ComponentScan(basePackages = "bookapp.controller")
@Import(PingController::class)
class Application {
    /*
     * Create required HandlerMapping, to avoid several default HandlerMapping instances being created
     */
    @Bean
    fun handlerMapping(): HandlerMapping {
        return RequestMappingHandlerMapping()
    }

    /*
     * Create required HandlerAdapter, to avoid several default HandlerAdapter instances being created
     */
    @Bean
    fun handlerAdapter(): HandlerAdapter {
        return RequestMappingHandlerAdapter()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }
}