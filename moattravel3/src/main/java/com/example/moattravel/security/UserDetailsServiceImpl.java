package com.example.moattravel.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.moattravel.entity.User;
import com.example.moattravel.repository.UserRepository;
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	
	private final UserRepository userRepository;
	
	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	//throws Exception このメソッドが例外を発生させる可能性がある　と宣言さるために使用
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		try {
			User user = userRepository.findByEmail(email);
			String userRoleName = user.getRole().getName();
			Collection<GrantedAuthority> authorities = new ArrayList<>();
			
			authorities.add(new SimpleGrantedAuthority(userRoleName));
			
			return new UserDetailsImpl(user, authorities);
		} catch (Exception e) {
			//throw new Exception 例外を発生させる
			throw new UsernameNotFoundException("ユーザーが見つかりませんでした。");
		}
	}

}
