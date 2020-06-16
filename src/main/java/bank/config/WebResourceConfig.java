package bank.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan({"bank", "config"})
public class WebResourceConfig implements WebMvcConfigurer
{
    public WebResourceConfig()
    {
    }

    public void addResourceHandlers(final ResourceHandlerRegistry resourceHandlerRegistry)
    {
        resourceHandlerRegistry.addResourceHandler(new String[]{System.getProperty("user.dir")+"/**"})
        .addResourceLocations(new String[]{"file:/" + MainConfig.maxmindDirectory + "GeoLite2-City.mmdb"});
    }
}
