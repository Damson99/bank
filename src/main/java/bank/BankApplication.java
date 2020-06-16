package bank;

import bank.config.MainConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

import java.io.File;

@SpringBootApplication
@ComponentScan({"bank", "config"})
public class BankApplication extends SpringBootServletInitializer
{
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder)
	{
		return applicationBuilder.sources(BankApplication.class);
	}

	public static void main(String[] args) throws Exception
	{
		new File(MainConfig.maxmindDirectory).mkdir();
		SpringApplication.run(BankApplication.class, args);
	}

}
