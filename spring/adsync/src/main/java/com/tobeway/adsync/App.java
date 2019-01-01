package com.tobeway.adsync;

import java.util.List;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import static org.springframework.ldap.query.LdapQueryBuilder.query;
/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://192.168.1.40:3268");
        contextSource.setBase("DC=tobeway,DC=com");
        //contextSource.setUserDn("CN=administrator,CN=Users,DC=tobeway,DC=com");
        contextSource.setUserDn("CN=김신모,OU=tobeway,OU=Accounts,DC=tobeway,DC=com");
        contextSource.setPassword("tobeway2018");
        contextSource.afterPropertiesSet();
        
        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
        System.out.println( "authenticate OK" );

        //(&(objectCategory=person)(objectClass=user))
        List<User> list = ldapTemplate.search(query()
        		.base("OU=tobeway,OU=Accounts")
                .attributes("*")//"cn", "mail")
                .where("objectCategory").is("person").and("objectClass").is("user"),
                new UserAttributesMapper());

        for(User user : list) {
        	System.out.println(user.getFullName() + "," + user.getMail() + "," + user.getLastName() + "," + user.getDescription() + "," + user.getDistinguishedName());
        }
        
        System.out.println("Total: " + list.size());
    }
}
