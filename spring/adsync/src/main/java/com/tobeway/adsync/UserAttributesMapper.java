package com.tobeway.adsync;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

public class UserAttributesMapper implements AttributesMapper<User> {
	public User mapFromAttributes(Attributes attributes) throws NamingException {
		User user = new User();
		user.setFullName((String)attributes.get("cn").get());
        user.setLastName((String)(attributes.get("sn") == null ? "" : attributes.get("sn").get()));
        user.setMail((String)(attributes.get("mail") == null ? "" : attributes.get("mail").get()));
        user.setDescription((String)(attributes.get("description") == null ? "" : attributes.get("description").get()));
        user.setDistinguishedName((String)(attributes.get("distinguishedName") == null ? "" : attributes.get("distinguishedName").get()));
		return user;
	}
}
