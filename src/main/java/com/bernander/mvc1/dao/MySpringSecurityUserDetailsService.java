package com.bernander.mvc1.dao;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
public class MySpringSecurityUserDetailsService implements UserDetailsService {

	@Override
	public UserDetails loadUserByUsername(String arg0) throws UsernameNotFoundException {
		
//		UserDetails userDetails = new UserDetails();
		
		
		
		return null;
	}

}
