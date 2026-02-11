package gabri.dev.chatapp.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * WebConfig class to configure global settings for the Spring Boot application.
 * <p>
 * This class implements {@link WebMvcConfigurer} to customize the default configuration
 * for Cross-Origin Resource Sharing (CORS) and static resource handling for documentation.
 * </p>
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures CORS mapping for all endpoints in the application.
     *
     * @param registry the {@link CorsRegistry} used to register the CORS configuration.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization") // Exponer header Authorization
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * Adds resource handlers for serving static documentation files.
     * <p>
     * This method configures the following resource mappings:
     * <ul>
     *   <li>Root path (/) - serves index.html and static assets</li>
     *   <li>/java_doc/** - serves generated JavaDoc documentation</li>
     *   <li>/app_doc/** - serves manual application documentation</li>
     *   <li>/assets/** - serves images and other static assets</li>
     * </ul>
     * </p>
     *
     * @param registry the resource handler registry to add handlers to
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Sirve archivos estáticos desde /static (index.html, assets, etc.)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600); // Cache de 1 hora

        // Sirve JavaDoc generado automáticamente
        registry.addResourceHandler("/java_doc/**")
                .addResourceLocations("classpath:/static/java_doc/")
                .setCachePeriod(3600);

        // Sirve documentación manual de la aplicación
        registry.addResourceHandler("/app_doc/**")
                .addResourceLocations("classpath:/static/app_doc/")
                .setCachePeriod(3600);

        // Sirve imágenes y assets
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCachePeriod(86400); // Cache de 24 horas para assets
    }

    /**
     * Adds simple automated controllers pre-configured with the response status code
     * and/or a view to render the response body.
     * <p>
     * This maps the root URL "/" to serve the index.html page as the welcome page
     * for the documentation portal.
     * </p>
     *
     * @param registry the view controller registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Mapea la raíz a index.html (página principal de documentación)
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}