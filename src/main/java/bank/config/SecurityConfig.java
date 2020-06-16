package bank.config;

import bank.handlers.LoginAuthenticationFailureHandler;
import bank.handlers.LoginAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
    @Autowired
    private DataSource dataSource;

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler()
    {
        return new LoginAuthenticationSuccessHandler();
    }

    @Bean
    public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler()
    {
        return new LoginAuthenticationFailureHandler();
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception
    {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .usersByUsernameQuery("SELECT u.personal_id_number, u.password, u.enabled FROM user u WHERE u.personal_id_number = ?")
                .authoritiesByUsernameQuery("SELECT u.personal_id_number, r.user_role " +
                        "FROM user u " +
                        "LEFT JOIN user_role ur ON u.user_id = ur.user_id " +
                        "LEFT JOIN role r ON r.role_id = ur.role_id " +
                        "WHERE u.personal_id_number = ?")
                .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity security) throws Exception
    {
        security.authorizeRequests()
                .antMatchers("/resources/**").permitAll()
                .antMatchers("/", "/login", "/registration", "/passwordReset/**").permitAll()
                .antMatchers("/login").anonymous()
                .antMatchers("/home/**").hasAnyAuthority("USER")
                .antMatchers("/transfer/**").hasAnyAuthority("USER")
                .antMatchers("/history/**").hasAnyAuthority("USER")
                .antMatchers("/users/**").hasAnyAuthority("ADMIN", "EMPLOYEE")
                .antMatchers("/console/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
                .and().csrf().disable().formLogin().loginPage("/login")
                .failureUrl("/login?error=true")
                .successHandler(authenticationSuccessHandler()).failureHandler(authenticationFailureHandler())
                .usernameParameter("personalIdNumber").passwordParameter("password")
                .and().logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/login")
                .and().exceptionHandling()
                .accessDeniedPage("/login");
    }
}
