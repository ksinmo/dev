package tbwauth.security;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	private static final Logger log = LoggerFactory.getLogger(WebSecurityConfig.class);

	@Autowired
	DataSource dataSource;
	
	@Autowired
	SsoLogoutSuccessHandler ssoLogoutSuccessHandler;
	
	@Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.anyRequest().permitAll()//.authenticated()
				.and()
			.formLogin() // /oauth/authorize 
				.loginProcessingUrl("/login")
				.loginPage("/loginForm")
				.permitAll()
				.and() // End of
		  //.httpBasic().and() // /oauth/authorize
			.csrf()
				.requireCsrfProtectionMatcher(new AntPathRequestMatcher("/user*"))
				.disable()
			.logout()
				.logoutSuccessHandler(ssoLogoutSuccessHandler)
				.permitAll();
	}
    @Bean
    public PasswordEncoder passwordEncoder(){
    	System.out.println("encoder()");
    	return new AesPasswordEncoder();
    }
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		System.out.println("\n## configureGlobal123 " );
		try
		{
			auth
	        	.jdbcAuthentication()
	         	.dataSource(dataSource)
	         	.passwordEncoder(passwordEncoder())
			 	.usersByUsernameQuery("SELECT USRID, PASSWD, 1 FROM USR WHERE USRID=?")
			 	.authoritiesByUsernameQuery("SELECT USRID, 'USER_ROLE' FROM USR WHERE USRID=?");// USERNAME, AUTHORIY FROM OAUTH_AUTHORITIES WHERE USERNAME=?");
		} catch (Exception ex) {
			log.error("configureGlobal {}", ex);
		}
//		auth
//			.inMemoryAuthentication()
//			.withUser("tsong").password("aaa").roles("USER").and()
//			.withUser("jmpark").password("aaa").roles("USER").and()
//			.withUser("jkkang").password("aaa").roles("USER").and()
//			.withUser("test").password("aaa").roles("USER");
	}

}
