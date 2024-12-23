package burundi.treasure.service;

import java.util.Arrays;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import burundi.treasure.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{

	@Autowired
    private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// Kiểm tra xem user có tồn tại trong database không?
		burundi.treasure.model.User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        
        // Set ROLE mặc định cho tất cả User
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_USER");
        UserDetails userDetails = new User(user.getUsername(), user.getPassword(), Arrays.asList(grantedAuthority));
		return userDetails;
	}
	
	@Transactional
    public UserDetails loadUserById(Long id) {
		burundi.treasure.model.User user = userRepository.findById(id).orElseThrow(
                () -> new UsernameNotFoundException("User not found with id : " + id)
        );
		
		// Set ROLE mặc định cho tất cả User
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_USER");
		UserDetails userDetails = new User(user.getUsername(), user.getPassword(), Arrays.asList(grantedAuthority));
        return userDetails;
    }

}
